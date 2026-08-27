package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FinancialMetricsStrategyFactoryTest {

    @Test
    @DisplayName("Should create corresponding strategy when supported")
    void createStrategy_success() {
        FinancialMetricsStrategy mockStrategy = mock(FinancialMetricsStrategy.class);
        when(mockStrategy.supports("PRESTAMO", false)).thenReturn(true);

        FinancialMetricsStrategy result = FinancialMetricsStrategyFactory.createStrategy(
                "PRESTAMO", false, List.of(mockStrategy));

        assertEquals(mockStrategy, result);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when no strategy matches")
    void createStrategy_notFound() {
        FinancialMetricsStrategy mockStrategy = mock(FinancialMetricsStrategy.class);
        when(mockStrategy.supports(anyString(), anyBoolean())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> 
                FinancialMetricsStrategyFactory.createStrategy("UNKNOWN", false, List.of(mockStrategy)));
    }

    @Test
    @DisplayName("Should throw NullPointerException when inputs are null")
    void createStrategy_nullInputs() {
        assertThrows(NullPointerException.class, () -> 
                FinancialMetricsStrategyFactory.createStrategy(null, false, List.of()));

        assertThrows(NullPointerException.class, () -> 
                FinancialMetricsStrategyFactory.createStrategy("PRESTAMO", false, null));
    }
}
