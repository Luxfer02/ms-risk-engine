package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation;

import java.util.List;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.factories.GenericStrategyFactory;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;

/**
 * Factory class for resolving the appropriate risk calculation strategy
 * based on request type and revolving flag.
 *
 * For credit card types, the isRevolving flag disambiguates between
 * standard and revolving risk calculation strategies.
 *
 * @author Lucía Fernández Mancebo
 * @date 25/04/2026
 */
public final class RiskCalculationStrategyFactory {

    /**
     * Private constructor to prevent instantiation of this utility factory class.
     * This class is not meant to be instantiated; it only provides a static factory
     * method.
     * If instantiation is attempted, an UnsupportedOperationException is thrown to
     * indicate that this is a utility class.
     * 
     * @throws UnsupportedOperationException always thrown to prevent instantiation.
     */
    private RiskCalculationStrategyFactory() {
        throw new UnsupportedOperationException(LogMessage.FACTORY_CLASS_NEVER_INSTANTIATE);
    }

    /**
     * Resolves the appropriate risk calculation strategy.
     *
     * @param requestType the normalized request type; must not be null.
     * @param isRevolving the revolving flag for credit card disambiguation;
     *                    null for non-credit-card types.
     * @param strategies  the available strategy beans; must not be null.
     * @return the matching RiskCalculationStrategy.
     * @throws IllegalArgumentException if no strategy supports the given
     *                                  combination.
     * @throws NullPointerException     if requestType or strategies is null.
     */
    public static RiskCalculationStrategy createStrategy(
            final String requestType,
            final Boolean isRevolving,
            final List<RiskCalculationStrategy> strategies) {
        Objects.requireNonNull(requestType, LogMessage.REQUEST_TYPE_CANNOT_BE_NULL);
        Objects.requireNonNull(strategies, LogMessage.STRATEGIES_LIST_CANNOT_BE_NULL);
        return GenericStrategyFactory.selectStrategy(
                strategies,
                strategy -> strategy.supports(requestType, isRevolving),
                String.format(LogMessage.NO_RISK_CALCULATION_STRATEGY_FOUND, requestType, isRevolving));
    }
}
