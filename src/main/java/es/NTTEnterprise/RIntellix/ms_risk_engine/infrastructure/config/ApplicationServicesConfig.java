package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.CreditCardModelPayloadMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.LoanOrMortgageModelPayloadMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.ScoringResultMessageDTOMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.ScoringResultMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.SimulationModelPayloadMapper;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.ports.input.SimulationDraftPortService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.ports.input.ScoringProcessingPortService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution.CreditCardScoringModelExecutionStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution.LoanOrMortgageScoringModelExecutionStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution.ScoringModelExecutionStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.usecases.CalculateSimulationDraftUseCase;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.usecases.RiskMetricsCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.application.usecases.ScoringProcessingService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.ports.output.FetchScoringPort;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.ports.output.ModelPredictionPort;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.ports.output.ScoringResultPublisherPort;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.FinancialMetricsCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.HardCutoffRuleEvaluator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskGradeCalculator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskIndicatorCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.SimulationDeltaCalculator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation.RiskCalculationStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadUtilities;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.DtiCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.EnumNormalizer;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.BooleanConverter;

/**
 * Configuration class that instantiates all application layer services as
 * Spring Beans.
 * This keeps the application layer completely independent of the Spring
 * framework
 * in accordance with Hexagonal Architecture.
 *
 * @author Lucía Fernández Mancebo
 * @date 28/07/2026
 */
@Configuration
public class ApplicationServicesConfig {

    // --- Utilities & Domain Services ---

    @Bean
    public EnumNormalizer enumNormalizer() {
        return new EnumNormalizer();
    }

    @Bean
    public BooleanConverter booleanConverter() {
        return new BooleanConverter();
    }

    @Bean
    public ModelPayloadUtilities modelPayloadUtilities(EnumNormalizer enumNormalizer,
            BooleanConverter booleanConverter) {
        return new ModelPayloadUtilities(enumNormalizer, booleanConverter);
    }



    // --- Mappers ---

    @Bean
    public CreditCardModelPayloadMapper creditCardModelPayloadMapper(
            ModelPayloadUtilities modelPayloadUtilities) {
        return new CreditCardModelPayloadMapper(modelPayloadUtilities);
    }

    @Bean
    public LoanOrMortgageModelPayloadMapper loanOrMortgageModelPayloadMapper(
            ModelPayloadUtilities modelPayloadUtilities) {
        return new LoanOrMortgageModelPayloadMapper(modelPayloadUtilities);
    }

    @Bean
    public ScoringResultMessageDTOMapper scoringResultMessageDTOMapper() {
        return new ScoringResultMessageDTOMapper();
    }

    @Bean
    public ScoringResultMapper scoringResultMapper() {
        return new ScoringResultMapper();
    }

    @Bean
    public SimulationModelPayloadMapper simulationModelPayloadMapper(
            ModelPayloadUtilities modelPayloadUtilities) {
        return new SimulationModelPayloadMapper(modelPayloadUtilities);
    }

    // --- Use Cases & Strategies ---

    @Bean
    public RiskMetricsCalculationService riskMetricsCalculationService(
            ModelPredictionPort modelPredictionPort,
            List<RiskCalculationStrategy> riskCalculationStrategies,
            RiskGradeCalculator riskGradeCalculator,
            FinancialMetricsCalculationService financialMetricsCalculationService,
            HardCutoffRuleEvaluator hardCutoffRuleEvaluator) {
        return new RiskMetricsCalculationService(modelPredictionPort, riskCalculationStrategies, riskGradeCalculator,
                financialMetricsCalculationService, hardCutoffRuleEvaluator);
    }

    @Bean
    public LoanOrMortgageScoringModelExecutionStrategy loanOrMortgageScoringModelExecutionStrategy(
            LoanOrMortgageModelPayloadMapper loanOrMortgageModelPayloadMapper,
            RiskMetricsCalculationService riskMetricsCalculationService,
            DtiCalculationService dtiCalculationService,
            @Value("${risk.model.predict-loan-path:/api/v1/risk/predict-loan}") String predictLoanPath) {
        return new LoanOrMortgageScoringModelExecutionStrategy(loanOrMortgageModelPayloadMapper,
                riskMetricsCalculationService, dtiCalculationService, predictLoanPath);
    }

    @Bean
    public CreditCardScoringModelExecutionStrategy creditCardScoringModelExecutionStrategy(
            CreditCardModelPayloadMapper creditCardModelPayloadMapper,
            RiskMetricsCalculationService riskMetricsCalculationService,
            DtiCalculationService dtiCalculationService,
            @Value("${risk.model.predict-credit-card-path:/api/v1/risk/predict-credit-card}") String predictCreditCardPath) {
        return new CreditCardScoringModelExecutionStrategy(creditCardModelPayloadMapper, riskMetricsCalculationService,
                dtiCalculationService, predictCreditCardPath);
    }

    @Bean
    public ScoringProcessingPortService scoringProcessingService(
            List<ScoringModelExecutionStrategy> scoringModelExecutionStrategies,
            ScoringResultMapper scoringResultMapper,
            ScoringResultPublisherPort scoringResultPublisherPort,
            @Value("${risk.model.version:XGBoost_v1.0}") String modelVersion) {
        return new ScoringProcessingService(scoringModelExecutionStrategies, scoringResultMapper,
                scoringResultPublisherPort, modelVersion);
    }

    @Bean
    public SimulationDraftPortService calculateSimulationDraftUseCase(
            FetchScoringPort fetchScoringPort,
            List<ScoringModelExecutionStrategy> scoringModelExecutionStrategies,
            RiskMetricsCalculationService riskMetricsCalculationService,
            RiskIndicatorCalculationService riskIndicatorCalculationService,
            SimulationModelPayloadMapper simulationModelPayloadMapper,
            SimulationDeltaCalculator simulationDeltaCalculator,
            es.NTTEnterprise.RIntellix.ms_risk_engine.application.mappers.RiskMetricsCalculationContextMapper contextMapper) {
        return new CalculateSimulationDraftUseCase(
                fetchScoringPort,
                scoringModelExecutionStrategies,
                riskMetricsCalculationService,
                riskIndicatorCalculationService,
                simulationModelPayloadMapper,
                simulationDeltaCalculator,
                contextMapper);
    }
}
