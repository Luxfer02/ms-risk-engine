package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.services;

import java.util.Map;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.LogMessage;

/**
 * Context object for risk metrics calculation.
 * 
 * Encapsulates all necessary data for calculating risk metrics, abstracting
 * away whether the calculation is for scoring or simulation.
 *
 * This is a domain value object that represents the complete set of parameters
 * needed to execute risk metric calculations. It follows the Value Object
 * pattern
 * to aggregate all calculation inputs in a single, immutable structure.
 *
 * This pattern enables the domain service to be used in multiple application
 * contexts without coupling to specific use cases.
 *
 * Properties include:
 * - modelPayload: The payload to send to the ML model API
 * - requestId: Request identifier for tracing and logging
 * - modelEndpointPath: The endpoint path for model invocation
 * - requestType: Type of request (LOAN, MORTGAGE, CREDIT_CARD)
 * - loanAmount: Principal amount or credit limit
 * - ltv: Loan-to-Value ratio (null for credit cards)
 * - annualIncome: Borrower's annual income
 * - termMonths: Loan term in months (null for credit cards)
 * - interestRate: Annual interest rate
 *
 * @author Lucía Fernández Mancebo
 * @date 05/09/2026
 */
public record RiskMetricsCalculationContext(
        Map<String, Object> modelPayload,
        String requestId,
        String modelEndpointPath,
        String requestType,
        Boolean isRevolving,
        Double principalAmount,
        Double ltv,
        Double annualIncome,
        Integer termMonths,
        Double interestRate,
        Double existingMonthlyObligations) {
    public RiskMetricsCalculationContext {
        Objects.requireNonNull(modelPayload, LogMessage.MODEL_PAYLOAD_NULL);
        Objects.requireNonNull(requestId, LogMessage.REQUEST_ID_NULL_ERROR);
        Objects.requireNonNull(modelEndpointPath, LogMessage.ENDPOINT_PATH_NULL_ERROR);
        Objects.requireNonNull(requestType, LogMessage.REQUEST_TYPE_NULL_ERROR);
    }

}
