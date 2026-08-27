package es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.CreditCardScoringGenerationRequest;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringGenerationRequest;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.output.ScoringModelExecutionResultDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.CreditCardModelPayloadMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.usecases.RiskMetricsCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.ModelPredictionResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums.RequestType;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.DtiCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskMetricsCalculationContext;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskMetricsCalculationResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditCardScoringModelExecutionStrategy Tests")
class CreditCardScoringModelExecutionStrategyTest {

    @Mock
    private CreditCardModelPayloadMapper payloadMapper;

    @Mock
    private RiskMetricsCalculationService metricsCalculationService;

    @Mock
    private DtiCalculationService dtiCalculationService;

    private CreditCardScoringModelExecutionStrategy strategy;

    private static final String PREDICT_PATH = "/api/v1/predict/credit-card";

    @BeforeEach
    void setUp() {
        strategy = new CreditCardScoringModelExecutionStrategy(
                payloadMapper,
                metricsCalculationService,
                dtiCalculationService,
                PREDICT_PATH);
    }

    @Test
    @DisplayName("Should return true for TARJETA_CREDITO request type")
    void supports_withTarjetaCredito() {
        assertTrue(strategy.supports(RequestType.TARJETA_CREDITO.getValue()));
    }

    @Test
    @DisplayName("Should return false for PRESTAMO request type")
    void supports_withPrestamoPersonal() {
        assertFalse(strategy.supports(RequestType.PRESTAMO.getValue()));
    }

    @Test
    @DisplayName("Should return false for invalid request type")
    void supports_withInvalidType() {
        assertFalse(strategy.supports("INVALID_TYPE"));
    }

    @Test
    @DisplayName("Should throw exception if payload is not CreditCardScoringGenerationRequest")
    void executePredictionModel_withInvalidPayload() {
        ScoringGenerationRequest invalidPayload = new ScoringGenerationRequest();

        assertThrows(IllegalArgumentException.class,
                () -> strategy.executePredictionModel(invalidPayload, "TARJETA_CREDITO", "req-123"));
    }

    @Test
    @DisplayName("Should orchestrate the prediction and calculation correctly")
    void executePredictionModel_happyPath() {
        CreditCardScoringGenerationRequest request = new CreditCardScoringGenerationRequest();
        request.setAnnualIncome(60000.0);
        request.setExistingObligations(1500.0);
        request.setCreditLimit(10000.0);
        request.setIsRevolving(true);

        String requestType = "TARJETA_CREDITO";
        String requestId = "req-123";

        // Mock DTI calculation
        double expectedDti = 0.30;
        when(dtiCalculationService.calculateModelDtiForCreditCardScoring(
                60000.0, 1500.0, 10000.0, true)).thenReturn(expectedDti);

        // Mock Payload Mapper
        Map<String, Object> mockPayload = new HashMap<>();
        mockPayload.put("dti", expectedDti);
        when(payloadMapper.toModelPayload(request, requestType, expectedDti)).thenReturn(mockPayload);

        // Mock Metrics Calculation
        RiskMetrics mockRiskMetrics = new RiskMetrics();
        mockRiskMetrics.setProbabilityOfDefault(0.05);

        ModelPredictionResult mockPrediction = new ModelPredictionResult(0.05, "LOW", 500.0, null);

        RiskMetricsCalculationResult mockResult = new RiskMetricsCalculationResult(
                mockPrediction, mockRiskMetrics, false);

        when(metricsCalculationService.calculateRiskMetrics(any(RiskMetricsCalculationContext.class)))
                .thenReturn(mockResult);

        // Execute
        ScoringModelExecutionResultDTO result = strategy.executePredictionModel(request, requestType, requestId);

        // Assertions
        assertNotNull(result);
        assertEquals(mockPayload, result.getModelRequestPayload());
        assertEquals(mockPrediction, result.getPredictionResult());
        assertEquals(mockRiskMetrics, result.getRiskMetrics());
        assertFalse(result.isHardCutoff());

        // Verifications
        verify(dtiCalculationService).calculateModelDtiForCreditCardScoring(60000.0, 1500.0, 10000.0, true);
        verify(payloadMapper).toModelPayload(request, requestType, expectedDti);
        verify(metricsCalculationService).calculateRiskMetrics(argThat(context ->
                context.requestType().equals(requestType) &&
                context.requestId().equals(requestId) &&
                context.modelEndpointPath().equals(PREDICT_PATH) &&
                context.modelPayload().equals(mockPayload)
        ));
    }
}
