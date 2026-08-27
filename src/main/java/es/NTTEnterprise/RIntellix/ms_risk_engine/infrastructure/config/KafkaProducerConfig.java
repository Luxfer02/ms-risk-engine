package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

/**
 * Kafka producer configuration for scoring generation messages.
 *
 * Configures the KafkaTemplate bean for publishing polymorphic scoring
 * messages to Kafka topics. This template is used by ScoringKafkaProducer
 * to send both standard scoring generation requests and credit card specific
 * scoring requests using the strategy pattern.
 *
 * Supported message types:
 * - ScoringGenerationRequest (for loans and mortgages)
 * - CreditCardScoringGenerationRequest (for credit cards)
 *
 * Producer settings:
 * - Bootstrap servers: configured in application.properties
 * - Value serializer: JacksonJsonSerializer (polymorphic Object serialization)
 * - Retries: 3 (automatic retry on failure)
 * - Linger: 10ms (batch messages for efficiency)
 *
 * @author Lucía Fernández Mancebo
 * @date 15/03/2026
 */
@Configuration
@EnableKafka
public class KafkaProducerConfig {

    private final String bootstrapServers;
    private final String acks;
    private final int retries;
    private final int lingerMs;

    public KafkaProducerConfig(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.producer.acks}") String acks,
            @Value("${spring.kafka.producer.retries}") int retries,
            @Value("${spring.kafka.producer.properties.linger.ms}") int lingerMs) {
        this.bootstrapServers = Objects.requireNonNull(bootstrapServers);
        this.acks = Objects.requireNonNull(acks);
        this.retries = retries;
        this.lingerMs = lingerMs;
    }

    /**
     * Configures the ProducerFactory for KafkaTemplate.
     *
     * Uses Object as the value type to support polymorphic message sending.
     * This allows the same KafkaTemplate to send both ScoringGenerationDTO
     * and CreditCardScoringGenerationDTO objects.
     *
     * @return ProducerFactory configured with StringSerializer for keys
     *         and JacksonJsonSerializer for polymorphic Object values
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, acks);
        configProps.put(ProducerConfig.RETRIES_CONFIG, retries);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Configures the KafkaTemplate for sending messages to Kafka.
     *
     * Supports polymorphic message types (Object) for strategy-based
     * message publishing.
     *
     * @param producerFactory the producer factory bean
     * @return KafkaTemplate<String, Object> configured for polymorphic messages
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
