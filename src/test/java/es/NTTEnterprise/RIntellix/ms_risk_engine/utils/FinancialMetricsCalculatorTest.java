package es.NTTEnterprise.RIntellix.ms_risk_engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link FinancialMetricsCalculator}.
 * Covers monthly payment, DTI, total payment, total interest, and disposable income.
 * @date 27/08/2026
 */
@DisplayName("FinancialMetricsCalculator Tests")
class FinancialMetricsCalculatorTest {

    // ========== calculateMonthlyPayment ==========

    @Test
    @DisplayName("Should calculate correct monthly payment with standard values")
    void calculateMonthlyPayment_standard() {
        // 100,000 at 5% (0.05 as fraction) for 36 months
        double result = FinancialMetricsCalculator.calculateMonthlyPayment(100000.0, 0.05, 36);
        assertTrue(result > 0);
        assertEquals(2997.09, result, 1.5);
    }

    @Test
    @DisplayName("Should return simple division when rate is zero")
    void calculateMonthlyPayment_zeroRate() {
        double result = FinancialMetricsCalculator.calculateMonthlyPayment(12000.0, 0.0, 12);
        assertEquals(1000.0, result, 0.01);
    }

    @Test
    @DisplayName("Should clamp term to MIN_TERM_MONTHS when term <= 0")
    void calculateMonthlyPayment_termClamped() {
        double result = FinancialMetricsCalculator.calculateMonthlyPayment(12000.0, 0.0, 0);
        assertEquals(12000.0, result, 0.01);
    }

    @Test
    @DisplayName("Should handle negative term correctly")
    void calculateMonthlyPayment_negativeTerm() {
        double result = FinancialMetricsCalculator.calculateMonthlyPayment(12000.0, 0.0, -5);
        assertEquals(12000.0, result, 0.01);
    }

    // ========== calculateDti ==========

    @Test
    @DisplayName("Should calculate correct DTI with standard values")
    void calculateDti_standard() {
        // monthlyPayment=1000, annualIncome=48000 => monthlyIncome=4000 => DTI=0.25
        double result = FinancialMetricsCalculator.calculateDti(1000.0, 48000.0);
        assertEquals(0.25, result, 0.01);
    }

    @Test
    @DisplayName("Should return zero DTI when annual income is zero")
    void calculateDti_zeroIncome() {
        double result = FinancialMetricsCalculator.calculateDti(1000.0, 0.0);
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("Should return zero DTI when annual income is negative")
    void calculateDti_negativeIncome() {
        double result = FinancialMetricsCalculator.calculateDti(1000.0, -12000.0);
        assertEquals(0.0, result);
    }

    // ========== calculateTotalPayment ==========

    @Test
    @DisplayName("Should calculate total payment correctly")
    void calculateTotalPayment_standard() {
        double result = FinancialMetricsCalculator.calculateTotalPayment(1000.0, 36);
        assertEquals(36000.0, result, 0.01);
    }

    @Test
    @DisplayName("Should clamp term to MIN_TERM_MONTHS for total payment")
    void calculateTotalPayment_termClamped() {
        double result = FinancialMetricsCalculator.calculateTotalPayment(5000.0, 0);
        assertEquals(5000.0, result, 0.01);
    }

    // ========== calculateTotalInterest ==========

    @Test
    @DisplayName("Should calculate total interest correctly")
    void calculateTotalInterest_standard() {
        double result = FinancialMetricsCalculator.calculateTotalInterest(36000.0, 30000.0);
        assertEquals(6000.0, result, 0.01);
    }

    @Test
    @DisplayName("Should return zero interest when total equals principal")
    void calculateTotalInterest_noInterest() {
        double result = FinancialMetricsCalculator.calculateTotalInterest(30000.0, 30000.0);
        assertEquals(0.0, result, 0.01);
    }

    // ========== calculateDisposableIncome ==========

    @Test
    @DisplayName("Should calculate disposable income correctly")
    void calculateDisposableIncome_standard() {
        // annualIncome=60000, monthlyPayment=1000 => monthly income=5000, disposable=4000
        double result = FinancialMetricsCalculator.calculateDisposableIncome(60000.0, 1000.0);
        assertEquals(4000.0, result, 0.01);
    }

    @Test
    @DisplayName("Should return negative disposable income when payment exceeds income")
    void calculateDisposableIncome_negative() {
        double result = FinancialMetricsCalculator.calculateDisposableIncome(12000.0, 2000.0);
        assertTrue(result < 0, "Disposable income should be negative when payment exceeds monthly income");
    }

    // ========== Non-instantiability ==========

    @Test
    @DisplayName("Should not be instantiable - utility class")
    void shouldNotBeInstantiable() throws Exception {
        var constructor = FinancialMetricsCalculator.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
    }
}
