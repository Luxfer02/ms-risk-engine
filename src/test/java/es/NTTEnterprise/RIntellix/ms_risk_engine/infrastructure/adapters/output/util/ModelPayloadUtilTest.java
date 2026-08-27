package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.output.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModelPayloadUtilTest {

    @Test
    @DisplayName("Should throw IllegalArgumentException when payload is null")
    void validatePayloadNotNull_null() {
        assertThrows(IllegalArgumentException.class, () -> ModelPayloadUtil.validatePayloadNotNull(null));
    }

    @Test
    @DisplayName("Should pass when payload is not null")
    void validatePayloadNotNull_notNull() {
        assertDoesNotThrow(() -> ModelPayloadUtil.validatePayloadNotNull(new HashMap<>()));
    }
}
