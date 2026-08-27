package es.NTTEnterprise.RIntellix.ms_risk_engine.application.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.ModelPredictionResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.HardCutoffRejection;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.ports.output.ModelPredictionPort;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.FinancialMetricsCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.HardCutoffRuleEvaluator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskGradeCalculator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskMetricsCalculationContext;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskMetricsCalculationResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation.RiskCalculationStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadFieldNames;

/**
 * Unit tests for {@link RiskMetricsCalculationService}.
 * Covers normal model invocation flow, hard-cutoff bypass, parallel computation,
 * null payload fields, financial metrics attachment, and constructor null guards.
 * @date 27/08/2026
 */
@DisplayName("RiskMetricsCalculationService Tests")
@ExtendWith(MockitoExtension.class)
class RiskMetricsCalculationServiceTest {

    @Mock
    private ModelPredictionPort modelPredictionPort;

    @Mock
    private RiskCalculationStrategy riskCalculationStrategy;

    @Mock
    private RiskGradeCalculator riskGradeCalculator;

    @Mock
    private FinancialMetricsCalculationService financialMetricsCalculationService;

    @Mock
    private HardCutoffRuleEvaluator hardCutoffRuleEvaluator;

    private RiskMetricsCalculationService service;

    @BeforeEach
    void setUp() {
        lenient().when(riskCalculationStrategy.supports(anyString(), anyBoolean())).thenReturn(true);
        service = new RiskMetricsCalculationService(
                modelPredictionPort,
                List.of(riskCalculationStrategy),
                riskGradeCalculator,
                financialMetricsCalculationService,
                hardCutoffRuleEvaluator);
    }

    // ========== Normal flow: model invocation ==========

    @Test
    @DisplayName("Should invoke model async and assemble full metrics for PRESTAMO")
    void calculateRiskMetrics_normalFlow_invokesModelAndAssemblesMetrics() {
        Map<String, Object> payload = buildLoanPayload(20000.0, 5.0, 36, 50000.0, null);
        RiskMetricsCalculationContext context = new RiskMetricsCalculationContext(
                payload, "REQ-1", "/api/v1/risk/predict-loan", "PRESTAMO", false, 20000.0, null, 50000.0, 36, 5.0, 0.0);

        // No hard-cutoff
        when(hardCutoffRuleEvaluator.evaluateRules(payload, "PRESTAMO", "REQ-1"))
                .thenReturn(Optional.empty());

        // Model returns PD=0.10
        ModelPredictionResult prediction = new ModelPredictionResult(0.10, "Low", 0.3, List.of());
        when(modelPredictionPort.predictAsync(payload, "REQ-1", "/api/v1/risk/predict-loan"))
                .thenReturn(CompletableFuture.completedFuture(prediction));

        // Pre-PD metrics
        RiskMetrics prePdMetrics = new RiskMetrics();
        prePdMetrics.setExposureAtDefault(20000.0);
        prePdMetrics.setLossGivenDefault(0.70);
        when(riskCalculationStrategy.calculatePrePdMetrics(20000.0, null))
                .thenReturn(prePdMetrics);

        // Assembled full metrics
        RiskMetrics fullMetrics = new RiskMetrics();
        fullMetrics.setProbabilityOfDefault(0.10);
        fullMetrics.setExposureAtDefault(20000.0);
        fullMetrics.setLossGivenDefault(0.70);
        fullMetrics.setExpectedCalculatedLoss(1400.0);
        fullMetrics.setRiskLevel("E");
        when(riskCalculationStrategy.assembleFullMetricsWithGradeCalculator(
                eq(0.10), eq(prePdMetrics), eq(20000.0), eq(50000.0), eq(36), eq(5.0),
                eq(riskGradeCalculator)))
                .thenReturn(fullMetrics);

        // Financial metrics
        FinancialMetrics financialMetrics = new FinancialMetrics();
        financialMetrics.setMonthlyPayment(600.0);
        when(financialMetricsCalculationService.calculateFinancialMetrics(
                eq("PRESTAMO"), eq(false), eq(20000.0), eq(5.0), eq(36), eq(50000.0), eq(0.0)))
                .thenReturn(financialMetrics);

        RiskMetricsCalculationResult result = service.calculateRiskMetrics(context);

        assertNotNull(result);
        assertFalse(result.isHardCutoff());
        assertEquals(prediction, result.modelPredictionResult());
        assertEquals(0.10, result.riskMetrics().getProbabilityOfDefault());
        assertNotNull(result.riskMetrics().getFinancialMetrics());
        verify(modelPredictionPort).predictAsync(any(), any(), any());
    }

    // ========== Hard-cutoff flow ==========

    @Test
    @DisplayName("Should skip model invocation when hard-cutoff is triggered and set PD=1.0")
    void calculateRiskMetrics_hardCutoff_skipModelAndSetPdOne() {
        Map<String, Object> payload = buildLoanPayload(20000.0, 5.0, 36, 50000.0, null);
        payload.put(ModelPayloadFieldNames.FIELD_DTI, 0.60); // Exceeds threshold
        RiskMetricsCalculationContext context = new RiskMetricsCalculationContext(
                payload, "REQ-2", "/api/v1/risk/predict-loan", "PRESTAMO", false, 20000.0, null, 50000.0, 36, 5.0, 0.0);

        // Hard-cutoff triggered
        HardCutoffRejection rejection = new HardCutoffRejection("dti", 0.60, List.of());
        when(hardCutoffRuleEvaluator.evaluateRules(payload, "PRESTAMO", "REQ-2"))
                .thenReturn(Optional.of(rejection));

        // Pre-PD metrics
        RiskMetrics prePdMetrics = new RiskMetrics();
        prePdMetrics.setExposureAtDefault(20000.0);
        prePdMetrics.setLossGivenDefault(0.70);
        when(riskCalculationStrategy.calculatePrePdMetrics(20000.0, null))
                .thenReturn(prePdMetrics);

        // Full metrics assembled with PD=1.0
        RiskMetrics fullMetrics = new RiskMetrics();
        fullMetrics.setProbabilityOfDefault(1.0);
        fullMetrics.setRiskLevel("G");
        when(riskCalculationStrategy.assembleFullMetricsWithGradeCalculator(
                eq(1.0), eq(prePdMetrics), eq(20000.0), eq(50000.0), eq(36), eq(5.0),
                eq(riskGradeCalculator)))
                .thenReturn(fullMetrics);

        FinancialMetrics financialMetrics = new FinancialMetrics();
        when(financialMetricsCalculationService.calculateFinancialMetrics(
                any(), anyBoolean(), any(), anyDouble(), any(), any(), anyDouble()))
                .thenReturn(financialMetrics);

        RiskMetricsCalculationResult result = service.calculateRiskMetrics(context);

        assertTrue(result.isHardCutoff());
        assertEquals(1.0, result.modelPredictionResult().getProbabilityOfDefault());
        verifyNoInteractions(modelPredictionPort);
    }

    // ========== Credit card with revolving ==========

    @Test
    @DisplayName("Should resolve amount from credit limit when loan amount is absent")
    void calculateRiskMetrics_creditCard_resolvesAmountFromCreditLimit() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_CREDIT_LIMIT, 5000.0);
        payload.put(ModelPayloadFieldNames.FIELD_IS_REVOLVING, "Si");
        payload.put(ModelPayloadFieldNames.FIELD_ANNUAL_INCOME, 40000.0);
        payload.put(ModelPayloadFieldNames.FIELD_INTEREST_RATE, 20.0);

        RiskMetricsCalculationContext context = new RiskMetricsCalculationContext(
                payload, "REQ-3", "/api/v1/risk/predict-credit-card", "TARJETA_CREDITO", true, 5000.0, null, 40000.0, null, 20.0, 0.0);

        when(hardCutoffRuleEvaluator.evaluateRules(any(), any(), any()))
                .thenReturn(Optional.empty());

        ModelPredictionResult prediction = new ModelPredictionResult(0.25, "Medium", 0.4, List.of());
        when(modelPredictionPort.predictAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(prediction));

        RiskMetrics prePdMetrics = new RiskMetrics();
        when(riskCalculationStrategy.calculatePrePdMetrics(5000.0, null))
                .thenReturn(prePdMetrics);

        RiskMetrics fullMetrics = new RiskMetrics();
        when(riskCalculationStrategy.assembleFullMetricsWithGradeCalculator(
                any(), any(), eq(5000.0), eq(40000.0), isNull(), eq(20.0), any()))
                .thenReturn(fullMetrics);

        FinancialMetrics financialMetrics = new FinancialMetrics();
        when(financialMetricsCalculationService.calculateFinancialMetrics(
                eq("TARJETA_CREDITO"), eq(true), eq(5000.0), eq(20.0), isNull(), eq(40000.0), eq(0.0)))
                .thenReturn(financialMetrics);

        RiskMetricsCalculationResult result = service.calculateRiskMetrics(context);

        assertNotNull(result);
        assertFalse(result.isHardCutoff());
    }

    // ========== Existing obligations ==========

    @Test
    @DisplayName("Should calculate existing monthly obligations as annual/12")
    void calculateRiskMetrics_withExistingObligations_dividesBy12() {
        Map<String, Object> payload = buildLoanPayload(10000.0, 3.0, 24, 60000.0, 6000.0);
        RiskMetricsCalculationContext context = new RiskMetricsCalculationContext(
                payload, "REQ-4", "/api/v1/risk/predict-loan", "PRESTAMO", false, 10000.0, null, 60000.0, 24, 3.0, 500.0);

        when(hardCutoffRuleEvaluator.evaluateRules(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(modelPredictionPort.predictAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(new ModelPredictionResult(0.05, "Low", 0.2, List.of())));

        RiskMetrics prePdMetrics = new RiskMetrics();
        when(riskCalculationStrategy.calculatePrePdMetrics(any(), any())).thenReturn(prePdMetrics);

        RiskMetrics fullMetrics = new RiskMetrics();
        when(riskCalculationStrategy.assembleFullMetricsWithGradeCalculator(
                any(), any(), any(), any(), any(), any(), any())).thenReturn(fullMetrics);

        FinancialMetrics financialMetrics = new FinancialMetrics();
        when(financialMetricsCalculationService.calculateFinancialMetrics(
                eq("PRESTAMO"), eq(false), eq(10000.0), eq(3.0), eq(24), eq(60000.0), eq(500.0)))
                .thenReturn(financialMetrics);

        service.calculateRiskMetrics(context);

        // Verify existing obligations = 6000 / 12 = 500
        verify(financialMetricsCalculationService).calculateFinancialMetrics(
                "PRESTAMO", false, 10000.0, 3.0, 24, 60000.0, 500.0);
    }

    // ========== Null context guard ==========

    @Test
    @DisplayName("Should throw NPE when context is null")
    void calculateRiskMetrics_nullContext_throwsNPE() {
        assertThrows(NullPointerException.class, () -> service.calculateRiskMetrics(null));
    }

    // ========== Null interest rate handling ==========

    @Test
    @DisplayName("Should default interest rate to 0.0 when null in payload")
    void calculateRiskMetrics_nullInterestRate_defaultsToZero() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, 15000.0);
        payload.put(ModelPayloadFieldNames.FIELD_ANNUAL_INCOME, 45000.0);
        payload.put(ModelPayloadFieldNames.FIELD_TERM_MONTHS, 36.0);

        RiskMetricsCalculationContext context = new RiskMetricsCalculationContext(
                payload, "REQ-5", "/api/v1/risk/predict-loan", "PRESTAMO", false, 15000.0, null, 45000.0, 36, null, 0.0);

        when(hardCutoffRuleEvaluator.evaluateRules(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(modelPredictionPort.predictAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(new ModelPredictionResult(0.08, "Low", 0.3, List.of())));

        RiskMetrics prePdMetrics = new RiskMetrics();
        when(riskCalculationStrategy.calculatePrePdMetrics(any(), any())).thenReturn(prePdMetrics);

        RiskMetrics fullMetrics = new RiskMetrics();
        when(riskCalculationStrategy.assembleFullMetricsWithGradeCalculator(
                any(), any(), any(), any(), any(), isNull(), any())).thenReturn(fullMetrics);

        FinancialMetrics financialMetrics = new FinancialMetrics();
        when(financialMetricsCalculationService.calculateFinancialMetrics(
                any(), anyBoolean(), any(), eq(0.0), any(), any(), anyDouble()))
                .thenReturn(financialMetrics);

        service.calculateRiskMetrics(context);

        verify(financialMetricsCalculationService).calculateFinancialMetrics(
                "PRESTAMO", false, 15000.0, 0.0, 36, 45000.0, 0.0);
    }

    // ========== Constructor null guards ==========

    @Test
    @DisplayName("Constructor should throw NPE when modelPredictionPort is null")
    void constructor_nullModelPredictionPort_throwsNPE() {
        assertThrows(NullPointerException.class, () -> new RiskMetricsCalculationService(
                null, List.of(), riskGradeCalculator, financialMetricsCalculationService, hardCutoffRuleEvaluator));
    }

    @Test
    @DisplayName("Constructor should throw NPE when strategies list is null")
    void constructor_nullStrategies_throwsNPE() {
        assertThrows(NullPointerException.class, () -> new RiskMetricsCalculationService(
                modelPredictionPort, null, riskGradeCalculator, financialMetricsCalculationService,
                hardCutoffRuleEvaluator));
    }

    @Test
    @DisplayName("Constructor should throw NPE when riskGradeCalculator is null")
    void constructor_nullRiskGradeCalculator_throwsNPE() {
        assertThrows(NullPointerException.class, () -> new RiskMetricsCalculationService(
                modelPredictionPort, List.of(), null, financialMetricsCalculationService, hardCutoffRuleEvaluator));
    }

    @Test
    @DisplayName("Constructor should throw NPE when financialMetricsCalculationService is null")
    void constructor_nullFinancialMetricsService_throwsNPE() {
        assertThrows(NullPointerException.class, () -> new RiskMetricsCalculationService(
                modelPredictionPort, List.of(), riskGradeCalculator, null, hardCutoffRuleEvaluator));
    }

    @Test
    @DisplayName("Constructor should throw NPE when hardCutoffRuleEvaluator is null")
    void constructor_nullHardCutoffEvaluator_throwsNPE() {
        assertThrows(NullPointerException.class, () -> new RiskMetricsCalculationService(
                modelPredictionPort, List.of(), riskGradeCalculator, financialMetricsCalculationService, null));
    }

    // ========== Helpers ==========

    private Map<String, Object> buildLoanPayload(Double loanAmount, Double interestRate,
            Integer termMonths, Double annualIncome, Double existingObligations) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, loanAmount);
        payload.put(ModelPayloadFieldNames.FIELD_INTEREST_RATE, interestRate);
        if (termMonths != null) {
            payload.put(ModelPayloadFieldNames.FIELD_TERM_MONTHS, (double) termMonths);
        }
        payload.put(ModelPayloadFieldNames.FIELD_ANNUAL_INCOME, annualIncome);
        if (existingObligations != null) {
            payload.put(ModelPayloadFieldNames.FIELD_EXISTING_OBLIGATIONS, existingObligations);
        }
        return payload;
    }
}
