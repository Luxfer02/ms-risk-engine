package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.Scoring;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.exceptions.ScoringNotFoundException;
import es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.adapters.output.clients.MsCoreDataClient;
import es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.mappers.ScoringMapper;

@ExtendWith(MockitoExtension.class)
class MsCoreDataScoringAdapterTest {

    @Mock
    private MsCoreDataClient msCoreDataClient;

    @Mock
    private ScoringMapper scoringMapper;

    private MsCoreDataScoringAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MsCoreDataScoringAdapter(msCoreDataClient, scoringMapper);
    }

    @Test
    @DisplayName("Should fetch scoring successfully")
    void fetchByRequestId_success() {
        ScoringDTO dto = new ScoringDTO();
        Scoring domain = new Scoring();
        when(msCoreDataClient.getScoringByRequestId("REQ-1")).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));
        when(scoringMapper.toDomain(dto)).thenReturn(domain);

        Scoring result = adapter.fetchByRequestId("REQ-1");

        assertNotNull(result);
        assertEquals(domain, result);
    }

    @Test
    @DisplayName("Should throw exception when status is error")
    void fetchByRequestId_error() {
        when(msCoreDataClient.getScoringByRequestId("REQ-1")).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        assertThrows(ScoringNotFoundException.class, () -> adapter.fetchByRequestId("REQ-1"));
    }
}
