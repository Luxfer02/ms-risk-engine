package es.NTTEnterprise.RIntellix.ms_risk_engine.application.usecases;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.RiskMetricsCalculationContextMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.SimulationModelPayloadMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution.ModelEndpointResolver;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution.ScoringModelExecutionStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.Scoring;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.FormChanges;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.SimulationDelta;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.SimulationDraft;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.exceptions.InvalidFormChangesException;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.exceptions.ScoringNotFoundException;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.ports.input.SimulationDraftPortService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.ports.output.FetchScoringPort;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskIndicatorCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskMetricsCalculationContext;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.SimulationDeltaCalculator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case that orchestrates stateless simulation draft calculations.
 *
 * Implements the SimulationDraftPortService (domain input port) to properly
 * expose
 * the use case functionality through the hexagonal architecture boundary.
 *
 * Follows the same pattern as ScoringProcessingService:
 * - Fetches base scoring data via output ports
 * - Merges form changes with base features
 * - Invokes model prediction asynchronously (via ModelPredictionPort)
 * - Returns simulation draft with metrics and deltas
 *
 * @author Lucía Fernández Mancebo
 * @date 15/03/2026
 */
@Slf4j

public class CalculateSimulationDraftUseCase implements SimulationDraftPortService {

        private final FetchScoringPort fetchScoringPort;
        private final List<ScoringModelExecutionStrategy> scoringModelExecutionStrategies;
        private final RiskMetricsCalculationService metricsCalculationService;
        private final RiskIndicatorCalculationService riskIndicatorCalculationService;
        private final SimulationModelPayloadMapper simulationPayloadMapper;
        private final SimulationDeltaCalculator simulationDeltaCalculator;
        private final RiskMetricsCalculationContextMapper contextMapper;

        /**
         * Constructor of the CalculateSimulationDraftUseCase class.
         *
         * @param fetchScoringPort                output port to fetch scoring data.
         * @param scoringModelExecutionStrategies the available model execution
         *                                        strategies for determining
         *                                        model endpoints based on request type.
         * @param metricsCalculationService       service to calculate risk metrics
         *                                        (orchestrates model invocation and
         *                                        calculation).
         * @param riskIndicatorCalculationService domain service for calculating risk
         *                                        indicators
         *                                        (DTI and LTV).
         * @param simulationPayloadMapper         mapper for transforming simulation
         *                                        variables to model payload using
         *                                        canonical model field names.
         * @param simulationDeltaCalculator       calculator for simulation delta.
         * @param contextMapper                   mapper for building the calculation
         *                                        context.
         */
        public CalculateSimulationDraftUseCase(
                        final FetchScoringPort fetchScoringPort,
                        final List<ScoringModelExecutionStrategy> scoringModelExecutionStrategies,
                        final RiskMetricsCalculationService metricsCalculationService,
                        final RiskIndicatorCalculationService riskIndicatorCalculationService,
                        final SimulationModelPayloadMapper simulationPayloadMapper,
                        final SimulationDeltaCalculator simulationDeltaCalculator,
                        final RiskMetricsCalculationContextMapper contextMapper) {
                this.fetchScoringPort = Objects.requireNonNull(fetchScoringPort);
                this.scoringModelExecutionStrategies = Objects.requireNonNull(scoringModelExecutionStrategies);
                this.metricsCalculationService = Objects.requireNonNull(metricsCalculationService);
                this.riskIndicatorCalculationService = Objects.requireNonNull(riskIndicatorCalculationService);
                this.simulationPayloadMapper = Objects.requireNonNull(simulationPayloadMapper);
                this.simulationDeltaCalculator = Objects.requireNonNull(simulationDeltaCalculator);
                this.contextMapper = Objects.requireNonNull(contextMapper);
        }

        /**
         * Calculates a simulation draft without persistence.
         *
         * Process flow (following hexagonal architecture):
         * 1. Fetch base scoring data for the request
         * 2. Merge base features with form changes to create simulation input
         * 3. Fire async model prediction call to obtain new PD
         * 4. Calculate deltas by comparing simulated metrics with base metrics
         *
         * @param requestId   the request identifier.
         * @param formChanges the user-specified modified values.
         * @return the simulation draft with simulated metrics and deltas.
         * @throws ScoringNotFoundException    if scoring data cannot be retrieved
         * @throws InvalidFormChangesException if form changes are invalid
         */
        @Override
        public SimulationDraft calculateDraft(final String requestId, final String requestType,
                        final FormChanges formChanges) {

                if (formChanges == null || formChanges.getValues() == null || formChanges.getValues().isEmpty()) {
                        throw new InvalidFormChangesException(
                                        String.format(LogMessage.SIMULATION_FORM_CHANGES_REQUIRED, requestId));
                }

                // 1. Retrieve base scoring - contains input snapshot and base metrics
                final Scoring baseScoring = fetchScoringPort.fetchByRequestId(requestId);
                if (baseScoring == null || baseScoring.getResults() == null) {
                        throw new ScoringNotFoundException(String.format(
                                        LogMessage.SCORING_RETRIEVING_MESSAGE_EXCEPTION,
                                        requestId,
                                        LogMessage.SIMULATION_BASE_SCORING_NULL));
                }

                log.info(LogMessage.SIMULATION_BASE_SCORING_RETRIEVED, requestId, baseScoring.toString());

                // 2. Merge form changes with base scoring for calculation

                final Map<String, Object> mergedVariables = mergeData(baseScoring, formChanges, requestType);

                // 3. For dynamic calculated fields like dti & ltv, recalculate them and replace
                // then in the mergeVariables.
                riskIndicatorCalculationService.recalculateRiskIndicators(
                                mergedVariables, requestType, baseScoring.getInputSnapshot());

                // Assign all values to model input class.
                final RiskMetricsCalculationContext context = contextMapper.buildContext(
                                baseScoring,
                                formChanges,
                                requestType,
                                mergedVariables,
                                resolveModelEndpointPath(requestType),
                                requestId);

                // Reuse scoring calculation service to call model and calculate riskMetrics
                // based on strategy.
                final var result = metricsCalculationService.calculateRiskMetrics(context);
                final RiskMetrics simulatedMetrics = result.riskMetrics();

                // Calculate delta between base and simulated metrics
                final SimulationDelta delta = simulationDeltaCalculator.calculateDelta(baseScoring, simulatedMetrics);

                return new SimulationDraft(formChanges, simulatedMetrics, delta);
        }

        /**
         * Resolves the model endpoint path based on request type.
         * 
         * Delegates to ModelEndpointResolver to determine which AI model
         * should be invoked (Loan, Mortgage, or Credit Card).
         * This ensures consistency between scoring and simulation workflows.
         *
         * @param requestType the request type (PRESTAMO, HIPOTECA, TARJETA_CREDITO)
         * @return the appropriate model endpoint path.
         * @throws IllegalArgumentException if request type is not supported.
         */
        private String resolveModelEndpointPath(final String requestType) {
                return ModelEndpointResolver.resolveEndpointPath(requestType, scoringModelExecutionStrategies);
        }

        /**
         * Merges the base scoring input features with the user-provided
         * form changes to create a comprehensive set of variables for PD prediction.
         *
         * CRITICAL: Base variables (source from MongoDB) are mapped to the
         * canonical model field names BEFORE merging to ensure:
         * 1. All merged data uses consistent canonical model field names
         * 2. Form changes (already using canonical model field names) can properly
         * override base values
         * 3. No duplicate source/canonical field name conflicts
         *
         * @param baseScoring the base scoring data containing the original input
         *                    snapshot (in English) and metrics.
         * @param formChanges the user-specified modified values (already using
         *                    canonical model field names).
         * @return a Map containing the merged variables using canonical model field
         *         names, ready for model
         *         invocation without further transformation.
         */
        private Map<String, Object> mergeData(final Scoring baseScoring, final FormChanges formChanges,
                        final String requestType) {
                final Map<String, Object> baseInputs = baseScoring.getInputSnapshot();
                if (baseInputs == null || baseInputs.isEmpty()) {
                        throw new ScoringNotFoundException(String.format(
                                        LogMessage.SCORING_RETRIEVING_INPUT_EMPTY,
                                        baseScoring.getRequestId()));
                }

                // Normalize scoring variables (e.g., booleans, enums, fractions)
                final Map<String, Object> normalizedBase = simulationPayloadMapper
                                .normalizeVariables(baseInputs);

                // Normalize formChanges values (e.g., booleans, enums, fractions)
                final Map<String, Object> normalizedFormChanges = simulationPayloadMapper
                                .normalizeVariables(formChanges.getValues());

                // Override with normalized form changes
                normalizedBase.putAll(normalizedFormChanges);

                return normalizedBase;
        }
}
