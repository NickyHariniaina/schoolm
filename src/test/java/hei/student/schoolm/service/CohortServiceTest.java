package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.repository.CohortRepository;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CohortServiceTest {
  private static final UUID COHORT_J = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID COHORT_K = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID COHORT_P23 = UUID.fromString("00000000-0000-0000-0000-000000000003");

  @Mock CohortRepository cohortRepository;
  @InjectMocks CohortService cohortService;

  @Test
  void should_sort_cohorts_by_entry_year_desc_then_ref_asc() {
    var j = Cohort.builder().id(COHORT_J).ref("J").entryYear(Year.of(2024)).build();
    var k = Cohort.builder().id(COHORT_K).ref("K").entryYear(Year.of(2024)).build();
    var p23 = Cohort.builder().id(COHORT_P23).ref("P23").entryYear(Year.of(2023)).build();
    when(cohortRepository.findAll()).thenReturn(List.of(k, p23, j));

    var result = cohortService.getCohorts();

    assertEquals(List.of("K", "J", "P23"), result.stream().map(dto -> dto.getRef()).toList());
    assertEquals(
        List.of(2024, 2024, 2023), result.stream().map(dto -> dto.getEntryYear()).toList());
  }
}
