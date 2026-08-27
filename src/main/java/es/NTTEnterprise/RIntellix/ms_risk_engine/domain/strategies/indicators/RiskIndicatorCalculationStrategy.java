package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.indicators;

import java.util.Map;

/**
 * Strategy interface for recalculating risk indicators (DTI, LTV, etc.)
 * based on the specific type of financial product (Loan, Mortgage, Credit
 * Card).
 *
 * @author Lucía Fernández Mancebo
 * @date 16/08/2026
 */
public interface RiskIndicatorCalculationStrategy {

    /**
     * Determines if this strategy supports the given request type.
     *
     * @param requestType the type of the request (e.g., "PRESTAMO", "HIPOTECA",
     *                    "TARJETA_CREDITO")
     * @return true if supported, false otherwise
     */
    boolean supports(String requestType);

    /**
     * Recalculates the risk indicators and updates them in the merged variables
     * map.
     *
     * @param mergedVariables   the merged simulation variables (updated in-place)
     * @param baseInputSnapshot the base input snapshot containing original
     *                          parameters
     */
    void recalculateIndicators(Map<String, Object> mergedVariables, Map<String, Object> baseInputSnapshot);
}
