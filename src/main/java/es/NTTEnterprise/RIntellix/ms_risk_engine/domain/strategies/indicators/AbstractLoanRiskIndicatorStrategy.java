package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.indicators;

import java.util.Map;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.exceptions.InvalidFormChangesException;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.DtiCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.utils.MapUtilities;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.FinancialMetricsCalculator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.MathUtilities;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadFieldNames;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.SimulationConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base strategy for calculating common risk indicators (like DTI)
 * for loan-based products (Loans and Mortgages).
 *
 * @author Lucía Fernández Mancebo
 * @date 16/08/2026
 */
@Slf4j
public abstract class AbstractLoanRiskIndicatorStrategy implements RiskIndicatorCalculationStrategy {

        protected final DtiCalculationService dtiCalculationService;

        /**
         * Constructor for AbstractLoanRiskIndicatorStrategy.
         *
         * @param dtiCalculationService domain service for DTI calculations.
         */
        public AbstractLoanRiskIndicatorStrategy(final DtiCalculationService dtiCalculationService) {
                this.dtiCalculationService = Objects.requireNonNull(dtiCalculationService,
                                LogMessage.DTI_CALCULATION_SERVICE_CANNOT_BE_NULL);
        }

        /**
         * Calculates the DTI (Debt-to-Income) ratio based on the loan parameters and
         * updates the merged variables.
         * This logic is common for both standard Loans and Mortgages.
         *
         * @param mergedVariables   the merged simulation variables
         * @param baseInputSnapshot the base input snapshot
         * @throws InvalidFormChangesException if data parsing fails
         */
        protected void calculateAndSetDti(final Map<String, Object> mergedVariables,
                        final Map<String, Object> baseInputSnapshot)
                        throws InvalidFormChangesException {

                final double loanAmount = MapUtilities.getDouble(mergedVariables,
                                ModelPayloadFieldNames.FIELD_LOAN_AMOUNT, 0);
                final double interestRate = MapUtilities.getDouble(mergedVariables,
                                ModelPayloadFieldNames.FIELD_INTEREST_RATE, 0);
                final int termMonths = (int) MapUtilities.getDouble(mergedVariables,
                                ModelPayloadFieldNames.FIELD_TERM_MONTHS,
                                SimulationConstants.MIN_TERM_MONTHS);
                final double annualIncome = MapUtilities.getDouble(mergedVariables,
                                ModelPayloadFieldNames.FIELD_ANNUAL_INCOME, 0);

                final double monthlyPayment = FinancialMetricsCalculator.calculateMonthlyPayment(
                                loanAmount, interestRate, termMonths);

                final double existingObligationsAnnual = MapUtilities.getDouble(
                                mergedVariables, ModelPayloadFieldNames.FIELD_EXISTING_OBLIGATIONS, 0.0);
                final double existingObligations = existingObligationsAnnual / SimulationConstants.MONTHS_PER_YEAR;

                final double newDti = MathUtilities.roundFinal(
                                dtiCalculationService.calculateDtiWithExistingObligations(
                                                monthlyPayment,
                                                annualIncome,
                                                existingObligations));

                mergedVariables.put(ModelPayloadFieldNames.FIELD_DTI, newDti);
                log.info(LogMessage.SIMULATION_DTI_RECALCULATED, newDti, monthlyPayment, annualIncome);
        }
}
