package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.input.kafka;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringGenerationPayload;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.ports.input.ScoringProcessingPortService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.input.kafka.strategy.ScoringGenerationMessageStrategy;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScoringKafkaConsumer Tests")
class ScoringKafkaConsumerTest {

    @Mock
    private ScoringProcessingPortService scoringProcessingService;

    @Mock
    private ScoringGenerationMessageStrategy strategy;

    @Mock
    private Acknowledgment acknowledgment;

    @Mock
    private ScoringGenerationPayload mappedPayload;

    private ScoringKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        List<ScoringGenerationMessageStrategy> strategies = Arrays.asList(strategy);
        consumer = new ScoringKafkaConsumer(scoringProcessingService, strategies);
    }

    @Test
    @DisplayName("Should successfully consume, process and acknowledge message")
    void consumeScoring_happyPath() {
        // Arrange
        Map<String, Object> messageContent = new LinkedHashMap<>();
        messageContent.put("requestType", "TARJETA_CREDITO");
        
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("GenerateScoring", 0, 0, "key1", messageContent);

        when(strategy.supports("TARJETA_CREDITO")).thenReturn(true);
        when(strategy.map(messageContent)).thenReturn(mappedPayload);
        when(scoringProcessingService.processScoringMessage(mappedPayload)).thenReturn(true);

        // Act
        consumer.consumeScoring(record, acknowledgment);

        // Assert
        verify(scoringProcessingService).processScoringMessage(mappedPayload);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should throw IllegalStateException if processing fails")
    void consumeScoring_processingFails() {
        // Arrange
        Map<String, Object> messageContent = new LinkedHashMap<>();
        messageContent.put("requestType", "TARJETA_CREDITO");
        
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("GenerateScoring", 0, 0, "key1", messageContent);

        when(strategy.supports("TARJETA_CREDITO")).thenReturn(true);
        when(strategy.map(messageContent)).thenReturn(mappedPayload);
        when(scoringProcessingService.processScoringMessage(mappedPayload)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> consumer.consumeScoring(record, acknowledgment));
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if request_type is missing")
    void consumeScoring_missingRequestType() {
        // Arrange
        Map<String, Object> messageContent = new LinkedHashMap<>();
        // No requestType
        
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("GenerateScoring", 0, 0, "key1", messageContent);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> consumer.consumeScoring(record, acknowledgment));
        verify(scoringProcessingService, never()).processScoringMessage(any());
        verify(acknowledgment, never()).acknowledge();
    }
}
