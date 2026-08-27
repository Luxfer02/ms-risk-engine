package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_risk_engine.application.dtos.input.ScoringDTO;
import es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.common.Scoring;
import es.NTTEnterprise.RIntellix.ms_risk_engine.utils.ModelPayloadFieldNames;

class ScoringMapperTest {

    @Test
    @DisplayName("Maps input snapshot keys (handles camelCase cleanly without ACL aliases)")
    void shouldNormalizeInputSnapshotKeysToCamelCase() {
        ScoringMapper mapper = new ScoringMapper();

        Map<String, Object> inputFeatures = new HashMap<>();
        inputFeatures.put("annualIncome", 120000.0);
        inputFeatures.put("existingObligations", 4000.0);
        inputFeatures.put("loanAmount", 50000.0);
        inputFeatures.put("interestRate", 0.05);
        inputFeatures.put("termMonths", 36);
        inputFeatures.put("occupationSector", "Tecnologia");
        inputFeatures.put("dependents", 2);
        inputFeatures.put("loanType", "PRESTAMO");

        ScoringDTO dto = new ScoringDTO();
        dto.setScoringId("scoring-123");
        dto.setRequestId("req-123");
        dto.setInputFeatures(inputFeatures);

        Scoring scoring = mapper.toDomain(dto);

        assertThat(scoring).isNotNull();
        Map<String, Object> snapshot = scoring.getInputSnapshot();
        assertThat(snapshot).isNotNull();

        // Verify values map cleanly
        assertThat(snapshot.get(ModelPayloadFieldNames.FIELD_ANNUAL_INCOME)).isEqualTo(120000.0);
        assertThat(snapshot.get(ModelPayloadFieldNames.FIELD_EXISTING_OBLIGATIONS)).isEqualTo(4000.0);
        assertThat(snapshot.get(ModelPayloadFieldNames.FIELD_INTEREST_RATE)).isEqualTo(0.05);
        assertThat(snapshot.get(ModelPayloadFieldNames.FIELD_TERM_MONTHS)).isEqualTo(36);

        // Verify specific values
        assertThat(snapshot.get(ModelPayloadFieldNames.FIELD_LOAN_AMOUNT)).isEqualTo(50000.0);
        assertThat(snapshot.get(ModelPayloadFieldNames.FIELD_OCCUPATION_SECTOR)).isEqualTo("Tecnologia");
        assertThat(snapshot.get(ModelPayloadFieldNames.FIELD_DEPENDENTS)).isEqualTo(2);
        assertThat(snapshot.get(ModelPayloadFieldNames.FIELD_LOAN_TYPE)).isEqualTo("PRESTAMO");

        // Verify there are no snake_case leaks
        assertThat(snapshot).doesNotContainKey("annual_income");
        assertThat(snapshot).doesNotContainKey("existing_obligations");
        assertThat(snapshot).doesNotContainKey("requested_amount");
        assertThat(snapshot).doesNotContainKey("work_sector");
        assertThat(snapshot).doesNotContainKey("nr_dependants");
        assertThat(snapshot).doesNotContainKey("request_type");
    }
}
