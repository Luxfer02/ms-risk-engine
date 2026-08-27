package es.NTTEnterprise.RIntellix.ms_risk_engine.utils;

/**
 * Utility class for deterministic financial simulation metrics.
 *
 * @author Lucía Fernández Mancebo
 * @date 10/05/2026
 */
public final class FinancialMetricsCalculator {

    private FinancialMetricsCalculator() {
        throw new UnsupportedOperationException(LogMessage.UTILITY_CLASS_NEVER_INSTANTIATE);
    }

    /**
     * Calculates the monthly payment using French amortization formula.
     *
     * @param principal  the principal amount.
     * @param annualRate the annual nominal interest rate (as a fraction, e.g., 0.045 for 4.5%).
     * @param termMonths the loan term in months.
     * @return the monthly payment.
     */
    public static double calculateMonthlyPayment(final double principal, final double annualRate,
            final int termMonths) {
        final int safeTermMonths = Math.max(termMonths, SimulationConstants.MIN_TERM_MONTHS);
        final double monthlyRate = annualRate / SimulationConstants.MONTHS_PER_YEAR;

        if (monthlyRate <= SimulationConstants.ZERO_RATE) {
            return MathUtilities.roundFinal(principal / safeTermMonths);
        }

        final double onePlusRatePowN = Math.pow(1 + monthlyRate, safeTermMonths);
        final double numerator = principal * monthlyRate * onePlusRatePowN;
        final double denominator = onePlusRatePowN - 1;
        if (denominator == SimulationConstants.ZERO_VALUE) {
            return MathUtilities.roundFinal(principal / safeTermMonths);
        }
        return MathUtilities.roundFinal(numerator / denominator);
    }

    /**
     * Calculates debt-to-income ratio as a decimal (0-1 range).
     *
     * @param monthlyPayment the monthly payment.
     * @param annualIncome   the annual income.
     * @return DTI as decimal (0-1 range, not percentage).
     */
    public static double calculateDti(final double monthlyPayment, final double annualIncome) {
        final double monthlyIncome = annualIncome / SimulationConstants.MONTHS_PER_YEAR;
        if (monthlyIncome <= SimulationConstants.ZERO_VALUE) {
            return MathUtilities.roundFinal(SimulationConstants.ZERO_VALUE);
        }
        return MathUtilities.roundFinal(monthlyPayment / monthlyIncome);
    }

    /**
     * Calculates total payment across the term.
     *
     * @param monthlyPayment the monthly payment.
     * @param termMonths     the total number of months.
     * @return total payment.
     */
    public static double calculateTotalPayment(final double monthlyPayment, final int termMonths) {
        return MathUtilities.roundFinal(monthlyPayment * Math.max(termMonths, SimulationConstants.MIN_TERM_MONTHS));
    }

    /**
     * Calculates total interest paid.
     *
     * @param totalPayment the total paid amount.
     * @param principal    the principal amount.
     * @return total interest.
     */
    public static double calculateTotalInterest(final double totalPayment, final double principal) {
        return MathUtilities.roundFinal(totalPayment - principal);
    }

    /**
     * Calculates monthly disposable income.
     *
     * @param annualIncome   the annual income.
     * @param monthlyPayment the monthly payment.
     * @return disposable income.
     */
    public static double calculateDisposableIncome(final double annualIncome, final double monthlyPayment) {
        return MathUtilities.roundFinal((annualIncome / SimulationConstants.MONTHS_PER_YEAR) - monthlyPayment);
    }

    /**
     * Calculates Loan-to-Value (LTV) ratio as a decimal (0-1 range).
     *
     * @param loanAmount    the requested loan amount.
     * @param propertyValue the appraised property value.
     * @return LTV as decimal.
     */
    public static double calculateLtv(final double loanAmount, final double propertyValue) {
        if (propertyValue <= SimulationConstants.ZERO_VALUE) {
            return SimulationConstants.ZERO_VALUE;
        }
        return MathUtilities.roundFinal(loanAmount / propertyValue);
    }
}
