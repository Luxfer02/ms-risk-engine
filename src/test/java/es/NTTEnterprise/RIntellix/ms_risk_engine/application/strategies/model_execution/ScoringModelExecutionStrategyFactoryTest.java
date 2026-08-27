package es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class ScoringModelExecutionStrategyFactoryTest {

    @Test
    void createStrategy_ShouldReturnStrategy_WhenSupported() {
        ScoringModelExecutionStrategy mockStrategy = mock(ScoringModelExecutionStrategy.class);
        when(mockStrategy.supports("PRESTAMO")).thenReturn(true);

        ScoringModelExecutionStrategy result = ScoringModelExecutionStrategyFactory.createStrategy(
                "PRESTAMO", List.of(mockStrategy));

        assertEquals(mockStrategy, result);
    }

    @Test
    void createStrategy_ShouldThrowException_WhenNotSupported() {
        ScoringModelExecutionStrategy mockStrategy = mock(ScoringModelExecutionStrategy.class);
        when(mockStrategy.supports("INVALID")).thenReturn(false);

        List<ScoringModelExecutionStrategy> strategies = List.of(mockStrategy);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            ScoringModelExecutionStrategyFactory.createStrategy("INVALID", strategies)
        );
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void createStrategy_ShouldThrowException_WhenArgumentsAreNull() {
        assertThrows(NullPointerException.class, () -> 
            ScoringModelExecutionStrategyFactory.createStrategy(null, List.of())
        );

        assertThrows(NullPointerException.class, () -> 
            ScoringModelExecutionStrategyFactory.createStrategy("PRESTAMO", null)
        );
    }
}
