package es.NTTEnterprise.RIntellix.ms_risk_engine.application.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.ModelPredictionResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.ports.output.ModelPredictionPort;

@ExtendWith(MockitoExtension.class)
class ScoringModelInvocationServiceTest {

    @Mock
    private ModelPredictionPort modelPredictionPort;

    private ScoringModelInvocationService service;

    @BeforeEach
    void setUp() {
        service = new ScoringModelInvocationService(modelPredictionPort);
    }

    @Test
    @DisplayName("Should invoke prediction successfully")
    void invokePrediction_success() {
        Map<String, Object> payload = new HashMap<>();
        when(modelPredictionPort.predictAsync(any(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(new ModelPredictionResult()));

        CompletableFuture<ModelPredictionResult> result = service.invokePrediction(payload, "REQ-1", "/predict");

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw NullPointerException when payload is null")
    void invokePrediction_nullPayload() {
        assertThrows(NullPointerException.class, () -> service.invokePrediction(null, "REQ-1", "/predict"));
    }

    @Test
    @DisplayName("Should throw NullPointerException when requestId is null")
    void invokePrediction_nullRequestId() {
        assertThrows(NullPointerException.class, () -> service.invokePrediction(new HashMap<>(), null, "/predict"));
    }

    @Test
    @DisplayName("Should throw NullPointerException when endpoint is null")
    void invokePrediction_nullEndpoint() {
        assertThrows(NullPointerException.class, () -> service.invokePrediction(new HashMap<>(), "REQ-1", null));
    }

    @Test
    @DisplayName("Should throw NullPointerException when initialized with null port")
    void constructor_nullPort() {
        assertThrows(NullPointerException.class, () -> new ScoringModelInvocationService(null));
    }
}
