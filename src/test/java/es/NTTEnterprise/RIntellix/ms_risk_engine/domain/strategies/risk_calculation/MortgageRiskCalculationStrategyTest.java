package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.constants.RiskCalculationDefaults;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;

/**
 * Unit tests for {@link MortgageRiskCalculationStrategy}.
 * Covers supports() decision, EAD calculation, dynamic haircut,
 * LGD with collateral, LGD floor, zero LTV fallback, and validation.
 * @date 27/08/2026
 */
@DisplayName("MortgageRiskCalculationStrategy Tests")
class MortgageRiskCalculationStrategyTest {

    private MortgageRiskCalculationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new MortgageRiskCalculationStrategy();
    }

    // ========== supports() ==========

    @Test
    @DisplayName("Should support HIPOTECA request type")
    void supports_hipoteca_returnsTrue() {
        assertTrue(strategy.supports("HIPOTECA", null));
    }

    @Test
    @DisplayName("Should NOT support PRESTAMO request type")
    void supports_prestamo_returnsFalse() {
        assertFalse(strategy.supports("PRESTAMO", null));
    }

    @Test
    @DisplayName("Should NOT support TARJETA_CREDITO request type")
    void supports_tarjetaCredito_returnsFalse() {
        assertFalse(strategy.supports("TARJETA_CREDITO", false));
    }

    @Test
    @DisplayName("Should NOT support unknown request type")
    void supports_unknownType_returnsFalse() {
        assertFalse(strategy.supports("INVALID", null));
    }

    // ========== calculatePrePdMetrics() — EAD ==========

    @Test
    @DisplayName("Should set EAD equal to requested mortgage amount")
    void calculatePrePdMetrics_eadEqualsRequestedAmount() {
        RiskMetrics metrics = strategy.calculatePrePdMetrics(200000.0, 0.60);

        assertEquals(200000.0, metrics.getExposureAtDefault(), 0.001);
    }

    // ========== calculatePrePdMetrics() — LGD with normal LTV (≤80%) ==========

    @Test
    @DisplayName("Should calculate LGD considering collateral, haircut and foreclosure costs for normal LTV")
    void calculatePrePdMetrics_normalLtv_calculatesLgdWithCollateral() {
        // LTV = 0.60 → appraisal = 200000 / 0.60 = 333333.33
        // No dynamic haircut (LTV ≤ 80%), haircut = 20%
        // foreclosureCosts = 333333.33 * 0.10 = 33333.33
        // recoverableCollateral = 333333.33 * 0.80 - 33333.33 = 233333.33
        // calculatedLgd = (200000 - 233333.33) / 200000 = max(floor, negative) → floor = 0.10
        RiskMetrics metrics = strategy.calculatePrePdMetrics(200000.0, 0.60);

        assertEquals(RiskCalculationDefaults.MORTGAGE_LGD_FLOOR, metrics.getLossGivenDefault(), 0.001,
                "Low LTV mortgage should clamp to LGD floor");
    }

    // ========== calculatePrePdMetrics() — Dynamic haircut for high LTV ==========

    @Test
    @DisplayName("Should apply dynamic exponential haircut when LTV exceeds 80%")
    void calculatePrePdMetrics_highLtv_appliesDynamicHaircut() {
        // LTV=0.95 → dynamicHaircut = 0.20 + 0.5*(0.95-0.80)^2 = 0.20 + 0.5*0.0225 = 0.21125
        // appraisal = amount / 0.95
        // foreclosureCosts = appraisal * 0.10
        // recoverableCollateral = appraisal * (1 - 0.21125) - foreclosureCosts
        RiskMetrics metrics = strategy.calculatePrePdMetrics(190000.0, 0.95);

        assertTrue(metrics.getLossGivenDefault() > RiskCalculationDefaults.MORTGAGE_LGD_FLOOR,
                "High LTV should produce LGD above floor");
        assertTrue(metrics.getLossGivenDefault() <= 1.0,
                "LGD should be clamped to maximum 1.0");
    }

    @Test
    @DisplayName("Should calculate LGD at boundary LTV of exactly 80% without dynamic haircut")
    void calculatePrePdMetrics_ltvExactly80_noDynamicHaircut() {
        RiskMetrics metrics = strategy.calculatePrePdMetrics(160000.0, 0.80);

        // LTV=0.80, no dynamic haircut applied (condition is > 0.80)
        // appraisal = 160000/0.80 = 200000
        // foreclosureCosts = 200000 * 0.10 = 20000
        // recoverableCollateral = 200000 * 0.80 - 20000 = 140000
        // calculatedLgd = (160000 - 140000) / 160000 = 0.125 → max(0.10, 0.125) = 0.125
        assertEquals(0.125, metrics.getLossGivenDefault(), 0.001);
    }

    // ========== calculatePrePdMetrics() — Zero/null LTV ==========

    @Test
    @DisplayName("Should use unsecured LGD when LTV is zero (no collateral)")
    void calculatePrePdMetrics_zeroLtv_usesUnsecuredLgd() {
        RiskMetrics metrics = strategy.calculatePrePdMetrics(100000.0, 0.0);

        assertEquals(RiskCalculationDefaults.MORTGAGE_LGD_UNSECURED_LOAN,
                metrics.getLossGivenDefault(), 0.001);
    }

    @Test
    @DisplayName("Should use unsecured LGD when LTV is null")
    void calculatePrePdMetrics_nullLtv_usesUnsecuredLgd() {
        RiskMetrics metrics = strategy.calculatePrePdMetrics(100000.0, null);

        assertEquals(RiskCalculationDefaults.MORTGAGE_LGD_UNSECURED_LOAN,
                metrics.getLossGivenDefault(), 0.001);
    }

    // ========== calculatePrePdMetrics() — LTV > 1.0 capping ==========

    @Test
    @DisplayName("Should cap LTV at 1.0 when provided value exceeds 100%")
    void calculatePrePdMetrics_ltvAboveOne_clampedToOne() {
        RiskMetrics metricsOver = strategy.calculatePrePdMetrics(150000.0, 1.5);
        RiskMetrics metricsCapped = strategy.calculatePrePdMetrics(150000.0, 1.0);

        assertEquals(metricsCapped.getLossGivenDefault(), metricsOver.getLossGivenDefault(), 0.001,
                "LTV > 1.0 should be capped to 1.0, producing same result");
    }

    // ========== Validation ==========

    @Test
    @DisplayName("Should throw IllegalArgumentException when mortgage amount is null")
    void calculatePrePdMetrics_nullAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> strategy.calculatePrePdMetrics(null, 0.70));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when mortgage amount is zero")
    void calculatePrePdMetrics_zeroAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> strategy.calculatePrePdMetrics(0.0, 0.70));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when mortgage amount is negative")
    void calculatePrePdMetrics_negativeAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> strategy.calculatePrePdMetrics(-100000.0, 0.70));
    }
}
