package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics;

import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums.RequestType;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.FinancialMetricsCalculator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.MathUtilities;
import lombok.extern.slf4j.Slf4j;

/**
 * Financial metrics strategy for loans and mortgages.
 * Utilizes the French amortization formulas.
 *
 * @author Lucía Fernández Mancebo
 * @date 27/08/2026
 */
@Slf4j
public class LoanFinancialMetricsStrategy implements FinancialMetricsStrategy {

    @Override
    public boolean supports(String requestType, Boolean isRevolving) {
        try {
            RequestType type = RequestType.fromValue(requestType);
            return type == RequestType.PRESTAMO || type == RequestType.HIPOTECA;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public FinancialMetrics calculateFinancialMetrics(
            Double amount,
            Double interestRate,
            Double annualIncome,
            Double existingObligations,
            Integer termMonths) {

        Objects.requireNonNull(amount, LogMessage.PRINCIPAL_AMOUNT_CANNOT_BE_NULL);
        Objects.requireNonNull(interestRate, LogMessage.ANNUAL_INTEREST_RATE_CANNOT_BE_NULL);
        Objects.requireNonNull(termMonths, LogMessage.TERM_MONTHS_CANNOT_BE_NULL);
        Objects.requireNonNull(annualIncome, LogMessage.ANNUAL_INCOME_CANNOT_BE_NULL);
        Objects.requireNonNull(existingObligations, LogMessage.EXISTING_OBLIGATIONS_CANNOT_BE_NULL);

        log.debug(LogMessage.FINANCIAL_METRICS_CALCULATION_START,
                amount, interestRate, termMonths, annualIncome, existingObligations);

        final double interestRateFraction = MathUtilities.percentageToFraction(interestRate);
        final double monthlyPayment = FinancialMetricsCalculator.calculateMonthlyPayment(amount, interestRateFraction,
                termMonths);

        final double totalMonthlyObligations = existingObligations + monthlyPayment;
        final double dti = FinancialMetricsCalculator.calculateDti(totalMonthlyObligations, annualIncome);
        final double totalPayment = FinancialMetricsCalculator.calculateTotalPayment(monthlyPayment, termMonths);
        final double totalInterest = FinancialMetricsCalculator.calculateTotalInterest(totalPayment, amount);
        final double disposableIncome = FinancialMetricsCalculator.calculateDisposableIncome(annualIncome,
                totalMonthlyObligations);

        log.debug(LogMessage.FINANCIAL_METRICS_CALCULATION_COMPLETE,
                monthlyPayment, dti, totalPayment, totalInterest, disposableIncome);

        final FinancialMetrics metrics = new FinancialMetrics();
        metrics.setMonthlyPayment(monthlyPayment);
        metrics.setDebtToIncomeRatio(dti);
        metrics.setTotalPayment(totalPayment);
        metrics.setTotalInterest(totalInterest);
        metrics.setMonthlyDisposableIncome(disposableIncome);

        return metrics;
    }
}
