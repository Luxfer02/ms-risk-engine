package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.ModelPredictionResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.output.dtos.ModelPredictionResponseDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.output.handler.ModelPredictionErrorHandler;

@ExtendWith(MockitoExtension.class)
class ModelPredictionAdapterTest {

    @Mock
    private ModelPredictionErrorHandler errorHandler;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ModelPredictionAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ModelPredictionAdapter("http://localhost:8080", errorHandler);
        ReflectionTestUtils.setField(adapter, "webClient", webClient);
    }

    @Test
    @DisplayName("Should successfully return prediction result")
    void predictAsync_success() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("field", "value");

        ModelPredictionResponseDTO responseDTO = new ModelPredictionResponseDTO();
        responseDTO.setProbabilityOfDefault(0.05);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        org.mockito.Mockito.doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ModelPredictionResponseDTO.class)).thenReturn(reactor.core.publisher.Mono.just(responseDTO));

        CompletableFuture<ModelPredictionResult> future = adapter.predictAsync(payload, "REQ-1", "/predict");
        ModelPredictionResult result = future.get();

        assertNotNull(result);
        assertEquals(0.05, result.getProbabilityOfDefault());
    }

    @Test
    @DisplayName("Should fail when payload is null")
    void predictAsync_nullPayload() throws Exception {
        CompletableFuture<ModelPredictionResult> future = adapter.predictAsync(null, "REQ-1", "/predict");
        
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Should fail when endpoint is null")
    void predictAsync_nullEndpoint() throws Exception {
        CompletableFuture<ModelPredictionResult> future = adapter.predictAsync(new HashMap<>(), "REQ-1", null);
        
        assertTrue(future.isCompletedExceptionally());
    }
}
