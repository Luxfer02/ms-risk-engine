package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DtiCalculationService}.
 * Covers DTI for scoring, credit card scoring, existing obligations, and
 * monthly obligation resolution.
 * @date 27/08/2026
 */
@DisplayName("DtiCalculationService Tests")
class DtiCalculationServiceTest {

    private DtiCalculationService service;

    @BeforeEach
    void setUp() {
        service = new DtiCalculationService();
    }

    // ========== calculateModelDtiForScoring ==========

    @Test
    @DisplayName("Should calculate model DTI for standard scoring case")
    void calculateModelDtiForScoring_standardCase() {
        double result = service.calculateModelDtiForScoring(60000.0, 6000.0, 20000.0, 5.0, 36);
        // Income: 60000 / 12 = 5000
        // Existing obligations: 6000 / 12 = 500
        // Loan payment: 20000, 5%, 36mo -> ~599.42
        // DTI = (500 + 599.42) / 5000 = 0.22
        assertEquals(0.22, result, 0.01, "DTI should match mathematically expected value");
    }

    @Test
    @DisplayName("Should return zero DTI when annual income is zero")
    void calculateModelDtiForScoring_zeroIncome() {
        double result = service.calculateModelDtiForScoring(0.0, 0.0, 20000.0, 5.0, 36);
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("Should return zero DTI when annual income is negative")
    void calculateModelDtiForScoring_negativeIncome() {
        double result = service.calculateModelDtiForScoring(-10000.0, 0.0, 20000.0, 5.0, 36);
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("Should use MIN_TERM_MONTHS when termMonths is null")
    void calculateModelDtiForScoring_nullTermMonths() {
        double result = service.calculateModelDtiForScoring(60000.0, 0.0, 20000.0, 5.0, null);
        assertTrue(result > 0);
    }

    // ========== calculateModelDtiForCreditCardScoring ==========

    @Test
    @DisplayName("Should calculate model DTI for revolving credit card")
    void calculateModelDtiForCreditCardScoring_revolving() {
        double result = service.calculateModelDtiForCreditCardScoring(60000.0, 6000.0, 5000.0, true);
        assertTrue(result > 0);
    }

    @Test
    @DisplayName("Should calculate model DTI for standard credit card")
    void calculateModelDtiForCreditCardScoring_standard() {
        double result = service.calculateModelDtiForCreditCardScoring(60000.0, 6000.0, 5000.0, false);
        assertTrue(result > 0);
        // Standard CC payment = creditLimit = 5000, so DTI should be higher
    }

    @Test
    @DisplayName("Should return zero DTI for CC when income is zero")
    void calculateModelDtiForCreditCardScoring_zeroIncome() {
        double result = service.calculateModelDtiForCreditCardScoring(0.0, 0.0, 5000.0, true);
        assertEquals(0.0, result);
    }

    // ========== calculateDtiWithExistingObligations ==========

    @Test
    @DisplayName("Should calculate DTI with existing obligations standard case")
    void calculateDtiWithExistingObligations_standardCase() {
        // monthlyPayment=500, annualIncome=60000 (monthlyIncome=5000),
        // existingObligations=200
        // DTI = (200+500)/5000 = 0.14
        double result = service.calculateDtiWithExistingObligations(500.0, 60000.0, 200.0);
        assertEquals(0.14, result, 0.001);
    }

    @Test
    @DisplayName("Should return zero DTI when monthly income is zero")
    void calculateDtiWithExistingObligations_zeroIncome() {
        double result = service.calculateDtiWithExistingObligations(500.0, 0.0, 200.0);
        assertEquals(0.0, result);
    }
}
