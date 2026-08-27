package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.indicators.CreditCardRiskIndicatorStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.indicators.LoanRiskIndicatorStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.indicators.MortgageRiskIndicatorStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadFieldNames;

/**
 * Unit tests for {@link RiskIndicatorCalculationService}.
 * Covers DTI and LTV recalculation for different request types using strategies.
 * @date 27/08/2026
 */
@DisplayName("RiskIndicatorCalculationService Tests")
@ExtendWith(MockitoExtension.class)
class RiskIndicatorCalculationServiceTest {

    @Mock
    private DtiCalculationService dtiCalculationService;

    private RiskIndicatorCalculationService service;

    @BeforeEach
    void setUp() {
        LoanRiskIndicatorStrategy loanStrategy = new LoanRiskIndicatorStrategy(dtiCalculationService);
        MortgageRiskIndicatorStrategy mortgageStrategy = new MortgageRiskIndicatorStrategy(dtiCalculationService);
        CreditCardRiskIndicatorStrategy ccStrategy = new CreditCardRiskIndicatorStrategy(dtiCalculationService);
        
        service = new RiskIndicatorCalculationService(Arrays.asList(loanStrategy, mortgageStrategy, ccStrategy));
    }

    @Test
    @DisplayName("Should recalculate DTI for PRESTAMO")
    void recalculateRiskIndicators_prestamo_updatesDti() {
        Map<String, Object> mergedVariables = new HashMap<>();
        mergedVariables.put(ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, 10000.0);
        mergedVariables.put(ModelPayloadFieldNames.FIELD_INTEREST_RATE, 5.0 / 100.0); 
        mergedVariables.put(ModelPayloadFieldNames.FIELD_TERM_MONTHS, 36.0);
        mergedVariables.put(ModelPayloadFieldNames.FIELD_ANNUAL_INCOME, 50000.0);
        mergedVariables.put(ModelPayloadFieldNames.FIELD_EXISTING_OBLIGATIONS, 1200.0); // 100/month

        Map<String, Object> baseSnapshot = new HashMap<>();

        when(dtiCalculationService.calculateDtiWithExistingObligations(anyDouble(), eq(50000.0), eq(100.0)))
                .thenReturn(0.35);

        service.recalculateRiskIndicators(mergedVariables, "PRESTAMO", baseSnapshot);

        assertEquals(0.35, mergedVariables.get(ModelPayloadFieldNames.FIELD_DTI));
    }

    @Test
    @DisplayName("Should recalculate DTI and LTV for HIPOTECA and remove property value")
    void recalculateRiskIndicators_hipoteca_updatesDtiAndLtv() {
        Map<String, Object> mergedVariables = new HashMap<>();
        mergedVariables.put(ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, 80000.0);
        mergedVariables.put(ModelPayloadFieldNames.FIELD_ANNUAL_INCOME, 50000.0);
        mergedVariables.put(ModelPayloadFieldNames.FIELD_PROPERTY_VALUE, 100000.0); // LTV should be 0.80

        Map<String, Object> baseSnapshot = new HashMap<>();
        
        when(dtiCalculationService.calculateDtiWithExistingObligations(anyDouble(), eq(50000.0), eq(0.0)))
                .thenReturn(0.30);

        service.recalculateRiskIndicators(mergedVariables, "HIPOTECA", baseSnapshot);

        assertEquals(0.30, mergedVariables.get(ModelPayloadFieldNames.FIELD_DTI));
        assertEquals(0.80, (Double) mergedVariables.get(ModelPayloadFieldNames.FIELD_LTV), 0.001);
        assertTrue(mergedVariables.containsKey(ModelPayloadFieldNames.FIELD_PROPERTY_VALUE), 
            "Property value should NOT be removed in the new implementation");
    }

    @Test
    @DisplayName("Should calculate LTV using base snapshot ltv and loanAmount if missing in merged")
    void recalculateRiskIndicators_hipoteca_usesBasePropertyValue() {
        Map<String, Object> mergedVariables = new HashMap<>();
        mergedVariables.put(ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, 90000.0);

        Map<String, Object> baseSnapshot = new HashMap<>();
        baseSnapshot.put(ModelPayloadFieldNames.FIELD_LTV, 0.90);
        baseSnapshot.put(ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, 90000.0);
        
        when(dtiCalculationService.calculateDtiWithExistingObligations(anyDouble(), eq(0.0), eq(0.0)))
                .thenReturn(0.40);

        service.recalculateRiskIndicators(mergedVariables, "HIPOTECA", baseSnapshot);

        assertEquals(0.90, (Double) mergedVariables.get(ModelPayloadFieldNames.FIELD_LTV), 0.001);
    }

    @Test
    @DisplayName("Should recalculate DTI for TARJETA_CREDITO using credit limit")
    void recalculateRiskIndicators_tarjetaCredito_updatesDti() {
        Map<String, Object> mergedVariables = new HashMap<>();
        mergedVariables.put(ModelPayloadFieldNames.FIELD_CREDIT_LIMIT, 5000.0);
        mergedVariables.put(ModelPayloadFieldNames.FIELD_IS_REVOLVING, false);
        mergedVariables.put(ModelPayloadFieldNames.FIELD_ANNUAL_INCOME, 60000.0);

        Map<String, Object> baseSnapshot = new HashMap<>();

        // Standard CC monthly payment = 5000
        when(dtiCalculationService.calculateDtiWithExistingObligations(eq(5000.0), eq(60000.0), eq(0.0)))
                .thenReturn(0.10);

        service.recalculateRiskIndicators(mergedVariables, "TARJETA_CREDITO", baseSnapshot);

        assertEquals(0.10, mergedVariables.get(ModelPayloadFieldNames.FIELD_DTI));
    }

    @Test
    @DisplayName("Constructor should throw NPE when Strategies list is null")
    void constructor_shouldThrow_whenServiceNull() {
        assertThrows(NullPointerException.class, () -> new RiskIndicatorCalculationService(null));
    }
}
