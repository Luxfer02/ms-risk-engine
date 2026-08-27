package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.Scoring;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.SimulationDelta;

/**
 * Unit tests for {@link SimulationDeltaCalculator}.
 * Uses black-box and white-box testing strategies, edge conditions, outliers and branches.
 * @date 27/08/2026
 */
@DisplayName("SimulationDeltaCalculator Tests")
class SimulationDeltaCalculatorTest {

    private SimulationDeltaCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new SimulationDeltaCalculator();
    }

    @Test
    @DisplayName("White Box - Normal Flow: Should calculate correct delta between base and simulated metrics")
    void calculateDelta_normalFlow() {
        // Base scoring setup
        Scoring baseScoring = new Scoring();
        RiskMetrics baseMetrics = new RiskMetrics();
        baseMetrics.setProbabilityOfDefault(0.05);
        baseMetrics.setExpectedCalculatedLoss(100.0);
        baseMetrics.setLossGivenDefault(0.40);
        baseMetrics.setExposureAtDefault(10000.0);
        baseMetrics.setRiskLevel("C");
        
        FinancialMetrics baseFinMetrics = new FinancialMetrics();
        baseFinMetrics.setMonthlyPayment(300.0);
        baseFinMetrics.setDebtToIncomeRatio(0.30);
        baseFinMetrics.setTotalPayment(10800.0);
        baseFinMetrics.setTotalInterest(800.0);
        baseFinMetrics.setMonthlyDisposableIncome(2000.0);
        baseMetrics.setFinancialMetrics(baseFinMetrics);
        baseScoring.setResults(baseMetrics);

        // Simulated metrics setup
        RiskMetrics simMetrics = new RiskMetrics();
        simMetrics.setProbabilityOfDefault(0.08);
        simMetrics.setExpectedCalculatedLoss(150.0);
        simMetrics.setLossGivenDefault(0.35);
        simMetrics.setExposureAtDefault(9000.0);
        simMetrics.setRiskLevel("D");

        FinancialMetrics simFinMetrics = new FinancialMetrics();
        simFinMetrics.setMonthlyPayment(400.0);
        simFinMetrics.setDebtToIncomeRatio(0.40);
        simFinMetrics.setTotalPayment(14400.0);
        simFinMetrics.setTotalInterest(1400.0);
        simFinMetrics.setMonthlyDisposableIncome(1900.0);
        simMetrics.setFinancialMetrics(simFinMetrics);

        SimulationDelta delta = calculator.calculateDelta(baseScoring, simMetrics);

        assertEquals(0.03, delta.getPdChange(), 0.001); 
        assertEquals(50.0, delta.getEclChange(), 0.001); 
        assertEquals(-0.05, delta.getLgdChange(), 0.001); 
        assertEquals(-1000.0, delta.getEadChange(), 0.001); 
        assertEquals("C -> D", delta.getRiskGradeChange());
        assertEquals(100.0, delta.getMonthlyPaymentChange(), 0.001); 
        assertEquals(0.10, delta.getDtiChange(), 0.001); 
        assertEquals(3600.0, delta.getTotalPaymentChange(), 0.001); 
        assertEquals(600.0, delta.getTotalInterestChange(), 0.001); 
        assertEquals(-100.0, delta.getMonthlyDisposableIncomeChange(), 0.001); 
    }

    @Test
    @DisplayName("Edge Case - Missing Base Financial Metrics")
    void calculateDelta_missingBaseFinancialMetrics() {
        Scoring baseScoring = new Scoring();
        RiskMetrics baseMetrics = new RiskMetrics();
        baseMetrics.setProbabilityOfDefault(0.05);
        baseMetrics.setRiskLevel("A");
        baseScoring.setResults(baseMetrics);

        RiskMetrics simMetrics = new RiskMetrics();
        simMetrics.setProbabilityOfDefault(0.05);
        simMetrics.setRiskLevel("A");

        FinancialMetrics simFinMetrics = new FinancialMetrics();
        simFinMetrics.setMonthlyPayment(400.0);
        simFinMetrics.setDebtToIncomeRatio(0.20);
        simFinMetrics.setTotalPayment(14400.0);
        simFinMetrics.setTotalInterest(400.0);
        simFinMetrics.setMonthlyDisposableIncome(1000.0);
        simMetrics.setFinancialMetrics(simFinMetrics);

        SimulationDelta delta = calculator.calculateDelta(baseScoring, simMetrics);

        assertEquals(400.0, delta.getMonthlyPaymentChange(), 0.001);
        assertEquals(0.20, delta.getDtiChange(), 0.001);
        assertEquals(14400.0, delta.getTotalPaymentChange(), 0.001);
        assertEquals(400.0, delta.getTotalInterestChange(), 0.001);
        assertEquals(1000.0, delta.getMonthlyDisposableIncomeChange(), 0.001);
    }

    @Test
    @DisplayName("Edge Case - Missing Simulated Financial Metrics")
    void calculateDelta_missingSimulatedFinancialMetrics() {
        Scoring baseScoring = new Scoring();
        RiskMetrics baseMetrics = new RiskMetrics();
        
        FinancialMetrics baseFinMetrics = new FinancialMetrics();
        baseFinMetrics.setMonthlyPayment(300.0);
        baseFinMetrics.setTotalPayment(10800.0);
        baseFinMetrics.setTotalInterest(800.0);
        baseFinMetrics.setMonthlyDisposableIncome(2000.0);
        baseFinMetrics.setDebtToIncomeRatio(0.30);
        baseMetrics.setFinancialMetrics(baseFinMetrics);
        baseScoring.setResults(baseMetrics);

        RiskMetrics simMetrics = new RiskMetrics();

        SimulationDelta delta = calculator.calculateDelta(baseScoring, simMetrics);

        assertEquals(-300.0, delta.getMonthlyPaymentChange(), 0.001);
        assertEquals(-0.30, delta.getDtiChange(), 0.001);
        assertEquals(-10800.0, delta.getTotalPaymentChange(), 0.001);
        assertEquals(-800.0, delta.getTotalInterestChange(), 0.001);
        assertEquals(-2000.0, delta.getMonthlyDisposableIncomeChange(), 0.001);
    }

    @Test
    @DisplayName("Outliers - Extreme large values")
    void calculateDelta_extremeLargeValues() {
        Scoring baseScoring = new Scoring();
        RiskMetrics baseMetrics = new RiskMetrics();
        FinancialMetrics baseFinMetrics = new FinancialMetrics();
        baseFinMetrics.setMonthlyPayment(1_000_000_000.0);
        baseMetrics.setFinancialMetrics(baseFinMetrics);
        baseScoring.setResults(baseMetrics);

        RiskMetrics simMetrics = new RiskMetrics();
        FinancialMetrics simFinMetrics = new FinancialMetrics();
        simFinMetrics.setMonthlyPayment(2_000_000_000.0);
        simMetrics.setFinancialMetrics(simFinMetrics);

        SimulationDelta delta = calculator.calculateDelta(baseScoring, simMetrics);

        assertEquals(1_000_000_000.0, delta.getMonthlyPaymentChange(), 0.001);
    }

    @Test
    @DisplayName("Outliers - Negative values and NaN tolerance")
    void calculateDelta_negativeAndNan() {
        Scoring baseScoring = new Scoring();
        RiskMetrics baseMetrics = new RiskMetrics();
        FinancialMetrics baseFinMetrics = new FinancialMetrics();
        baseFinMetrics.setMonthlyPayment(-500.0);
        baseMetrics.setFinancialMetrics(baseFinMetrics);
        baseScoring.setResults(baseMetrics);

        RiskMetrics simMetrics = new RiskMetrics();
        FinancialMetrics simFinMetrics = new FinancialMetrics();
        simFinMetrics.setMonthlyPayment(Double.NaN);
        simMetrics.setFinancialMetrics(simFinMetrics);

        SimulationDelta delta = calculator.calculateDelta(baseScoring, simMetrics);

        // MathUtilities.calculateDelta handles NaN by returning NaN
        assertEquals(Double.NaN, delta.getMonthlyPaymentChange());
    }

    @Test
    @DisplayName("Zero Change - Identity check")
    void calculateDelta_zeroChange() {
        Scoring baseScoring = new Scoring();
        RiskMetrics baseMetrics = new RiskMetrics();
        FinancialMetrics baseFinMetrics = new FinancialMetrics();
        baseFinMetrics.setMonthlyPayment(300.0);
        baseMetrics.setFinancialMetrics(baseFinMetrics);
        baseScoring.setResults(baseMetrics);

        RiskMetrics simMetrics = new RiskMetrics();
        FinancialMetrics simFinMetrics = new FinancialMetrics();
        simFinMetrics.setMonthlyPayment(300.0);
        simMetrics.setFinancialMetrics(simFinMetrics);

        SimulationDelta delta = calculator.calculateDelta(baseScoring, simMetrics);

        assertEquals(0.0, delta.getMonthlyPaymentChange(), 0.001);
    }
}
