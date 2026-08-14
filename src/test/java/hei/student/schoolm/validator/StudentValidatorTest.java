package hei.student.schoolm.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.repository.StudentRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentValidatorTest {
  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Mock private StudentRepository studentRepository;
  @InjectMocks private StudentValidator studentValidator;

  private Student createStudent() {
    return Student.builder().id(STUDENT_ID).reference("S-001").build();
  }

  @Test
  void should_return_student_when_exists() {
    var student = createStudent();
    when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

    var result = studentValidator.checkStudentExists(STUDENT_ID);

    assertEquals(student, result);
  }

  @Test
  void should_throw_not_found_when_student_missing() {
    when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

    var exception =
        assertThrows(NotFoundException.class, () -> studentValidator.checkStudentExists(STUDENT_ID));

    assertTrue(exception.getMessage().contains(STUDENT_ID.toString()));
  }
}