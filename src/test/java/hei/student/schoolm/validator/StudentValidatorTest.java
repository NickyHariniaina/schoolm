package hei.student.schoolm.validator;

import static hei.student.schoolm.utils.SemesterValidationTestUtils.STUDENT_ID;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.createStudent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import hei.student.schoolm.exception.NotFoundException;
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
  @Mock StudentRepository studentRepository;
  @InjectMocks StudentValidator studentValidator;

  @Test
  void should_return_student_when_exists() {
    when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(createStudent()));

    var result = studentValidator.checkStudentExists(STUDENT_ID);

    assertEquals(STUDENT_ID, result.getId());
  }

  @Test
  void should_throw_not_found_when_student_missing() {
    var unknownStudent = UUID.fromString("99999999-9999-9999-9999-999999999999");
    when(studentRepository.findById(unknownStudent)).thenReturn(Optional.empty());

    var exception =
        assertThrows(
            NotFoundException.class, () -> studentValidator.checkStudentExists(unknownStudent));

    assertTrue(exception.getMessage().contains(unknownStudent.toString()));
  }
}
