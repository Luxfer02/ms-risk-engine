package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.indicators;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.DtiCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadFieldNames;

@ExtendWith(MockitoExtension.class)
class LoanRiskIndicatorStrategyTest {

    @Mock
    private DtiCalculationService dtiCalculationService;

    private LoanRiskIndicatorStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new LoanRiskIndicatorStrategy(dtiCalculationService);
    }

    @Test
    @DisplayName("Should support PRESTAMO request type")
    void supports_true() {
        assertTrue(strategy.supports("PRESTAMO"));
    }

    @Test
    @DisplayName("Should not support other request types or null")
    void supports_false() {
        assertFalse(strategy.supports("HIPOTECA"));
        assertFalse(strategy.supports(null));
        assertFalse(strategy.supports("UNKNOWN"));
    }

    @Test
    @DisplayName("Should recalculate indicators successfully")
    void recalculateIndicators_success() throws Exception {
        Map<String, Object> merged = new HashMap<>();
        merged.put(ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, 10000.0);
        merged.put(ModelPayloadFieldNames.FIELD_TERM_MONTHS, 12);
        merged.put(ModelPayloadFieldNames.FIELD_INTEREST_RATE, 5.0);
        merged.put(ModelPayloadFieldNames.FIELD_ANNUAL_INCOME, 60000.0);
        merged.put(ModelPayloadFieldNames.FIELD_EXISTING_OBLIGATIONS, 1200.0); // 100/mo

        when(dtiCalculationService.calculateDtiWithExistingObligations(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.2);

        strategy.recalculateIndicators(merged, null);

        assertTrue(merged.containsKey(ModelPayloadFieldNames.FIELD_DTI));
        verify(dtiCalculationService).calculateDtiWithExistingObligations(anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Should handle missing values gracefully")
    void recalculateIndicators_missingValues() {
        Map<String, Object> merged = new HashMap<>();
        when(dtiCalculationService.calculateDtiWithExistingObligations(anyDouble(), anyDouble(), anyDouble()))
            .thenThrow(new IllegalArgumentException("Annual income cannot be zero"));
        
        // This will trigger exception inside calculateAndSetDti but we catch it
        strategy.recalculateIndicators(merged, null);
        
        assertFalse(merged.containsKey(ModelPayloadFieldNames.FIELD_DTI));
    }
}
