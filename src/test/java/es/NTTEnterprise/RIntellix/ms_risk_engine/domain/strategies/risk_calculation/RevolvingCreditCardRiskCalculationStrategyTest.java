package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.constants.RiskCalculationDefaults;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;

/**
 * Unit tests for {@link RevolvingCreditCardRiskCalculationStrategy}.
 * Covers supports() decision, EAD = limit × CCF, LGD constant, and validation.
 * @date 27/08/2026
 */
@DisplayName("RevolvingCreditCardRiskCalculationStrategy Tests")
class RevolvingCreditCardRiskCalculationStrategyTest {

    private RevolvingCreditCardRiskCalculationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new RevolvingCreditCardRiskCalculationStrategy();
    }

    // ========== supports() ==========

    @Test
    @DisplayName("Should support TARJETA_CREDITO with revolving=true")
    void supports_tarjetaCreditoRevolving_returnsTrue() {
        assertTrue(strategy.supports("TARJETA_CREDITO", true));
    }

    @Test
    @DisplayName("Should NOT support TARJETA_CREDITO with revolving=false")
    void supports_tarjetaCreditoNotRevolving_returnsFalse() {
        assertFalse(strategy.supports("TARJETA_CREDITO", false));
    }

    @Test
    @DisplayName("Should NOT support TARJETA_CREDITO with revolving=null")
    void supports_tarjetaCreditoNullRevolving_returnsFalse() {
        assertFalse(strategy.supports("TARJETA_CREDITO", null));
    }

    @Test
    @DisplayName("Should NOT support PRESTAMO even with revolving=true")
    void supports_prestamo_returnsFalse() {
        assertFalse(strategy.supports("PRESTAMO", true));
    }

    @Test
    @DisplayName("Should NOT support unknown request type")
    void supports_unknownType_returnsFalse() {
        assertFalse(strategy.supports("INVALID", true));
    }

    // ========== calculatePrePdMetrics() ==========

    @Test
    @DisplayName("Should calculate EAD as credit limit × revolving CCF")
    void calculatePrePdMetrics_eadEqualsLimitTimesCcf() {
        RiskMetrics metrics = strategy.calculatePrePdMetrics(10000.0, null);

        double expectedEad = 10000.0 * RiskCalculationDefaults.CC_REVOLVING_CCF;
        assertEquals(expectedEad, metrics.getExposureAtDefault(), 0.001);
    }

    @Test
    @DisplayName("Should set LGD to CC_REVOLVING_LGD constant (0.90)")
    void calculatePrePdMetrics_lgdIsRevolvingConstant() {
        RiskMetrics metrics = strategy.calculatePrePdMetrics(10000.0, null);

        assertEquals(RiskCalculationDefaults.CC_REVOLVING_LGD, metrics.getLossGivenDefault(), 0.001);
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
}
