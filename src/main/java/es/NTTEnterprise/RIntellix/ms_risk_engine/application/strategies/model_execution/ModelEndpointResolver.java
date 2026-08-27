package es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution;

import java.util.List;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;

/**
 * Resolves the correct model endpoint path based on request type.
 * 
 * This resolver is reusable across both scoring and simulation workflows,
 * eliminating code duplication and ensuring consistent model routing.
 * 
 * The endpoint path determines which AI model to invoke:
 * - Loan/Mortgage: /api/v1/risk/predict-loan
 * - Credit Card: /api/v1/risk/predict-credit-card
 * 
 * @author Lucía Fernández Mancebo
 * @date 27/08/2026
 */
public final class ModelEndpointResolver {

    private ModelEndpointResolver() {
        throw new UnsupportedOperationException(LogMessage.FACTORY_CLASS_NEVER_INSTANTIATE);
    }

    /**
     * Resolves the model endpoint path for a given request type.
     * 
     * Delegates to the appropriate strategy to determine the endpoint,
     * ensuring consistency between scoring and simulation.
     *
     * @param requestType the request type (PRESTAMO, HIPOTECA, TARJETA_CREDITO).
     * @param strategies  the available scoring strategies.
     * @return the model endpoint path (e.g., /api/v1/risk/predict-loan).
     * @throws IllegalArgumentException if request type is not supported.
     */
    public static String resolveEndpointPath(
            final String requestType,
            final List<ScoringModelExecutionStrategy> strategies) {
        Objects.requireNonNull(requestType, LogMessage.REQUEST_TYPE_CANNOT_BE_NULL);
        Objects.requireNonNull(strategies, LogMessage.STRATEGIES_LIST_CANNOT_BE_NULL);

        // Find the strategy that supports this request type
        for (ScoringModelExecutionStrategy strategy : strategies) {
            if (strategy.supports(requestType)) {
                return strategy.modelEndpointPath();
            }
        }

        throw new IllegalArgumentException(
                String.format(LogMessage.REQUEST_TYPE_NOT_FOUND, requestType));
    }
}
