package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.FinancialMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.RiskMetrics;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.Scoring;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation.SimulationDelta;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.SimulationConstants;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.MathUtilities;

/**
 * Domain service for calculating the delta (difference) between base metrics
 * and simulated metrics.
 * 
 * Extracts simulation delta logic into a domain service for better SRP and
 * testability.
 *
 * @author Lucía Fernández Mancebo
 * @date 27/08/2026
 */
public class SimulationDeltaCalculator {

        public SimulationDeltaCalculator() {
                // No dependencies required
        }

        /**
         * Builds the simulation delta by comparing base metrics with simulated metrics.
         * Delta represents the change in key metrics between the base scenario and the
         * simulated scenario.
         * 
         * @param baseScoring      the original scoring object with base metrics.
         * @param simulatedMetrics the new metrics from simulation.
         * @return the SimulationDelta with all calculated changes.
         */
        public SimulationDelta calculateDelta(
                        final Scoring baseScoring,
                        final RiskMetrics simulatedMetrics) {

                // Extract base financial metrics from the scoring
                final RiskMetrics baseMetrics = baseScoring.getResults();
                final double baseMonthlyPayment = baseMetrics.getFinancialMetrics() != null && baseMetrics.getFinancialMetrics().getMonthlyPayment() != null
                                ? baseMetrics.getFinancialMetrics().getMonthlyPayment()
                                : 0.0;
                final double baseDti = baseMetrics.getFinancialMetrics() != null && baseMetrics.getFinancialMetrics().getDebtToIncomeRatio() != null
                                ? baseMetrics.getFinancialMetrics().getDebtToIncomeRatio()
                                : 0.0;
                final double baseTotalPayment = baseMetrics.getFinancialMetrics() != null && baseMetrics.getFinancialMetrics().getTotalPayment() != null
                                ? baseMetrics.getFinancialMetrics().getTotalPayment()
                                : 0.0;
                final double baseTotalInterest = baseMetrics.getFinancialMetrics() != null && baseMetrics.getFinancialMetrics().getTotalInterest() != null
                                ? baseMetrics.getFinancialMetrics().getTotalInterest()
                                : 0.0;
                final double baseDisposableIncome = baseMetrics.getFinancialMetrics() != null && baseMetrics.getFinancialMetrics().getMonthlyDisposableIncome() != null
                                ? baseMetrics.getFinancialMetrics().getMonthlyDisposableIncome()
                                : 0.0;

                // Extract simulated financial metrics
                final FinancialMetrics simFinancialMetrics = simulatedMetrics.getFinancialMetrics();
                final double simMonthlyPayment = simFinancialMetrics != null && simFinancialMetrics.getMonthlyPayment() != null
                                ? simFinancialMetrics.getMonthlyPayment()
                                : 0.0;
                final double simTotalPayment = simFinancialMetrics != null && simFinancialMetrics.getTotalPayment() != null
                                ? simFinancialMetrics.getTotalPayment()
                                : 0.0;
                final double simTotalInterest = simFinancialMetrics != null && simFinancialMetrics.getTotalInterest() != null
                                ? simFinancialMetrics.getTotalInterest()
                                : 0.0;
                final double simDisposableIncome = simFinancialMetrics != null && simFinancialMetrics.getMonthlyDisposableIncome() != null
                                ? simFinancialMetrics.getMonthlyDisposableIncome()
                                : 0.0;
                final double simDti = simFinancialMetrics != null && simFinancialMetrics.getDebtToIncomeRatio() != null
                                ? simFinancialMetrics.getDebtToIncomeRatio()
                                : 0.0;

                // Build delta
                final SimulationDelta delta = new SimulationDelta();
                delta.setPdChange(MathUtilities.roundIntermediate(
                                SimulationConstants.getSafe(simulatedMetrics.getProbabilityOfDefault())
                                                - SimulationConstants.getSafe(baseMetrics.getProbabilityOfDefault())));
                delta.setEclChange(MathUtilities.roundFinal(SimulationConstants
                                .getSafe(simulatedMetrics.getExpectedCalculatedLoss())
                                - SimulationConstants.getSafe(baseMetrics.getExpectedCalculatedLoss())));

                final String baseRiskGradeName = baseMetrics.getRiskLevel() != null ? baseMetrics.getRiskLevel()
                                : SimulationConstants.UNKNOWN_RISK_GRADE;
                final String simRiskGradeName = simulatedMetrics.getRiskLevel() != null
                                ? simulatedMetrics.getRiskLevel()
                                : SimulationConstants.UNKNOWN_RISK_GRADE;

                delta.setRiskGradeChange(baseRiskGradeName + SimulationConstants.RISK_GRADE_ARROW + simRiskGradeName);
                
                delta.setLgdChange(MathUtilities.roundIntermediate(
                                SimulationConstants.getSafe(simulatedMetrics.getLossGivenDefault())
                                                - SimulationConstants.getSafe(baseMetrics.getLossGivenDefault())));
                delta.setEadChange(MathUtilities.roundFinal(
                                SimulationConstants.getSafe(simulatedMetrics.getExposureAtDefault())
                                                - SimulationConstants.getSafe(baseMetrics.getExposureAtDefault())));

                delta.setMonthlyPaymentChange(
                                MathUtilities.calculateDelta(simMonthlyPayment, baseMonthlyPayment));
                delta.setDtiChange(MathUtilities.calculateDelta(simDti, baseDti));
                delta.setTotalPaymentChange(MathUtilities.calculateDelta(simTotalPayment, baseTotalPayment));
                delta.setTotalInterestChange(MathUtilities.calculateDelta(simTotalInterest, baseTotalInterest));
                delta.setMonthlyDisposableIncomeChange(
                                MathUtilities.calculateDelta(simDisposableIncome, baseDisposableIncome));

                return delta;
        }
}
