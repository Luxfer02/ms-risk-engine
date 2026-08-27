package es.NTTEnterprise.RIntellix.ms_risk_engine.application.usecases;

import java.util.List;
import java.util.Objects;




import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringGenerationPayload;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.output.ScoringModelExecutionResultDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.ScoringResultMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.Scoring;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.ports.input.ScoringProcessingPortService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.ports.output.ScoringResultPublisherPort;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution.ScoringModelExecutionStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution.ScoringModelExecutionStrategyFactory;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Application service that orchestrates scoring message processing.
 *
 * Responsibilities:
 * - Evaluate hard-cutoff financial-ratio rules (DTI / LTV / LTI) before the
 * AI model is invoked. If a rule fires, a rejected Scoring with PD=1 is
 * published directly and the model call is skipped entirely.
 * - Obtain and normalize request type from scoring payload.
 * - Select a request-type strategy and trigger model execution.
 * - Map strategy execution result into a Scoring domain entity.
 * - Publish the scoring result to Kafka for persistence.
 *
 * @author Lucía Fernández Mancebo
 * @date 25/04/2026
 */
@Slf4j

public class ScoringProcessingService implements ScoringProcessingPortService {

    private final List<ScoringModelExecutionStrategy> scoringModelExecutionStrategies;
    private final ScoringResultMapper scoringResultMapper;
    private final ScoringResultPublisherPort scoringResultPublisher;
    private final String modelVersion;

    /**
     * Constructor of the ScoringProcessingService class.
     *
     * @param scoringModelExecutionStrategies the available model execution
     *                                        strategies.
     * @param scoringResultMapper             the mapper for converting execution
     *                                        result to
     *                                        scoring.
     * @param scoringResultPublisher          the output port for publishing scoring
     *                                        results to Kafka.
     * @param modelVersion                    the configured model version label.
     */
    public ScoringProcessingService(
            final List<ScoringModelExecutionStrategy> scoringModelExecutionStrategies,
            final ScoringResultMapper scoringResultMapper,
            final ScoringResultPublisherPort scoringResultPublisher,
            final String modelVersion) {
        this.scoringModelExecutionStrategies = Objects.requireNonNull(scoringModelExecutionStrategies);
        this.scoringResultMapper = Objects.requireNonNull(scoringResultMapper);
        this.scoringResultPublisher = Objects.requireNonNull(scoringResultPublisher);
        this.modelVersion = Objects.requireNonNull(modelVersion);
    }

    @Override
    public boolean processScoringMessage(final ScoringGenerationPayload scoringCreationPayload) {

        if (scoringCreationPayload == null) {
            log.warn(LogMessage.SCORING_PAYLOAD_NULL);
            return false;
        }

        final String requestId = scoringCreationPayload.getRequestId();
        final String requestType = scoringCreationPayload.getRequestType();
        log.info(LogMessage.SCORING_MESSAGE_PROCESSING, requestId, requestType);

        try {
            final long startTime = System.currentTimeMillis();

            // We get the strategy based on the request type.
            final ScoringModelExecutionStrategy strategy = ScoringModelExecutionStrategyFactory.createStrategy(
                    requestType,
                    scoringModelExecutionStrategies);
            final ScoringModelExecutionResultDTO executionResult = strategy.executePredictionModel(
                    scoringCreationPayload,
                    requestType,
                    requestId);

            final boolean isCutoff = isHardCutoffResult(executionResult);
            final String actualModelVersion = isCutoff ? "RULE_ENGINE_v1" : modelVersion;

            final Scoring scoring = scoringResultMapper.toScoring(
                    requestId,
                    actualModelVersion,
                    executionResult.getModelRequestPayload(),
                    executionResult.getPredictionResult(),
                    executionResult.getRiskMetrics());

            scoringResultPublisher.publishScoringResult(scoring);

            final long endTime = System.currentTimeMillis();
            log.info(LogMessage.SCORING_EXECUTION_TIME, (endTime - startTime), requestId);

            // Log different messages based on whether the result is a hard cutoff
            if (isCutoff) {
                log.info(LogMessage.SCORING_HARD_CUTOFF_PERSISTED,
                        requestId,
                        scoring.getResults() != null ? scoring.getResults().getExposureAtDefault() : 0.0,
                        scoring.getResults() != null ? scoring.getResults().getLossGivenDefault() : 0.0,
                        scoring.getResults() != null ? scoring.getResults().getExpectedCalculatedLoss() : 0.0);
            } else {
                log.info(LogMessage.SCORING_PROCESSED_SUCCESSFULLY,
                        requestId,
                        scoring.getResults() == null ? null : scoring.getResults().getProbabilityOfDefault(),
                        scoring.getResults() == null ? null : scoring.getResults().getRiskLevel(),
                        scoring.getExplainability() == null ? 0 : scoring.getExplainability().size());
            }
            return true;
        } catch (RuntimeException ex) {
            log.error(LogMessage.ERROR_PROCESSING_SCORING_MESSAGE, requestId, requestType, ex.getMessage(), ex);
            return false;
        }
    }

    private boolean isHardCutoffResult(final ScoringModelExecutionResultDTO executionResult) {
        return executionResult != null && executionResult.isHardCutoff();
    }
}
