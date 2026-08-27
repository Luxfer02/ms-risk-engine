package es.NTTEnterprise.RIntellix.ms_risk_engine.application.strategies.model_execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class ModelEndpointResolverTest {

    @Test
    void resolveEndpointPath_ShouldReturnPath_WhenSupported() {
        ScoringModelExecutionStrategy mockStrategy = mock(ScoringModelExecutionStrategy.class);
        when(mockStrategy.supports("PRESTAMO")).thenReturn(true);
        when(mockStrategy.modelEndpointPath()).thenReturn("/api/predict");

        String path = ModelEndpointResolver.resolveEndpointPath("PRESTAMO", List.of(mockStrategy));

        assertEquals("/api/predict", path);
    }

    @Test
    void resolveEndpointPath_ShouldThrowException_WhenNotSupported() {
        ScoringModelExecutionStrategy mockStrategy = mock(ScoringModelExecutionStrategy.class);
        when(mockStrategy.supports("INVALID")).thenReturn(false);

        List<ScoringModelExecutionStrategy> strategies = List.of(mockStrategy);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            ModelEndpointResolver.resolveEndpointPath("INVALID", strategies)
        );
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void resolveEndpointPath_ShouldThrowException_WhenArgumentsAreNull() {
        assertThrows(NullPointerException.class, () -> 
            ModelEndpointResolver.resolveEndpointPath(null, List.of())
        );

        assertThrows(NullPointerException.class, () -> 
            ModelEndpointResolver.resolveEndpointPath("PRESTAMO", null)
        );
    }
}
