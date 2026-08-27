package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.indicators.RiskIndicatorCalculationStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * Domain service for calculating critical risk indicators (DTI and LTV).
 * 
 * Encapsulates business logic for recalculating risk metrics during simulation,
 * ensuring that critical input features are properly computed before model
 * invocation.
 * 
 * This service follows the Hexagonal Architecture pattern by being a domain
 * service that contains pure business logic. It now uses the Strategy pattern
 * to determine how to calculate indicators based on the specific product type.
 *
 * @author Lucía Fernández Mancebo
 * @date 27/08/2026
 */
@Slf4j
public class RiskIndicatorCalculationService {

    private final List<RiskIndicatorCalculationStrategy> calculationStrategies;

    /**
     * Constructor for RiskIndicatorCalculationService.
     *
     * @param calculationStrategies list of available risk indicator calculation
     *                              strategies.
     */
    public RiskIndicatorCalculationService(final List<RiskIndicatorCalculationStrategy> calculationStrategies) {
        this.calculationStrategies = Objects.requireNonNull(calculationStrategies,
                "Risk indicator calculation strategies list cannot be null");
    }

    /**
     * Recalculates critical risk indicators (DTI and LTV) before model invocation
     * using the appropriate strategy for the request type.
     *
     * @param mergedVariables   the merged simulation variables (updated in-place).
     * @param requestType       the request type (PRESTAMO, HIPOTECA,
     *                          TARJETA_CREDITO).
     * @param baseInputSnapshot the base input snapshot containing original
     *                          parameters.
     */
    public void recalculateRiskIndicators(
            final Map<String, Object> mergedVariables,
            final String requestType,
            final Map<String, Object> baseInputSnapshot) {

        final Optional<RiskIndicatorCalculationStrategy> strategyOpt = calculationStrategies.stream()
                .filter(strategy -> strategy.supports(requestType))
                .findFirst();

        if (strategyOpt.isPresent()) {
            strategyOpt.get().recalculateIndicators(mergedVariables, baseInputSnapshot);
        } else {
            log.warn("No RiskIndicatorCalculationStrategy found for requestType: {}", requestType);
        }
    }
}
