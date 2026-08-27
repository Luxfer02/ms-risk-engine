package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RequestType} enum.
 * Covers fromValue parsing, null/invalid handling, and getValue.
 * @date 27/08/2026
 */
@DisplayName("RequestType Enum Tests")
class RequestTypeTest {

    @Test
    @DisplayName("fromValue should parse PRESTAMO by value")
    void fromValue_shouldParse_PRESTAMO() {
        assertEquals(RequestType.PRESTAMO, RequestType.fromValue("PRESTAMO"));
    }

    @Test
    @DisplayName("fromValue should parse HIPOTECA by value")
    void fromValue_shouldParse_HIPOTECA() {
        assertEquals(RequestType.HIPOTECA, RequestType.fromValue("HIPOTECA"));
    }

    @Test
    @DisplayName("fromValue should parse TARJETA_CREDITO by value")
    void fromValue_shouldParse_TARJETA_CREDITO() {
        assertEquals(RequestType.TARJETA_CREDITO, RequestType.fromValue("TARJETA_CREDITO"));
    }

    @Test
    @DisplayName("fromValue should parse by enum name")
    void fromValue_shouldParse_byEnumName() {
        for (RequestType type : RequestType.values()) {
            assertEquals(type, RequestType.fromValue(type.name()));
        }
    }

    @Test
    @DisplayName("fromValue should throw IllegalArgumentException for null")
    void fromValue_shouldThrow_forNull() {
        assertThrows(IllegalArgumentException.class, () -> RequestType.fromValue(null));
    }

    @Test
    @DisplayName("fromValue should throw IllegalArgumentException for unknown value")
    void fromValue_shouldThrow_forUnknown() {
        assertThrows(IllegalArgumentException.class, () -> RequestType.fromValue("DESCONOCIDO"));
    }

    @Test
    @DisplayName("getValue should return correct value for each enum")
    void getValue_shouldReturnCorrectValue() {
        assertEquals("PRESTAMO", RequestType.PRESTAMO.getValue());
        assertEquals("HIPOTECA", RequestType.HIPOTECA.getValue());
        assertEquals("TARJETA_CREDITO", RequestType.TARJETA_CREDITO.getValue());
    }

    @Test
    @DisplayName("All enum values should be parseable via fromValue")
    void allValues_shouldBeParseable() {
        for (RequestType type : RequestType.values()) {
            assertEquals(type, RequestType.fromValue(type.getValue()));
        }
    }
}
