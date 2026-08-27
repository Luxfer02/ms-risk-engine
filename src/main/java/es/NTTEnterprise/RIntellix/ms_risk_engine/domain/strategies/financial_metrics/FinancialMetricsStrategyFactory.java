package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics;

import java.util.List;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;

/**
 * Factory class that creates the appropriate FinancialMetricsStrategy based on
 * the request type and revolving status.
 *
 * @author Lucía Fernández Mancebo
 * @date 27/08/2026
 */
public final class FinancialMetricsStrategyFactory {

    private FinancialMetricsStrategyFactory() {
        throw new UnsupportedOperationException(LogMessage.FACTORY_CLASS_NEVER_INSTANTIATE);
    }

    /**
     * Creates and returns the appropriate FinancialMetricsStrategy.
     *
     * @param requestType the request type
     * @param isRevolving whether the request is revolving
     * @param strategies  the list of available strategies
     * @return the resolved strategy
     * @throws IllegalArgumentException if no matching strategy is found
     */
    public static FinancialMetricsStrategy createStrategy(
            final String requestType,
            final Boolean isRevolving,
            final List<FinancialMetricsStrategy> strategies) {
        
        Objects.requireNonNull(requestType, LogMessage.REQUEST_TYPE_CANNOT_BE_NULL);
        Objects.requireNonNull(strategies, LogMessage.FINANCIAL_METRICS_STRATEGIES_CANNOT_BE_NULL);

        for (FinancialMetricsStrategy strategy : strategies) {
            if (strategy.supports(requestType, isRevolving)) {
                return strategy;
            }
        }

        throw new IllegalArgumentException(String.format(LogMessage.NO_FINANCIAL_METRICS_STRATEGY_FOUND, requestType, isRevolving));
    }
}
