package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.output;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.output.ScoringResultMessageDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.ScoringResultMessageDTOMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.Scoring;

@ExtendWith(MockitoExtension.class)
class ScoringKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ScoringResultMessageDTOMapper mapper;

    private ScoringKafkaProducer producer;

    @BeforeEach
    void setUp() {
        producer = new ScoringKafkaProducer(kafkaTemplate, mapper, "test-topic");
    }

    @Test
    @DisplayName("Should do nothing when scoring is null")
    void publishScoringResult_null() {
        producer.publishScoringResult(null);
        verify(kafkaTemplate, never()).send(any(Message.class));
    }

    @Test
    @DisplayName("Should publish scoring successfully")
    void publishScoringResult_success() {
        Scoring scoring = new Scoring();
        scoring.setRequestId("REQ-1");
        
        ScoringResultMessageDTO dto = new ScoringResultMessageDTO();
        
        when(mapper.toDTO(scoring)).thenReturn(dto);
        when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));

        assertDoesNotThrow(() -> producer.publishScoringResult(scoring));
        verify(kafkaTemplate).send(any(Message.class));
    }

    @Test
    @DisplayName("Should throw runtime exception on kafka failure")
    void publishScoringResult_failure() {
        Scoring scoring = new Scoring();
        scoring.setRequestId("REQ-1");
        
        ScoringResultMessageDTO dto = new ScoringResultMessageDTO();
        
        when(mapper.toDTO(scoring)).thenReturn(dto);
        
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new ExecutionException("Kafka error", new RuntimeException()));
        when(kafkaTemplate.send(any(Message.class))).thenReturn(future);

        assertThrows(RuntimeException.class, () -> producer.publishScoringResult(scoring));
    }

    @Test
    @DisplayName("Should throw runtime exception on interruption")
    void publishScoringResult_interrupted() {
        Scoring scoring = new Scoring();
        scoring.setRequestId("REQ-1");
        
        ScoringResultMessageDTO dto = new ScoringResultMessageDTO();
        
        when(mapper.toDTO(scoring)).thenReturn(dto);
        
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        try {
            when(future.get()).thenThrow(new InterruptedException("Interrupted"));
        } catch (Exception e) {}
        
        when(kafkaTemplate.send(any(Message.class))).thenReturn(future);

        assertThrows(RuntimeException.class, () -> producer.publishScoringResult(scoring));
    }
}
