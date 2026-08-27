package es.NTTEnterprise.RIntellix.ms_risk_engine.application.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringGenerationPayload;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.output.ScoringModelExecutionResultDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.ScoringResultMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution.ScoringModelExecutionStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.ModelPredictionResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.Scoring;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.ports.output.ScoringResultPublisherPort;

/**
 * Unit tests for {@link ScoringProcessingService}.
 * Covers null payload guard, normal scoring flow, hard-cutoff model version override,
 * strategy delegation, Kafka publishing, and exception handling.
 * @date 27/08/2026
 */
@DisplayName("ScoringProcessingService Tests")
@ExtendWith(MockitoExtension.class)
class ScoringProcessingServiceTest {

    @Mock
    private ScoringModelExecutionStrategy strategy;

    @Mock
    private ScoringResultMapper scoringResultMapper;

    @Mock
    private ScoringResultPublisherPort scoringResultPublisher;

    private ScoringProcessingService service;

    private static final String MODEL_VERSION = "xgboost-v2.1";

    @BeforeEach
    void setUp() {
        lenient().when(strategy.supports("PRESTAMO")).thenReturn(true);
        service = new ScoringProcessingService(
                List.of(strategy), scoringResultMapper, scoringResultPublisher, MODEL_VERSION);
    }

    // ========== Null payload guard ==========

    @Test
    @DisplayName("Should return false when payload is null")
    void processScoringMessage_nullPayload_returnsFalse() {
        boolean result = service.processScoringMessage(null);

        assertFalse(result, "Null payload should return false");
        verifyNoInteractions(strategy, scoringResultMapper, scoringResultPublisher);
    }

    // ========== Normal scoring flow ==========

    @Test
    @DisplayName("Should process normal scoring and publish result returning true")
    void processScoringMessage_normalFlow_returnsTrue() {
        ScoringGenerationPayload payload = createPayload("REQ-1", "PRESTAMO");

        ScoringModelExecutionResultDTO executionResult = new ScoringModelExecutionResultDTO(
                Map.of("field", "value"),
                new ModelPredictionResult(0.15, "Medium", 0.5, List.of()),
                new RiskMetrics(),
                false);

        Scoring scoring = new Scoring();
        RiskMetrics scoringResults = new RiskMetrics();
        scoringResults.setProbabilityOfDefault(0.15);
        scoringResults.setRiskLevel("C");
        scoring.setResults(scoringResults);

        when(strategy.executePredictionModel(payload, "PRESTAMO", "REQ-1"))
                .thenReturn(executionResult);
        when(scoringResultMapper.toScoring(eq("REQ-1"), eq(MODEL_VERSION), any(), any(), any()))
                .thenReturn(scoring);

        boolean result = service.processScoringMessage(payload);

        assertTrue(result);
        verify(scoringResultPublisher).publishScoringResult(scoring);
    }

    // ========== Hard-cutoff flow ==========

    @Test
    @DisplayName("Should use RULE_ENGINE_v1 model version when hard-cutoff is triggered")
    void processScoringMessage_hardCutoff_usesRuleEngineVersion() {
        ScoringGenerationPayload payload = createPayload("REQ-2", "PRESTAMO");

        ScoringModelExecutionResultDTO executionResult = new ScoringModelExecutionResultDTO(
                Map.of("dti", 0.60),
                new ModelPredictionResult(1.0, "HIGH", 0.0, List.of()),
                new RiskMetrics(),
                true); // hardCutoff = true

        Scoring scoring = new Scoring();
        RiskMetrics scoringResults = new RiskMetrics();
        scoringResults.setExposureAtDefault(10000.0);
        scoringResults.setLossGivenDefault(0.45);
        scoringResults.setExpectedCalculatedLoss(4500.0);
        scoring.setResults(scoringResults);

        when(strategy.executePredictionModel(payload, "PRESTAMO", "REQ-2"))
                .thenReturn(executionResult);

        ArgumentCaptor<String> versionCaptor = ArgumentCaptor.forClass(String.class);
        when(scoringResultMapper.toScoring(eq("REQ-2"), versionCaptor.capture(), any(), any(), any()))
                .thenReturn(scoring);

        boolean result = service.processScoringMessage(payload);

        assertTrue(result);
        assertEquals("RULE_ENGINE_v1", versionCaptor.getValue(),
                "Hard-cutoff should use RULE_ENGINE_v1 model version");
    }

    @Test
    @DisplayName("Should use configured model version when result is NOT hard-cutoff")
    void processScoringMessage_notHardCutoff_usesConfiguredModelVersion() {
        ScoringGenerationPayload payload = createPayload("REQ-3", "PRESTAMO");

        ScoringModelExecutionResultDTO executionResult = new ScoringModelExecutionResultDTO(
                Map.of(), new ModelPredictionResult(0.05, "Low", 0.3, List.of()),
                new RiskMetrics(), false);

        Scoring scoring = new Scoring();
        scoring.setResults(new RiskMetrics());

        when(strategy.executePredictionModel(payload, "PRESTAMO", "REQ-3"))
                .thenReturn(executionResult);

        ArgumentCaptor<String> versionCaptor = ArgumentCaptor.forClass(String.class);
        when(scoringResultMapper.toScoring(eq("REQ-3"), versionCaptor.capture(), any(), any(), any()))
                .thenReturn(scoring);

        service.processScoringMessage(payload);

        assertEquals(MODEL_VERSION, versionCaptor.getValue());
    }

    // ========== Exception handling ==========

    @Test
    @DisplayName("Should return false and not publish when strategy throws RuntimeException")
    void processScoringMessage_strategyThrows_returnsFalse() {
        ScoringGenerationPayload payload = createPayload("REQ-4", "PRESTAMO");

        when(strategy.executePredictionModel(payload, "PRESTAMO", "REQ-4"))
                .thenThrow(new RuntimeException("Model timeout"));

        boolean result = service.processScoringMessage(payload);

        assertFalse(result);
        verifyNoInteractions(scoringResultPublisher);
    }

    @Test
    @DisplayName("Should return false when mapper throws RuntimeException")
    void processScoringMessage_mapperThrows_returnsFalse() {
        ScoringGenerationPayload payload = createPayload("REQ-5", "PRESTAMO");

        ScoringModelExecutionResultDTO executionResult = new ScoringModelExecutionResultDTO(
                Map.of(), new ModelPredictionResult(), new RiskMetrics(), false);

        when(strategy.executePredictionModel(payload, "PRESTAMO", "REQ-5"))
                .thenReturn(executionResult);
        when(scoringResultMapper.toScoring(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Mapping error"));

        boolean result = service.processScoringMessage(payload);

        assertFalse(result);
        verifyNoInteractions(scoringResultPublisher);
    }

    // ========== Constructor null guards ==========

    @Test
    @DisplayName("Constructor should throw NPE when strategies list is null")
    void constructor_nullStrategies_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> new ScoringProcessingService(null, scoringResultMapper, scoringResultPublisher, MODEL_VERSION));
    }

    @Test
    @DisplayName("Constructor should throw NPE when result mapper is null")
    void constructor_nullMapper_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> new ScoringProcessingService(List.of(strategy), null, scoringResultPublisher, MODEL_VERSION));
    }

    @Test
    @DisplayName("Constructor should throw NPE when publisher is null")
    void constructor_nullPublisher_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> new ScoringProcessingService(List.of(strategy), scoringResultMapper, null, MODEL_VERSION));
    }

    @Test
    @DisplayName("Constructor should throw NPE when model version is null")
    void constructor_nullModelVersion_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> new ScoringProcessingService(List.of(strategy), scoringResultMapper, scoringResultPublisher,
                        null));
    }

    // ========== Helper ==========

    private ScoringGenerationPayload createPayload(String requestId, String requestType) {
        // ScoringGenerationPayload is abstract, so we create a concrete anonymous subclass
        return new ScoringGenerationPayload(requestId, requestType) {
        };
    }
}
