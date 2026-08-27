package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.config;

import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Core component: KafkaConsumerConfig.
 * Encapsulates the logic and responsibilities assigned to this element
 * within the Hexagonal Architecture, ensuring separation of concerns.
 *
 * @author Lucía Fernández Mancebo
 * @date 28/07/2026
 */
@Slf4j
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final String AUTO_OFFSET_RESET_EARLIEST = "earliest";
    private static final String TRUSTED_PACKAGES_ALL = "*";
    private static final int MAX_POLL_RECORDS = 100;
    private static final int SESSION_TIMEOUT_MS = 30000;
    private static final int SINGLE_CONSUMER_CONCURRENCY = 1;

    private final String bootstrapServers;
    private final String groupId;
    private final int maxRetryAttempts;
    private final long initialDelayMs;

    public KafkaConsumerConfig(
            @Value("${spring.kafka.bootstrap-servers}") final String bootstrapServers,
            @Value("${scoring.kafka.consumer.group-id}") final String groupId,
            @Value("${scoring.kafka.consumer.retry.max-attempts}") final int maxRetryAttempts,
            @Value("${scoring.kafka.consumer.retry.initial-delay}") final long initialDelayMs) {
        this.bootstrapServers = Objects.requireNonNull(bootstrapServers);
        this.groupId = Objects.requireNonNull(groupId);
        this.maxRetryAttempts = maxRetryAttempts;
        this.initialDelayMs = initialDelayMs;
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET_EARLIEST);
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, SESSION_TIMEOUT_MS);

        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JacksonJsonDeserializer.class.getName());
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
        configProps.put("spring.json.value.default.type", Object.class.getName());
        configProps.put("spring.json.trusted.packages", TRUSTED_PACKAGES_ALL);
        configProps.put("spring.json.use.type.headers", false);

        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setCommonErrorHandler(kafkaErrorHandler());
        factory.setConcurrency(SINGLE_CONSUMER_CONCURRENCY);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public KafkaListenerConfigurer kafkaListenerConfigurer(final LocalValidatorFactoryBean validator) {
        return registrar -> {
            final DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
            factory.setValidator(validator);
            factory.afterPropertiesSet();
            registrar.setMessageHandlerMethodFactory(factory);
        };
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor(final LocalValidatorFactoryBean validator) {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator);
        return processor;
    }

    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        FixedBackOff backoff = new FixedBackOff(initialDelayMs, maxRetryAttempts);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, exception) -> {
            if (exception instanceof ListenerExecutionFailedException
                    && exception.getCause() instanceof MethodArgumentNotValidException validationEx) {
                validationEx.getBindingResult().getAllErrors().forEach(error -> log.warn(
                        LogMessage.KAFKA_CONSUMER_VALIDATION_REJECTED,
                        error.getObjectName(),
                        error.getDefaultMessage()));
            } else {
                log.error(LogMessage.KAFKA_CONSUMER_ERROR, consumerRecord, exception);
            }
        }, backoff);

        errorHandler.addNotRetryableExceptions(MethodArgumentNotValidException.class);
        return errorHandler;
    }
}
