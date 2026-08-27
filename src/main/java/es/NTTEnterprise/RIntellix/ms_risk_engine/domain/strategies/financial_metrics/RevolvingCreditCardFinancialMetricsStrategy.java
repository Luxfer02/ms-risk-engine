package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.financial_metrics;

import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums.RequestType;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.CreditCardFinancialMetricsCalculator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.CreditCardFinancialMetricsCalculator.RevolvingSimulationResult;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.MathUtilities;
import lombok.extern.slf4j.Slf4j;

/**
 * Financial metrics strategy for revolving credit cards.
 * Revolving cards accrue interest over time and have a minimum payment threshold.
 *
 * @author Lucía Fernández Mancebo
 * @date 27/08/2026
 */
@Slf4j
public class RevolvingCreditCardFinancialMetricsStrategy implements FinancialMetricsStrategy {

    @Override
    public boolean supports(String requestType, Boolean isRevolving) {
        try {
            RequestType type = RequestType.fromValue(requestType);
            return type == RequestType.TARJETA_CREDITO && Boolean.TRUE.equals(isRevolving);
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
        Objects.requireNonNull(annualIncome, LogMessage.ANNUAL_INCOME_CANNOT_BE_NULL);

        log.debug(LogMessage.CC_FINANCIAL_METRICS_CALCULATION_START);

        // For revolving cards, monthly payment estimated using standard DTI heuristic
        final double monthlyPayment = CreditCardFinancialMetricsCalculator.calculateRevolvingMonthlyPayment(amount);
        
        final double safeExisting = existingObligations != null ? existingObligations : 0.0;
        final double totalMonthlyObligations = safeExisting + monthlyPayment;
        final double dti = CreditCardFinancialMetricsCalculator.calculateCreditCardDti(totalMonthlyObligations, annualIncome);
        
        // Calculate total interest and total payment using the iterative algorithm
        final double interestRateFraction = MathUtilities.percentageToFraction(interestRate);
        final RevolvingSimulationResult simulationResult = CreditCardFinancialMetricsCalculator.simulateRevolvingPayoff(amount, interestRateFraction);
        
        final double disposableIncome = CreditCardFinancialMetricsCalculator.calculateCreditCardDisposableIncome(annualIncome, totalMonthlyObligations);

        log.debug(LogMessage.FINANCIAL_METRICS_CALCULATION_COMPLETE,
                monthlyPayment, dti, simulationResult.totalPayment(), simulationResult.totalInterest(), disposableIncome);

        final FinancialMetrics metrics = new FinancialMetrics();
        metrics.setMonthlyPayment(monthlyPayment);
        metrics.setDebtToIncomeRatio(dti);
        metrics.setTotalPayment(simulationResult.totalPayment());
        metrics.setTotalInterest(simulationResult.totalInterest());
        metrics.setMonthlyDisposableIncome(disposableIncome);

        return metrics;
    }
}
