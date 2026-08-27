package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.indicators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
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
class MortgageRiskIndicatorStrategyTest {

    @Mock
    private DtiCalculationService dtiCalculationService;

    private MortgageRiskIndicatorStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new MortgageRiskIndicatorStrategy(dtiCalculationService);
    }

    @Test
    @DisplayName("Should support HIPOTECA request type")
    void supports_true() {
        assertTrue(strategy.supports("HIPOTECA"));
    }

    @Test
    @DisplayName("Should not support other request types or null")
    void supports_false() {
        assertFalse(strategy.supports("PRESTAMO"));
        assertFalse(strategy.supports(null));
        assertFalse(strategy.supports("UNKNOWN"));
    }

    @Test
    @DisplayName("Should recalculate indicators successfully with property value")
    void recalculateIndicators_success() throws Exception {
        Map<String, Object> merged = new HashMap<>();
        merged.put(ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, 100000.0);
        merged.put(ModelPayloadFieldNames.FIELD_TERM_MONTHS, 120);
        merged.put(ModelPayloadFieldNames.FIELD_INTEREST_RATE, 3.5);
        merged.put(ModelPayloadFieldNames.FIELD_ANNUAL_INCOME, 80000.0);
        merged.put(ModelPayloadFieldNames.FIELD_PROPERTY_VALUE, 125000.0);

        when(dtiCalculationService.calculateDtiWithExistingObligations(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(0.15);

        strategy.recalculateIndicators(merged, null);

        assertTrue(merged.containsKey(ModelPayloadFieldNames.FIELD_DTI));
        assertTrue(merged.containsKey(ModelPayloadFieldNames.FIELD_LTV));
        assertEquals(0.8, (Double) merged.get(ModelPayloadFieldNames.FIELD_LTV), 0.001); // 100k / 125k = 0.8
    }

    @Test
    @DisplayName("Should recalculate indicators successfully with base input LTV")
    void recalculateIndicators_baseInput() throws Exception {
        Map<String, Object> merged = new HashMap<>();
        merged.put(ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, 80000.0); // original was 100k
        merged.put(ModelPayloadFieldNames.FIELD_TERM_MONTHS, 120);
        merged.put(ModelPayloadFieldNames.FIELD_INTEREST_RATE, 3.5);
        merged.put(ModelPayloadFieldNames.FIELD_ANNUAL_INCOME, 80000.0);

        Map<String, Object> base = new HashMap<>();
        base.put(ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, 100000.0);
        base.put(ModelPayloadFieldNames.FIELD_LTV, 0.8);

        when(dtiCalculationService.calculateDtiWithExistingObligations(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(0.15);

        strategy.recalculateIndicators(merged, base);

        assertTrue(merged.containsKey(ModelPayloadFieldNames.FIELD_LTV));
        // Property value implied = 100k / 0.8 = 125k
        // New LTV = 80k / 125k = 0.64
        assertEquals(0.64, (Double) merged.get(ModelPayloadFieldNames.FIELD_LTV), 0.001);
    }
}
