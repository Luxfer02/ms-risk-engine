package es.NTTEnterprise.RIntellix.ms_risk_engine.application.usecases;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.ModelPredictionResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.HardCutoffRejection;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.ports.output.ModelPredictionPort;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.FinancialMetricsCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.HardCutoffRuleEvaluator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation.RiskCalculationStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskGradeCalculator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskMetricsCalculationContext;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskMetricsCalculationResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation.RiskCalculationStrategyFactory;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Core service for calculating risk metrics.
 * 
 * Encapsulates the common logic for model invocation and risk metric
 * calculation
 * that is reused across both scoring and simulation workflows.
 *
 * Responsibilities:
 * - Orchestrate async model invocation
 * - Parallel computation of pre-PD metrics (EAD/LGD)
 * - Assembly of full risk metrics combining PD with pre-computed values
 * - Calculation of ECL and risk grade
 *
 * This service enables reuse of complex orchestration logic without code
 * duplication or breaking clean architecture principles.
 *
 * Usage:
 * - Scoring: Use to calculate metrics for a new scoring request
 * - Simulation: Use to calculate metrics for a what-if scenario
 * - Any other use case: Use to calculate risk metrics with custom payload
 *
 * @author Lucía Fernández Mancebo
 * @date 05/09/2026
 */
@Slf4j

public class RiskMetricsCalculationService {

        private final ModelPredictionPort modelPredictionPort;
        private final List<RiskCalculationStrategy> riskCalculationStrategies;
        private final RiskGradeCalculator riskGradeCalculator;
        private final FinancialMetricsCalculationService financialMetricsCalculationService;
        private final HardCutoffRuleEvaluator hardCutoffRuleEvaluator;

        /**
         * Constructor of the RiskMetricsCalculationService class.
         *
         * @param modelPredictionPort                the output port for model
         *                                           invocation.
         * @param riskCalculationStrategies          the available risk calculation
         *                                           strategies.
         * @param riskGradeCalculator                the domain service for risk grade
         *                                           calculation.
         * @param financialMetricsCalculationService the domain service for financial
         *                                           metrics calculation.
         * @param hardCutoffRuleEvaluator            the domain service for hard-cutoff
         *                                           rule evaluation.
         */
        public RiskMetricsCalculationService(
                        final ModelPredictionPort modelPredictionPort,
                        final List<RiskCalculationStrategy> riskCalculationStrategies,
                        final RiskGradeCalculator riskGradeCalculator,
                        final FinancialMetricsCalculationService financialMetricsCalculationService,
                        final HardCutoffRuleEvaluator hardCutoffRuleEvaluator) {
                this.modelPredictionPort = Objects.requireNonNull(modelPredictionPort,
                                LogMessage.MODEL_PREDICTION_PORT_CANNOT_BE_NULL);
                this.riskCalculationStrategies = Objects.requireNonNull(riskCalculationStrategies,
                                LogMessage.STRATEGIES_LIST_CANNOT_BE_NULL);
                this.riskGradeCalculator = Objects.requireNonNull(riskGradeCalculator,
                                LogMessage.RISK_GRADE_CALCULATOR_CANNOT_BE_NULL);
                this.financialMetricsCalculationService = Objects.requireNonNull(financialMetricsCalculationService,
                                LogMessage.FINANCIAL_METRICS_CALCULATION_SERVICE_CANNOT_BE_NULL);
                this.hardCutoffRuleEvaluator = Objects.requireNonNull(hardCutoffRuleEvaluator,
                                "HardCutoffRuleEvaluator cannot be null");
        }

        /**
         * Calculates full risk metrics with model invocation and metric assembly.
         *
         * Execution flow:
         * 1. Fire async model invocation (returns immediately)
         * 2. While model processes, pre-compute EAD/LGD in parallel
         * 3. Join futures when both complete
         * 4. Assemble full metrics (ECL, RiskGrade) combining PD with pre-computed
         * values
         *
         * This orchestration pattern minimizes latency by parallelizing independent
         * computations.
         *
         * @param context the RiskMetricsCalculationContext containing all necessary
         *                data for calculation.
         * @return the calculation result containing both model prediction and fully
         *         assembled RiskMetrics entity.
         */
        public RiskMetricsCalculationResult calculateRiskMetrics(final RiskMetricsCalculationContext context) {
                Objects.requireNonNull(context, LogMessage.RISK_METRICS_CONTEXT_CANNOT_BE_NULL);

                log.info(LogMessage.RISK_METRICS_CALCULATION_STARTED, context.requestId());

                final Optional<HardCutoffRejection> rejectionOpt = hardCutoffRuleEvaluator.evaluateRules(
                                context.modelPayload(),
                                context.requestType(),
                                context.requestId());

                // Step 1: Fire async model call (returns immediately) or complete instantly if
                // cutoff triggered
                final CompletableFuture<ModelPredictionResult> modelFuture;
                final boolean isHardCutoff = rejectionOpt.isPresent();

                if (isHardCutoff) {
                        final HardCutoffRejection rejection = rejectionOpt.get();
                        final ModelPredictionResult bypassedPrediction = new ModelPredictionResult(
                                        1.0,
                                        "HIGH",
                                        0.0,
                                        rejection.getExplainability());
                        modelFuture = CompletableFuture.completedFuture(bypassedPrediction);
                        log.warn(LogMessage.RISK_METRICS_HARD_CUTOFF_TRIGGERED, context.requestId());
                } else {
                        modelFuture = modelPredictionPort.predictAsync(
                                        context.modelPayload(),
                                        context.requestId(),
                                        context.modelEndpointPath());
                        log.debug(LogMessage.ASYNCHRONOUS_MODEL_INVOCATION, context.requestId());
                }

                // Step 2: While model processes, pre-compute EAD/LGD in parallel
                final RiskCalculationStrategy riskStrategy = RiskCalculationStrategyFactory.createStrategy(
                                context.requestType(),
                                context.isRevolving(),
                                riskCalculationStrategies);

                final RiskMetrics prePdMetrics = riskStrategy.calculatePrePdMetrics(
                                context.principalAmount(),
                                context.ltv());

                log.debug(LogMessage.PRE_PD_METRICS_COMPUTED,
                                context.requestId(),
                                prePdMetrics.getProbabilityOfDefault(),
                                prePdMetrics.getLossGivenDefault());

                // Step 3: Join futures and get model result
                final ModelPredictionResult prediction = modelFuture.join();
                log.info(LogMessage.MODEL_PREDICTION_RESULT,
                                context.requestId(),
                                prediction.getProbabilityOfDefault());

                // Step 4: Assemble full metrics combining PD with pre-computed EAD/LGD
                final RiskMetrics fullMetrics = riskStrategy.assembleFullMetricsWithGradeCalculator(
                                prediction.getProbabilityOfDefault(),
                                prePdMetrics,
                                context.principalAmount(),
                                context.annualIncome(),
                                context.termMonths(),
                                context.interestRate(),
                                riskGradeCalculator);

                log.info(LogMessage.FULL_METRICS_ASSEMBLED,
                                context.requestId(),
                                fullMetrics.getProbabilityOfDefault(),
                                fullMetrics.getExposureAtDefault(),
                                fullMetrics.getLossGivenDefault(),
                                fullMetrics.getExpectedCalculatedLoss(),
                                fullMetrics.getRiskLevel());

                // Step 5: Calculate and attach financial metrics
                final FinancialMetrics financialMetrics = financialMetricsCalculationService.calculateFinancialMetrics(
                                context.requestType(),
                                context.isRevolving(),
                                context.principalAmount(),
                                context.interestRate() != null ? context.interestRate() : 0.0,
                                context.termMonths(),
                                context.annualIncome(),
                                context.existingMonthlyObligations());

                fullMetrics.setFinancialMetrics(financialMetrics);
                log.debug(LogMessage.FINANCIAL_METRICS_ATTACHED,
                                financialMetrics.getMonthlyPayment(),
                                financialMetrics.getDebtToIncomeRatio());

                return new RiskMetricsCalculationResult(prediction, fullMetrics, isHardCutoff);
        }
}
