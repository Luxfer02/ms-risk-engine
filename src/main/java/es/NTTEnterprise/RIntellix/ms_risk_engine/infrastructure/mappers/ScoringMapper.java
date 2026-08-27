package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.mappers;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.TopFeatureDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.ModelInputs;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskFeature;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.Scoring;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestrates the transformation of inbound data transfer objects (ScoringDTO)
 * into the canonical domain entities (Scoring) required by the risk engine.
 * This component acts as an anti-corruption layer, ensuring that external data
 * structures
 * are correctly parsed, normalized (e.g., resolving field aliases), and
 * validated before
 * entering the core domain logic.
 *
 * @author Lucía Fernández Mancebo
 * @date 10/05/2026
 */
@Component
public class ScoringMapper {

    public ScoringMapper() {
    }

    /**
     * Constructs a valid domain Scoring entity from external API input.
     * This process includes parsing timestamps to domain-compatible dates, mapping
     * nested risk metrics, and translating the raw input snapshot features into
     * canonical domain properties.
     * 
     * @param scoringDTO The inbound payload containing raw scoring data and
     *                   results.
     * @return A fully populated Scoring domain entity, or null if the input is
     *         null.
     */
    public Scoring toDomain(ScoringDTO scoringDTO) {

        if (scoringDTO == null) {
            return null;
        }

        final Scoring scoring = new Scoring();
        scoring.setId(scoringDTO.getScoringId());
        scoring.setRequestId(scoringDTO.getRequestId());
        scoring.setBaseValue(scoringDTO.getBaseValue());
        if (scoringDTO.getScoringDate() != null) {
            scoring.setExecutionDate(Date.from(LocalDateTime.parse(scoringDTO.getScoringDate())
                    .atZone(ZoneId.systemDefault()).toInstant()));
        }

        scoring.setModelVersion(scoringDTO.getModelVersion());
        scoring.setResults(mapRiskMetrics(scoringDTO));
        scoring.setExplainability(mapExplainability(scoringDTO.getTopFeatures()));
        scoring.setInputSnapshot(mapInputSnapshot(scoringDTO).getFeatures());

        return scoring;

    }

    /**
     * Extracts and models the quantitative risk outcomes (PD, LGD, EAD, ECL)
     * and associated financial metrics from the payload.
     * 
     * @param scoringDTO The inbound payload.
     * @return Domain representation of the calculated risk metrics.
     */
    private RiskMetrics mapRiskMetrics(ScoringDTO scoringDTO) {
        RiskMetrics riskMetrics = new RiskMetrics();
        riskMetrics.setProbabilityOfDefault(scoringDTO.getPd());
        riskMetrics.setLossGivenDefault(scoringDTO.getLgd());
        riskMetrics.setExposureAtDefault(scoringDTO.getEad());
        riskMetrics.setExpectedCalculatedLoss(scoringDTO.getEcl());
        riskMetrics.setRiskLevel(scoringDTO.getRiskGrade());

        // Map financial metrics if available
        if (scoringDTO.getMonthlyPayment() != null) {
            FinancialMetrics financialMetrics = new FinancialMetrics();
            financialMetrics.setMonthlyPayment(scoringDTO.getMonthlyPayment());
            financialMetrics.setDebtToIncomeRatio(scoringDTO.getDti());
            financialMetrics.setTotalPayment(scoringDTO.getTotalPayment());
            financialMetrics.setTotalInterest(scoringDTO.getTotalInterest());
            financialMetrics.setMonthlyDisposableIncome(scoringDTO.getMonthlyDisposableIncome());
            riskMetrics.setFinancialMetrics(financialMetrics);
        }

        return riskMetrics;
    }

    /**
     * Transforms external SHAP data (explainability) into domain RiskFeature
     * objects.
     * 
     * @param featureImportanceDTOs A list of top contributing features and their
     *                              SHAP values.
     * @return A list of RiskFeature entities, or an empty list if no explainability
     *         data exists.
     */
    private List<RiskFeature> mapExplainability(List<TopFeatureDTO> featureImportanceDTOs) {
        if (featureImportanceDTOs == null) {
            return Collections.emptyList();
        }

        return featureImportanceDTOs.stream()
                .map(dto -> {
                    RiskFeature feature = new RiskFeature();
                    feature.setFeatureName(dto.getFeatureName());
                    feature.setFeatureValue(dto.getFeatureValue());
                    feature.setShapValue(dto.getShapValue());
                    return feature;
                })
                .collect(Collectors.toList());
    }

    /**
     * Normalizes the raw request snapshot features into canonical ModelInputs.
     * This is critical to ensure that legacy form keys (e.g. snake_case or aliases)
     * are correctly resolved to the exact feature names the risk model expects.
     * 
     * @param scoringDTO The inbound payload containing the raw user input snapshot.
     * @return The normalized inputs ready for model evaluation or persistence.
     */
    private ModelInputs mapInputSnapshot(ScoringDTO scoringDTO) {
        if (scoringDTO.getInputFeatures() == null) {
            return new ModelInputs();
        }

        final Map<String, Object> source = scoringDTO.getInputFeatures();
        final HashMap<String, Object> normalized = new HashMap<>();

        for (final Map.Entry<String, Object> entry : source.entrySet()) {
            normalized.put(entry.getKey(), entry.getValue());
        }

        final ModelInputs modelInputs = new ModelInputs();
        modelInputs.setFeatures(normalized);
        return modelInputs;
    }

}
