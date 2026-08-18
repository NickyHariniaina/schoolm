package hei.student.schoolm.repository.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hei.student.schoolm.repository.model.JCohort;
import java.time.Instant;
import java.time.Year;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JCohortMapperTest {
  private static final UUID COHORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private final JCohortMapper jCohortMapper = new JCohortMapper();

  @Test
  void toDomain_maps_all_fields() {
    var createdAt = Instant.parse("2024-10-01T08:00:00Z");
    var updatedAt = Instant.parse("2025-02-01T08:00:00Z");
    var jCohort =
        JCohort.builder()
            .id(COHORT_ID)
            .ref("P24")
            .entryYear(2024)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();

    var cohort = jCohortMapper.toDomain(jCohort);

    assertEquals(COHORT_ID, cohort.getId());
    assertEquals("P24", cohort.getRef());
    assertEquals(Year.of(2024), cohort.getEntryYear());
    assertEquals(createdAt, cohort.getCreatedAt());
    assertEquals(updatedAt, cohort.getUpdatedAt());
  }
}
