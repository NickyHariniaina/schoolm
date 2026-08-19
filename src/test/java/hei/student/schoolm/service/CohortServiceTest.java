package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.dto.CohortRequest;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.repository.CohortRepository;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    assertEquals(List.of("J", "K", "P23"), result.stream().map(dto -> dto.getRef()).toList());
    assertEquals(
        List.of(2024, 2024, 2023), result.stream().map(dto -> dto.getEntryYear()).toList());
  }

  @Test
  void should_get_cohort_by_id() {
    var cohort = Cohort.builder().id(COHORT_J).ref("J").entryYear(Year.of(2024)).build();
    when(cohortRepository.findById(COHORT_J)).thenReturn(Optional.of(cohort));

    var result = cohortService.getById(COHORT_J);

    assertEquals(COHORT_J, result.getId());
    assertEquals("J", result.getRef());
    assertEquals(2024, result.getEntryYear());
  }

  @Test
  void should_throw_when_cohort_not_found() {
    when(cohortRepository.findById(COHORT_J)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> cohortService.getById(COHORT_J));
  }

  @Test
  void should_create_cohort_when_id_is_null() {
    var request = new CohortRequest(null, "promo", 2026);

    when(cohortRepository.save(any(Cohort.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, Cohort.class));

    cohortService.upsert(request);

    var captor = ArgumentCaptor.forClass(Cohort.class);
    verify(cohortRepository).save(captor.capture());
    assertEquals("PROMO", captor.getValue().getRef());
    assertEquals(Year.of(2026), captor.getValue().getEntryYear());
  }

  @Test
  void should_update_existing_cohort_when_id_is_present() {
    var existing = Cohort.builder().id(COHORT_J).ref("OLD").entryYear(Year.of(2024)).build();
    when(cohortRepository.findById(COHORT_J)).thenReturn(Optional.of(existing));
    when(cohortRepository.save(any(Cohort.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, Cohort.class));
    var request = new CohortRequest(COHORT_J, "newref", 2025);

    var result = cohortService.upsert(request);

    assertEquals(COHORT_J, result.getId());
    assertEquals("NEWREF", result.getRef());
    assertEquals(2025, result.getEntryYear());
    var captor = ArgumentCaptor.forClass(Cohort.class);
    verify(cohortRepository).save(captor.capture());
    assertEquals("NEWREF", captor.getValue().getRef());
  }

  @Test
  void should_throw_when_updating_missing_cohort() {
    when(cohortRepository.findById(COHORT_J)).thenReturn(Optional.empty());
    var request = new CohortRequest(COHORT_J, "newref", 2025);

    assertThrows(NotFoundException.class, () -> cohortService.upsert(request));
  }
}
