package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.dto.MoveStudentGroupRequest;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.GroupFlow;
import hei.student.schoolm.model.GroupFlowType;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.repository.GroupFlowRepository;
import hei.student.schoolm.repository.StudentRepository;
import hei.student.schoolm.util.SecurityUtil;
import hei.student.schoolm.validator.GroupValidator;
import hei.student.schoolm.validator.StudentValidator;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupFlowServiceTest {
  @Mock private GroupFlowRepository groupFlowRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private StudentValidator studentValidator;
  @Mock private GroupValidator groupValidator;
  @Mock private SecurityUtil securityUtil;
  @InjectMocks private GroupFlowService groupFlowService;

  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000701");
  private static final UUID GROUP_OLD_ID = UUID.fromString("00000000-0000-0000-0000-000000000702");
  private static final UUID GROUP_NEW_ID = UUID.fromString("00000000-0000-0000-0000-000000000703");

  @BeforeEach
  void setUp() {
    org.mockito.Mockito.lenient().when(securityUtil.isAdmin()).thenReturn(true);
  }

  private Student student() {
    return Student.builder()
        .id(STUDENT_ID)
        .group(Group.builder().id(GROUP_OLD_ID).ref("L1-EL-01").build())
        .build();
  }

  @Test
  void should_return_history_ordered_by_created_at_desc() {
    var student = student();
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    var flow =
        GroupFlow.builder()
            .id(UUID.randomUUID())
            .student(student)
            .group(Group.builder().id(GROUP_OLD_ID).build())
            .groupFlowType(GroupFlowType.JOIN)
            .createdAt(Instant.parse("2024-10-01T00:00:00Z"))
            .build();
    when(groupFlowRepository.findByStudentId(STUDENT_ID)).thenReturn(List.of(flow));

    var result = groupFlowService.getHistory(STUDENT_ID);

    assertEquals(1, result.size());
    assertEquals(GROUP_OLD_ID, result.get(0).groupId());
    assertEquals(GroupFlowType.JOIN, result.get(0).groupFlowType());
  }

  @Test
  void should_throw_when_student_missing_for_history() {
    when(studentValidator.checkStudentExists(STUDENT_ID))
        .thenThrow(new NotFoundException("Student " + STUDENT_ID + " not found"));

    assertThrows(NotFoundException.class, () -> groupFlowService.getHistory(STUDENT_ID));
  }

  @Test
  void should_move_student_to_new_group_and_keep_old_group_in_union() {
    var student = student();
    var newGroup = Group.builder().id(GROUP_NEW_ID).ref("L1-TN-01").build();
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(groupValidator.checkGroupExists(GROUP_NEW_ID)).thenReturn(newGroup);
    var savedFlow =
        GroupFlow.builder()
            .id(UUID.randomUUID())
            .student(student)
            .group(newGroup)
            .groupFlowType(GroupFlowType.JOIN)
            .createdAt(Instant.parse("2025-09-01T00:00:00Z"))
            .build();
    when(groupFlowRepository.save(org.mockito.ArgumentMatchers.any(GroupFlow.class)))
        .thenReturn(savedFlow);

    var result = groupFlowService.move(STUDENT_ID, new MoveStudentGroupRequest(GROUP_NEW_ID));

    assertEquals(GROUP_NEW_ID, result.groupId());
    assertEquals(GroupFlowType.JOIN, result.groupFlowType());
    verify(studentRepository).save(student);
  }

  @Test
  void should_return_current_and_flow_group_ids_as_union() {
    var student = student();
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(groupFlowRepository.findGroupIdsByStudentId(STUDENT_ID)).thenReturn(List.of(GROUP_NEW_ID));

    var result = groupFlowService.studentGroupIds(STUDENT_ID);

    assertEquals(2, result.size());
    assertTrue(result.contains(GROUP_OLD_ID));
    assertTrue(result.contains(GROUP_NEW_ID));
  }
}
