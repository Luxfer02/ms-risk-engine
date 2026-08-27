package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums.RequestType;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.constants.RiskCalculationDefaults;

/**
 * Risk calculation strategy for fixed-rate mortgages.
 *
 * Business logic: High-value debt secured by real estate.
 * Risk is mitigated by the physical collateral.
 *
 * EAD = Requested Loan Amount.
 * LGD = max(0.10, (EAD - (AppraisalValue * (1 - h) - ForeclosureCosts)) / EAD).
 * h (Haircut) = 0.20 (20% market stress drop).
 * Foreclosure Costs = 10% of appraisal value.
 * Regulatory Floor: LGD cannot be lower than 10%.
 *
 * @author Lucía Fernández Mancebo
 * @date 25/04/2026
 */
public class MortgageRiskCalculationStrategy implements RiskCalculationStrategy {

    @Override
    public boolean supports(final String requestType, final Boolean isRevolving) {
        try {
            return RequestType.fromValue(requestType) == RequestType.HIPOTECA;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Calculates the pre-PD metrics (EAD and LGD) for a mortgage.
     *
     * @param requestedAmount the requested mortgage amount
     * @param ltv             the loan-to-value ratio
     * @return the calculated RiskMetrics containing EAD and LGD
     * @throws IllegalArgumentException if the requested amount or LTV is invalid
     */
    @Override
    public RiskMetrics calculatePrePdMetrics(final Double requestedAmount, final Double ltv)
            throws IllegalArgumentException {

        Double appraisalValue = null;
        Double foreclosureCosts = null;
        Double recoverableCollateral = null;
        Double calculatedLgd = null;
        Double safeLtv = null;
        Double ead = null;
        Double safeCreditLimit = null;

        safeCreditLimit = RiskCalculationDefaults.validateRequestAmount(requestedAmount);

        // For calculate the LGD.
        // Cap LTV at 1.0 (100%) to prevent anomalous recovery values
        safeLtv = Math.min(1.0, RiskCalculationDefaults.clampRatio(ltv == null ? 0.0 : ltv));

        if (safeLtv <= 0.0) {
            calculatedLgd = RiskCalculationDefaults.MORTGAGE_LGD_UNSECURED_LOAN;
        } else {
            ead = safeCreditLimit;
            appraisalValue = safeCreditLimit / safeLtv;
            foreclosureCosts = appraisalValue * RiskCalculationDefaults.MORTGAGE_FORECLOSURE_COST_RATE;

            // Dynamic Haircut: Exponentially increase haircut for LTV > 80%
            double dynamicHaircut = RiskCalculationDefaults.MORTGAGE_HAIRCUT; // Base 20%
            if (safeLtv > 0.8) {
                // Exponential penalty: 0.20 + 0.5 * (LTV - 0.8)^2
                dynamicHaircut += 0.5 * Math.pow(safeLtv - 0.8, 2);
            }

            recoverableCollateral = appraisalValue * (1.0 - dynamicHaircut) - foreclosureCosts;
            // Prevent negative recovery if haircut + foreclosure > 100%
            recoverableCollateral = Math.max(0.0, recoverableCollateral);

            calculatedLgd = (ead - recoverableCollateral) / ead;

            calculatedLgd = RiskCalculationDefaults.clampRatio(Math.max(calculatedLgd,
                    RiskCalculationDefaults.MORTGAGE_LGD_FLOOR));
        }

        final RiskMetrics metrics = new RiskMetrics();
        metrics.setExposureAtDefault(safeCreditLimit);
        metrics.setLossGivenDefault(calculatedLgd);
        return metrics;
    }
}
