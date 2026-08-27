package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation.RiskCalculationStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation.LoanRiskCalculationStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation.MortgageRiskCalculationStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation.RevolvingCreditCardRiskCalculationStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation.StandardCreditCardRiskCalculationStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics.FinancialMetricsStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics.LoanFinancialMetricsStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics.StandardCreditCardFinancialMetricsStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics.RevolvingCreditCardFinancialMetricsStrategy;

/**
 * Infrastructure configuration that registers domain risk calculation
 * strategies as Spring beans.
 *
 * Each strategy is a pure domain POJO that reads its constants directly
 * from RiskCalculationDefaults. This configuration class acts as the
 * infrastructure adapter that makes them available to the Spring context.
 *
 * @author Lucía Fernández Mancebo
 * @date 25/04/2026
 */
@Configuration
public class RiskCalculationStrategyConfig {

    /**
     * Creates the loan risk calculation strategy bean.
     *
     * @return the LoanRiskCalculationStrategy instance.
     */
    @Bean
    public LoanRiskCalculationStrategy loanRiskCalculationStrategy() {
        return new LoanRiskCalculationStrategy();
    }

    /**
     * Creates the mortgage risk calculation strategy bean.
     *
     * @return the MortgageRiskCalculationStrategy instance.
     */
    @Bean
    public MortgageRiskCalculationStrategy mortgageRiskCalculationStrategy() {
        return new MortgageRiskCalculationStrategy();
    }

    /**
     * Creates the standard credit card risk calculation strategy bean.
     *
     * @return the StandardCreditCardRiskCalculationStrategy instance.
     */
    @Bean
    public StandardCreditCardRiskCalculationStrategy standardCreditCardRiskCalculationStrategy() {
        return new StandardCreditCardRiskCalculationStrategy();
    }

    /**
     * Creates the revolving credit card risk calculation strategy bean.
     *
     * @return the RevolvingCreditCardRiskCalculationStrategy instance.
     */
    @Bean
    public RevolvingCreditCardRiskCalculationStrategy revolvingCreditCardRiskCalculationStrategy() {
        return new RevolvingCreditCardRiskCalculationStrategy();
    }

    /**
     * Aggregates all risk calculation strategies into an ordered list.
     *
     * @param loan      the loan strategy.
     * @param mortgage  the mortgage strategy.
     * @param standard  the standard credit card strategy.
     * @param revolving the revolving credit card strategy.
     * @return the list of all available risk calculation strategies.
     */
    @Bean
    public List<RiskCalculationStrategy> riskCalculationStrategies(
            final LoanRiskCalculationStrategy loan,
            final MortgageRiskCalculationStrategy mortgage,
            final StandardCreditCardRiskCalculationStrategy standard,
            final RevolvingCreditCardRiskCalculationStrategy revolving) {
        return List.of(loan, mortgage, standard, revolving);
    }

    /**
     * Creates the loan financial metrics strategy bean.
     */
    @Bean
    public LoanFinancialMetricsStrategy loanFinancialMetricsStrategy() {
        return new LoanFinancialMetricsStrategy();
    }

    /**
     * Creates the standard credit card financial metrics strategy bean.
     */
    @Bean
    public StandardCreditCardFinancialMetricsStrategy standardCreditCardFinancialMetricsStrategy() {
        return new StandardCreditCardFinancialMetricsStrategy();
    }

    /**
     * Creates the revolving credit card financial metrics strategy bean.
     */
    @Bean
    public RevolvingCreditCardFinancialMetricsStrategy revolvingCreditCardFinancialMetricsStrategy() {
        return new RevolvingCreditCardFinancialMetricsStrategy();
    }

    /**
     * Aggregates all financial metrics strategies into an ordered list.
     */
    @Bean
    public List<FinancialMetricsStrategy> financialMetricsStrategies(
            final LoanFinancialMetricsStrategy loan,
            final StandardCreditCardFinancialMetricsStrategy standard,
            final RevolvingCreditCardFinancialMetricsStrategy revolving) {
        return List.of(loan, standard, revolving);
    }
}
