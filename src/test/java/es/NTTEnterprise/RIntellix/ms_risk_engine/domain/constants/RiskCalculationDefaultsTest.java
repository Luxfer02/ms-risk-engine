package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.constants;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RiskCalculationDefaults}.
 * Covers clampRatio, validateRequestAmount, and non-instantiability.
 * @date 27/08/2026
 */
@DisplayName("RiskCalculationDefaults Tests")
class RiskCalculationDefaultsTest {

    // ========== clampRatio ==========

    @Test
    @DisplayName("clampRatio should return 0.0 for negative values")
    void clampRatio_negative() {
        assertEquals(0.0, RiskCalculationDefaults.clampRatio(-0.5));
    }

    @Test
    @DisplayName("clampRatio should return 1.0 for values above 1.0")
    void clampRatio_aboveOne() {
        assertEquals(1.0, RiskCalculationDefaults.clampRatio(1.5));
    }

    @Test
    @DisplayName("clampRatio should return original value when within range")
    void clampRatio_withinRange() {
        assertEquals(0.5, RiskCalculationDefaults.clampRatio(0.5));
    }

    @Test
    @DisplayName("clampRatio should handle zero boundary")
    void clampRatio_zero() {
        assertEquals(0.0, RiskCalculationDefaults.clampRatio(0.0));
    }

    @Test
    @DisplayName("clampRatio should handle one boundary")
    void clampRatio_one() {
        assertEquals(1.0, RiskCalculationDefaults.clampRatio(1.0));
    }

    // ========== validateRequestAmount ==========

    @Test
    @DisplayName("validateRequestAmount should return value when positive")
    void validateRequestAmount_positive() {
        assertEquals(10000.0, RiskCalculationDefaults.validateRequestAmount(10000.0));
    }

    @Test
    @DisplayName("validateRequestAmount should throw when null")
    void validateRequestAmount_null() {
        assertThrows(IllegalArgumentException.class,
                () -> RiskCalculationDefaults.validateRequestAmount(null));
    }

    @Test
    @DisplayName("validateRequestAmount should throw when zero")
    void validateRequestAmount_zero() {
        assertThrows(IllegalArgumentException.class,
                () -> RiskCalculationDefaults.validateRequestAmount(0.0));
    }

    @Test
    @DisplayName("validateRequestAmount should throw when negative")
    void validateRequestAmount_negative() {
        assertThrows(IllegalArgumentException.class,
                () -> RiskCalculationDefaults.validateRequestAmount(-100.0));
    }

    // ========== Constants integrity ==========

    @Test
    @DisplayName("PD thresholds should be in ascending order")
    void pdThresholds_shouldBeAscending() {
        assertTrue(RiskCalculationDefaults.PD_THRESHOLD_GRADE_A < RiskCalculationDefaults.PD_THRESHOLD_GRADE_B);
        assertTrue(RiskCalculationDefaults.PD_THRESHOLD_GRADE_B < RiskCalculationDefaults.PD_THRESHOLD_GRADE_C);
        assertTrue(RiskCalculationDefaults.PD_THRESHOLD_GRADE_C < RiskCalculationDefaults.PD_THRESHOLD_GRADE_D);
        assertTrue(RiskCalculationDefaults.PD_THRESHOLD_GRADE_D < RiskCalculationDefaults.PD_THRESHOLD_GRADE_E);
        assertTrue(RiskCalculationDefaults.PD_THRESHOLD_GRADE_E < RiskCalculationDefaults.PD_THRESHOLD_GRADE_F);
    }

    @Test
    @DisplayName("Hard cutoff thresholds should be between 0 and 1")
    void hardCutoffThresholds_shouldBeInRange() {
        assertTrue(RiskCalculationDefaults.HARD_CUTOFF_DTI_THRESHOLD > 0
                && RiskCalculationDefaults.HARD_CUTOFF_DTI_THRESHOLD <= 1.0);
        assertTrue(RiskCalculationDefaults.HARD_CUTOFF_LTV_THRESHOLD > 0
                && RiskCalculationDefaults.HARD_CUTOFF_LTV_THRESHOLD <= 1.0);
        assertTrue(RiskCalculationDefaults.HARD_CUTOFF_LTI_THRESHOLD > 0
                && RiskCalculationDefaults.HARD_CUTOFF_LTI_THRESHOLD <= 1.0);
    }

    // ========== Non-instantiability ==========

    @Test
    @DisplayName("Should not be instantiable - utility class")
    void shouldNotBeInstantiable() throws Exception {
        var constructor = RiskCalculationDefaults.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
    }
}
