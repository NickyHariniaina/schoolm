package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.validator.StudentValidator;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Mock private StudentValidator studentValidator;
  @InjectMocks private StudentService studentService;

  @Test
  void should_return_student_group() {
    var group = Group.builder().id(GROUP_ID).ref("L1-EL-01").build();
    var student = Student.builder().id(STUDENT_ID).reference("S-001").group(group).build();
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);

    var result = studentService.getGroup(STUDENT_ID);

    assertEquals(group, result);
  }
}