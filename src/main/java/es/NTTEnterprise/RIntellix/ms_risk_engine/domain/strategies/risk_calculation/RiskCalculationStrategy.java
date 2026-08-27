package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.RiskGradeCalculator;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services.FinancialMetricsCalculationService;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.MathUtilities;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums.RiskGrade;

/**
 * Strategy interface for product-specific admission risk calculation logic.
 *
 * Moved to domain.strategies to reflect that this is a domain internal
 * strategy, not an external output port.
 * 
 * Updated: 05-26-2026 - Added financial metrics calculation support
 */
/**
 * Core component: RiskCalculationStrategy.
 * Encapsulates the logic and responsibilities assigned to this element
 * within the Hexagonal Architecture, ensuring separation of concerns.
 *
 * @author Lucía Fernández Mancebo
 * @date 28/07/2026
 */
public interface RiskCalculationStrategy {

    boolean supports(String requestType, Boolean isRevolving);

    RiskMetrics calculatePrePdMetrics(Double requestedAmount, Double ltv);

    /**
     * Assemble full RiskMetrics with both risk and financial metrics.
     * 
     * Combines model PD with pre-PD metrics (EAD, LGD), calculates ECL and risk
     * grade,
     * and also computes financial affordability metrics (payment, DTI, etc.).
     *
     * This is the preferred method for comprehensive scoring/simulation that
     * includes
     * both risk assessment and affordability analysis.
     *
     * @param requestType                        the type of credit request
     * @param isRevolving                        whether the product is revolving
     * @param probabilityOfDefault               the PD from the model prediction
     * @param prePdMetrics                       pre-calculated EAD and LGD metrics
     * @param amount                             the loan/mortgage principal amount
     * @param annualIncome                       the customer's annual income
     * @param termMonths                         the loan term in months
     * @param interestRate                       the annual nominal interest rate
     * @param existingObligations                the customer's existing monthly
     *                                           financial obligations (optional)
     * @param riskGradeCalculator                the calculator for risk grade
     *                                           determination
     * @param financialMetricsCalculationService the service for calculating
     *                                           financial metrics
     * @return complete RiskMetrics with both risk and financial metrics
     */
    default RiskMetrics assembleFullMetricsWithFinancialMetrics(
            final String requestType,
            final Boolean isRevolving,
            final Double probabilityOfDefault,
            final RiskMetrics prePdMetrics,
            final Double amount,
            final Double annualIncome,
            final Integer termMonths,
            final Double interestRate,
            final Double existingObligations,
            final RiskGradeCalculator riskGradeCalculator,
            final FinancialMetricsCalculationService financialMetricsCalculationService) {

        // Step 1: Assemble risk metrics (PD, EAD, LGD, ECL, RiskGrade)
        final RiskMetrics riskMetrics = assembleFullMetricsWithGradeCalculator(
                probabilityOfDefault,
                prePdMetrics,
                amount,
                annualIncome,
                termMonths,
                interestRate,
                riskGradeCalculator);

        // Step 2: Calculate financial metrics
        final Double safeExistingObligations = existingObligations != null ? existingObligations : 0.0;
        final FinancialMetrics financialMetrics = financialMetricsCalculationService.calculateFinancialMetrics(
                requestType,
                isRevolving,
                amount,
                interestRate,
                termMonths,
                annualIncome,
                safeExistingObligations);

        // Step 3: Attach financial metrics to risk metrics
        riskMetrics.setFinancialMetrics(financialMetrics);

        return riskMetrics;
    }

    /**
     * Assemble full RiskMetrics combining model PD with pre-PD metrics (EAD, LGD)
     * and calculating ECL and risk grade using the provided calculator.
     *
     * A default implementation is provided so concrete strategies only need to
     * implement pre-PD computation and support resolution.
     */
    default RiskMetrics assembleFullMetricsWithGradeCalculator(
            final Double probabilityOfDefault,
            final RiskMetrics prePdMetrics,
            final Double amount,
            final Double annualIncome,
            final Integer termMonths,
            final Double interestRate,
            final RiskGradeCalculator riskGradeCalculator) {

        final double pd = MathUtilities.roundIntermediate(probabilityOfDefault == null ? 0.0 : probabilityOfDefault);
        final double ead = MathUtilities
                .roundIntermediate(prePdMetrics == null || prePdMetrics.getExposureAtDefault() == null
                        ? 0.0
                        : prePdMetrics.getExposureAtDefault());
        final double lgd = MathUtilities
                .roundIntermediate(prePdMetrics == null || prePdMetrics.getLossGivenDefault() == null
                        ? 0.0
                        : prePdMetrics.getLossGivenDefault());

        final double ecl = MathUtilities.roundFinal(pd * lgd * ead);

        final RiskMetrics metrics = new RiskMetrics();
        metrics.setProbabilityOfDefault(pd);
        metrics.setExposureAtDefault(ead);
        metrics.setLossGivenDefault(lgd);
        metrics.setExpectedCalculatedLoss(ecl);

        try {
            final RiskGrade grade = riskGradeCalculator.calculateRiskGrade(pd);
            metrics.setRiskLevel(grade.name());
        } catch (Exception ex) {
            // In case of any grade calculation issue, leave risk level null
            metrics.setRiskLevel(null);
        }

        return metrics;
    }

}
