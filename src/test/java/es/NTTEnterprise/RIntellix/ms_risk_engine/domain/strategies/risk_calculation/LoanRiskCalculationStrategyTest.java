package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.constants.RiskCalculationDefaults;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;

/**
 * Unit tests for {@link LoanRiskCalculationStrategy}.
 * Covers supports() decision logic, EAD/LGD calculation, and validation.
 * @date 27/08/2026
 */
@DisplayName("LoanRiskCalculationStrategy Tests")
class LoanRiskCalculationStrategyTest {

    private LoanRiskCalculationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new LoanRiskCalculationStrategy();
    }

    // ========== supports() ==========

    @Test
    @DisplayName("Should support PRESTAMO request type")
    void supports_prestamo_returnsTrue() {
        assertTrue(strategy.supports("PRESTAMO", null));
    }

    @Test
    @DisplayName("Should support PRESTAMO regardless of revolving flag")
    void supports_prestamo_ignoresRevolvingFlag() {
        assertTrue(strategy.supports("PRESTAMO", true));
        assertTrue(strategy.supports("PRESTAMO", false));
    }

    @Test
    @DisplayName("Should NOT support HIPOTECA request type")
    void supports_hipoteca_returnsFalse() {
        assertFalse(strategy.supports("HIPOTECA", null));
    }

    @Test
    @DisplayName("Should NOT support TARJETA_CREDITO request type")
    void supports_tarjetaCredito_returnsFalse() {
        assertFalse(strategy.supports("TARJETA_CREDITO", null));
    }

    @Test
    @DisplayName("Should NOT support unknown request type")
    void supports_unknownType_returnsFalse() {
        assertFalse(strategy.supports("UNKNOWN_TYPE", null));
    }

    // ========== calculatePrePdMetrics() ==========

    @Test
    @DisplayName("Should calculate EAD equal to requested amount for loan")
    void calculatePrePdMetrics_eadEqualsRequestedAmount() {
        RiskMetrics metrics = strategy.calculatePrePdMetrics(25000.0, null);

        assertEquals(25000.0, metrics.getExposureAtDefault(), 0.001);
    }

    @Test
    @DisplayName("Should set LGD to LOAN_LGD constant (0.70)")
    void calculatePrePdMetrics_lgdIsLoanConstant() {
        RiskMetrics metrics = strategy.calculatePrePdMetrics(25000.0, null);

        assertEquals(RiskCalculationDefaults.LOAN_LGD, metrics.getLossGivenDefault(), 0.001);
    }

    @Test
    @DisplayName("Should ignore LTV parameter for loans")
    void calculatePrePdMetrics_ignoresLtv() {
        RiskMetrics metrics = strategy.calculatePrePdMetrics(10000.0, 0.80);

        assertEquals(10000.0, metrics.getExposureAtDefault(), 0.001);
        assertEquals(RiskCalculationDefaults.LOAN_LGD, metrics.getLossGivenDefault(), 0.001);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when amount is null")
    void calculatePrePdMetrics_nullAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> strategy.calculatePrePdMetrics(null, null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when amount is zero")
    void calculatePrePdMetrics_zeroAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> strategy.calculatePrePdMetrics(0.0, null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when amount is negative")
    void calculatePrePdMetrics_negativeAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> strategy.calculatePrePdMetrics(-5000.0, null));
    }
}
