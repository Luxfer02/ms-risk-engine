package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.CalculateSimulationDraftRequestDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.output.SimulationDraftResponseDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.output.SimulationMetricsResponseDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.ports.input.SimulationDraftPortService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.FormChanges;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.SimulationDraft;
import es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.mappers.SimulationDraftMapper;

@ExtendWith(MockitoExtension.class)
class SimulationDraftControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SimulationDraftPortService calculateSimulationDraftUseCase;

    @Mock
    private SimulationDraftMapper simulationDraftMapper;

    @InjectMocks
    private SimulationDraftController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should return 200 OK and calculated draft")
    void calculateDraft_success() throws Exception {
        CalculateSimulationDraftRequestDTO requestDTO = new CalculateSimulationDraftRequestDTO();
        requestDTO.setRequestId("REQ-123");
        requestDTO.setRequestType("PRESTAMO");

        SimulationDraft draft = new SimulationDraft();
        RiskMetrics metrics = new RiskMetrics();
        metrics.setProbabilityOfDefault(0.05);
        draft.setSimulatedResults(metrics);

        SimulationDraftResponseDTO responseDTO = new SimulationDraftResponseDTO();
        SimulationMetricsResponseDTO metricsDTO = new SimulationMetricsResponseDTO();
        metricsDTO.setPd(0.05);
        responseDTO.setSimulatedResults(metricsDTO);

        when(simulationDraftMapper.toFormChanges(any())).thenReturn(new FormChanges(new java.util.HashMap<>()));
        when(calculateSimulationDraftUseCase.calculateDraft(anyString(), anyString(), any())).thenReturn(draft);
        when(simulationDraftMapper.toApiResponse(draft)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/simulations/draft")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.simulatedResults.pd").value(0.05));
    }

    @Test
    @DisplayName("Should return 400 Bad Request on validation failure")
    void calculateDraft_badRequest() throws Exception {
        // Validation fails if request is empty, for example.
        // We test integration via mockMvc if standard validation is applied,
        // but standalone setup might need explicit validator.
        // Since request is empty and might violate @Valid we check if it handles it.
        // If not, we just rely on standard spring behaviors. Let's just pass empty
        // body.
        mockMvc.perform(post("/api/v1/simulations/draft")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
