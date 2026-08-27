package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.indicators;

import java.util.Map;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.exceptions.InvalidFormChangesException;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums.RequestType;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.DtiCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.utils.MapUtilities;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.FinancialMetricsCalculator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadFieldNames;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for calculating risk indicators (DTI, LTV) for Mortgages.
 *
 * @author Lucía Fernández Mancebo
 * @date 16/08/2026
 */
@Slf4j
public class MortgageRiskIndicatorStrategy extends AbstractLoanRiskIndicatorStrategy {

    /**
     * Constructor for MortgageRiskIndicatorStrategy.
     *
     * @param dtiCalculationService domain service for DTI calculations.
     */
    public MortgageRiskIndicatorStrategy(final DtiCalculationService dtiCalculationService) {
        super(dtiCalculationService);
    }

    @Override
    public boolean supports(final String requestType) {
        if (requestType == null) {
            return false;
        }
        try {
            return RequestType.fromValue(requestType) == RequestType.HIPOTECA;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public void recalculateIndicators(final Map<String, Object> mergedVariables,
            final Map<String, Object> baseInputSnapshot) {
        try {
            // Calculate DTI using common logic
            calculateAndSetDti(mergedVariables, baseInputSnapshot);

            // Calculate LTV specific to Mortgages
            final double loanAmount = MapUtilities.getDouble(mergedVariables, ModelPayloadFieldNames.FIELD_LOAN_AMOUNT,
                    0);
            recalculateLtvForMortgage(mergedVariables, baseInputSnapshot, loanAmount);

        } catch (IllegalArgumentException | ClassCastException | NullPointerException e) {
            log.warn(LogMessage.SIMULATION_RISK_INDICATORS_CALCULATION_ERROR, e.getMessage());
        } catch (InvalidFormChangesException e) {
            log.warn(LogMessage.SIMULATION_RISK_INDICATORS_CALCULATION_ERROR, e.getMessage());
        }
    }

    /**
     * Recalculates LTV for mortgages.
     * 
     * @param mergedVariables   map containing simulation variables.
     * @param baseInputSnapshot map containing base input snapshot.
     * @param loanAmount        the loan amount.
     * @throws InvalidFormChangesException if the property value is invalid.
     */
    private void recalculateLtvForMortgage(
            final Map<String, Object> mergedVariables,
            final Map<String, Object> baseInputSnapshot,
            final double loanAmount) throws InvalidFormChangesException {

        Double propertyValue = null;
        // Get the propertyValue from formChanges.
        if (mergedVariables.containsKey(ModelPayloadFieldNames.FIELD_PROPERTY_VALUE)) {
            propertyValue = MapUtilities.getDouble(mergedVariables, ModelPayloadFieldNames.FIELD_PROPERTY_VALUE, 0);
        }
        // If no changes applied obtain the original from base.
        else if (baseInputSnapshot != null && baseInputSnapshot.containsKey(ModelPayloadFieldNames.FIELD_LTV)
                && baseInputSnapshot.containsKey(ModelPayloadFieldNames.FIELD_LOAN_AMOUNT)) {
            final double originalLtv = MapUtilities.getDouble(baseInputSnapshot, ModelPayloadFieldNames.FIELD_LTV, 0);
            final double originalLoan = MapUtilities.getDouble(baseInputSnapshot,
                    ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, 0);
            if (originalLtv > 0) {
                propertyValue = originalLoan / originalLtv;
            }
        }
        // Call to calculateLtv method from FinancialMetricsCalculator class.
        if (propertyValue != null && propertyValue > 0) {
            final double newLtv = FinancialMetricsCalculator.calculateLtv(loanAmount, propertyValue);
            mergedVariables.put(ModelPayloadFieldNames.FIELD_LTV, newLtv);
            log.info(LogMessage.SIMULATION_LTV_RECALCULATED, newLtv, loanAmount, propertyValue);
        }
    }
}
