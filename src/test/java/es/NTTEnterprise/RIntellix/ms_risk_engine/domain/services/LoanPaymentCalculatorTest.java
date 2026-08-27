package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LoanPaymentCalculator}.
 * Covers French amortization formula, edge cases, and input validation.
 * @date 27/08/2026
 */
@DisplayName("LoanPaymentCalculator Tests")
class LoanPaymentCalculatorTest {

    private LoanPaymentCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new LoanPaymentCalculator();
    }

    @Test
    @DisplayName("Should calculate correct monthly payment for standard loan")
    void shouldCalculateCorrectPayment_standardLoan() {
        // 100,000 at 5% annual over 36 months
        double result = calculator.calculateFrenchMonthlyPayment(100000.0, 36, 0.05);
        // Expected ~2997.09 using French amortization
        assertTrue(result > 0, "Monthly payment should be positive");
        assertEquals(2997.09, result, 1.0, "Payment should be approximately 2997");
    }

    @Test
    @DisplayName("Should return simple division when interest rate is zero")
    void shouldReturnSimpleDivision_whenInterestRateIsZero() {
        double result = calculator.calculateFrenchMonthlyPayment(12000.0, 12, 0.0);
        assertEquals(1000.0, result, 0.01);
    }

    @Test
    @DisplayName("Should treat null interest rate as zero")
    void shouldReturnSimpleDivision_whenInterestRateIsNull() {
        double result = calculator.calculateFrenchMonthlyPayment(12000.0, 12, null);
        assertEquals(1000.0, result, 0.01);
    }

    @Test
    @DisplayName("Should throw NullPointerException when principal is null")
    void shouldThrowNPE_whenPrincipalIsNull() {
        assertThrows(NullPointerException.class,
                () -> calculator.calculateFrenchMonthlyPayment(null, 12, 0.05));
    }

    @Test
    @DisplayName("Should throw NullPointerException when term months is null")
    void shouldThrowNPE_whenTermMonthsIsNull() {
        assertThrows(NullPointerException.class,
                () -> calculator.calculateFrenchMonthlyPayment(100000.0, null, 0.05));
    }

    @Test
    @DisplayName("Should normalize principal to zero when negative")
    void shouldNormalizePrincipal_whenNegative() {
        double result = calculator.calculateFrenchMonthlyPayment(-5000.0, 12, 0.05);
        assertEquals(0.0, result, 0.01);
    }

    @Test
    @DisplayName("Should normalize term months to 1 when zero or negative")
    void shouldNormalizeTermMonths_whenZeroOrNegative() {
        double result = calculator.calculateFrenchMonthlyPayment(12000.0, 0, 0.0);
        assertEquals(12000.0, result, 0.01);
    }

    @Test
    @DisplayName("Should handle very large principal without overflow")
    void shouldHandleVeryLargePrincipal() {
        double result = calculator.calculateFrenchMonthlyPayment(10_000_000.0, 360, 0.035);
        assertTrue(result > 0, "Payment should be positive for large principal");
        assertTrue(Double.isFinite(result), "Payment should be finite");
    }

    @Test
    @DisplayName("Should handle very small interest rate")
    void shouldHandleVerySmallInterestRate() {
        double result = calculator.calculateFrenchMonthlyPayment(100000.0, 12, 0.001);
        assertTrue(result > 0, "Payment should be positive");
        // With near-zero rate, payment ≈ principal/term
        assertEquals(100000.0 / 12, result, 5.0);
    }

    @Test
    @DisplayName("Should return value rounded to 2 decimal places")
    void shouldReturnRoundedValue() {
        double result = calculator.calculateFrenchMonthlyPayment(100000.0, 36, 0.05);
        double rounded = Math.round(result * 100.0) / 100.0;
        assertEquals(rounded, result, 0.001, "Result should be rounded to 2 decimal places");
    }

    @Test
    @DisplayName("Should handle extremely short term (1 month)")
    void shouldHandleExtremelyShortTerm() {
        double result = calculator.calculateFrenchMonthlyPayment(12000.0, 1, 0.12);
        // 1 month at 12% annual (1% monthly). Payment = 12000 + 12000*0.01 = 12120
        assertEquals(12120.0, result, 0.01);
    }

    @Test
    @DisplayName("Should handle extremely long term (600 months)")
    void shouldHandleExtremelyLongTerm() {
        double result = calculator.calculateFrenchMonthlyPayment(500000.0, 600, 0.04);
        assertTrue(result > 0, "Payment should be positive for extremely long term");
        assertTrue(Double.isFinite(result), "Payment should be finite");
    }

    @Test
    @DisplayName("Should handle extremely high interest rate")
    void shouldHandleHighInterestRate() {
        double result = calculator.calculateFrenchMonthlyPayment(10000.0, 12, 0.50);
        assertTrue(result > 10000.0 / 12, "Payment should be significantly higher than simple division");
        assertTrue(Double.isFinite(result), "Payment should be finite");
    }
}
