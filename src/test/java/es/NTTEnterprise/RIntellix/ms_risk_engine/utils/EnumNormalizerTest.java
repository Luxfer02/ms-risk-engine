package es.NTTEnterprise.RIntellix.ms_risk_engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EnumNormalizer}.
 * Covers title case conversion with/without spaces, null/empty handling, and edge cases.
 * @date 27/08/2026
 */
@DisplayName("EnumNormalizer Tests")
class EnumNormalizerTest {

    private EnumNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new EnumNormalizer();
    }

    @Test
    @DisplayName("Should normalize single-word enum to title case with spaces")
    void shouldNormalize_singleWord_withSpaces() {
        assertEquals("Soltero", normalizer.normalizeToTitleCase("SOLTERO", true));
    }

    @Test
    @DisplayName("Should normalize multi-word enum to title case with spaces")
    void shouldNormalize_multiWord_withSpaces() {
        assertEquals("Formacion Profesional", normalizer.normalizeToTitleCase("FORMACION_PROFESIONAL", true));
    }

    @Test
    @DisplayName("Should normalize multi-word enum to title case with underscores")
    void shouldNormalize_multiWord_withUnderscores() {
        assertEquals("Formacion_Profesional", normalizer.normalizeToTitleCase("FORMACION_PROFESIONAL", false));
    }

    @Test
    @DisplayName("Should return null when value is null")
    void shouldReturnNull_whenNull() {
        assertNull(normalizer.normalizeToTitleCase(null, true));
    }

    @Test
    @DisplayName("Should return empty string when value is empty")
    void shouldReturnEmpty_whenEmpty() {
        assertEquals("", normalizer.normalizeToTitleCase("", true));
    }

    @Test
    @DisplayName("Should handle single character input")
    void shouldHandleSingleChar() {
        assertEquals("A", normalizer.normalizeToTitleCase("A", true));
    }

    @Test
    @DisplayName("Should handle already normalized value")
    void shouldHandleAlreadyNormalized() {
        assertEquals("Casado", normalizer.normalizeToTitleCase("Casado", true));
    }

    @Test
    @DisplayName("Should handle lowercase input")
    void shouldHandleLowercaseInput() {
        assertEquals("Soltero", normalizer.normalizeToTitleCase("soltero", true));
    }

    @Test
    @DisplayName("Should handle value with spaces as separator")
    void shouldHandleSpaceSeparator() {
        assertEquals("Sector Publico", normalizer.normalizeToTitleCase("SECTOR PUBLICO", true));
    }
}
