package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.input.kafka.factories;

import java.util.List;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.input.kafka.strategy.ScoringGenerationMessageStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.factories.GenericStrategyFactory;

/**
 * Factory class for selecting ScoringGenerationMessageStrategy instances based on
 * request type.
 * @date 27/08/2026
 */
public final class ScoringKafkaMessageStrategyFactory {

    private ScoringKafkaMessageStrategyFactory() {
        throw new UnsupportedOperationException(LogMessage.FACTORY_CLASS_NEVER_INSTANTIATE);
    }

    /**
     * Selects the appropriate scoring message strategy based on request type.
     *
     * @param requestType the request type; must not be null.
     * @param strategies  the available strategy beans; must not be null.
     * @return a ScoringGenerationMessageStrategy suitable for the request type.
     * @throws IllegalArgumentException if request type is not recognized.
     * @throws NullPointerException     if request type or strategies is null.
     */
    public static ScoringGenerationMessageStrategy selectStrategy(
            final String requestType,
            final List<ScoringGenerationMessageStrategy> strategies) {
        Objects.requireNonNull(requestType, LogMessage.REQUEST_TYPE_CANNOT_BE_NULL);
        Objects.requireNonNull(strategies, LogMessage.STRATEGIES_LIST_CANNOT_BE_NULL);
        return GenericStrategyFactory.selectStrategy(
                strategies,
                strategy -> strategy.supports(requestType),
                String.format(LogMessage.REQUEST_TYPE_NOT_FOUND, requestType));
    }
}
