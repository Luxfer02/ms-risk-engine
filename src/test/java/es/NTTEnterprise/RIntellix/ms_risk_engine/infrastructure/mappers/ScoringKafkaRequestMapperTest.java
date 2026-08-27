package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.CreditCardScoringGenerationRequest;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;

/**
 * Unit tests for {@link ScoringKafkaRequestMapper}.
 * Covers generic payload to DTO mapping and request type extraction.
 * @date 27/08/2026
 */
@DisplayName("ScoringKafkaRequestMapper Tests")
class ScoringKafkaRequestMapperTest {

    @Test
    @DisplayName("toType should map Map payload to target DTO class")
    void toType_mapsMapToDto() {
        Map<String, Object> payload = Map.of(
            "age", 30,
            "annualIncome", 50000.0,
            "creditLimit", 10000.0
        );

        CreditCardScoringGenerationRequest result = ScoringKafkaRequestMapper.toType(payload, CreditCardScoringGenerationRequest.class);

        assertNotNull(result);
        assertEquals(30, result.getAge());
        assertEquals(50000.0, result.getAnnualIncome());
        assertEquals(10000.0, result.getCreditLimit());
    }

    @Test
    @DisplayName("toType should cast if payload is already instance of target type")
    void toType_castsIfInstance() {
        CreditCardScoringGenerationRequest payload = new CreditCardScoringGenerationRequest();
        payload.setAge(40);

        CreditCardScoringGenerationRequest result = ScoringKafkaRequestMapper.toType(payload, CreditCardScoringGenerationRequest.class);

        assertSame(payload, result);
    }

    @Test
    @DisplayName("toType should throw IllegalArgumentException when payload is null")
    void toType_nullPayload() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> ScoringKafkaRequestMapper.toType(null, CreditCardScoringGenerationRequest.class));
        assertEquals(LogMessage.KAFKA_PAYLOAD_NULL_ERROR, ex.getMessage());
    }

    @Test
    @DisplayName("toType should throw NullPointerException when targetType is null")
    void toType_nullTargetType() {
        NullPointerException ex = assertThrows(NullPointerException.class, 
            () -> ScoringKafkaRequestMapper.toType(Map.of(), null));
        assertEquals(LogMessage.TARGET_TYPE_NULL_ERROR, ex.getMessage());
    }

    @Test
    @DisplayName("extractRequestType should return normalized request type")
    void extractRequestType_returnsNormalized() {
        Map<String, Object> payload = Map.of("requestType", "  prestamo  ");
        
        String result = ScoringKafkaRequestMapper.extractRequestType(payload);
        
        assertEquals("PRESTAMO", result);
    }

    @Test
    @DisplayName("extractRequestType should return null if requestType not found")
    void extractRequestType_notFound() {
        Map<String, Object> payload = Map.of("otherKey", "value");
        
        String result = ScoringKafkaRequestMapper.extractRequestType(payload);
        
        assertNull(result);
    }

    @Test
    @DisplayName("extractRequestType should return null if payload is null")
    void extractRequestType_nullPayload() {
        assertNull(ScoringKafkaRequestMapper.extractRequestType(null));
    }

    @Test
    @DisplayName("Should not be instantiable - utility class")
    void shouldNotBeInstantiable() throws Exception {
        var constructor = ScoringKafkaRequestMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
    }
}
