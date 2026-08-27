package es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringGenerationPayload;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringGenerationRequest;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.output.ScoringModelExecutionResultDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.LoanOrMortgageModelPayloadMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.usecases.RiskMetricsCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.ModelPredictionResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums.RequestType;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.DtiCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskMetricsCalculationContext;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskMetricsCalculationResult;

@ExtendWith(MockitoExtension.class)
class LoanOrMortgageScoringModelExecutionStrategyTest {

    @Mock
    private LoanOrMortgageModelPayloadMapper payloadMapper;

    @Mock
    private RiskMetricsCalculationService metricsCalculationService;

    @Mock
    private DtiCalculationService dtiCalculationService;

    private final String predictLoanPath = "/api/v1/predict/loan";

    private LoanOrMortgageScoringModelExecutionStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new LoanOrMortgageScoringModelExecutionStrategy(
                payloadMapper, metricsCalculationService, dtiCalculationService, predictLoanPath);
    }

    @Test
    void supports_ShouldReturnTrue_ForPrestamoAndHipoteca() {
        assertTrue(strategy.supports(RequestType.PRESTAMO.getValue()));
        assertTrue(strategy.supports(RequestType.HIPOTECA.getValue()));
    }

    @Test
    void supports_ShouldReturnFalse_ForTarjetaCreditoOrInvalid() {
        assertFalse(strategy.supports(RequestType.TARJETA_CREDITO.getValue()));
        assertFalse(strategy.supports("INVALID_TYPE"));
        assertFalse(strategy.supports(null));
    }

    @Test
    void modelEndpointPath_ShouldReturnConfiguredPath() {
        assertEquals(predictLoanPath, strategy.modelEndpointPath());
    }

    @Test
    void executePredictionModel_ShouldThrowException_WhenPayloadIsInvalid() {
        ScoringGenerationPayload invalidPayload = new ScoringGenerationPayload() {};

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            strategy.executePredictionModel(invalidPayload, "PRESTAMO", "req-123"));
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void executePredictionModel_ShouldReturnResult_WhenSuccessful() {
        // Arrange
        ScoringGenerationRequest request = mock(ScoringGenerationRequest.class);
        when(request.getAnnualIncome()).thenReturn(50000.0);
        when(request.getExistingObligations()).thenReturn(12000.0);
        when(request.getLoanAmount()).thenReturn(10000.0);
        when(request.getInterestRate()).thenReturn(5.0);
        when(request.getTermMonths()).thenReturn(60);
        when(request.getLtv()).thenReturn(80.0);

        when(dtiCalculationService.calculateModelDtiForScoring(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(30.0);
        
        Map<String, Object> payloadMap = Map.of("key", "value");
        when(payloadMapper.toModelPayload(request, 30.0)).thenReturn(payloadMap);

        RiskMetrics riskMetrics = mock(RiskMetrics.class);
        ModelPredictionResult prediction = mock(ModelPredictionResult.class);
        RiskMetricsCalculationResult calculationResult = new RiskMetricsCalculationResult(prediction, riskMetrics, false);
        
        when(metricsCalculationService.calculateRiskMetrics(any(RiskMetricsCalculationContext.class)))
                .thenReturn(calculationResult);

        // Act
        ScoringModelExecutionResultDTO result = strategy.executePredictionModel(request, "PRESTAMO", "req-123");

        // Assert
        assertNotNull(result);
        assertEquals(payloadMap, result.getModelRequestPayload());
        assertEquals(prediction, result.getPredictionResult());
        assertEquals(riskMetrics, result.getRiskMetrics());
        assertFalse(result.isHardCutoff());
        
        verify(dtiCalculationService).calculateModelDtiForScoring(50000.0, 12000.0, 10000.0, 5.0, 60);
        verify(payloadMapper).toModelPayload(request, 30.0);
        verify(metricsCalculationService).calculateRiskMetrics(any(RiskMetricsCalculationContext.class));
    }
}
