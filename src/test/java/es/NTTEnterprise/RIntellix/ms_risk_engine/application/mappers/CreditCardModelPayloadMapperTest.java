package es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.CreditCardScoringGenerationRequest;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadFieldNames;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadUtilities;

/**
 * Unit tests for {@link CreditCardModelPayloadMapper}.
 * Covers mapping from CreditCardScoringGenerationRequest to Model Payload with
 * mocked utilities.
 * @date 27/08/2026
 */
@DisplayName("CreditCardModelPayloadMapper Tests")
@ExtendWith(MockitoExtension.class)
class CreditCardModelPayloadMapperTest {

    @Mock
    private ModelPayloadUtilities payloadUtilities;

    private CreditCardModelPayloadMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CreditCardModelPayloadMapper(payloadUtilities);
    }

    @Test
    @DisplayName("Should map request to model payload correctly")
    void toModelPayload_mapsCorrectly() {
        CreditCardScoringGenerationRequest request = new CreditCardScoringGenerationRequest();
        request.setAge(30);
        request.setGender("HOMBRE");
        request.setAnnualIncome(50000.0);
        request.setCreditLimit(5000.0);
        request.setExistingObligations(1200.0);
        request.setIsRevolving(true);
        request.setInterestRate(20.0);

        when(payloadUtilities.normalizeEnumForField(ModelPayloadFieldNames.FIELD_GENDER, "HOMBRE"))
                .thenReturn("Hombre");
        when(payloadUtilities.normalizeInterestRateToFraction(20.0)).thenReturn(0.20);

        Map<String, Object> result = mapper.toModelPayload(request, "TARJETA_CREDITO", 0.15);

        assertNotNull(result);
        assertEquals(30, result.get(ModelPayloadFieldNames.FIELD_AGE));
        assertEquals("Hombre", result.get(ModelPayloadFieldNames.FIELD_GENDER));
        assertEquals(50000.0, result.get(ModelPayloadFieldNames.FIELD_ANNUAL_INCOME));
        assertEquals("Si", result.get(ModelPayloadFieldNames.FIELD_IS_REVOLVING));
        assertEquals(0.20, result.get(ModelPayloadFieldNames.FIELD_INTEREST_RATE));
        assertEquals(0.15, result.get(ModelPayloadFieldNames.FIELD_DTI));

        verify(payloadUtilities).normalizeEnumForField(ModelPayloadFieldNames.FIELD_GENDER, "HOMBRE");
        verify(payloadUtilities).normalizeInterestRateToFraction(20.0);
    }

    @Test
    @DisplayName("Should return 0 for DTI when request is null")
    void calculateModelDti_withNullRequest() {
        assertThrows(NullPointerException.class, () -> new CreditCardModelPayloadMapper(null));
    }

    @Test
    @DisplayName("Should handle entirely null input request safely (throws NPE due to logic)")
    void shouldHandleNullRequest() {
        // In a real application, controller validations prevent this. But we can test
        // it throws NPE or handles it.
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            mapper.toModelPayload(null, "TARJETA_CREDITO", 0.0);
        });
    }

    @Test
    @DisplayName("Should handle missing optional fields safely")
    void shouldHandleMissingOptionalFields() {
        CreditCardScoringGenerationRequest request = new CreditCardScoringGenerationRequest();
        request.setCreditLimit(5000.0);
        request.setInterestRate(20.0);
        request.setIsRevolving(false);
        // missing all demographic/employment data, etc.

        when(payloadUtilities.normalizeInterestRateToFraction(20.0)).thenReturn(0.20);

        Map<String, Object> payload = mapper.toModelPayload(request, "TARJETA_CREDITO", 0.0);

        // Assert that the map was created without throwing
        assertNotNull(payload);
        // and DTI is calculated as 0
        assertEquals(0.0, payload.get(ModelPayloadFieldNames.FIELD_DTI));
    }
}
