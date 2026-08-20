package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.dto.GroupRequest;
import hei.student.schoolm.dto.StudentResponse;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.repository.GroupRepository;
import hei.student.schoolm.repository.StudentRepository;
import hei.student.schoolm.validator.GroupValidator;
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
class GroupServiceTest {
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID COHORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

  @Mock private GroupRepository groupRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private CohortService cohortService;
  @Mock private GroupValidator groupValidator;
  @InjectMocks private GroupService groupService;

  @Test
  void should_return_group_cohort() {
    var cohort = Cohort.builder().id(COHORT_ID).ref("P24").entryYear(Year.of(2024)).build();
    var group = Group.builder().id(GROUP_ID).ref("L1-EL-01").cohort(cohort).build();
    when(groupValidator.checkGroupExists(GROUP_ID)).thenReturn(group);

    var result = groupService.getCohort(GROUP_ID);

    assertEquals(cohort, result);
  }

  @Test
  void should_get_group_by_id() {
    var group = buildGroup();
    when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.of(group));

    var result = groupService.getById(GROUP_ID);

    assertEquals(GROUP_ID, result.id());
    assertEquals("L1-EL-01", result.ref());
    assertEquals(COHORT_ID, result.cohortId());
    assertEquals("P24", result.cohortRef());
    assertEquals(Track.EL, result.track());
  }

  @Test
  void should_list_all_groups_when_no_cohort_filter() {
    when(groupRepository.findAll()).thenReturn(List.of(buildGroup()));

    var result = groupService.getAll(null);

    assertEquals(1, result.size());
    assertEquals("L1-EL-01", result.get(0).ref());
  }

  @Test
  void should_list_groups_by_cohort() {
    when(groupRepository.findAllByCohortId(COHORT_ID)).thenReturn(List.of(buildGroup()));

    var result = groupService.getAll(COHORT_ID);

    assertEquals(1, result.size());
  }

  @Test
  void should_create_group_when_id_is_null() {
    var cohort = buildCohort();
    when(cohortService.getEntityOrThrow(COHORT_ID)).thenReturn(cohort);
    when(groupRepository.save(any(Group.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, Group.class));
    var request = new GroupRequest(null, COHORT_ID, "l1-el-01", Track.EL);

    var result = groupService.upsert(request);

    assertEquals("L1-EL-01", result.ref());
    assertEquals(COHORT_ID, result.cohortId());
    assertEquals(Track.EL, result.track());
    var captor = ArgumentCaptor.forClass(Group.class);
    verify(groupRepository).save(captor.capture());
    assertEquals("L1-EL-01", captor.getValue().getRef());
    assertEquals(COHORT_ID, captor.getValue().getCohort().getId());
  }

  @Test
  void should_update_existing_group_when_id_is_present() {
    var existing = buildGroup();
    when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.of(existing));
    when(cohortService.getEntityOrThrow(COHORT_ID)).thenReturn(buildCohort());
    when(groupRepository.save(any(Group.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, Group.class));
    var request = new GroupRequest(GROUP_ID, COHORT_ID, "l1-tn-02", Track.TN);

    var result = groupService.upsert(request);

    assertEquals("L1-TN-02", result.ref());
    assertEquals(Track.TN, result.track());
  }

  @Test
  void should_throw_when_updating_missing_group() {
    when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.empty());
    var request = new GroupRequest(GROUP_ID, COHORT_ID, "l1-tn-02", Track.TN);

    assertThrows(NotFoundException.class, () -> groupService.upsert(request));
  }

  @Test
  void should_list_students_of_a_group() {
    when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.of(buildGroup()));
    var student = buildStudent();
    when(studentRepository.findAllByGroupId(GROUP_ID)).thenReturn(List.of(student));

    var result = groupService.getStudents(GROUP_ID);

    assertEquals(1, result.size());
    StudentResponse response = result.get(0);
    assertEquals(STUDENT_ID, response.id());
    assertEquals("STD-1", response.reference());
    assertEquals("John", response.firstName());
    assertEquals(GROUP_ID, response.groupId());
  }

  @Test
  void should_throw_when_listing_students_of_missing_group() {
    when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> groupService.getStudents(GROUP_ID));
  }

  private Group buildGroup() {
    return Group.builder()
        .id(GROUP_ID)
        .ref("L1-EL-01")
        .cohort(buildCohort())
        .track(Track.EL)
        .build();
  }

  private Cohort buildCohort() {
    return Cohort.builder().id(COHORT_ID).ref("P24").entryYear(Year.of(2024)).build();
  }

  private Student buildStudent() {
    return Student.builder()
        .id(STUDENT_ID)
        .reference("STD-1")
        .firstName("John")
        .lastName("Doe")
        .email("john.doe@hei.school")
        .group(buildGroup())
        .build();
  }
}
