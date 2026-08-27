package es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers;

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
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadUtilities;

/**
 * Unit tests for {@link SimulationModelPayloadMapper}.
 * Covers normalization of base variables and form changes, delegating to Mocked
 * dependencies.
 * @date 27/08/2026
 */
@DisplayName("SimulationModelPayloadMapper Tests")
@ExtendWith(MockitoExtension.class)
class SimulationModelPayloadMapperTest {

    @Mock
    private ModelPayloadUtilities payloadUtilities;

    private SimulationModelPayloadMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SimulationModelPayloadMapper(payloadUtilities);
    }

    @Test
    @DisplayName("normalizeBaseVariables should process all entries")
    void normalizeBaseVariables_processesAll() {
        Map<String, Object> base = Map.of("loanAmount", 10000.0, "interestRate", 5.0);

        when(payloadUtilities.normalizeInterestRateToFraction(5.0)).thenReturn(0.05);

        Map<String, Object> result = mapper.normalizeVariables(base);

        assertEquals(10000.0, result.get("loanAmount"));
        assertEquals(0.05, result.get("interestRate"));

        verify(payloadUtilities).normalizeInterestRateToFraction(5.0);
    }

    @Test
    @DisplayName("normalizeFormChangesToCamelcase should handle empty map")
    void normalizeFormChangesToCamelcase_emptyMap() {
        Map<String, Object> result = mapper.normalizeVariables(new HashMap<>());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("normalizeValue should convert booleans")
    void normalizeValue_booleans() {
        Map<String, Object> changes = Map.of("hasMortgage", true);

        when(payloadUtilities.toModelBoolean(true)).thenReturn("Si");

        Map<String, Object> result = mapper.normalizeVariables(changes);

        assertEquals("Si", result.get("hasMortgage"));
    }

    @Test
    @DisplayName("normalizeValue should normalize enums")
    void normalizeValue_enums() {
        Map<String, Object> changes = Map.of("gender", "HOMBRE");

        when(payloadUtilities.normalizeEnumForField("gender", "HOMBRE")).thenReturn("Hombre");

        Map<String, Object> result = mapper.normalizeVariables(changes);

        assertEquals("Hombre", result.get("gender"));
    }

    @Test
    @DisplayName("normalizeVariables should return empty map on null")
    void normalizeBaseVariables_null() {
        Map<String, Object> result = mapper.normalizeVariables(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("synchronizeDependentFields should add incomeType when employmentStatus is Indefinido")
    void synchronizeDependentFields_indefinido() {
        // Enums will be already normalized when synchronizeDependentFields is called
        Map<String, Object> changes = new HashMap<>();
        changes.put("employmentStatus", "INDEFINIDO");

        when(payloadUtilities.normalizeEnumForField("employmentStatus", "INDEFINIDO")).thenReturn("Indefinido");

        Map<String, Object> result = mapper.normalizeVariables(changes);

        assertEquals("Indefinido", result.get("employmentStatus"));
        assertEquals("Salario", result.get("incomeType"));
    }

    @Test
    @DisplayName("synchronizeDependentFields should add incomeType when employmentStatus is Autonomo")
    void synchronizeDependentFields_autonomo() {
        Map<String, Object> changes = new HashMap<>();
        changes.put("employmentStatus", "AUTONOMO");

        when(payloadUtilities.normalizeEnumForField("employmentStatus", "AUTONOMO")).thenReturn("Autonomo");

        Map<String, Object> result = mapper.normalizeVariables(changes);

        assertEquals("Autonomo", result.get("employmentStatus"));
        assertEquals("Autonomo", result.get("incomeType"));
    }

    @Test
    @DisplayName("synchronizeDependentFields should ignore unknown employmentStatus")
    void synchronizeDependentFields_unknown() {
        Map<String, Object> changes = new HashMap<>();
        changes.put("employmentStatus", "UNKNOWN");

        when(payloadUtilities.normalizeEnumForField("employmentStatus", "UNKNOWN")).thenReturn("Unknown");

        Map<String, Object> result = mapper.normalizeVariables(changes);

        assertEquals("Unknown", result.get("employmentStatus"));
        assertNull(result.get("incomeType"));
    }
}
