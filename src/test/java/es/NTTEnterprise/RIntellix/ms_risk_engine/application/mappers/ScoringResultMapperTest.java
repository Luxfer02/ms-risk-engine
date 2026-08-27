package es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.ModelPredictionResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskFeature;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.Scoring;

/**
 * Unit tests for {@link ScoringResultMapper}.
 * Covers mapping to Scoring entity and resolving SHAP features from snapshot.
 * @date 27/08/2026
 */
@DisplayName("ScoringResultMapper Tests")
class ScoringResultMapperTest {

    private ScoringResultMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ScoringResultMapper();
    }

    @Test
    @DisplayName("Should map to Scoring entity successfully")
    void toScoring_mapsSuccessfully() {
        String requestId = "REQ-123";
        String modelVersion = "v1.0";
        Map<String, Object> inputSnapshot = Map.of("loanAmount", 10000.0, "Age", 30);
        
        RiskFeature shapFeature = new RiskFeature();
        shapFeature.setFeatureName("loanAmount");
        shapFeature.setShapValue(0.5);
        
        ModelPredictionResult predictionResult = new ModelPredictionResult();
        predictionResult.setBaseValue(1.5);
        predictionResult.setShapExplanations(List.of(shapFeature));
        
        RiskMetrics riskMetrics = new RiskMetrics();
        riskMetrics.setProbabilityOfDefault(0.05);

        Scoring result = mapper.toScoring(requestId, modelVersion, inputSnapshot, predictionResult, riskMetrics);

        assertNotNull(result);
        assertEquals(requestId, result.getRequestId());
        assertEquals(modelVersion, result.getModelVersion());
        assertNotNull(result.getExecutionDate());
        assertEquals(inputSnapshot, result.getInputSnapshot());
        assertEquals(riskMetrics, result.getResults());
        assertEquals(1.5, result.getBaseValue());
        
        // Check explainability resolution
        List<RiskFeature> explainability = result.getExplainability();
        assertEquals(1, explainability.size());
        assertEquals("loanAmount", explainability.get(0).getFeatureName());
        assertEquals("10000.0", explainability.get(0).getFeatureValue());
    }

    @Test
    @DisplayName("Should resolve feature value case-insensitively")
    void resolveFeatureValue_caseInsensitive() {
        Map<String, Object> inputSnapshot = Map.of("loanAmount", 10000.0);
        
        RiskFeature shapFeature = new RiskFeature();
        shapFeature.setFeatureName("LOANAMOUNT"); // Uppercase in SHAP
        
        ModelPredictionResult predictionResult = new ModelPredictionResult();
        predictionResult.setShapExplanations(List.of(shapFeature));

        Scoring result = mapper.toScoring("REQ", "v1", inputSnapshot, predictionResult, new RiskMetrics());

        assertEquals("10000.0", result.getExplainability().get(0).getFeatureValue());
    }

    @Test
    @DisplayName("Should return empty string for unresolved feature")
    void resolveFeatureValue_unresolved() {
        Map<String, Object> inputSnapshot = Map.of();
        
        RiskFeature shapFeature = new RiskFeature();
        shapFeature.setFeatureName("unknownField");
        
        ModelPredictionResult predictionResult = new ModelPredictionResult();
        predictionResult.setShapExplanations(List.of(shapFeature));

        Scoring result = mapper.toScoring("REQ", "v1", inputSnapshot, predictionResult, new RiskMetrics());

        assertEquals("", result.getExplainability().get(0).getFeatureValue());
    }

    @Test
    @DisplayName("Should handle null SHAP explanations")
    void toScoring_nullShap() {
        ModelPredictionResult predictionResult = new ModelPredictionResult();
        predictionResult.setShapExplanations(null);

        Scoring result = mapper.toScoring("REQ", "v1", Map.of(), predictionResult, new RiskMetrics());

        assertTrue(result.getExplainability().isEmpty());
    }
}
