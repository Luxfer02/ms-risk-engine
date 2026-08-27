package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.constants.RiskCalculationDefaults;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums.RiskGrade;

/**
 * Unit tests for {@link RiskGradeCalculator}.
 * Covers all 7 risk grade branches (A-G) plus boundary values.
 * @date 27/08/2026
 */
@DisplayName("RiskGradeCalculator Tests")
class RiskGradeCalculatorTest {

    private RiskGradeCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new RiskGradeCalculator();
    }

    // ========== Grade A ==========

    @Test
    @DisplayName("Should return grade A when PD is below threshold A")
    void shouldReturnGradeA_whenPdBelowThresholdA() {
        double pd = RiskCalculationDefaults.PD_THRESHOLD_GRADE_A - 0.0001;
        assertEquals(RiskGrade.A, calculator.calculateRiskGrade(pd));
    }

    @Test
    @DisplayName("Should return grade A when PD is zero")
    void shouldReturnGradeA_whenPdIsZero() {
        assertEquals(RiskGrade.A, calculator.calculateRiskGrade(0.0));
    }

    // ========== Grade B ==========

    @Test
    @DisplayName("Should return grade B when PD is between threshold A and B")
    void shouldReturnGradeB_whenPdBetweenAandB() {
        double pd = (RiskCalculationDefaults.PD_THRESHOLD_GRADE_A + RiskCalculationDefaults.PD_THRESHOLD_GRADE_B) / 2;
        assertEquals(RiskGrade.B, calculator.calculateRiskGrade(pd));
    }

    @Test
    @DisplayName("Should return grade B when PD is exactly at threshold A")
    void shouldReturnGradeB_whenPdExactlyAtThresholdA() {
        assertEquals(RiskGrade.B, calculator.calculateRiskGrade(RiskCalculationDefaults.PD_THRESHOLD_GRADE_A));
    }

    // ========== Grade C ==========

    @Test
    @DisplayName("Should return grade C when PD is between threshold B and C")
    void shouldReturnGradeC_whenPdBetweenBandC() {
        double pd = (RiskCalculationDefaults.PD_THRESHOLD_GRADE_B + RiskCalculationDefaults.PD_THRESHOLD_GRADE_C) / 2;
        assertEquals(RiskGrade.C, calculator.calculateRiskGrade(pd));
    }

    @Test
    @DisplayName("Should return grade C when PD is exactly at threshold B")
    void shouldReturnGradeC_whenPdExactlyAtThresholdB() {
        assertEquals(RiskGrade.C, calculator.calculateRiskGrade(RiskCalculationDefaults.PD_THRESHOLD_GRADE_B));
    }

    // ========== Grade D ==========

    @Test
    @DisplayName("Should return grade D when PD is between threshold C and D")
    void shouldReturnGradeD_whenPdBetweenCandD() {
        double pd = (RiskCalculationDefaults.PD_THRESHOLD_GRADE_C + RiskCalculationDefaults.PD_THRESHOLD_GRADE_D) / 2;
        assertEquals(RiskGrade.D, calculator.calculateRiskGrade(pd));
    }

    // ========== Grade E ==========

    @Test
    @DisplayName("Should return grade E when PD is between threshold D and E")
    void shouldReturnGradeE_whenPdBetweenDandE() {
        double pd = (RiskCalculationDefaults.PD_THRESHOLD_GRADE_D + RiskCalculationDefaults.PD_THRESHOLD_GRADE_E) / 2;
        assertEquals(RiskGrade.E, calculator.calculateRiskGrade(pd));
    }

    // ========== Grade F ==========

    @Test
    @DisplayName("Should return grade F when PD is between threshold E and F")
    void shouldReturnGradeF_whenPdBetweenEandF() {
        double pd = (RiskCalculationDefaults.PD_THRESHOLD_GRADE_E + RiskCalculationDefaults.PD_THRESHOLD_GRADE_F) / 2;
        assertEquals(RiskGrade.F, calculator.calculateRiskGrade(pd));
    }

    // ========== Grade G ==========

    @Test
    @DisplayName("Should return grade G when PD is above threshold F")
    void shouldReturnGradeG_whenPdAboveThresholdF() {
        double pd = RiskCalculationDefaults.PD_THRESHOLD_GRADE_F + 0.01;
        assertEquals(RiskGrade.G, calculator.calculateRiskGrade(pd));
    }

    @Test
    @DisplayName("Should return grade G when PD is exactly at threshold F")
    void shouldReturnGradeG_whenPdExactlyAtThresholdF() {
        assertEquals(RiskGrade.G, calculator.calculateRiskGrade(RiskCalculationDefaults.PD_THRESHOLD_GRADE_F));
    }

    @Test
    @DisplayName("Should return grade G when PD is 1.0 (maximum)")
    void shouldReturnGradeG_whenPdIsOne() {
        assertEquals(RiskGrade.G, calculator.calculateRiskGrade(1.0));
    }
}
