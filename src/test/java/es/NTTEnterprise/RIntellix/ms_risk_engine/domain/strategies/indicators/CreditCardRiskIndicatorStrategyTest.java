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
class CreditCardRiskIndicatorStrategyTest {

    @Mock
    private DtiCalculationService dtiCalculationService;

    private CreditCardRiskIndicatorStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new CreditCardRiskIndicatorStrategy(dtiCalculationService);
    }

    @Test
    @DisplayName("Should support TARJETA_CREDITO request type")
    void supports_true() {
        assertTrue(strategy.supports("TARJETA_CREDITO"));
    }

    @Test
    @DisplayName("Should not support other request types or null")
    void supports_false() {
        assertFalse(strategy.supports("PRESTAMO"));
        assertFalse(strategy.supports(null));
        assertFalse(strategy.supports("UNKNOWN"));
    }

    @Test
    @DisplayName("Should recalculate indicators successfully")
    void recalculateIndicators_success() throws Exception {
        Map<String, Object> merged = new HashMap<>();
        merged.put(ModelPayloadFieldNames.FIELD_CREDIT_LIMIT, 5000.0);
        merged.put(ModelPayloadFieldNames.FIELD_IS_REVOLVING, true);
        merged.put(ModelPayloadFieldNames.FIELD_ANNUAL_INCOME, 48000.0);
        merged.put(ModelPayloadFieldNames.FIELD_EXISTING_OBLIGATIONS, 1200.0);

        when(dtiCalculationService.calculateDtiWithExistingObligations(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.05);

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
        
        strategy.recalculateIndicators(merged, null);
        
        assertFalse(merged.containsKey(ModelPayloadFieldNames.FIELD_DTI)); // Annual income is 0 which can throw exception
    }
}
