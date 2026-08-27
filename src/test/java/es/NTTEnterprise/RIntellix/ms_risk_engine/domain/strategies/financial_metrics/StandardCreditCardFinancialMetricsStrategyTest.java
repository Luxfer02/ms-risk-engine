package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;

/**
 * Unit tests for {@link StandardCreditCardFinancialMetricsStrategy}.
 * Covers supports() decision, full-payment logic, zero interest,
 * disposable income, and null guard validations.
 * @date 27/08/2026
 */
@DisplayName("StandardCreditCardFinancialMetricsStrategy Tests")
class StandardCreditCardFinancialMetricsStrategyTest {

    private StandardCreditCardFinancialMetricsStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new StandardCreditCardFinancialMetricsStrategy();
    }

    // ========== supports() ==========

    @Test
    @DisplayName("Should support TARJETA_CREDITO with revolving=false")
    void supports_standardCard_returnsTrue() {
        assertTrue(strategy.supports("TARJETA_CREDITO", false));
    }

    @Test
    @DisplayName("Should NOT support TARJETA_CREDITO with revolving=true")
    void supports_revolving_returnsFalse() {
        assertFalse(strategy.supports("TARJETA_CREDITO", true));
    }

    @Test
    @DisplayName("Should NOT support PRESTAMO")
    void supports_prestamo_returnsFalse() {
        assertFalse(strategy.supports("PRESTAMO", false));
    }

    // ========== calculateFinancialMetrics() ==========

    @Test
    @DisplayName("Should set total interest to zero for standard credit card")
    void calculateFinancialMetrics_zeroInterest() {
        FinancialMetrics metrics = strategy.calculateFinancialMetrics(5000.0, 0.0, 60000.0, 0.0, null);

        assertEquals(0.0, metrics.getTotalInterest(), 0.001,
                "Standard cards do not accrue interest if paid in full");
    }

    @Test
    @DisplayName("Should set total payment equal to credit limit")
    void calculateFinancialMetrics_totalPaymentEqualsLimit() {
        FinancialMetrics metrics = strategy.calculateFinancialMetrics(3000.0, 0.0, 60000.0, 0.0, null);

        assertEquals(3000.0, metrics.getTotalPayment(), 0.001);
    }

    @Test
    @DisplayName("Should populate monthly payment and DTI for standard card")
    void calculateFinancialMetrics_metricsPopulated() {
        FinancialMetrics metrics = strategy.calculateFinancialMetrics(1000.0, 0.0, 60000.0, 0.0, null);

        assertTrue(metrics.getMonthlyPayment() > 0, "Monthly payment should be positive");
        assertTrue(metrics.getDebtToIncomeRatio() > 0, "DTI should be positive");
        assertTrue(metrics.getMonthlyDisposableIncome() >= 0, "Disposable income should be non-negative");
    }

    @Test
    @DisplayName("Should treat null existing obligations as zero")
    void calculateFinancialMetrics_nullObligations_treatedAsZero() {
        FinancialMetrics withNull = strategy.calculateFinancialMetrics(4000.0, 0.0, 50000.0, null, null);
        FinancialMetrics withZero = strategy.calculateFinancialMetrics(4000.0, 0.0, 50000.0, 0.0, null);

        assertEquals(withZero.getDebtToIncomeRatio(), withNull.getDebtToIncomeRatio(), 0.001);
    }

    // ========== Null guards ==========

    @Test
    @DisplayName("Should throw NPE when credit limit is null")
    void calculateFinancialMetrics_nullAmount_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> strategy.calculateFinancialMetrics(null, 0.0, 60000.0, 0.0, null));
    }

    @Test
    @DisplayName("Should throw NPE when annual income is null")
    void calculateFinancialMetrics_nullIncome_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> strategy.calculateFinancialMetrics(5000.0, 0.0, null, 0.0, null));
    }
}
