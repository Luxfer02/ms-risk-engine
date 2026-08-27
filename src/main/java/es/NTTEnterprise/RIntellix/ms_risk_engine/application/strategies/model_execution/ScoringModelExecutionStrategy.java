package es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringGenerationPayload;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.output.ScoringModelExecutionResultDTO;

/**
 * Strategy interface for request-type specific model execution.
 *
 * @author Lucía Fernández Mancebo
 * @date 25/04/2026
 */
public interface ScoringModelExecutionStrategy {

    /**
     * Indicates whether strategy supports normalized request type.
     *
     * @param requestType the normalized request type.
     * @return true when strategy supports the request type.
     */
    boolean supports(String requestType);

    /**
     * Executes full model flow for specific request type.
     *
     * @param payload     the scoring message payload.
     * @param requestType the normalized request type.
     * @param requestId   the request identifier.
     * @return the execution result with input snapshot and prediction output.
     */
    ScoringModelExecutionResultDTO executePredictionModel(
            ScoringGenerationPayload payload,
            String requestType,
            String requestId);

    /**
     * Returns the model endpoint path for the specific execution strategy.
     *
     * @return the endpoint path string.
     */
    String modelEndpointPath();
}
