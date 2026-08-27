package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common;

/**
 * Class representing the risk metrics calculated by the scoring model.
 * Contains the standard Basel II/III metrics: Probability of Default, Loss
 * Given Default, Exposure
 * at Default, and Expected Loss, as well as a derived risk level
 * classification.
 * 
 * Also includes financial affordability metrics (monthly payment, DTI, etc.)
 * that provide business and customer insights.
 * 
 * This class is used as part of the Scoring results to encapsulate all
 * risk-related outputs in a single object.
 *
 * @author: Lucía Fernández Mancebo
 *          Date: 03-02-2026
 *          Updated: 05-26-2026 - Added FinancialMetrics
 * @date 27/08/2026
 */
public class RiskMetrics {

    private Double probabilityOfDefault;
    private Double lossGivenDefault;
    private Double exposureAtDefault;
    private Double expectedCalculatedLoss;
    private String riskLevel;
    private FinancialMetrics financialMetrics;

    /**
     * Default constructor for RiskMetrics.
     */
    public RiskMetrics() {
    }

    /**
     * Parameterized constructor for RiskMetrics. Allows setting all fields at once.
     *
     * @param probabilityOfDefault   The probability that the borrower will default
     *                               on the contract, expressed as a percentage
     *                               (0-100).
     * @param lossGivenDefault       The percentage of the exposure that would be
     *                               lost if a default occurs (0-100).
     * @param exposureAtDefault      The total value at risk at the time of default,
     *                               typically the outstanding balance of the
     *                               contract.
     * @param expectedCalculatedLoss The expected loss calculated as (Probability of
     *                               Default * Loss Given Default * Exposure at
     *                               Default).
     * @param riskLevel              A categorical classification of risk (e.g.,
     *                               "Low", "Medium", "High") derived from the
     *                               calculated metrics, used for easier
     *                               interpretation by end-users.
     * @param financialMetrics       Financial affordability metrics (payment, DTI,
     *                               etc.)
     */
    public RiskMetrics(Double probabilityOfDefault, Double lossGivenDefault, Double exposureAtDefault,
            Double expectedCalculatedLoss, String riskLevel, FinancialMetrics financialMetrics) {
        this.probabilityOfDefault = probabilityOfDefault;
        this.lossGivenDefault = lossGivenDefault;
        this.exposureAtDefault = exposureAtDefault;
        this.expectedCalculatedLoss = expectedCalculatedLoss;
        this.riskLevel = riskLevel;
        this.financialMetrics = financialMetrics;
    }

    /**
     * Parameterized constructor for RiskMetrics (legacy, without financial
     * metrics).
     *
     * @param probabilityOfDefault   The probability that the borrower will default
     *                               on the contract, expressed as a percentage
     *                               (0-100).
     * @param lossGivenDefault       The percentage of the exposure that would be
     *                               lost if a default occurs (0-100).
     * @param exposureAtDefault      The total value at risk at the time of default,
     *                               typically the outstanding balance of the
     *                               contract.
     * @param expectedCalculatedLoss The expected loss calculated as (Probability of
     *                               Default * Loss Given Default * Exposure at
     *                               Default).
     * @param riskLevel              A categorical classification of risk (e.g.,
     *                               "Low", "Medium", "High") derived from the
     *                               calculated metrics, used for easier
     *                               interpretation by end-users.
     */
    public RiskMetrics(Double probabilityOfDefault, Double lossGivenDefault, Double exposureAtDefault,
            Double expectedCalculatedLoss, String riskLevel) {
        this.probabilityOfDefault = probabilityOfDefault;
        this.lossGivenDefault = lossGivenDefault;
        this.exposureAtDefault = exposureAtDefault;
        this.expectedCalculatedLoss = expectedCalculatedLoss;
        this.riskLevel = riskLevel;
    }

    // Getters and Setters

    public Double getProbabilityOfDefault() {
        return probabilityOfDefault;
    }

    public void setProbabilityOfDefault(Double probabilityOfDefault) {
        this.probabilityOfDefault = probabilityOfDefault;
    }

    public Double getLossGivenDefault() {
        return lossGivenDefault;
    }

    public void setLossGivenDefault(Double lossGivenDefault) {
        this.lossGivenDefault = lossGivenDefault;
    }

    public Double getExposureAtDefault() {
        return exposureAtDefault;
    }

    public void setExposureAtDefault(Double exposureAtDefault) {
        this.exposureAtDefault = exposureAtDefault;
    }

    public Double getExpectedCalculatedLoss() {
        return expectedCalculatedLoss;
    }

    public void setExpectedCalculatedLoss(Double expectedCalculatedLoss) {
        this.expectedCalculatedLoss = expectedCalculatedLoss;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public FinancialMetrics getFinancialMetrics() {
        return financialMetrics;
    }

    public void setFinancialMetrics(FinancialMetrics financialMetrics) {
        this.financialMetrics = financialMetrics;
    }

    // toString, hashCode and equals
    @Override
    public String toString() {
        return "RiskMetrics [probabilityOfDefault=" + probabilityOfDefault + ", lossGivenDefault=" + lossGivenDefault
                + ", exposureAtDefault=" + exposureAtDefault + ", expectedCalculatedLoss=" + expectedCalculatedLoss
                + ", riskLevel=" + riskLevel + ", financialMetrics=" + financialMetrics + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((probabilityOfDefault == null) ? 0 : probabilityOfDefault.hashCode());
        result = prime * result + ((lossGivenDefault == null) ? 0 : lossGivenDefault.hashCode());
        result = prime * result + ((exposureAtDefault == null) ? 0 : exposureAtDefault.hashCode());
        result = prime * result + ((expectedCalculatedLoss == null) ? 0 : expectedCalculatedLoss.hashCode());
        result = prime * result + ((riskLevel == null) ? 0 : riskLevel.hashCode());
        result = prime * result + ((financialMetrics == null) ? 0 : financialMetrics.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RiskMetrics other = (RiskMetrics) obj;
        if (probabilityOfDefault == null) {
            if (other.probabilityOfDefault != null)
                return false;
        } else if (!probabilityOfDefault.equals(other.probabilityOfDefault))
            return false;
        if (lossGivenDefault == null) {
            if (other.lossGivenDefault != null)
                return false;
        } else if (!lossGivenDefault.equals(other.lossGivenDefault))
            return false;
        if (exposureAtDefault == null) {
            if (other.exposureAtDefault != null)
                return false;
        } else if (!exposureAtDefault.equals(other.exposureAtDefault))
            return false;
        if (expectedCalculatedLoss == null) {
            if (other.expectedCalculatedLoss != null)
                return false;
        } else if (!expectedCalculatedLoss.equals(other.expectedCalculatedLoss))
            return false;
        if (riskLevel == null) {
            if (other.riskLevel != null)
                return false;
        } else if (!riskLevel.equals(other.riskLevel))
            return false;
        if (financialMetrics == null) {
            if (other.financialMetrics != null)
                return false;
        } else if (!financialMetrics.equals(other.financialMetrics))
            return false;
        return true;
    }

}
