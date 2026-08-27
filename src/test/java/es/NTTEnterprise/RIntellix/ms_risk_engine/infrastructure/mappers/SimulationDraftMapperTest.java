package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.CalculateSimulationDraftRequestDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.output.SimulationDraftResponseDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.FormChanges;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.SimulationDelta;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.SimulationDraft;

/**
 * Unit tests for {@link SimulationDraftMapper}.
 * Covers domain/DTO mapping for requests and responses.
 * @date 27/08/2026
 */
@DisplayName("SimulationDraftMapper Tests")
class SimulationDraftMapperTest {

    private SimulationDraftMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SimulationDraftMapper();
    }

    @Test
    @DisplayName("toFormChanges should map from request DTO")
    void toFormChanges_mapsFromRequest() {
        CalculateSimulationDraftRequestDTO request = new CalculateSimulationDraftRequestDTO();
        request.setFormChanges(Map.of("loanAmount", 15000.0));

        FormChanges result = mapper.toFormChanges(request);

        assertNotNull(result);
        assertEquals(15000.0, result.getValues().get("loanAmount"));
    }

    @Test
    @DisplayName("toFormChanges should return empty FormChanges when request is null")
    void toFormChanges_nullRequest() {
        FormChanges result = mapper.toFormChanges(null);

        assertNotNull(result);
        assertTrue(result.getValues().isEmpty());
    }

    @Test
    @DisplayName("toApiResponse should map full SimulationDraft to DTO")
    void toApiResponse_mapsFullDraft() {
        SimulationDraft draft = new SimulationDraft();
        draft.setFormChanges(new FormChanges(Map.of("loanAmount", 20000.0)));
        
        RiskMetrics metrics = new RiskMetrics();
        metrics.setProbabilityOfDefault(0.04);
        metrics.setRiskLevel("B");
        
        FinancialMetrics finMetrics = new FinancialMetrics();
        finMetrics.setMonthlyPayment(500.0);
        metrics.setFinancialMetrics(finMetrics);
        draft.setSimulatedResults(metrics);
        
        SimulationDelta delta = new SimulationDelta();
        delta.setPdChange(0.01);
        delta.setLgdChange(-0.05);
        delta.setEadChange(-1000.0);
        delta.setMonthlyPaymentChange(100.0);
        draft.setDelta(delta);

        SimulationDraftResponseDTO result = mapper.toApiResponse(draft);

        assertNotNull(result);
        assertEquals(Map.of("loanAmount", 20000.0), result.getFormChanges());
        
        assertNotNull(result.getSimulatedResults());
        assertEquals(0.04, result.getSimulatedResults().getPd());
        assertEquals(500.0, result.getSimulatedResults().getMonthlyPayment());
        
        assertNotNull(result.getDelta());
        assertEquals(0.01, result.getDelta().getPdChange());
        assertEquals(-0.05, result.getDelta().getLgdChange());
        assertEquals(-1000.0, result.getDelta().getEadChange());
        assertEquals(100.0, result.getDelta().getMonthlyPaymentChange());
    }

    @Test
    @DisplayName("toApiResponse should handle null SimulationDraft")
    void toApiResponse_nullDraft() {
        SimulationDraftResponseDTO result = mapper.toApiResponse(null);
        
        assertNotNull(result);
        assertNull(result.getFormChanges());
        assertNull(result.getSimulatedResults());
        assertNull(result.getDelta());
    }

    @Test
    @DisplayName("toApiResponse should handle null FinancialMetrics in RiskMetrics")
    void toApiResponse_nullFinancialMetrics() {
        SimulationDraft draft = new SimulationDraft();
        draft.setFormChanges(new FormChanges(Map.of()));
        
        RiskMetrics metrics = new RiskMetrics(); // No FinancialMetrics
        draft.setSimulatedResults(metrics);

        SimulationDraftResponseDTO result = mapper.toApiResponse(draft);

        assertNotNull(result.getSimulatedResults());
        assertEquals(0.0, result.getSimulatedResults().getMonthlyPayment()); // Defaults to 0.0
    }
    
    @Test
    @DisplayName("toApiResponse should handle null SimulationDelta")
    void toApiResponse_nullDelta() {
        SimulationDraft draft = new SimulationDraft();
        draft.setFormChanges(new FormChanges(Map.of()));
        draft.setDelta(null);

        SimulationDraftResponseDTO result = mapper.toApiResponse(draft);

        assertNotNull(result);
        assertNotNull(result.getDelta()); // It creates an empty response for nested nulls
        assertNull(result.getDelta().getPdChange()); // Default for Double is null
    }
}
