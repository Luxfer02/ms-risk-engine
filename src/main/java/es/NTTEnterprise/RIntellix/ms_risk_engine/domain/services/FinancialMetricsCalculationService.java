package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import java.util.List;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics.FinancialMetricsStrategy;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics.FinancialMetricsStrategyFactory;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Domain service for calculating financial affordability metrics.
 * 
 * Encapsulates the calculation of financial metrics used to assess customer
 * affordability and payment obligations for a credit product.
 * 
 * This service delegates to the appropriate FinancialMetricsStrategy based on
 * the request type and revolving status, which then assembles the metrics into
 * a complete FinancialMetrics value object.
 *
 * @author Lucía Fernández Mancebo
 * @date 27/08/2026
 */
@Slf4j
public class FinancialMetricsCalculationService {

    private final List<FinancialMetricsStrategy> strategies;

    public FinancialMetricsCalculationService(final List<FinancialMetricsStrategy> strategies) {
        this.strategies = Objects.requireNonNull(strategies, LogMessage.FINANCIAL_METRICS_STRATEGIES_CANNOT_BE_NULL);
    }

    /**
     * Calculates complete financial metrics for a credit product dynamically via
     * strategies.
     *
     * @param requestType         the request type (e.g. PRESTAMO, TARJETA_CREDITO)
     * @param isRevolving         the revolving status (for credit cards)
     * @param amount              the principal amount (loan amount or credit limit)
     * @param annualInterestRate  the annual nominal interest rate
     * @param termMonths          the loan term in months
     * @param annualIncome        the customer's annual income
     * @param existingObligations the customer's existing monthly financial
     *                            obligations
     * @return a complete FinancialMetrics object
     */
    public FinancialMetrics calculateFinancialMetrics(
            final String requestType,
            final Boolean isRevolving,
            final Double amount,
            final Double annualInterestRate,
            final Integer termMonths,
            final Double annualIncome,
            final Double existingObligations) {

        final FinancialMetricsStrategy strategy = FinancialMetricsStrategyFactory.createStrategy(requestType,
                isRevolving, strategies);
        return strategy.calculateFinancialMetrics(amount, annualInterestRate, annualIncome, existingObligations,
                termMonths);
    }

    /**
     * Calculates financial metrics without existing obligations (simplified DTI).
     */
    public FinancialMetrics calculateFinancialMetricsWithoutExistingObligations(
            final String requestType,
            final Boolean isRevolving,
            final Double amount,
            final Double annualInterestRate,
            final Integer termMonths,
            final Double annualIncome) {

        return calculateFinancialMetrics(requestType, isRevolving, amount, annualInterestRate, termMonths, annualIncome,
                0.0);
    }
}
