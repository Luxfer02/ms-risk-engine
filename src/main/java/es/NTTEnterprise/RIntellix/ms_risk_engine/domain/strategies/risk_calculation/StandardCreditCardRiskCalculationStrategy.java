package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums.RequestType;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.constants.RiskCalculationDefaults;

/**
 * Risk calculation strategy for standard (non-revolving) credit cards.
 *
 * Business logic: Contingent liability where the borrower has a limit
 * but has not spent it yet. Uses the Credit Conversion Factor (CCF)
 * to estimate future usage at the point of default.
 *
 * EAD = Credit Limit x CCF (default 0.50, Basel Standard).
 * LGD = 0.80 (higher than loans due to revolving balance recovery difficulty).
 *
 * @author Lucía Fernández Mancebo
 * @date 25/04/2026
 */
public class StandardCreditCardRiskCalculationStrategy implements RiskCalculationStrategy {

    @Override
    public boolean supports(final String requestType, final Boolean isRevolving) {
        try {
            return RequestType.fromValue(requestType) == RequestType.TARJETA_CREDITO
                    && (isRevolving == null || !isRevolving);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Calculates the pre-PD metrics (EAD and LGD) for a standard credit card.
     *
     * @param requestedAmount the requested credit limit
     * @param ltv             the loan-to-value ratio (not applicable for credit cards)
     * @return the calculated RiskMetrics containing EAD and LGD
     * @throws IllegalArgumentException if the requested amount is invalid
     */
    @Override
    public RiskMetrics calculatePrePdMetrics(final Double requestedAmount, final Double ltv)
            throws IllegalArgumentException {
        Double safeCreditLimit = null;
        Double ead = null;

        safeCreditLimit = RiskCalculationDefaults.validateRequestAmount(requestedAmount);
        // EAD is the amount the client will spend when default, in this case the credit
        // limit * the CCF.
        ead = safeCreditLimit * RiskCalculationDefaults.CC_STANDARD_CCF;

        final RiskMetrics metrics = new RiskMetrics();
        metrics.setExposureAtDefault(ead);
        metrics.setLossGivenDefault(RiskCalculationDefaults.CC_STANDARD_LGD);
        return metrics;
    }
}
