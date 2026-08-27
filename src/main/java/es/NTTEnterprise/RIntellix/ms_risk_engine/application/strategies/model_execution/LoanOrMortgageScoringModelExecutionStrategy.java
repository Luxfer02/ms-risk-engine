package es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution;

import java.util.Map;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringGenerationPayload;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringGenerationRequest;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.output.ScoringModelExecutionResultDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.LoanOrMortgageModelPayloadMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.usecases.RiskMetricsCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.ModelPredictionResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums.RequestType;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.DtiCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskMetricsCalculationContext;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.SimulationConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for loan and mortgage model execution with parallelized risk
 * calculation.
 *
 * Execution flow:
 * 1. Map payload to model request format.
 * 2. Fire async AI model call (returns CompletableFuture).
 * 3. While model call is in-flight: compute EAD and LGD via risk calculation
 * strategy.
 * 4. Join model future to obtain PD.
 * 5. Assemble full risk metrics (ECL, RiskGrade) combining PD with pre-computed
 * EAD/LGD.
 *
 * @author Lucía Fernández Mancebo
 * @date 25/04/2026
 */
@Slf4j

public class LoanOrMortgageScoringModelExecutionStrategy implements ScoringModelExecutionStrategy {

        private final LoanOrMortgageModelPayloadMapper payloadMapper;
        private final RiskMetricsCalculationService metricsCalculationService;
        private final DtiCalculationService dtiCalculationService;
        private final String predictLoanPath;

        /**
         * Constructor of the LoanOrMortgageScoringModelExecutionStrategy class.
         *
         * @param payloadMapper             the mapper that prepares loan model payload.
         * @param metricsCalculationService the service that orchestrates risk metric
         *                                  calculation.
         *                                  mortgage.
         */
        public LoanOrMortgageScoringModelExecutionStrategy(
                        final LoanOrMortgageModelPayloadMapper payloadMapper,
                        final RiskMetricsCalculationService metricsCalculationService,
                        final DtiCalculationService dtiCalculationService,
                        final String predictLoanPath) {
                this.payloadMapper = Objects.requireNonNull(payloadMapper);
                this.metricsCalculationService = Objects.requireNonNull(metricsCalculationService);
                this.dtiCalculationService = Objects.requireNonNull(dtiCalculationService);
                this.predictLoanPath = Objects.requireNonNull(predictLoanPath);
        }

        @Override
        public boolean supports(final String requestType) {
                try {
                        RequestType type = RequestType.fromValue(requestType);
                        return type == RequestType.PRESTAMO || type == RequestType.HIPOTECA;
                } catch (IllegalArgumentException e) {
                        return false;
                }
        }

        @Override
        public ScoringModelExecutionResultDTO executePredictionModel(
                        final ScoringGenerationPayload payload,
                        final String requestType,
                        final String requestId) {

                if (!(payload instanceof ScoringGenerationRequest request)) {
                        throw new IllegalArgumentException(LogMessage.STRATEGY_ERROR_PAYLOAD);
                }

                // 1. Calculate DTI and Map payload to model request format
                final double annualIncome = SimulationConstants.getSafe(request.getAnnualIncome());
                final double existingObligations = SimulationConstants.getSafe(request.getExistingObligations());
                final double loanAmount = SimulationConstants.getSafe(request.getLoanAmount());
                final double interestRate = SimulationConstants.getSafe(request.getInterestRate());
                final double calculatedDti = dtiCalculationService.calculateModelDtiForScoring(
                                annualIncome,
                                existingObligations,
                                loanAmount,
                                interestRate,
                                request.getTermMonths());

                final Map<String, Object> modelRequestPayload = payloadMapper.toModelPayload(request, calculatedDti);

                // Step 2: Create calculation context with all necessary data
                final RiskMetricsCalculationContext context = new RiskMetricsCalculationContext(
                                modelRequestPayload,
                                requestId,
                                predictLoanPath,
                                requestType,
                                false, // Loans/Mortgages are not revolving
                                request.getLoanAmount(),
                                request.getLtv(),
                                request.getAnnualIncome(),
                                request.getTermMonths(),
                                request.getInterestRate(),
                                request.getExistingObligations() != null ? request.getExistingObligations() / 12.0 : 0.0);

                // Step 3: DELEGATE core calculation to reusable service
                // This service handles:
                // - Async model invocation
                // - Parallel pre-PD metrics calculation
                // - Full metrics assembly with ECL and risk grade
                final var result = metricsCalculationService.calculateRiskMetrics(context);
                final RiskMetrics fullMetrics = result.riskMetrics();
                final ModelPredictionResult prediction = result.modelPredictionResult();

                log.info(LogMessage.MODEL_EXECUTION_RESULT_LOAN_MORTGAGE,
                                fullMetrics.getProbabilityOfDefault());

                return new ScoringModelExecutionResultDTO(modelRequestPayload, prediction, fullMetrics, result.isHardCutoff());
        }

        @Override
        public String modelEndpointPath() {
                return this.predictLoanPath;
        }
}
