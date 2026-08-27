package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.indicators;

import java.util.Map;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.exceptions.InvalidFormChangesException;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums.RequestType;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.DtiCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for calculating risk indicators (DTI) for Loans.
 *
 * @author Lucía Fernández Mancebo
 * @date 16/08/2026
 */
@Slf4j
public class LoanRiskIndicatorStrategy extends AbstractLoanRiskIndicatorStrategy {

    /**
     * Constructor for LoanRiskIndicatorStrategy.
     *
     * @param dtiCalculationService domain service for DTI calculations.
     */
    public LoanRiskIndicatorStrategy(final DtiCalculationService dtiCalculationService) {
        super(dtiCalculationService);
    }

    @Override
    public boolean supports(final String requestType) {
        if (requestType == null) {
            return false;
        }
        try {
            return RequestType.fromValue(requestType) == RequestType.PRESTAMO;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public void recalculateIndicators(final Map<String, Object> mergedVariables,
            final Map<String, Object> baseInputSnapshot) {
        try {
            calculateAndSetDti(mergedVariables, baseInputSnapshot);
        } catch (IllegalArgumentException | ClassCastException | NullPointerException e) {
            log.warn(LogMessage.SIMULATION_RISK_INDICATORS_CALCULATION_ERROR, e.getMessage());
        } catch (InvalidFormChangesException e) {
            log.warn(LogMessage.SIMULATION_RISK_INDICATORS_CALCULATION_ERROR,
                    "Invalid data type in variables: " + e.getMessage());
        }
    }
}
