package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.ModelPredictionResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;

class RiskMetricsCalculationResultTest {

    @Test
    @DisplayName("Should create successfully when required fields are provided")
    void create_success() {
        assertDoesNotThrow(() -> new RiskMetricsCalculationResult(
                new ModelPredictionResult(), new RiskMetrics(), false));
    }

    @Test
    @DisplayName("Should throw NullPointerException when model prediction result is null")
    void create_nullModelPredictionResult() {
        assertThrows(NullPointerException.class, () -> new RiskMetricsCalculationResult(
                null, new RiskMetrics(), false));
    }

    @Test
    @DisplayName("Should throw NullPointerException when risk metrics is null")
    void create_nullRiskMetrics() {
        assertThrows(NullPointerException.class, () -> new RiskMetricsCalculationResult(
                new ModelPredictionResult(), null, false));
    }

    @Test
    @DisplayName("Should retrieve fields correctly")
    void getFields() {
        ModelPredictionResult pred = new ModelPredictionResult();
        RiskMetrics metrics = new RiskMetrics();

        RiskMetricsCalculationResult result = new RiskMetricsCalculationResult(pred, metrics, true);

        assertEquals(pred, result.modelPredictionResult());
        assertEquals(metrics, result.riskMetrics());
        assertEquals(true, result.isHardCutoff());
    }
}
