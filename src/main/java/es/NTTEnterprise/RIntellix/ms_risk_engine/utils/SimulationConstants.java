package es.NTTEnterprise.RIntellix.ms_risk_engine.utils;

import java.util.Map;

/**
 * Centralized constants for simulation draft calculations.
 *
 * @author Lucía Fernández Mancebo
 * @date 10/05/2026
 */
public final class SimulationConstants {

    private SimulationConstants() {
        throw new UnsupportedOperationException(LogMessage.UTILITY_CLASS_NEVER_INSTANTIATE);
    }

    public static final double MONTHS_PER_YEAR = 12.0;
    public static final double PERCENTAGE_DIVISOR = 100.0;
    public static final double ZERO_RATE = 0.0;
    public static final double ZERO_VALUE = 0.0;
    public static final int MIN_TERM_MONTHS = 1;
    public static final double CC_BALANCE_THRESHOLD = 0.01;
    public static final String RISK_GRADE_ARROW = " -> ";
    public static final String UNKNOWN_RISK_GRADE = "UNKNOWN";

    /**
     * Safely returns the value of a Double or ZERO_VALUE if null.
     *
     * @param value the Double value to check.
     * @return the double value or ZERO_VALUE if value is null.
     */
    public static double getSafe(final Double value) {
        return value == null ? ZERO_VALUE : value;
    }

    /**
     * Defines the deterministic relationship between employment status
     * and income type, as learned by the credit card model from training data.
     * 
     * When employmentStatus changes in a simulation, incomeType MUST be
     * synchronized to maintain data coherence with the model's training domain.
     */
    public static final java.util.Map<String, String> EMPLOYMENT_TO_INCOME_TYPE = Map.of(
            "Indefinido", "Salario",
            "Temporal", "Salario",
            "Funcionario", "Salario",
            "Autonomo", "Autonomo",
            "Desempleado", "Ayudas",
            "Inactivo", "Pension");
}
