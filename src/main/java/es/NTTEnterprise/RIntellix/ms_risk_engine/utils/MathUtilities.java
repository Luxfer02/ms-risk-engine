package es.NTTEnterprise.RIntellix.ms_risk_engine.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for mathematical operations, such as rounding.
 * 
 * @author Lucía Fernández Mancebo
 * @date 30/05/2026
 */
public final class MathUtilities {

    private MathUtilities() {
        throw new UnsupportedOperationException(LogMessage.UTILITY_CLASS_NEVER_INSTANTIATE);
    }

    /**
     * Rounds a value to 2 decimal places.
     * Use this for final values that are retrieved or displayed by the API.
     * 
     * @param value the value to round
     * @return the rounded value
     */
    public static double roundFinal(final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return value;
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Rounds a value to 4 decimal places.
     * Use this for intermediate values used in calculations that are not displayed.
     * 
     * @param value the value to round
     * @return the rounded value
     */
    public static double roundIntermediate(final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return value;
        }
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Calculates the absolute difference between two values and rounds to 2 decimal places.
     * Useful for computing simulation deltas cleanly.
     * 
     * @param simValue the simulated value
     * @param baseValue the base value
     * @return the absolute rounded difference
     */
    public static double calculateDelta(final double simValue, final double baseValue) {
        return roundFinal(simValue - baseValue);
    }

    /**
     * Converts a percentage value (e.g., 18.0) to a decimal fraction (e.g., 0.18).
     * 
     * @param percentage the percentage value
     * @return the decimal fraction
     */
    public static double percentageToFraction(final double percentage) {
        return percentage / SimulationConstants.PERCENTAGE_DIVISOR;
    }
}
