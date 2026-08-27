package es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadFieldNames;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadUtilities;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.SimulationConstants;

/**
 * Mapper for transforming merged simulation variables into model payload
 * format.
 *
 * Field names are expected to be in canonical camelCase format natively.
 * This keeps the mapper generic and eliminates the need for alias translation.
 */

/**
 * Core component: SimulationModelPayloadMapper.
 * Encapsulates the logic and responsibilities assigned to this element
 * within the Hexagonal Architecture, ensuring separation of concerns.
 *
 * @author Lucía Fernández Mancebo
 * @date 28/07/2026
 */
public class SimulationModelPayloadMapper {

    private final ModelPayloadUtilities payloadUtilities;

    public SimulationModelPayloadMapper(final ModelPayloadUtilities payloadUtilities) {
        this.payloadUtilities = Objects.requireNonNull(payloadUtilities,
                LogMessage.MODEL_PAYLOAD_UTILITIES_CANNOT_BE_NULL);
    }

    /**
     * Method that normalizes the values of the variables.
     * Converts interest rate from percentage to fraction, normalizes enums and
     * booleans.
     * 
     * @param variables variables map (base or form changes)
     * @return variables map normalized
     */
    public Map<String, Object> normalizeVariables(final Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return new HashMap<>();
        }

        final Map<String, Object> normalized = new HashMap<>();
        for (final Map.Entry<String, Object> entry : variables.entrySet()) {
            normalized.put(entry.getKey(), normalizeValue(entry.getKey(), entry.getValue()));
        }

        // Sincronizar incomeType si employmentStatus está presente
        synchronizeDependentFields(normalized);

        return normalized;
    }

    /**
     * Normalizes the value for a specific model feature based on payload rules.
     * 
     * @param fieldName The name of the field.
     * @param value     The value to normalize.
     * @return The normalized value.
     */
    private Object normalizeValue(final String fieldName, final Object value) {
        if (value == null) {
            return null;
        }

        if (ModelPayloadFieldNames.FIELD_HAS_MORTGAGE.equals(fieldName)) {
            return payloadUtilities.toModelBoolean((Boolean) value);
        }

        if (ModelPayloadFieldNames.FIELD_INTEREST_RATE.equals(fieldName) && value instanceof Number numValue) {
            return payloadUtilities.normalizeInterestRateToFraction(numValue.doubleValue());
        }

        if (ModelPayloadFieldNames.FIELD_IS_REVOLVING.equals(fieldName) && value instanceof Boolean boolValue) {
            return payloadUtilities.toModelBoolean(boolValue);
        }

        if (value instanceof String stringValue && isEnumField(fieldName)) {
            return payloadUtilities.normalizeEnumForField(fieldName, stringValue);
        }
        if (value instanceof Number numValue) {
            return numValue.doubleValue();
        }

        return value;
    }

    /**
     * Method that checks if a field is an enum field.
     * 
     * @param fieldName field name
     * @return true if the field is an enum field
     */
    private boolean isEnumField(final String fieldName) {
        return ModelPayloadFieldNames.FIELD_GENDER.equals(fieldName)
                || ModelPayloadFieldNames.FIELD_MARITAL_STATUS.equals(fieldName)
                || ModelPayloadFieldNames.FIELD_EDUCATION.equals(fieldName)
                || ModelPayloadFieldNames.FIELD_EMPLOYMENT_STATUS.equals(fieldName)
                || ModelPayloadFieldNames.FIELD_OCCUPATION_SECTOR.equals(fieldName)
                || ModelPayloadFieldNames.FIELD_HOME_OWNERSHIP.equals(fieldName)
                || ModelPayloadFieldNames.FIELD_LOAN_TYPE.equals(fieldName)
                || ModelPayloadFieldNames.FIELD_PURPOSE.equals(fieldName)
                || ModelPayloadFieldNames.FIELD_INCOME_TYPE.equals(fieldName);
    }

    /**
     * Synchronizes dependent fields like incomeType based on employmentStatus.
     * 
     * @param normalized The normalized variables map.
     */
    private void synchronizeDependentFields(final Map<String, Object> normalized) {
        final Object employment = normalized.get(ModelPayloadFieldNames.FIELD_EMPLOYMENT_STATUS);
        if (employment instanceof String employmentStr) {
            final String derivedIncomeType = SimulationConstants.EMPLOYMENT_TO_INCOME_TYPE.get(employmentStr);
            if (derivedIncomeType != null) {
                normalized.put(ModelPayloadFieldNames.FIELD_INCOME_TYPE, derivedIncomeType);
            }
        }
    }
}
