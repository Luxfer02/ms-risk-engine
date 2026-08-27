package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;

/**
 * Unit tests for {@link LoanFinancialMetricsStrategy}.
 * Covers supports() decision, French amortization calculation, DTI, total payment,
 * total interest, disposable income, and null guard validations.
 * @date 27/08/2026
 */
@DisplayName("LoanFinancialMetricsStrategy Tests")
class LoanFinancialMetricsStrategyTest {

    private LoanFinancialMetricsStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new LoanFinancialMetricsStrategy();
    }

    // ========== supports() ==========

    @Test
    @DisplayName("Should support PRESTAMO request type")
    void supports_prestamo_returnsTrue() {
        assertTrue(strategy.supports("PRESTAMO", null));
    }

    @Test
    @DisplayName("Should support HIPOTECA request type")
    void supports_hipoteca_returnsTrue() {
        assertTrue(strategy.supports("HIPOTECA", null));
    }

    @Test
    @DisplayName("Should NOT support TARJETA_CREDITO")
    void supports_tarjetaCredito_returnsFalse() {
        assertFalse(strategy.supports("TARJETA_CREDITO", false));
    }

    @Test
    @DisplayName("Should NOT support unknown request type")
    void supports_unknownType_returnsFalse() {
        assertFalse(strategy.supports("INVALID", null));
    }

    // ========== calculateFinancialMetrics() — normal case ==========

    @Test
    @DisplayName("Should calculate all financial metrics for a standard loan")
    void calculateFinancialMetrics_standardLoan_allMetricsPopulated() {
        // 20000€ loan, 5% rate, 36 months, 60000€ annual income, 200€/month existing obligations
        FinancialMetrics metrics = strategy.calculateFinancialMetrics(20000.0, 0.05, 60000.0, 200.0, 36);

        assertTrue(metrics.getMonthlyPayment() > 0, "Monthly payment should be positive");
        assertTrue(metrics.getDebtToIncomeRatio() > 0, "DTI should be positive");
        assertTrue(metrics.getTotalPayment() > 20000.0, "Total payment should exceed principal");
        assertTrue(metrics.getTotalInterest() > 0, "Total interest should be positive");
        assertTrue(metrics.getMonthlyDisposableIncome() > 0, "Disposable income should be positive");
    }

    @Test
    @DisplayName("Should compute total interest as total payment minus principal")
    void calculateFinancialMetrics_totalInterest_equalsPaymentMinusPrincipal() {
        FinancialMetrics metrics = strategy.calculateFinancialMetrics(10000.0, 6.0, 50000.0, 0.0, 24);

        double expectedInterest = metrics.getTotalPayment() - 10000.0;
        assertEquals(expectedInterest, metrics.getTotalInterest(), 0.01);
    }

    @Test
    @DisplayName("Should include existing obligations in DTI calculation")
    void calculateFinancialMetrics_existingObligations_affectDti() {
        FinancialMetrics withoutObl = strategy.calculateFinancialMetrics(15000.0, 4.0, 60000.0, 0.0, 24);
        FinancialMetrics withObl = strategy.calculateFinancialMetrics(15000.0, 4.0, 60000.0, 500.0, 24);

        assertTrue(withObl.getDebtToIncomeRatio() > withoutObl.getDebtToIncomeRatio(),
                "DTI should be higher when existing obligations are present");
    }

    // ========== Null guards ==========

    @Test
    @DisplayName("Should throw NPE when amount is null")
    void calculateFinancialMetrics_nullAmount_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> strategy.calculateFinancialMetrics(null, 5.0, 60000.0, 0.0, 36));
    }

    @Test
    @DisplayName("Should throw NPE when interest rate is null")
    void calculateFinancialMetrics_nullRate_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> strategy.calculateFinancialMetrics(10000.0, null, 60000.0, 0.0, 36));
    }

    @Test
    @DisplayName("Should throw NPE when term months is null")
    void calculateFinancialMetrics_nullTermMonths_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> strategy.calculateFinancialMetrics(10000.0, 5.0, 60000.0, 0.0, null));
    }

    @Test
    @DisplayName("Should throw NPE when annual income is null")
    void calculateFinancialMetrics_nullIncome_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> strategy.calculateFinancialMetrics(10000.0, 5.0, null, 0.0, 36));
    }

    @Test
    @DisplayName("Should throw NPE when existing obligations is null")
    void calculateFinancialMetrics_nullObligations_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> strategy.calculateFinancialMetrics(10000.0, 5.0, 60000.0, null, 36));
    }
}
