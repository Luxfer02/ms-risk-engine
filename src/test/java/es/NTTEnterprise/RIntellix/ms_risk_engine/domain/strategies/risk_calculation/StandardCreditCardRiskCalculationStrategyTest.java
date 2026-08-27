package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.constants.RiskCalculationDefaults;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;

/**
 * Unit tests for {@link StandardCreditCardRiskCalculationStrategy}.
 * Covers supports() decision with revolving=false/null, EAD = limit × CCF,
 * LGD constant, and validation.
 * @date 27/08/2026
 */
@DisplayName("StandardCreditCardRiskCalculationStrategy Tests")
class StandardCreditCardRiskCalculationStrategyTest {

    private StandardCreditCardRiskCalculationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new StandardCreditCardRiskCalculationStrategy();
    }

    // ========== supports() ==========

    @Test
    @DisplayName("Should support TARJETA_CREDITO with revolving=false")
    void supports_tarjetaCreditoNotRevolving_returnsTrue() {
        assertTrue(strategy.supports("TARJETA_CREDITO", false));
    }

    @Test
    @DisplayName("Should support TARJETA_CREDITO with revolving=null")
    void supports_tarjetaCreditoNullRevolving_returnsTrue() {
        assertTrue(strategy.supports("TARJETA_CREDITO", null));
    }

    @Test
    @DisplayName("Should NOT support TARJETA_CREDITO with revolving=true")
    void supports_tarjetaCreditoRevolving_returnsFalse() {
        assertFalse(strategy.supports("TARJETA_CREDITO", true));
    }

    @Test
    @DisplayName("Should NOT support PRESTAMO")
    void supports_prestamo_returnsFalse() {
        assertFalse(strategy.supports("PRESTAMO", false));
    }

    @Test
    @DisplayName("Should NOT support HIPOTECA")
    void supports_hipoteca_returnsFalse() {
        assertFalse(strategy.supports("HIPOTECA", false));
    }

    @Test
    @DisplayName("Should NOT support unknown request type")
    void supports_unknownType_returnsFalse() {
        assertFalse(strategy.supports("UNKNOWN", null));
    }

    // ========== calculatePrePdMetrics() ==========

    @Test
    @DisplayName("Should calculate EAD as credit limit × standard CCF")
    void calculatePrePdMetrics_eadEqualsLimitTimesStandardCcf() {
        RiskMetrics metrics = strategy.calculatePrePdMetrics(8000.0, null);

        double expectedEad = 8000.0 * RiskCalculationDefaults.CC_STANDARD_CCF;
        assertEquals(expectedEad, metrics.getExposureAtDefault(), 0.001);
    }

    @Test
    @DisplayName("Should set LGD to CC_STANDARD_LGD constant (0.60)")
    void calculatePrePdMetrics_lgdIsStandardConstant() {
        RiskMetrics metrics = strategy.calculatePrePdMetrics(8000.0, null);

        assertEquals(RiskCalculationDefaults.CC_STANDARD_LGD, metrics.getLossGivenDefault(), 0.001);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when credit limit is null")
    void calculatePrePdMetrics_nullAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> strategy.calculatePrePdMetrics(null, null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when credit limit is zero")
    void calculatePrePdMetrics_zeroAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> strategy.calculatePrePdMetrics(0.0, null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when credit limit is negative")
    void calculatePrePdMetrics_negativeAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> strategy.calculatePrePdMetrics(-1000.0, null));
    }
}
