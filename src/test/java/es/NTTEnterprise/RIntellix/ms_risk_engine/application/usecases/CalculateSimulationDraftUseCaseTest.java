package es.NTTEnterprise.RIntellix.ms_risk_engine.application.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.SimulationModelPayloadMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.RiskMetricsCalculationContextMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution.ScoringModelExecutionStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.ModelPredictionResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.Scoring;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.FormChanges;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.SimulationDelta;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.SimulationDraft;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.exceptions.InvalidFormChangesException;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.exceptions.ScoringNotFoundException;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.ports.output.FetchScoringPort;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskIndicatorCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskMetricsCalculationContext;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskMetricsCalculationResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.SimulationDeltaCalculator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadFieldNames;

@ExtendWith(MockitoExtension.class)
class CalculateSimulationDraftUseCaseTest {

    @Mock
    private FetchScoringPort fetchScoringPort;

    @Mock
    private ScoringModelExecutionStrategy mockStrategy;

    @Mock
    private RiskMetricsCalculationService metricsCalculationService;

    @Mock
    private RiskIndicatorCalculationService riskIndicatorCalculationService;

    @Mock
    private SimulationModelPayloadMapper simulationPayloadMapper;

    @Mock
    private SimulationDeltaCalculator simulationDeltaCalculator;

    @Mock
    private RiskMetricsCalculationContextMapper contextMapper;

    private CalculateSimulationDraftUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CalculateSimulationDraftUseCase(
                fetchScoringPort,
                List.of(mockStrategy),
                metricsCalculationService,
                riskIndicatorCalculationService,
                simulationPayloadMapper,
                simulationDeltaCalculator,
                contextMapper);
    }

    @Test
    void calculateDraft_ShouldThrowException_WhenFormChangesAreNullOrEmpty() {
        assertThrows(InvalidFormChangesException.class, () -> useCase.calculateDraft("req-123", "PRESTAMO", null));

        FormChanges emptyChanges = new FormChanges(Map.of());
        assertThrows(InvalidFormChangesException.class,
                () -> useCase.calculateDraft("req-123", "PRESTAMO", emptyChanges));
    }

    @Test
    void calculateDraft_ShouldThrowException_WhenBaseScoringNotFound() {
        when(fetchScoringPort.fetchByRequestId("req-123")).thenReturn(null);

        FormChanges changes = new FormChanges(Map.of("field", "value"));

        assertThrows(ScoringNotFoundException.class, () -> useCase.calculateDraft("req-123", "PRESTAMO", changes));
    }

    @Test
    void calculateDraft_ShouldReturnSimulationDraft_WhenSuccessful() {
        // Arrange
        String requestId = "req-123";
        String requestType = "PRESTAMO";

        Map<String, Object> formValues = Map.of(ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, 15000.0);
        FormChanges formChanges = new FormChanges(formValues);

        Scoring baseScoring = mock(Scoring.class);
        when(baseScoring.getResults()).thenReturn(mock(RiskMetrics.class));
        Map<String, Object> baseInputs = Map.of(
                ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, 10000.0,
                ModelPayloadFieldNames.FIELD_TERM_MONTHS, 60,
                ModelPayloadFieldNames.FIELD_LTV, 80.0);
        when(baseScoring.getInputSnapshot()).thenReturn(baseInputs);
        when(fetchScoringPort.fetchByRequestId(requestId)).thenReturn(baseScoring);

        Map<String, Object> normalizedBase = new HashMap<>();
        normalizedBase.putAll(baseInputs);
        when(simulationPayloadMapper.normalizeVariables(baseInputs)).thenReturn(normalizedBase);

        Map<String, Object> normalizedForm = new HashMap<>(formValues);
        when(simulationPayloadMapper.normalizeVariables(formValues)).thenReturn(normalizedForm);

        when(mockStrategy.supports(requestType)).thenReturn(true);
        when(mockStrategy.modelEndpointPath()).thenReturn("/api/predict");

        RiskMetrics simulatedMetrics = mock(RiskMetrics.class);
        ModelPredictionResult prediction = mock(ModelPredictionResult.class);
        RiskMetricsCalculationResult calculationResult = new RiskMetricsCalculationResult(prediction, simulatedMetrics,
                false);

        RiskMetricsCalculationContext mockContext = mock(RiskMetricsCalculationContext.class);
        when(contextMapper.buildContext(eq(baseScoring), eq(formChanges), eq(requestType), anyMap(), anyString(), eq(requestId)))
                .thenReturn(mockContext);

        when(metricsCalculationService.calculateRiskMetrics(mockContext))
                .thenReturn(calculationResult);

        SimulationDelta delta = mock(SimulationDelta.class);
        when(simulationDeltaCalculator.calculateDelta(baseScoring, simulatedMetrics)).thenReturn(delta);

        // Act
        SimulationDraft result = useCase.calculateDraft(requestId, requestType, formChanges);

        // Assert
        assertNotNull(result);
        assertEquals(formChanges, result.getFormChanges());
        assertEquals(simulatedMetrics, result.getSimulatedResults());
        assertEquals(delta, result.getDelta());

        verify(riskIndicatorCalculationService).recalculateRiskIndicators(anyMap(), eq(requestType), eq(baseInputs));
        verify(metricsCalculationService).calculateRiskMetrics(any(RiskMetricsCalculationContext.class));
        verify(simulationDeltaCalculator).calculateDelta(baseScoring, simulatedMetrics);
    }
}
