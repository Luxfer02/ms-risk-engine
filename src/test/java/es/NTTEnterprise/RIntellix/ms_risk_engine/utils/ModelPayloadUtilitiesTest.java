package es.NTTEnterprise.RIntellix.ms_risk_engine.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ModelPayloadUtilities}.
 * Covers delegation to EnumNormalizer/BooleanConverter, auto-detection logic, and interest rate normalization.
 * @date 27/08/2026
 */
@DisplayName("ModelPayloadUtilities Tests")
@ExtendWith(MockitoExtension.class)
class ModelPayloadUtilitiesTest {

    @Mock
    private EnumNormalizer enumNormalizer;

    @Mock
    private BooleanConverter booleanConverter;

    private ModelPayloadUtilities utilities;

    @BeforeEach
    void setUp() {
        utilities = new ModelPayloadUtilities(enumNormalizer, booleanConverter);
    }

    // ========== normalizeEnumToField ==========

    @Test
    @DisplayName("normalizeEnumToField should delegate to EnumNormalizer")
    void normalizeEnumToField_delegatesToEnumNormalizer() {
        when(enumNormalizer.normalizeToTitleCase("SOLTERO", true)).thenReturn("Soltero");

        String result = utilities.normalizeEnumToField("SOLTERO", true);

        assertEquals("Soltero", result);
        verify(enumNormalizer).normalizeToTitleCase("SOLTERO", true);
    }

    // ========== toModelBoolean ==========

    @Test
    @DisplayName("toModelBoolean should delegate to BooleanConverter")
    void toModelBoolean_delegatesToBooleanConverter() {
        when(booleanConverter.toModelBoolean(true)).thenReturn("Si");

        String result = utilities.toModelBoolean(true);

        assertEquals("Si", result);
        verify(booleanConverter).toModelBoolean(true);
    }

    // ========== normalizeEnumForField ==========

    @Test
    @DisplayName("normalizeEnumForField should detect spaces and use withSpaces=true")
    void normalizeEnumForField_detectsSpaces() {
        when(enumNormalizer.normalizeToTitleCaseWithSeparator("SECTOR PUBLICO", " ")).thenReturn("Sector Publico");

        String result = utilities.normalizeEnumForField("occupationSector", "SECTOR PUBLICO");

        assertEquals("Sector Publico", result);
        verify(enumNormalizer).normalizeToTitleCaseWithSeparator("SECTOR PUBLICO", " ");
    }

    @Test
    @DisplayName("normalizeEnumForField should detect no spaces and use withSpaces=false")
    void normalizeEnumForField_detectsUnderscores() {
        when(enumNormalizer.normalizeToTitleCaseWithSeparator("FORMACION_PROFESIONAL", "_")).thenReturn("Formacion_Profesional");

        String result = utilities.normalizeEnumForField("homeOwnership", "FORMACION_PROFESIONAL");

        assertEquals("Formacion_Profesional", result);
        verify(enumNormalizer).normalizeToTitleCaseWithSeparator("FORMACION_PROFESIONAL", "_");
    }

    @Test
    @DisplayName("normalizeEnumForField should return null for null value")
    void normalizeEnumForField_nullValue() {
        assertNull(utilities.normalizeEnumForField("field", null));
        verifyNoInteractions(enumNormalizer);
    }

    // ========== normalizeInterestRateToFraction ==========

    @Test
    @DisplayName("Should normalize 24.91% to 0.2491 fraction")
    void normalizeInterestRateToFraction_standard() {
        assertEquals(0.2491, utilities.normalizeInterestRateToFraction(24.91), 0.0001);
    }

    @Test
    @DisplayName("Should normalize 5% to 0.05 fraction")
    void normalizeInterestRateToFraction_five() {
        assertEquals(0.05, utilities.normalizeInterestRateToFraction(5.0), 0.0001);
    }

    @Test
    @DisplayName("Should return null for null interest rate")
    void normalizeInterestRateToFraction_null() {
        assertNull(utilities.normalizeInterestRateToFraction(null));
    }

    @Test
    @DisplayName("Should normalize 0% to 0.0")
    void normalizeInterestRateToFraction_zero() {
        assertEquals(0.0, utilities.normalizeInterestRateToFraction(0.0));
    }

    // ========== Constructor validation ==========

    @Test
    @DisplayName("Constructor should throw when EnumNormalizer is null")
    void constructor_shouldThrow_whenEnumNormalizerNull() {
        assertThrows(NullPointerException.class,
                () -> new ModelPayloadUtilities(null, booleanConverter));
    }

    @Test
    @DisplayName("Constructor should throw when BooleanConverter is null")
    void constructor_shouldThrow_whenBooleanConverterNull() {
        assertThrows(NullPointerException.class,
                () -> new ModelPayloadUtilities(enumNormalizer, null));
    }
}
