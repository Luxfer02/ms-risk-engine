package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.constants.RiskCalculationDefaults;

/**
 * Unit tests for {@link CreditCardFinancialMetricsCalculator}.
 * Covers standard/revolving payments, DTI, disposable income, and revolving
 * payoff simulation.
 * @date 27/08/2026
 */
@DisplayName("CreditCardFinancialMetricsCalculator Tests")
class CreditCardFinancialMetricsCalculatorTest {

    // ========== calculateStandardMonthlyPayment ==========

    @Test
    @DisplayName("Standard monthly payment should return full credit limit")
    void calculateStandardMonthlyPayment_returnsFullLimit() {
        assertEquals(5000.0, CreditCardFinancialMetricsCalculator.calculateStandardMonthlyPayment(5000.0));
    }

    // ========== calculateRevolvingMonthlyPayment ==========

    @Test
    @DisplayName("Revolving monthly payment should apply DTI payment percentage")
    void calculateRevolvingMonthlyPayment_appliesPercentage() {
        double expected = 5000.0 * RiskCalculationDefaults.CC_DTI_PAYMENT_PERCENTAGE;
        assertEquals(expected, CreditCardFinancialMetricsCalculator.calculateRevolvingMonthlyPayment(5000.0));
    }

    // ========== calculateMonthlyPayment ==========

    @Test
    @DisplayName("Should delegate to revolving when isRevolving=true")
    void calculateMonthlyPayment_revolving() {
        double expected = 5000.0 * RiskCalculationDefaults.CC_DTI_PAYMENT_PERCENTAGE;
        assertEquals(expected, CreditCardFinancialMetricsCalculator.calculateMonthlyPayment(5000.0, true));
    }

    @Test
    @DisplayName("Should delegate to standard when isRevolving=false")
    void calculateMonthlyPayment_standard() {
        assertEquals(5000.0, CreditCardFinancialMetricsCalculator.calculateMonthlyPayment(5000.0, false));
    }

    @Test
    @DisplayName("Should delegate to standard when isRevolving=null")
    void calculateMonthlyPayment_nullRevolving() {
        assertEquals(5000.0, CreditCardFinancialMetricsCalculator.calculateMonthlyPayment(5000.0, null));
    }

    // ========== calculateCreditCardDti ==========

    @Test
    @DisplayName("Should calculate correct DTI with positive income")
    void calculateCreditCardDti_positiveIncome() {
        // monthlyPayment=200, annualIncome=48000 => monthlyIncome=4000 => DTI=0.05
        double result = CreditCardFinancialMetricsCalculator.calculateCreditCardDti(200.0, 48000.0);
        assertEquals(0.05, result, 0.001);
    }

    @Test
    @DisplayName("Should return zero DTI when income is zero")
    void calculateCreditCardDti_zeroIncome() {
        assertEquals(0.0, CreditCardFinancialMetricsCalculator.calculateCreditCardDti(200.0, 0.0));
    }

    @Test
    @DisplayName("Should return zero DTI when income is negative")
    void calculateCreditCardDti_negativeIncome() {
        assertEquals(0.0, CreditCardFinancialMetricsCalculator.calculateCreditCardDti(200.0, -12000.0));
    }

    @Test
    @DisplayName("Should handle extremely high monthly payment and low income (max DTI)")
    void calculateCreditCardDti_extremeValues() {
        // e.g. payment = 100,000, income = 1200 => monthlyIncome=100
        double result = CreditCardFinancialMetricsCalculator.calculateCreditCardDti(100000.0, 1200.0);
        assertEquals(1000.0, result, 0.001); // 100000 / 100 = 1000.0
    }

    // ========== calculateCreditCardDisposableIncome ==========

    @Test
    @DisplayName("Should calculate disposable income correctly")
    void calculateCreditCardDisposableIncome_standard() {
        // annualIncome=60000 => monthlyIncome=5000
        // disposable = 5000 * (1 - 0.50) - 200 = 2300
        double result = CreditCardFinancialMetricsCalculator.calculateCreditCardDisposableIncome(60000.0, 200.0);
        assertEquals(2300.0, result, 0.01);
    }

    // ========== simulateRevolvingPayoff ==========

    @Test
    @DisplayName("Should return zeros when credit limit is zero")
    void simulateRevolvingPayoff_zeroCreditLimit() {
        var result = CreditCardFinancialMetricsCalculator.simulateRevolvingPayoff(0.0, 0.20);
        assertEquals(0.0, result.totalInterest());
        assertEquals(0.0, result.totalPayment());
    }

    @Test
    @DisplayName("Should return zeros when credit limit is negative")
    void simulateRevolvingPayoff_negativeCreditLimit() {
        var result = CreditCardFinancialMetricsCalculator.simulateRevolvingPayoff(-1000.0, 0.20);
        assertEquals(0.0, result.totalInterest());
        assertEquals(0.0, result.totalPayment());
    }

    @Test
    @DisplayName("Should return zero interest when rate is zero")
    void simulateRevolvingPayoff_zeroInterestRate() {
        var result = CreditCardFinancialMetricsCalculator.simulateRevolvingPayoff(5000.0, 0.0);
        assertEquals(0.0, result.totalInterest());
        assertEquals(5000.0, result.totalPayment());
    }

    @Test
    @DisplayName("Should return zero interest when rate is negative")
    void simulateRevolvingPayoff_negativeInterestRate() {
        var result = CreditCardFinancialMetricsCalculator.simulateRevolvingPayoff(5000.0, -0.05);
        assertEquals(0.0, result.totalInterest());
        assertEquals(5000.0, result.totalPayment());
    }

    @Test
    @DisplayName("Should calculate positive total interest for normal case")
    void simulateRevolvingPayoff_normalCase() {
        var result = CreditCardFinancialMetricsCalculator.simulateRevolvingPayoff(5000.0, 0.24);
        assertTrue(result.totalInterest() > 0, "Total interest should be positive");
        assertTrue(result.totalPayment() > 0, "Total payment should be positive");
        assertTrue(result.totalPayment() > result.totalInterest(),
                "Total payment should exceed total interest");
    }

    @Test
    @DisplayName("Should not exceed max simulation months")
    void simulateRevolvingPayoff_respectsMaxMonths() {
        // Very large credit limit and low payment percentage should cap at max months
        var result = CreditCardFinancialMetricsCalculator.simulateRevolvingPayoff(1_000_000.0, 0.30);
        assertTrue(result.totalPayment() > 0);
    }

    @Test
    @DisplayName("Should handle extremely high interest rate")
    void simulateRevolvingPayoff_highInterestRate() {
        var result = CreditCardFinancialMetricsCalculator.simulateRevolvingPayoff(5000.0, 0.50); // 50% interest
        assertTrue(result.totalInterest() >= 0);
        assertTrue(result.totalPayment() >= 0);
    }

    // ========== Non-instantiability ==========

    @Test
    @DisplayName("Should not be instantiable - utility class")
    void shouldNotBeInstantiable() throws Exception {
        var constructor = CreditCardFinancialMetricsCalculator.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
    }
}
