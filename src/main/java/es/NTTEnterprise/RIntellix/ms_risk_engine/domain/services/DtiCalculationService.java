package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.FinancialMetricsCalculator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.MathUtilities;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.SimulationConstants;

/**
 * Domain service for DTI calculations and existing obligation derivation.
 *
 * Encapsulates shared DTI-related logic used across scoring and simulation
 * workflows to keep mappers and orchestrators focused on single
 * responsibilities.
 *
 * @author Lucía Fernández Mancebo
 * @date 30/05/2026
 */
public class DtiCalculationService {

    /**
     * Calculates the model DTI for scoring requests by adding existing DTI and
     * the new-loan DTI derived from the request's loan parameters.
     *
     * @param annualIncome     annual income used for DTI normalization.
     * @param existingDtiRatio existing DTI ratio from the request payload.
     * @param loanAmount       loan principal for the new request.
     * @param interestRate     annual nominal interest rate (percentage).
     * @param termMonths       loan term in months.
     * @return the model DTI ratio to send to the AI model.
     */
    public double calculateModelDtiForScoring(
            final double annualIncome,
            final double existingObligationsAnnual,
            final double loanAmount,
            final double interestRate,
            final Integer termMonths) {
        if (annualIncome <= SimulationConstants.ZERO_VALUE) {
            return SimulationConstants.ZERO_VALUE;
        }

        final double existingMonthly = existingObligationsAnnual / SimulationConstants.MONTHS_PER_YEAR;
        final int safeTermMonths = termMonths == null ? SimulationConstants.MIN_TERM_MONTHS : termMonths;
        final double interestRateFraction = MathUtilities.percentageToFraction(interestRate);
        final double monthlyPayment = FinancialMetricsCalculator.calculateMonthlyPayment(
                loanAmount,
                interestRateFraction,
                safeTermMonths);
        final double monthlyIncome = annualIncome / SimulationConstants.MONTHS_PER_YEAR;
        return MathUtilities.roundFinal((existingMonthly + monthlyPayment) / monthlyIncome);
    }

    /**
     * Calculates the model DTI for credit card scoring requests by adding existing
     * DTI
     * and the new credit card DTI derived from the request parameters.
     *
     * @param annualIncome              annual income used for DTI normalization.
     * @param existingObligationsAnnual existing annual obligations.
     * @param creditLimit               credit limit for the new card.
     * @param isRevolving               whether the credit card is revolving.
     * @return the model DTI ratio to send to the AI model.
     */
    public double calculateModelDtiForCreditCardScoring(
            final double annualIncome,
            final double existingObligationsAnnual,
            final double creditLimit,
            final Boolean isRevolving) {
        if (annualIncome <= SimulationConstants.ZERO_VALUE) {
            return SimulationConstants.ZERO_VALUE;
        }

        final double existingMonthly = existingObligationsAnnual / SimulationConstants.MONTHS_PER_YEAR;
        final double monthlyPayment = CreditCardFinancialMetricsCalculator.calculateMonthlyPayment(creditLimit, isRevolving);

        final double monthlyIncome = annualIncome / SimulationConstants.MONTHS_PER_YEAR;
        return MathUtilities.roundFinal((existingMonthly + monthlyPayment) / monthlyIncome);
    }

    /**
     * Calculates DTI using existing monthly obligations plus a new monthly payment.
     *
     * @param monthlyPayment      the new monthly payment.
     * @param annualIncome        the annual income used for normalization.
     * @param existingObligations existing monthly obligations.
     * @return the recalculated DTI ratio.
     */
    public double calculateDtiWithExistingObligations(
            final double monthlyPayment,
            final double annualIncome,
            final double existingObligations) {
        final double monthlyIncome = annualIncome / SimulationConstants.MONTHS_PER_YEAR;
        if (monthlyIncome <= SimulationConstants.ZERO_VALUE) {
            return SimulationConstants.ZERO_VALUE;
        }
        final double totalMonthlyObligations = existingObligations + monthlyPayment;
        return totalMonthlyObligations / monthlyIncome;
    }

}
