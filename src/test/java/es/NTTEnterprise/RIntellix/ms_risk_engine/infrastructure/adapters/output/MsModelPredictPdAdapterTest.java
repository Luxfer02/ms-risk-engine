package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.exceptions.ModelPredictionException;
import es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.output.clients.MsModelClient;

@ExtendWith(MockitoExtension.class)
class MsModelPredictPdAdapterTest {

    @Mock
    private MsModelClient msModelClient;

    private MsModelPredictPdAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MsModelPredictPdAdapter(msModelClient);
    }

    @Test
    @DisplayName("Should predict PD successfully with pd key")
    void predictPd_success_pdKey() {
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        response.put("pd", 0.05);

        when(msModelClient.predictPd(any())).thenReturn(response);

        Double result = adapter.predictPd(input, "REQ-1");

        assertEquals(0.05, result, 0.001);
    }

    @Test
    @DisplayName("Should predict PD successfully with probability_of_default key")
    void predictPd_success_fullKey() {
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        response.put("probability_of_default", "0.06"); // test string parsing

        when(msModelClient.predictPd(any())).thenReturn(response);

        Double result = adapter.predictPd(input, "REQ-1");

        assertEquals(0.06, result, 0.001);
    }

    @Test
    @DisplayName("Should throw when response is null")
    void predictPd_nullResponse() {
        when(msModelClient.predictPd(any())).thenReturn(null);

        assertThrows(ModelPredictionException.class, () -> adapter.predictPd(new HashMap<>(), "REQ-1"));
    }

    @Test
    @DisplayName("Should throw when response is empty")
    void predictPd_emptyResponse() {
        when(msModelClient.predictPd(any())).thenReturn(Collections.emptyMap());

        assertThrows(ModelPredictionException.class, () -> adapter.predictPd(new HashMap<>(), "REQ-1"));
    }

    @Test
    @DisplayName("Should throw when pd is missing")
    void predictPd_missingPd() {
        Map<String, Object> response = new HashMap<>();
        response.put("other", 1);
        when(msModelClient.predictPd(any())).thenReturn(response);

        assertThrows(ModelPredictionException.class, () -> adapter.predictPd(new HashMap<>(), "REQ-1"));
    }

    @Test
    @DisplayName("Should handle generic runtime exception")
    void predictPd_runtimeException() {
        when(msModelClient.predictPd(any())).thenThrow(new RuntimeException("Connection error"));

        assertThrows(ModelPredictionException.class, () -> adapter.predictPd(new HashMap<>(), "REQ-1"));
    }
}
