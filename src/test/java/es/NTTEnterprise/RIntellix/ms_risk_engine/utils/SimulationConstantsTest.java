package es.NTTEnterprise.RIntellix.ms_risk_engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SimulationConstants}.
 * Covers getSafe utility method and non-instantiability.
 * @date 27/08/2026
 */
@DisplayName("SimulationConstants Tests")
class SimulationConstantsTest {

    @Test
    @DisplayName("getSafe should return value when not null")
    void getSafe_shouldReturnValue_whenNotNull() {
        assertEquals(42.0, SimulationConstants.getSafe(42.0));
    }

    @Test
    @DisplayName("getSafe should return ZERO_VALUE when null")
    void getSafe_shouldReturnZero_whenNull() {
        assertEquals(SimulationConstants.ZERO_VALUE, SimulationConstants.getSafe(null));
    }

    @Test
    @DisplayName("getSafe should return zero for zero input")
    void getSafe_shouldReturnZero_forZeroInput() {
        assertEquals(0.0, SimulationConstants.getSafe(0.0));
    }

    @Test
    @DisplayName("Constants should have expected values")
    void constants_shouldHaveExpectedValues() {
        assertEquals(12.0, SimulationConstants.MONTHS_PER_YEAR);
        assertEquals(100.0, SimulationConstants.PERCENTAGE_DIVISOR);
        assertEquals(1, SimulationConstants.MIN_TERM_MONTHS);
        assertEquals(" -> ", SimulationConstants.RISK_GRADE_ARROW);
        assertEquals("UNKNOWN", SimulationConstants.UNKNOWN_RISK_GRADE);
    }

    @Test
    @DisplayName("Should not be instantiable - utility class")
    void shouldNotBeInstantiable() throws Exception {
        var constructor = SimulationConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
    }
}
