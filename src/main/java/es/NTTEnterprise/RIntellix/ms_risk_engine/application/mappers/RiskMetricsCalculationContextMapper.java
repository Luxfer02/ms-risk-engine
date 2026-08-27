package es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers;

import java.util.Map;
import org.springframework.stereotype.Component;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.Scoring;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.FormChanges;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskMetricsCalculationContext;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadFieldNames;

/**
 * Mapper for building the RiskMetricsCalculationContext from form changes and base scoring.
 * @date 27/08/2026
 */
@Component
public class RiskMetricsCalculationContextMapper {

    public RiskMetricsCalculationContext buildContext(
            Scoring baseScoring,
            FormChanges formChanges,
            String requestType,
            Map<String, Object> mergedVariables,
            String endpointPath,
            String requestId) {
        
        final Map<String, Object> baseInputs = baseScoring.getInputSnapshot();
        final Map<String, Object> formValues = formChanges.getValues();

        final Boolean isRevolving = extractBoolean(formValues, baseInputs, ModelPayloadFieldNames.FIELD_IS_REVOLVING);

        Double amount = extractDouble(formValues, baseInputs, ModelPayloadFieldNames.FIELD_LOAN_AMOUNT);
        if (amount == null) {
            amount = extractDouble(formValues, baseInputs, ModelPayloadFieldNames.FIELD_CREDIT_LIMIT);
        }

        final Double ltv = extractDouble(formValues, baseInputs, ModelPayloadFieldNames.FIELD_LTV);
        final Double annualIncome = extractDouble(formValues, baseInputs, ModelPayloadFieldNames.FIELD_ANNUAL_INCOME);
        final Integer termMonths = extractInteger(formValues, baseInputs, ModelPayloadFieldNames.FIELD_TERM_MONTHS);
        final Double interestRate = extractDouble(formValues, baseInputs, ModelPayloadFieldNames.FIELD_INTEREST_RATE);
        final Double existingObligations = extractDouble(formValues, baseInputs, ModelPayloadFieldNames.FIELD_EXISTING_OBLIGATIONS);
        final Double existingMonthly = existingObligations != null ? existingObligations / 12.0 : 0.0;

        return new RiskMetricsCalculationContext(
                mergedVariables,
                requestId,
                endpointPath,
                requestType,
                isRevolving != null ? isRevolving : false,
                amount,
                ltv,
                annualIncome,
                termMonths,
                interestRate,
                existingMonthly
        );
    }

    private Boolean extractBoolean(Map<String, Object> formValues, Map<String, Object> baseInputs, String fieldName) {
        if (formValues != null && formValues.containsKey(fieldName)) {
            return (Boolean) formValues.get(fieldName);
        }
        return (Boolean) baseInputs.get(fieldName);
    }

    private Double extractDouble(Map<String, Object> formValues, Map<String, Object> baseInputs, String fieldName) {
        Object val = null;
        if (formValues != null && formValues.containsKey(fieldName)) {
            val = formValues.get(fieldName);
        } else {
            val = baseInputs.get(fieldName);
        }
        if (val instanceof Number n) {
            return n.doubleValue();
        }
        return null;
    }

    private Integer extractInteger(Map<String, Object> formValues, Map<String, Object> baseInputs, String fieldName) {
        Object val = null;
        if (formValues != null && formValues.containsKey(fieldName)) {
            val = formValues.get(fieldName);
        } else {
            val = baseInputs.get(fieldName);
        }
        if (val instanceof Number n) {
            return n.intValue();
        }
        return null;
    }
}
