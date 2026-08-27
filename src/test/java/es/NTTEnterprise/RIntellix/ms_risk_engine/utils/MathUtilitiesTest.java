package es.NTTEnterprise.RIntellix.ms_risk_engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MathUtilities}.
 * Covers rounding, delta calculation, NaN/Infinity handling, and non-instantiability.
 * @date 27/08/2026
 */
@DisplayName("MathUtilities Tests")
class MathUtilitiesTest {

    // ========== roundFinal ==========

    @Test
    @DisplayName("roundFinal should round to 2 decimal places")
    void roundFinal_shouldRoundTo2Decimals() {
        assertEquals(3.15, MathUtilities.roundFinal(3.1459));
    }

    @Test
    @DisplayName("roundFinal should handle HALF_UP rounding mode")
    void roundFinal_shouldHandleHalfUp() {
        assertEquals(2.35, MathUtilities.roundFinal(2.345));
    }

    @Test
    @DisplayName("roundFinal should return NaN when input is NaN")
    void roundFinal_shouldReturnNaN_whenNaN() {
        assertTrue(Double.isNaN(MathUtilities.roundFinal(Double.NaN)));
    }

    @Test
    @DisplayName("roundFinal should return Infinity when input is Infinity")
    void roundFinal_shouldReturnInfinity_whenInfinite() {
        assertTrue(Double.isInfinite(MathUtilities.roundFinal(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isInfinite(MathUtilities.roundFinal(Double.NEGATIVE_INFINITY)));
    }

    @Test
    @DisplayName("roundFinal should handle zero")
    void roundFinal_shouldHandleZero() {
        assertEquals(0.0, MathUtilities.roundFinal(0.0));
    }

    @Test
    @DisplayName("roundFinal should handle negative values")
    void roundFinal_shouldHandleNegativeValues() {
        assertEquals(-3.15, MathUtilities.roundFinal(-3.1459));
    }

    // ========== roundIntermediate ==========

    @Test
    @DisplayName("roundIntermediate should round to 4 decimal places")
    void roundIntermediate_shouldRoundTo4Decimals() {
        assertEquals(3.1416, MathUtilities.roundIntermediate(3.14159));
    }

    @Test
    @DisplayName("roundIntermediate should handle NaN")
    void roundIntermediate_shouldHandleNaN() {
        assertTrue(Double.isNaN(MathUtilities.roundIntermediate(Double.NaN)));
    }

    @Test
    @DisplayName("roundIntermediate should handle Infinity")
    void roundIntermediate_shouldHandleInfinity() {
        assertTrue(Double.isInfinite(MathUtilities.roundIntermediate(Double.POSITIVE_INFINITY)));
    }

    // ========== calculateDelta ==========

    @Test
    @DisplayName("calculateDelta should return positive delta when sim > base")
    void calculateDelta_positive() {
        assertEquals(2.0, MathUtilities.calculateDelta(10.0, 8.0));
    }

    @Test
    @DisplayName("calculateDelta should return negative delta when sim < base")
    void calculateDelta_negative() {
        assertEquals(-3.0, MathUtilities.calculateDelta(5.0, 8.0));
    }

    @Test
    @DisplayName("calculateDelta should return zero when sim equals base")
    void calculateDelta_zero() {
        assertEquals(0.0, MathUtilities.calculateDelta(5.0, 5.0));
    }

    @Test
    @DisplayName("calculateDelta should round result to 2 decimals")
    void calculateDelta_shouldRound() {
        double result = MathUtilities.calculateDelta(10.123, 8.456);
        assertEquals(1.67, result, 0.001);
    }

    // ========== Non-instantiability ==========

    @Test
    @DisplayName("Should not be instantiable - utility class")
    void shouldNotBeInstantiable() throws Exception {
        var constructor = MathUtilities.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
    }
}
