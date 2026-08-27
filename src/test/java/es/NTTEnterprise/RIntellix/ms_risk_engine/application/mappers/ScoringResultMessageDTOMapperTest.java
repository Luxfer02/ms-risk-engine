package es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.output.ScoringResultMessageDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskFeature;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.Scoring;

/**
 * Unit tests for {@link ScoringResultMessageDTOMapper}.
 * Covers mapping from Scoring domain entity to output DTO.
 * @date 27/08/2026
 */
@DisplayName("ScoringResultMessageDTOMapper Tests")
class ScoringResultMessageDTOMapperTest {

    private ScoringResultMessageDTOMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ScoringResultMessageDTOMapper();
    }

    @Test
    @DisplayName("Should map Scoring to DTO successfully")
    void toDTO_mapsSuccessfully() {
        Scoring scoring = new Scoring();
        scoring.setRequestId("REQ-1");
        scoring.setModelVersion("v2");
        scoring.setExecutionDate(new Date());
        scoring.setInputSnapshot(Map.of("key", "value"));
        scoring.setBaseValue(1.23);

        RiskMetrics riskMetrics = new RiskMetrics();
        riskMetrics.setProbabilityOfDefault(0.05);
        
        FinancialMetrics finMetrics = new FinancialMetrics();
        finMetrics.setMonthlyPayment(100.0);
        riskMetrics.setFinancialMetrics(finMetrics);
        scoring.setResults(riskMetrics);

        RiskFeature feature = new RiskFeature();
        feature.setFeatureName("age");
        scoring.setExplainability(List.of(feature));

        ScoringResultMessageDTO dto = mapper.toDTO(scoring);

        assertNotNull(dto);
        assertEquals("REQ-1", dto.getRequestId());
        assertEquals("v2", dto.getModelVersion());
        assertEquals(Map.of("key", "value"), dto.getInputSnapshot());
        assertEquals(1.23, dto.getBaseValue());
        
        assertNotNull(dto.getResults());
        assertEquals(0.05, dto.getResults().getProbabilityOfDefault());
        
        assertNotNull(dto.getResults().getFinancialMetrics());
        assertEquals(100.0, dto.getResults().getFinancialMetrics().getMonthlyPayment());
        
        assertEquals(1, dto.getExplainability().size());
        assertEquals("age", dto.getExplainability().get(0).getFeatureName());
    }

    @Test
    @DisplayName("Should return null when input is null")
    void toDTO_nullInput() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    @DisplayName("Should handle null risk metrics gracefully")
    void toDTO_nullRiskMetrics() {
        Scoring scoring = new Scoring();
        scoring.setResults(null);
        
        ScoringResultMessageDTO dto = mapper.toDTO(scoring);
        
        assertNull(dto.getResults());
    }

    @Test
    @DisplayName("Should handle null financial metrics gracefully")
    void toDTO_nullFinancialMetrics() {
        Scoring scoring = new Scoring();
        RiskMetrics riskMetrics = new RiskMetrics();
        riskMetrics.setProbabilityOfDefault(0.05);
        scoring.setResults(riskMetrics);
        
        ScoringResultMessageDTO dto = mapper.toDTO(scoring);
        
        assertNotNull(dto.getResults());
        assertNull(dto.getResults().getFinancialMetrics());
        assertEquals(0.05, dto.getResults().getProbabilityOfDefault());
    }

    @Test
    @DisplayName("Should handle empty explainability list gracefully")
    void toDTO_emptyExplainability() {
        Scoring scoring = new Scoring();
        scoring.setExplainability(List.of());
        
        ScoringResultMessageDTO dto = mapper.toDTO(scoring);
        
        assertTrue(dto.getExplainability().isEmpty());
    }
}
