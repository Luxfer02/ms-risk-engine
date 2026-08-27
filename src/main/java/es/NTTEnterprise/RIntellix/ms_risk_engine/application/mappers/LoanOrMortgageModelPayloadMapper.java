package es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringGenerationRequest;

import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadConstants;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadFieldNames;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadUtilities;

/**
 * Mapper for transforming loan and mortgage scoring requests into
 * the payload contract required by the AI model.
 *
 * Normalizes enum values while preserving English field naming throughout.
 *
 * @author Lucía Fernández Mancebo
 * @date 25/04/2026
 */

public class LoanOrMortgageModelPayloadMapper {

        private final ModelPayloadUtilities payloadUtilities;

        public LoanOrMortgageModelPayloadMapper(final ModelPayloadUtilities payloadUtilities) {
                this.payloadUtilities = Objects.requireNonNull(payloadUtilities,
                                LogMessage.MODEL_PAYLOAD_UTILITIES_CANNOT_BE_NULL);
        }

        /**
         * Maps loan or mortgage generation request to model payload.
         *
         * @param request       the source scoring generation request.
         * @param calculatedDti the pre-calculated DTI value.
         * @return the model payload with English field names and normalized values.
         */
        public Map<String, Object> toModelPayload(final ScoringGenerationRequest request, final double calculatedDti) {
                final Map<String, Object> modelPayload = new LinkedHashMap<>();
                final String loanType = request.getLoanType();
                modelPayload.put(ModelPayloadFieldNames.FIELD_AGE, request.getAge());
                modelPayload.put(ModelPayloadFieldNames.FIELD_GENDER,
                                payloadUtilities.normalizeEnumForField(ModelPayloadFieldNames.FIELD_GENDER,
                                                request.getGender()));
                modelPayload.put(ModelPayloadFieldNames.FIELD_MARITAL_STATUS,
                                payloadUtilities.normalizeEnumForField(ModelPayloadFieldNames.FIELD_MARITAL_STATUS,
                                                request.getMaritalStatus()));
                // Education enums use spaces: "Sin Estudios", "Formacion Profesional"
                modelPayload.put(ModelPayloadFieldNames.FIELD_EDUCATION,
                                payloadUtilities.normalizeEnumForField(ModelPayloadFieldNames.FIELD_EDUCATION,
                                                request.getEducation()));
                modelPayload.put(ModelPayloadFieldNames.FIELD_EMPLOYMENT_STATUS,
                                payloadUtilities.normalizeEnumForField(ModelPayloadFieldNames.FIELD_EMPLOYMENT_STATUS,
                                                request.getEmploymentStatus()));
                // Sector trabajo enums use spaces: "Sector Publico"
                modelPayload.put(ModelPayloadFieldNames.FIELD_OCCUPATION_SECTOR,
                                payloadUtilities.normalizeEnumForField(ModelPayloadFieldNames.FIELD_OCCUPATION_SECTOR,
                                                request.getOccupationSector()));
                modelPayload.put(ModelPayloadFieldNames.FIELD_DEPENDENTS, request.getDependents());
                modelPayload.put(ModelPayloadFieldNames.FIELD_HOME_OWNERSHIP,
                                payloadUtilities.normalizeEnumForField(ModelPayloadFieldNames.FIELD_HOME_OWNERSHIP,
                                                request.getHomeOwnership()));
                modelPayload.put(ModelPayloadFieldNames.FIELD_HAS_MORTGAGE,
                                Boolean.TRUE.equals(request.getHasMortgage()) ? ModelPayloadConstants.BOOLEAN_VALUE_YES
                                                : ModelPayloadConstants.BOOLEAN_VALUE_NO);
                modelPayload.put(ModelPayloadFieldNames.FIELD_ANNUAL_INCOME, request.getAnnualIncome());
                modelPayload.put(ModelPayloadFieldNames.FIELD_LOAN_TYPE,
                                payloadUtilities.normalizeEnumForField(ModelPayloadFieldNames.FIELD_LOAN_TYPE,
                                                loanType));
                modelPayload.put(ModelPayloadFieldNames.FIELD_PURPOSE,
                                payloadUtilities.normalizeEnumForField(ModelPayloadFieldNames.FIELD_PURPOSE,
                                                request.getPurpose()));
                modelPayload.put(ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, request.getLoanAmount());
                modelPayload.put(ModelPayloadFieldNames.FIELD_TERM_MONTHS, request.getTermMonths());
                modelPayload.put(ModelPayloadFieldNames.FIELD_INTEREST_RATE,
                                payloadUtilities.normalizeInterestRateToFraction(request.getInterestRate()));
                // Default LTV to 0.0 if not provided
                modelPayload.put(ModelPayloadFieldNames.FIELD_LTV,
                                Objects.requireNonNullElse(request.getLtv(), ModelPayloadConstants.DEFAULT_LTV));
                modelPayload.put(ModelPayloadFieldNames.FIELD_EXISTING_OBLIGATIONS, request.getExistingObligations());
                modelPayload.put(ModelPayloadFieldNames.FIELD_DTI, calculatedDti);
                modelPayload.put(ModelPayloadFieldNames.FIELD_PREVIOUS_LOANS_COUNT, request.getPreviousLoansCount());
                modelPayload.put(ModelPayloadFieldNames.FIELD_PREVIOUS_DEFAULTS_COUNT,
                                request.getPreviousDefaultsCount());
                return modelPayload;
        }

}
