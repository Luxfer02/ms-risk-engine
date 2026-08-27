package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.input.kafka.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringGenerationPayload;

class StandardScoringGenerationMessageStrategyTest {

    private StandardScoringGenerationMessageStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new StandardScoringGenerationMessageStrategy();
    }

    @Test
    @DisplayName("Should support PRESTAMO and HIPOTECA")
    void supports_true() {
        assertTrue(strategy.supports("PRESTAMO"));
        assertTrue(strategy.supports("HIPOTECA"));
    }

    @Test
    @DisplayName("Should not support other types or null")
    void supports_false() {
        assertFalse(strategy.supports("TARJETA_CREDITO"));
        assertFalse(strategy.supports(null));
        assertFalse(strategy.supports("UNKNOWN"));
    }

    @Test
    @DisplayName("Should map payload successfully")
    void map_success() {
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("requestId", "REQ-1");
        payloadMap.put("loanAmount", 15000.0);

        ScoringGenerationPayload payload = strategy.map(payloadMap);

        assertNotNull(payload);
    }
}
