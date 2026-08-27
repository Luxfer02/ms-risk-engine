package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;

/**
 * Unit tests for {@link RevolvingCreditCardFinancialMetricsStrategy}.
 * Covers supports() decision, revolving monthly payment, DTI, simulation-based
 * total interest, disposable income, and null guard validations.
 * @date 27/08/2026
 */
@DisplayName("RevolvingCreditCardFinancialMetricsStrategy Tests")
class RevolvingCreditCardFinancialMetricsStrategyTest {

    private RevolvingCreditCardFinancialMetricsStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new RevolvingCreditCardFinancialMetricsStrategy();
    }

    // ========== supports() ==========

    @Test
    @DisplayName("Should support TARJETA_CREDITO with revolving=true")
    void supports_revolvingTrue_returnsTrue() {
        assertTrue(strategy.supports("TARJETA_CREDITO", true));
    }

    @Test
    @DisplayName("Should NOT support TARJETA_CREDITO with revolving=false")
    void supports_revolvingFalse_returnsFalse() {
        assertFalse(strategy.supports("TARJETA_CREDITO", false));
    }

    @Test
    @DisplayName("Should NOT support PRESTAMO")
    void supports_prestamo_returnsFalse() {
        assertFalse(strategy.supports("PRESTAMO", true));
    }

    // ========== calculateFinancialMetrics() ==========

    @Test
    @DisplayName("Should calculate all metrics for revolving credit card")
    void calculateFinancialMetrics_allMetricsPopulated() {
        FinancialMetrics metrics = strategy.calculateFinancialMetrics(5000.0, 20.0, 40000.0, 0.0, null);

        assertTrue(metrics.getMonthlyPayment() > 0, "Monthly payment should be positive");
        assertTrue(metrics.getDebtToIncomeRatio() > 0, "DTI should be positive");
        assertTrue(metrics.getTotalPayment() > 0, "Total payment should be positive");
        assertTrue(metrics.getTotalInterest() > 0, "Revolving card should accrue interest");
        assertTrue(metrics.getMonthlyDisposableIncome() >= 0, "Disposable income should be non-negative");
    }

    @Test
    @DisplayName("Should handle null existing obligations as zero")
    void calculateFinancialMetrics_nullExistingObligations_treatedAsZero() {
        FinancialMetrics withNull = strategy.calculateFinancialMetrics(5000.0, 18.0, 50000.0, null, null);
        FinancialMetrics withZero = strategy.calculateFinancialMetrics(5000.0, 18.0, 50000.0, 0.0, null);

        assertEquals(withZero.getDebtToIncomeRatio(), withNull.getDebtToIncomeRatio(), 0.001);
    }

    @Test
    @DisplayName("Should produce higher DTI when existing obligations are present")
    void calculateFinancialMetrics_existingObligations_increasesDti() {
        FinancialMetrics withoutObl = strategy.calculateFinancialMetrics(5000.0, 18.0, 50000.0, 0.0, null);
        FinancialMetrics withObl = strategy.calculateFinancialMetrics(5000.0, 18.0, 50000.0, 300.0, null);

        assertTrue(withObl.getDebtToIncomeRatio() > withoutObl.getDebtToIncomeRatio());
    }

    // ========== Null guards ==========

    @Test
    @DisplayName("Should throw NPE when credit limit is null")
    void calculateFinancialMetrics_nullAmount_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> strategy.calculateFinancialMetrics(null, 18.0, 50000.0, 0.0, null));
    }

    @Test
    @DisplayName("Should throw NPE when interest rate is null")
    void calculateFinancialMetrics_nullRate_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> strategy.calculateFinancialMetrics(5000.0, null, 50000.0, 0.0, null));
    }

    @Test
    @DisplayName("Should throw NPE when annual income is null")
    void calculateFinancialMetrics_nullIncome_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> strategy.calculateFinancialMetrics(5000.0, 18.0, null, 0.0, null));
    }
}
