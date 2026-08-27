package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.strategies.risk_calculation;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.enums.RequestType;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.constants.RiskCalculationDefaults;

/**
 * Risk calculation strategy for revolving credit cards.
 *
 * Business logic: Characterized by "minimum payment" structures and perpetual
 * debt cycles. These present higher volatility than standard cards, reflecting
 * the higher probability that a struggling borrower will max out the card
 * before defaulting.
 *
 * EAD = Credit Limit x CCF_rev (default 0.75).
 * LGD = 0.90 (extremely low recovery rates on revolving balances in EU market).
 *
 * @author Lucía Fernández Mancebo
 * @date 25/04/2026
 */
public class RevolvingCreditCardRiskCalculationStrategy implements RiskCalculationStrategy {

    @Override
    public boolean supports(final String requestType, final Boolean isRevolving) {
        try {
            return RequestType.fromValue(requestType) == RequestType.TARJETA_CREDITO
                    && Boolean.TRUE.equals(isRevolving);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Calculates the pre-PD metrics (EAD and LGD) for a revolving credit card.
     *
     * @param requestedAmount the requested credit limit
     * @param ltv             the loan-to-value ratio (not applicable for credit
     *                        cards)
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
        ead = safeCreditLimit * RiskCalculationDefaults.CC_REVOLVING_CCF;
        final RiskMetrics metrics = new RiskMetrics();
        metrics.setExposureAtDefault(ead);
        metrics.setLossGivenDefault(RiskCalculationDefaults.CC_REVOLVING_LGD);
        return metrics;
    }
}
