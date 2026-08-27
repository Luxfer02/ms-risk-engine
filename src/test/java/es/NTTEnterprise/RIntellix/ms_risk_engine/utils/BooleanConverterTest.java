package es.NTTEnterprise.RIntellix.ms_risk_engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BooleanConverter}.
 * Covers true/false/null conversion to model-expected "Si"/"No" format.
 * @date 27/08/2026
 */
@DisplayName("BooleanConverter Tests")
class BooleanConverterTest {

    private BooleanConverter converter;

    @BeforeEach
    void setUp() {
        converter = new BooleanConverter();
    }

    @Test
    @DisplayName("Should convert true to 'Si'")
    void shouldConvertTrue_toYes() {
        assertEquals(ModelPayloadConstants.BOOLEAN_VALUE_YES, converter.toModelBoolean(true));
    }

    @Test
    @DisplayName("Should convert false to 'No'")
    void shouldConvertFalse_toNo() {
        assertEquals(ModelPayloadConstants.BOOLEAN_VALUE_NO, converter.toModelBoolean(false));
    }

    @Test
    @DisplayName("Should return null when input is null")
    void shouldReturnNull_whenNull() {
        assertNull(converter.toModelBoolean(null));
    }
}
