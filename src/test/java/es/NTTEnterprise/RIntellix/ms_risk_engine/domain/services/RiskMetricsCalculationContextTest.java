package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RiskMetricsCalculationContextTest {

    @Test
    @DisplayName("Should create successfully when all required fields are provided")
    void create_success() {
        assertDoesNotThrow(() -> new RiskMetricsCalculationContext(
                new HashMap<>(), "REQ-1", "/predict", "PRESTAMO", false,
                1000.0, 0.8, 50000.0, 12, 5.0, 100.0));
    }

    @Test
    @DisplayName("Should throw NullPointerException when modelPayload is null")
    void create_nullPayload() {
        assertThrows(NullPointerException.class, () -> new RiskMetricsCalculationContext(
                null, "REQ-1", "/predict", "PRESTAMO", false,
                1000.0, 0.8, 50000.0, 12, 5.0, 100.0));
    }

    @Test
    @DisplayName("Should throw NullPointerException when requestId is null")
    void create_nullRequestId() {
        assertThrows(NullPointerException.class, () -> new RiskMetricsCalculationContext(
                new HashMap<>(), null, "/predict", "PRESTAMO", false,
                1000.0, 0.8, 50000.0, 12, 5.0, 100.0));
    }

    @Test
    @DisplayName("Should throw NullPointerException when endpoint is null")
    void create_nullEndpoint() {
        assertThrows(NullPointerException.class, () -> new RiskMetricsCalculationContext(
                new HashMap<>(), "REQ-1", null, "PRESTAMO", false,
                1000.0, 0.8, 50000.0, 12, 5.0, 100.0));
    }

    @Test
    @DisplayName("Should throw NullPointerException when requestType is null")
    void create_nullRequestType() {
        assertThrows(NullPointerException.class, () -> new RiskMetricsCalculationContext(
                new HashMap<>(), "REQ-1", "/predict", null, false,
                1000.0, 0.8, 50000.0, 12, 5.0, 100.0));
    }

    @Test
    @DisplayName("Should retrieve fields correctly")
    void getFields() {
        RiskMetricsCalculationContext ctx = new RiskMetricsCalculationContext(
                new HashMap<>(), "REQ-1", "/predict", "PRESTAMO", false,
                1000.0, 0.8, 50000.0, 12, 5.0, 100.0);

        assertEquals("REQ-1", ctx.requestId());
        assertEquals("PRESTAMO", ctx.requestType());
        assertEquals(1000.0, ctx.principalAmount());
    }
}
