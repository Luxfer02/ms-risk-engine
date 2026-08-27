package es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution;

import java.util.List;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.factories.GenericStrategyFactory;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;

/**
 * Factory class for creating ScoringModelExecutionStrategy instances based on
 * request type.
 * 
 * @date 27/08/2026
 */
public final class ScoringModelExecutionStrategyFactory {

    private ScoringModelExecutionStrategyFactory() {
        throw new UnsupportedOperationException(LogMessage.FACTORY_CLASS_NEVER_INSTANTIATE);
    }

    /**
     * Creates the appropriate scoring execution strategy based on request type.
     *
     * Uses existing Spring-injected strategy beans instead of creating new
     * strategy instances manually.
     *
     * @param requestType the request type; must not be null.
     * @param strategies  the available strategy beans; must not be null.
     * @return a ScoringModelExecutionStrategy suitable for the request type.
     * @throws IllegalArgumentException if request type is not recognized.
     * @throws NullPointerException     if request type or strategies is null.
     */
    public static ScoringModelExecutionStrategy createStrategy(
            final String requestType,
            final List<ScoringModelExecutionStrategy> strategies) {
        Objects.requireNonNull(requestType, LogMessage.REQUEST_TYPE_CANNOT_BE_NULL);
        Objects.requireNonNull(strategies, LogMessage.STRATEGIES_LIST_CANNOT_BE_NULL);
        return GenericStrategyFactory.selectStrategy(
                strategies,
                strategy -> strategy.supports(requestType),
                String.format(LogMessage.REQUEST_TYPE_NOT_FOUND, requestType));
    }

}
