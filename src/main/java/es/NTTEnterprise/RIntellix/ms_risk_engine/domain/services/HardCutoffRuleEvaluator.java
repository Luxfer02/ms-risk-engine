package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.HardCutoffRejection;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskFeature;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.constants.RiskCalculationDefaults;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.utils.MapUtilities;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums.RequestType;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadFieldNames;
import lombok.extern.slf4j.Slf4j;

/**
 * Domain service that evaluates hard-cutoff financial ratio rules (DTI / LTV /
 * LTI)
 * against mapped model payloads.
 *
 * This evaluator runs purely within the domain layer and has no dependency on
 * application layer DTOs.
 *
 * @author Lucía Fernández Mancebo
 * @date 28/06/2026
 */
@Slf4j
public class HardCutoffRuleEvaluator {

    /**
     * Explainability message template for hard-cutoff rules.
     * {0}: feature name (e.g., DTI)
     * {1}: feature value
     */
    private static final String EXPLAINABILITY_MESSAGE = "Hard-cutoff rule: {} with value {} exceeds the threshold";

    /**
     * Evaluates hard-cutoff financial-ratio rules in priority order:
     * <ol>
     * <li>DTI &gt; 50 % — applicable to ALL request types</li>
     * <li>LTV &gt; 80 % — mortgages (HIPOTECA) only</li>
     * <li>LTI &gt; 40 % — credit cards (TARJETA_CREDITO) only</li>
     * </ol>
     *
     * @param modelPayload the mapped model request payload.
     * @param requestType  the normalized request type string.
     * @param requestId    the request identifier (for logging).
     * @return an Optional containing the HardCutoffRejection if a rule is
     *         triggered,
     *         or Optional.empty() if all checks pass.
     */
    public Optional<HardCutoffRejection> evaluateRules(
            final Map<String, Object> modelPayload,
            final String requestType,
            final String requestId) {

        RequestType parsedRequestType = null;
        try {
            parsedRequestType = RequestType.fromValue(requestType);
        } catch (IllegalArgumentException e) {
            // Ignored, parsedRequestType remains null
        }

        // Rule 1 — DTI > 50 % (all types)
        if (modelPayload.containsKey(ModelPayloadFieldNames.FIELD_DTI)) {
            final double dti = MapUtilities.getDouble(modelPayload, ModelPayloadFieldNames.FIELD_DTI, 0.0);
            if (dti < 0) {
                throw new IllegalArgumentException("DTI cannot be negative");
            }
            if (dti > RiskCalculationDefaults.HARD_CUTOFF_DTI_THRESHOLD) {
                log.warn(LogMessage.SCORING_HARD_CUTOFF_DTI, dti, requestId);
                return Optional.of(buildRejection(modelPayload, parsedRequestType, ModelPayloadFieldNames.FIELD_DTI, dti));
            }
        }

        // Rule 2 — LTV > 80 % (mortgages only)
        if (RequestType.HIPOTECA == parsedRequestType && modelPayload.containsKey(ModelPayloadFieldNames.FIELD_LTV)) {
            final double ltv = MapUtilities.getDouble(modelPayload, ModelPayloadFieldNames.FIELD_LTV, 0.0);
            if (ltv < 0) {
                throw new IllegalArgumentException("LTV cannot be negative");
            }
            if (ltv > RiskCalculationDefaults.HARD_CUTOFF_LTV_THRESHOLD) {
                log.warn(LogMessage.SCORING_HARD_CUTOFF_LTV, ltv, requestId);
                return Optional.of(buildRejection(modelPayload, parsedRequestType, ModelPayloadFieldNames.FIELD_LTV, ltv));
            }
        }

        // Rule 3 — LTI > 40 % (credit cards only)
        if (RequestType.TARJETA_CREDITO == parsedRequestType && modelPayload.containsKey(ModelPayloadFieldNames.FIELD_LTI)) {
            final double lti = MapUtilities.getDouble(modelPayload, ModelPayloadFieldNames.FIELD_LTI, 0.0);
            if (lti < 0) {
                throw new IllegalArgumentException("LTI cannot be negative");
            }
            if (lti > RiskCalculationDefaults.HARD_CUTOFF_LTI_THRESHOLD) {
                log.warn(LogMessage.SCORING_HARD_CUTOFF_LTI, lti, requestId);
                return Optional
                        .of(buildRejection(modelPayload, parsedRequestType, ModelPayloadFieldNames.FIELD_LTI, lti));
            }
        }

        return Optional.empty();
    }

    /**
     * Auxiliar method that builds a HardCutoffRejection object.
     * 
     * @param modelPayload the mapped model request payload.
     * @param requestType  the normalized request type string.
     * @param featureName  the name of the triggering ratio field.
     * @param featureValue the value of the triggering ratio.
     * @return a HardCutoffRejection object.
     */
    private HardCutoffRejection buildRejection(
            final Map<String, Object> modelPayload,
            final RequestType requestType,
            final String featureName,
            final Double featureValue) {

        final RiskFeature topFeature = new RiskFeature(
                featureName,
                String.valueOf(featureValue),
                1.0, // maximum SHAP contribution
                String.format(EXPLAINABILITY_MESSAGE, featureName.toUpperCase(), featureValue));

        return new HardCutoffRejection(featureName, featureValue, List.of(topFeature));
    }
}
