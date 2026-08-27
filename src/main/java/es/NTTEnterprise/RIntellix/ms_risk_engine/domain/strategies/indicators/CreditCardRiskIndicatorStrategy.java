package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.indicators;

import java.util.Map;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.exceptions.InvalidFormChangesException;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums.RequestType;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.CreditCardFinancialMetricsCalculator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.DtiCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.utils.MapUtilities;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.MathUtilities;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadFieldNames;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.SimulationConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for calculating risk indicators (DTI) for Credit Cards.
 *
 * @author Lucía Fernández Mancebo
 * @date 16/08/2026
 */
@Slf4j
public class CreditCardRiskIndicatorStrategy implements RiskIndicatorCalculationStrategy {

    private final DtiCalculationService dtiCalculationService;

    /**
     * Constructor for CreditCardRiskIndicatorStrategy.
     *
     * @param dtiCalculationService domain service for DTI calculations.
     */
    public CreditCardRiskIndicatorStrategy(final DtiCalculationService dtiCalculationService) {
        this.dtiCalculationService = Objects.requireNonNull(dtiCalculationService,
                LogMessage.DTI_CALCULATION_SERVICE_CANNOT_BE_NULL);
    }

    @Override
    public boolean supports(final String requestType) {
        if (requestType == null) {
            return false;
        }
        try {
            return RequestType.fromValue(requestType) == RequestType.TARJETA_CREDITO;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public void recalculateIndicators(final Map<String, Object> mergedVariables,
            final Map<String, Object> baseInputSnapshot) {
        try {
            final double creditLimit = MapUtilities.getDouble(mergedVariables,
                    ModelPayloadFieldNames.FIELD_CREDIT_LIMIT, 0);
            final double annualIncome = MapUtilities.getDouble(mergedVariables,
                    ModelPayloadFieldNames.FIELD_ANNUAL_INCOME, 0);
            final Boolean isRevolving = MapUtilities.getBoolean(mergedVariables,
                    ModelPayloadFieldNames.FIELD_IS_REVOLVING, false);

            final double monthlyPayment = CreditCardFinancialMetricsCalculator.calculateMonthlyPayment(creditLimit,
                    isRevolving);

            final double existingObligationsAnnual = MapUtilities.getDouble(
                    mergedVariables, ModelPayloadFieldNames.FIELD_EXISTING_OBLIGATIONS, 0.0);
            final double existingObligations = existingObligationsAnnual / SimulationConstants.MONTHS_PER_YEAR;

            final double newDti = MathUtilities.roundFinal(
                    dtiCalculationService.calculateDtiWithExistingObligations(
                            monthlyPayment,
                            annualIncome,
                            existingObligations));

            mergedVariables.put(ModelPayloadFieldNames.FIELD_DTI, newDti);
            log.info(LogMessage.SIMULATION_DTI_RECALCULATED, newDti, monthlyPayment, annualIncome);

        } catch (IllegalArgumentException | ClassCastException | NullPointerException e) {
            log.warn(LogMessage.SIMULATION_RISK_INDICATORS_CALCULATION_ERROR, e.getMessage());
        } catch (InvalidFormChangesException e) {
            log.warn(LogMessage.SIMULATION_RISK_INDICATORS_CALCULATION_ERROR,
                    "Invalid data type in variables: " + e.getMessage());
        }
    }
}
