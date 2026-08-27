package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.input.kafka;

import java.util.List;
import java.util.Objects;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringGenerationPayload;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.ports.input.ScoringProcessingPortService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.input.kafka.strategy.ScoringGenerationMessageStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.mappers.ScoringKafkaRequestMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.input.kafka.factories.ScoringKafkaMessageStrategyFactory;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer adapter for receiving and processing scoring generation
 * requests.
 * Listens to the GenerateScoring topic in standalone mode.
 * Validates incoming messages, processes them through the scoring pipeline,
 * and sends manual acknowledgment.
 * Implements error handling with retry policy.
 * 
 * @author Lucía Fernández Mancebo
 * @date 21/03/2026
 */
@Slf4j
@Component
@Validated
public class ScoringKafkaConsumer {

    private final ScoringProcessingPortService scoringProcessingService;
    private final List<ScoringGenerationMessageStrategy> strategies;

    /**
     * Constructs a ScoringKafkaConsumer with the required service dependency.
     * 
     * @param scoringProcessingService the application service for processing
     *                                 scoring messages.
     * @param strategies               strategy list for payload type resolution
     *                                 and mapping.
     */
    public ScoringKafkaConsumer(
            final ScoringProcessingPortService scoringProcessingService,
            final List<ScoringGenerationMessageStrategy> strategies) {
        this.scoringProcessingService = Objects.requireNonNull(scoringProcessingService);
        this.strategies = Objects.requireNonNull(strategies);
    }

    /**
     * Consumes scoring generation messages from the Kafka topic and processes
     * them through the scoring pipeline. Acknowledges the message manually
     * after successful processing.
     * 
     * @param record         the incoming Kafka consumer record containing the
     *                       scoring generation request details
     *                       (can be of different types following the Strategy
     *                       Pattern for type-specific processing).
     * @param acknowledgment the Kafka acknowledgment object for manual message
     *                       acknowledgment.
     */
    @KafkaListener(topics = "${scoring.kafka.topic.generation}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeScoring(
            final ConsumerRecord<String, Object> record,
            final Acknowledgment acknowledgment) {

        final Object message = record.value();
        log.info(LogMessage.KAFKA_MESSAGE_RECEIVED,
                record.key(), record.topic(), record.offset());

        // Map the incoming message to the appropiate domain request using the strategy
        // pattern
        ScoringGenerationPayload mappedMessage = mapWithStrategy(message);
        log.info(LogMessage.RECEIVED_REQUEST, mappedMessage.toString());

        final boolean processed = scoringProcessingService.processScoringMessage(mappedMessage);
        if (!processed) {
            throw new IllegalStateException(LogMessage.KAFKA_MESSAGE_PROCESSING_FAILED);
        }

        acknowledgment.acknowledge();
        log.info(LogMessage.KAFKA_MESSAGE_PROCESSED);

    }

    /**
     * Auxiliar method that extracts the real message received by kafka
     * queue by converting all the message content in different
     * types of messages based on the type of the request.
     * 
     */
    private ScoringGenerationPayload mapWithStrategy(final Object message) {
        final String requestType = ScoringKafkaRequestMapper.extractRequestType(message);
        if (requestType == null) {
            throw new IllegalArgumentException(LogMessage.REQUEST_TYPE_IS_REQUIRED);
        }

        ScoringGenerationMessageStrategy strategy = ScoringKafkaMessageStrategyFactory.selectStrategy(requestType, strategies);
        return strategy.map(message);
    }
}
