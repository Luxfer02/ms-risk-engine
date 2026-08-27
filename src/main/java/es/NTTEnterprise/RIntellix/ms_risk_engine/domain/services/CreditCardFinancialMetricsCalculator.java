package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import lombok.extern.slf4j.Slf4j;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.constants.RiskCalculationDefaults;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.SimulationConstants;

/**
 * Utility class for calculating credit card specific financial metrics.
 *
 * @author Lucía Fernández Mancebo
 * @date 27/08/2026
 */
@Slf4j
public final class CreditCardFinancialMetricsCalculator {

    private CreditCardFinancialMetricsCalculator() {
        throw new UnsupportedOperationException(LogMessage.UTILITY_CLASS_NEVER_INSTANTIATE);
    }

    /**
     * Calculates the standard monthly payment for a non-revolving credit card.
     * Non-revolving cards require full balance payment each month.
     *
     * @param creditLimit the credit limit
     * @return the monthly payment (which is the full credit limit)
     */
    public static double calculateStandardMonthlyPayment(double creditLimit) {
        return creditLimit;
    }

    /**
     * Calculates the estimated monthly payment for a revolving credit card
     * using the standard DTI payment percentage heuristic.
     *
     * @param creditLimit the credit limit
     * @return the estimated monthly payment
     */
    public static double calculateRevolvingMonthlyPayment(double creditLimit) {
        return creditLimit * RiskCalculationDefaults.CC_DTI_PAYMENT_PERCENTAGE;
    }

    /**
     * Calculates the monthly payment for a credit card based on whether it is revolving or standard.
     *
     * @param creditLimit the credit limit
     * @param isRevolving true if revolving, false/null if standard
     * @return the calculated monthly payment
     */
    public static double calculateMonthlyPayment(double creditLimit, Boolean isRevolving) {
        if (Boolean.TRUE.equals(isRevolving)) {
            return calculateRevolvingMonthlyPayment(creditLimit);
        }
        return calculateStandardMonthlyPayment(creditLimit);
    }

    /**
     * Calculates the Debt-to-Income (DTI) ratio for a credit card.
     *
     * @param monthlyPayment the calculated monthly payment
     * @param annualIncome   the annual income
     * @return the DTI ratio
     */
    public static double calculateCreditCardDti(double monthlyPayment, double annualIncome) {
        if (annualIncome <= 0) {
            return 0.0;
        }
        return monthlyPayment / (annualIncome / SimulationConstants.MONTHS_PER_YEAR);
    }

    /**
     * Calculates the monthly disposable income for a credit card holder.
     *
     * @param annualIncome   the annual income
     * @param monthlyPayment the monthly payment
     * @return the monthly disposable income
     */
    public static double calculateCreditCardDisposableIncome(double annualIncome, double monthlyPayment) {
        double monthlyIncome = annualIncome / SimulationConstants.MONTHS_PER_YEAR;
        return monthlyIncome * (1.0 - RiskCalculationDefaults.CC_FIXED_COSTS_INCOME_PERCENTAGE) - monthlyPayment;
    }

    /**
     * Record to hold the results of a revolving credit card simulation.
     */
    public record RevolvingSimulationResult(double totalInterest, double totalPayment) {}

    /**
     * Simulates the payoff of a revolving credit card to calculate total interest and total payment.
     * Uses the Average Payment Scenario heuristic.
     *
     * @param creditLimit  the credit limit
     * @param interestRate the annual interest rate
     * @return the simulation results (total interest and total payment)
     */
    public static RevolvingSimulationResult simulateRevolvingPayoff(double creditLimit, double interestRate) {
        if (creditLimit <= 0) {
            return new RevolvingSimulationResult(0.0, 0.0);
        }
        
        if (interestRate <= 0) {
            return new RevolvingSimulationResult(0.0, creditLimit);
        }

        double currentBalance = creditLimit * RiskCalculationDefaults.CC_AVERAGE_UTILIZATION_RATE;
        double monthlyInterestRate = interestRate / SimulationConstants.MONTHS_PER_YEAR;
        
        double totalInterestAccumulator = 0.0;
        double totalPaymentAccumulator = 0.0;
        int monthCounter = 0;

        while (currentBalance > SimulationConstants.CC_BALANCE_THRESHOLD && monthCounter < RiskCalculationDefaults.CC_MAX_SIMULATION_MONTHS) {
            monthCounter++;
            
            // Calculate interest for the current month
            double monthlyInterest = currentBalance * monthlyInterestRate;
            totalInterestAccumulator += monthlyInterest;
            
            // Calculate minimum payment
            double percentagePayment = currentBalance * RiskCalculationDefaults.CC_AVERAGE_PAYMENT_PERCENTAGE;
            double monthlyPayment = Math.max(percentagePayment, RiskCalculationDefaults.CC_MINIMUM_ABSOLUTE_PAYMENT);
            
            // Cap payment if balance + interest is less than the calculated payment
            if (monthlyPayment > (currentBalance + monthlyInterest)) {
                monthlyPayment = currentBalance + monthlyInterest;
            }
            
            totalPaymentAccumulator += monthlyPayment;
            
            // Update balance
            double principalPayment = monthlyPayment - monthlyInterest;
            currentBalance -= principalPayment;
        }

        log.debug(LogMessage.CC_REVOLVING_SIMULATION_COMPLETE, monthCounter, currentBalance, totalInterestAccumulator, totalPaymentAccumulator);

        return new RevolvingSimulationResult(totalInterestAccumulator, totalPaymentAccumulator);
    }
}
