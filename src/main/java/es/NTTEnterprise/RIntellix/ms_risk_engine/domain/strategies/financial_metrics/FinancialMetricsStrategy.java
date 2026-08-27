package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;

/**
 * Strategy interface for calculating financial metrics based on product type.
 *
 * @author Lucía Fernández Mancebo
 * @date 27/08/2026
 */
public interface FinancialMetricsStrategy {

    /**
     * Determines if this strategy supports the given request type and revolving status.
     *
     * @param requestType the request type (e.g., PRESTAMO, TARJETA_CREDITO)
     * @param isRevolving the revolving status flag (can be null for non-credit cards)
     * @return true if the strategy supports the combination
     */
    boolean supports(String requestType, Boolean isRevolving);

    /**
     * Calculates financial metrics for the specific product.
     *
     * @param amount              the principal amount (loan amount or credit limit)
     * @param interestRate        the annual interest rate in percentage (e.g. 5.0 for 5%)
     * @param annualIncome        the borrower's annual income
     * @param existingObligations the sum of the borrower's existing monthly debt obligations
     * @param termMonths          the term in months (only used for loans/mortgages, can be null for credit cards)
     * @return the calculated financial metrics
     */
    FinancialMetrics calculateFinancialMetrics(
            Double amount,
            Double interestRate,
            Double annualIncome,
            Double existingObligations,
            Integer termMonths);
}
