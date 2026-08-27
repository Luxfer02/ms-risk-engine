package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics.FinancialMetricsStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;

/**
 * Unit tests for {@link FinancialMetricsCalculationService}.
 * Covers delegation to strategy for both methods.
 * @date 27/08/2026
 */
@DisplayName("FinancialMetricsCalculationService Tests")
@ExtendWith(MockitoExtension.class)
class FinancialMetricsCalculationServiceTest {

    @Mock
    private FinancialMetricsStrategy strategy;

    private FinancialMetricsCalculationService service;

    @BeforeEach
    void setUp() {
        // Mock the strategy support so the factory will pick it up
        lenient().when(strategy.supports(anyString(), anyBoolean())).thenReturn(true);
        service = new FinancialMetricsCalculationService(List.of(strategy));
    }

    @Test
    @DisplayName("calculateFinancialMetrics should delegate to strategy")
    void calculateFinancialMetrics_shouldDelegateToStrategy() {
        FinancialMetrics expectedMetrics = new FinancialMetrics();
        expectedMetrics.setMonthlyPayment(100.0);
        
        when(strategy.calculateFinancialMetrics(10000.0, 5.0, 50000.0, 200.0, 36))
                .thenReturn(expectedMetrics);

        FinancialMetrics result = service.calculateFinancialMetrics("PRESTAMO", false, 10000.0, 5.0, 36, 50000.0, 200.0);

        assertEquals(expectedMetrics, result);
        verify(strategy).calculateFinancialMetrics(10000.0, 5.0, 50000.0, 200.0, 36);
    }

    @Test
    @DisplayName("calculateFinancialMetricsWithoutExistingObligations should delegate with 0 obligations")
    void calculateFinancialMetricsWithoutExistingObligations_shouldDelegateWithZero() {
        FinancialMetrics expectedMetrics = new FinancialMetrics();
        expectedMetrics.setMonthlyPayment(100.0);
        
        when(strategy.calculateFinancialMetrics(10000.0, 5.0, 50000.0, 0.0, 36))
                .thenReturn(expectedMetrics);

        FinancialMetrics result = service.calculateFinancialMetricsWithoutExistingObligations("PRESTAMO", false, 10000.0, 5.0, 36, 50000.0);

        assertEquals(expectedMetrics, result);
        verify(strategy).calculateFinancialMetrics(10000.0, 5.0, 50000.0, 0.0, 36);
    }

    @Test
    @DisplayName("Constructor should throw NPE when strategies list is null")
    void constructor_shouldThrow_whenStrategiesNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, 
            () -> new FinancialMetricsCalculationService(null));
        assertEquals(LogMessage.FINANCIAL_METRICS_STRATEGIES_CANNOT_BE_NULL, exception.getMessage());
    }
}
