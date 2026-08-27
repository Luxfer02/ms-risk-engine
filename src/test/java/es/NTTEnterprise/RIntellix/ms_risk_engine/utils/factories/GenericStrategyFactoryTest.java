package es.NTTEnterprise.RIntellix.ms_risk_engine.utils.factories;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link GenericStrategyFactory}.
 * Covers strategy selection, no-match exception, and multiple-match priority.
 * @date 27/08/2026
 */
@DisplayName("GenericStrategyFactory Tests")
class GenericStrategyFactoryTest {

    interface TestStrategy {
        String getType();
    }

    static class StrategyA implements TestStrategy {
        @Override
        public String getType() {
            return "A";
        }
    }

    static class StrategyB implements TestStrategy {
        @Override
        public String getType() {
            return "B";
        }
    }

    @Test
    @DisplayName("Should return first matching strategy")
    void selectStrategy_shouldReturnFirst_matchingStrategy() {
        List<TestStrategy> strategies = List.of(new StrategyA(), new StrategyB());

        TestStrategy result = GenericStrategyFactory.selectStrategy(
                strategies,
                s -> s.getType().equals("B"),
                "No strategy found");

        assertEquals("B", result.getType());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when no match found")
    void selectStrategy_shouldThrow_whenNoMatch() {
        List<TestStrategy> strategies = List.of(new StrategyA(), new StrategyB());

        assertThrows(IllegalArgumentException.class,
                () -> GenericStrategyFactory.selectStrategy(
                        strategies,
                        s -> s.getType().equals("C"),
                        "No strategy for type C"));
    }

    @Test
    @DisplayName("Should return first match when multiple strategies match")
    void selectStrategy_shouldReturnFirst_whenMultipleMatch() {
        StrategyA first = new StrategyA();
        StrategyA second = new StrategyA();
        List<TestStrategy> strategies = List.of(first, second, new StrategyB());

        TestStrategy result = GenericStrategyFactory.selectStrategy(
                strategies,
                s -> s.getType().equals("A"),
                "No strategy found");

        assertSame(first, result, "Should return the first matching strategy");
    }

    @Test
    @DisplayName("Should throw with specified error message")
    void selectStrategy_shouldUseSpecifiedErrorMessage() {
        List<TestStrategy> strategies = List.of(new StrategyA());
        String expectedMsg = "Custom error: no strategy for type X";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> GenericStrategyFactory.selectStrategy(
                        strategies,
                        s -> false,
                        expectedMsg));

        assertEquals(expectedMsg, ex.getMessage());
    }
}
