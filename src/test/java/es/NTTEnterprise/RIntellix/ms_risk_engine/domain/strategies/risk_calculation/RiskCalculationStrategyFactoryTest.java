package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RiskCalculationStrategyFactoryTest {

    @Test
    @DisplayName("Should create corresponding strategy when supported")
    void createStrategy_success() {
        RiskCalculationStrategy mockStrategy = mock(RiskCalculationStrategy.class);
        when(mockStrategy.supports("PRESTAMO", false)).thenReturn(true);

        RiskCalculationStrategy result = RiskCalculationStrategyFactory.createStrategy(
                "PRESTAMO", false, List.of(mockStrategy));

        assertEquals(mockStrategy, result);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when no strategy matches")
    void createStrategy_notFound() {
        RiskCalculationStrategy mockStrategy = mock(RiskCalculationStrategy.class);
        when(mockStrategy.supports(anyString(), anyBoolean())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> 
                RiskCalculationStrategyFactory.createStrategy("UNKNOWN", false, List.of(mockStrategy)));
    }

    @Test
    @DisplayName("Should throw NullPointerException when inputs are null")
    void createStrategy_nullInputs() {
        assertThrows(NullPointerException.class, () -> 
                RiskCalculationStrategyFactory.createStrategy(null, false, List.of()));

        assertThrows(NullPointerException.class, () -> 
                RiskCalculationStrategyFactory.createStrategy("PRESTAMO", false, null));
    }
}
