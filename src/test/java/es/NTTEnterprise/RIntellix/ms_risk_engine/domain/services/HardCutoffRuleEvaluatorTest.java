package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.constants.RiskCalculationDefaults;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.HardCutoffRejection;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadFieldNames;

/**
 * Unit tests for {@link HardCutoffRuleEvaluator}.
 * Covers DTI/LTV/LTI rules, priority order, request type filtering, and edge cases.
 * @date 27/08/2026
 */
@DisplayName("HardCutoffRuleEvaluator Tests")
class HardCutoffRuleEvaluatorTest {

    private HardCutoffRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new HardCutoffRuleEvaluator();
    }

    // ========== Rule 1: DTI (all types) ==========

    @Test
    @DisplayName("Should reject when DTI exceeds threshold for PRESTAMO")
    void shouldRejectWhenDtiExceedsThreshold_prestamo() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, 0.60); // > 0.55

        Optional<HardCutoffRejection> result = evaluator.evaluateRules(payload, "PRESTAMO", "REQ-1");

        assertTrue(result.isPresent(), "Should reject when DTI > threshold");
        assertEquals(ModelPayloadFieldNames.FIELD_DTI, result.get().getFeatureName());
    }

    @Test
    @DisplayName("Should reject when DTI exceeds threshold for HIPOTECA")
    void shouldRejectWhenDtiExceedsThreshold_hipoteca() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, 0.60);

        Optional<HardCutoffRejection> result = evaluator.evaluateRules(payload, "HIPOTECA", "REQ-1");

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should reject when DTI exceeds threshold for TARJETA_CREDITO")
    void shouldRejectWhenDtiExceedsThreshold_tarjetaCredito() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, 0.60);

        Optional<HardCutoffRejection> result = evaluator.evaluateRules(payload, "TARJETA_CREDITO", "REQ-1");

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should throw exception when DTI is negative")
    void shouldThrowExceptionWhenDtiIsNegative() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, -0.01);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            evaluator.evaluateRules(payload, "PRESTAMO", "REQ-1")
        );
        assertEquals("DTI cannot be negative", ex.getMessage());
    }

    @Test
    @DisplayName("Should not reject when DTI is at the threshold limit")
    void shouldNotRejectWhenDtiAtThreshold() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, RiskCalculationDefaults.HARD_CUTOFF_DTI_THRESHOLD);

        Optional<HardCutoffRejection> result = evaluator.evaluateRules(payload, "PRESTAMO", "REQ-1");

        assertTrue(result.isEmpty());
    }

    // ========== Rule 2: LTV (mortgage only) ==========

    @Test
    @DisplayName("Should reject when LTV exceeds threshold for HIPOTECA")
    void shouldRejectWhenLtvExceedsThreshold_onlyMortgage() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, 0.30); // pass DTI
        payload.put(ModelPayloadFieldNames.FIELD_LTV, 0.85); // > 0.80

        Optional<HardCutoffRejection> result = evaluator.evaluateRules(payload, "HIPOTECA", "REQ-1");

        assertTrue(result.isPresent());
        assertEquals(ModelPayloadFieldNames.FIELD_LTV, result.get().getFeatureName());
    }

    @Test
    @DisplayName("Should NOT check LTV when request type is PRESTAMO")
    void shouldNotCheckLtv_whenNotMortgage() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, 0.30);
        payload.put(ModelPayloadFieldNames.FIELD_LTV, 0.95); // would fail if checked

        Optional<HardCutoffRejection> result = evaluator.evaluateRules(payload, "PRESTAMO", "REQ-1");

        assertTrue(result.isEmpty(), "LTV rule should not apply to PRESTAMO");
    }

    @Test
    @DisplayName("Should throw exception when LTV is negative")
    void shouldThrowExceptionWhenLtvIsNegative() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, 0.30);
        payload.put(ModelPayloadFieldNames.FIELD_LTV, -0.05);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            evaluator.evaluateRules(payload, "HIPOTECA", "REQ-1")
        );
        assertEquals("LTV cannot be negative", ex.getMessage());
    }

    // ========== Rule 3: LTI (credit card only) ==========

    @Test
    @DisplayName("Should reject when LTI exceeds threshold for TARJETA_CREDITO")
    void shouldRejectWhenLtiExceedsThreshold_onlyCreditCard() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, 0.30); // pass DTI
        payload.put(ModelPayloadFieldNames.FIELD_LTI, 0.45); // > 0.40

        Optional<HardCutoffRejection> result = evaluator.evaluateRules(payload, "TARJETA_CREDITO", "REQ-1");

        assertTrue(result.isPresent());
        assertEquals(ModelPayloadFieldNames.FIELD_LTI, result.get().getFeatureName());
    }

    @Test
    @DisplayName("Should NOT check LTI when request type is PRESTAMO")
    void shouldNotCheckLti_whenNotCreditCard() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, 0.30);
        payload.put(ModelPayloadFieldNames.FIELD_LTI, 0.95); // would fail if checked

        Optional<HardCutoffRejection> result = evaluator.evaluateRules(payload, "PRESTAMO", "REQ-1");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when LTI is negative")
    void shouldThrowExceptionWhenLtiIsNegative() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, 0.30);
        payload.put(ModelPayloadFieldNames.FIELD_LTI, -0.10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            evaluator.evaluateRules(payload, "TARJETA_CREDITO", "REQ-1")
        );
        assertEquals("LTI cannot be negative", ex.getMessage());
    }

    // ========== All rules pass ==========

    @Test
    @DisplayName("Should return empty when all rules pass")
    void shouldReturnEmpty_whenAllRulesPass() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, 0.20);
        payload.put(ModelPayloadFieldNames.FIELD_LTV, 0.50);

        Optional<HardCutoffRejection> result = evaluator.evaluateRules(payload, "HIPOTECA", "REQ-1");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty for credit card when all rules pass")
    void shouldReturnEmpty_forCreditCard_whenAllRulesPass() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, 0.20);
        payload.put(ModelPayloadFieldNames.FIELD_LTI, 0.20);

        Optional<HardCutoffRejection> result = evaluator.evaluateRules(payload, "TARJETA_CREDITO", "REQ-1");

        assertTrue(result.isEmpty());
    }

    // ========== Edge cases ==========

    @Test
    @DisplayName("Should handle invalid request type gracefully (only DTI checked)")
    void shouldHandleInvalidRequestType_gracefully() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, 0.20);

        Optional<HardCutoffRejection> result = evaluator.evaluateRules(payload, "INVALID_TYPE", "REQ-1");

        assertTrue(result.isEmpty(), "Invalid type should not trigger any specific rule beyond DTI");
    }

    @Test
    @DisplayName("DTI should be checked before LTV (priority order)")
    void shouldApplyDtiBeforeLtv_priorityOrder() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_DTI, 0.60); // fails DTI
        payload.put(ModelPayloadFieldNames.FIELD_LTV, 0.90); // would also fail LTV

        Optional<HardCutoffRejection> result = evaluator.evaluateRules(payload, "HIPOTECA", "REQ-1");

        assertTrue(result.isPresent());
        assertEquals(ModelPayloadFieldNames.FIELD_DTI, result.get().getFeatureName(),
                "DTI should be checked first (higher priority)");
    }

    @Test
    @DisplayName("Should handle missing DTI field with default value -1.0")
    void shouldHandleMissingDtiField_withDefaultValue() {
        Map<String, Object> payload = new HashMap<>(); // no DTI field

        Optional<HardCutoffRejection> result = evaluator.evaluateRules(payload, "PRESTAMO", "REQ-1");

        assertTrue(result.isEmpty(), "Default -1.0 should not trigger DTI rule");
    }
}
