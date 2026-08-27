package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.input.kafka.factories;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.input.kafka.strategy.ScoringGenerationMessageStrategy;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScoringKafkaMessageStrategyFactory Tests")
class ScoringKafkaMessageStrategyFactoryTest {

    @Mock
    private ScoringGenerationMessageStrategy strategyA;

    @Mock
    private ScoringGenerationMessageStrategy strategyB;

    @Test
    @DisplayName("Should throw NullPointerException if requestType is null")
    void selectStrategy_withNullRequestType() {
        List<ScoringGenerationMessageStrategy> strategies = Arrays.asList(strategyA);
        assertThrows(NullPointerException.class,
                () -> ScoringKafkaMessageStrategyFactory.selectStrategy(null, strategies));
    }

    @Test
    @DisplayName("Should throw NullPointerException if strategies list is null")
    void selectStrategy_withNullStrategies() {
        assertThrows(NullPointerException.class,
                () -> ScoringKafkaMessageStrategyFactory.selectStrategy("TARJETA_CREDITO", null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if no strategy supports the request type")
    void selectStrategy_withUnsupportedRequestType() {
        when(strategyA.supports("INVALID_TYPE")).thenReturn(false);
        when(strategyB.supports("INVALID_TYPE")).thenReturn(false);
        List<ScoringGenerationMessageStrategy> strategies = Arrays.asList(strategyA, strategyB);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ScoringKafkaMessageStrategyFactory.selectStrategy("INVALID_TYPE", strategies));

        assertTrue(exception.getMessage().contains("INVALID_TYPE"));
    }

    @Test
    @DisplayName("Should return the first strategy that supports the request type")
    void selectStrategy_happyPath() {
        when(strategyA.supports("TARJETA_CREDITO")).thenReturn(false);
        when(strategyB.supports("TARJETA_CREDITO")).thenReturn(true);
        List<ScoringGenerationMessageStrategy> strategies = Arrays.asList(strategyA, strategyB);

        ScoringGenerationMessageStrategy selectedStrategy = ScoringKafkaMessageStrategyFactory
                .selectStrategy("TARJETA_CREDITO", strategies);

        assertEquals(strategyB, selectedStrategy);
    }
}
