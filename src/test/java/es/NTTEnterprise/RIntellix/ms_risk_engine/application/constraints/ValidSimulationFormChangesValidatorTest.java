package es.NTTEnterprise.RIntellix.ms_risk_engine.application.constraints;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

/**
 * Unit tests for {@link ValidSimulationFormChangesValidator}.
 * Covers empty/null maps, valid fields, and invalid fields.
 * @date 27/08/2026
 */
@DisplayName("ValidSimulationFormChangesValidator Tests")
@ExtendWith(MockitoExtension.class)
class ValidSimulationFormChangesValidatorTest {

    private ValidSimulationFormChangesValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
        validator = new ValidSimulationFormChangesValidator();
    }

    @Test
    @DisplayName("Should return true when map is null")
    void shouldReturnTrue_whenMapIsNull() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    @DisplayName("Should return true when map is empty")
    void shouldReturnTrue_whenMapIsEmpty() {
        assertTrue(validator.isValid(new HashMap<>(), context));
    }

    @Test
    @DisplayName("Should return true when all fields are allowed")
    void shouldReturnTrue_whenAllFieldsAllowed() {
        Map<String, Object> changes = new HashMap<>();
        changes.put("loanAmount", 10000.0);
        changes.put("interestRate", 5.0);

        assertTrue(validator.isValid(changes, context));
        verifyNoInteractions(context);
    }

    @Test
    @DisplayName("Should return false and build violation when unsupported fields are present")
    void shouldReturnFalse_whenUnsupportedFieldsPresent() {
        Map<String, Object> changes = new HashMap<>();
        changes.put("loanAmount", 10000.0);
        changes.put("unsupportedField", "value");

        when(context.getDefaultConstraintMessageTemplate()).thenReturn("Invalid changes");
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        assertFalse(validator.isValid(changes, context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Invalid changes: [unsupportedField]");
        verify(builder).addConstraintViolation();
    }
}
