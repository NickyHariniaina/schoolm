package hei.student.schoolm.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.repository.TeacherRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TeacherValidatorTest {
  private static final UUID TEACHER_TOKY = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID TEACHER_YUME = UUID.fromString("00000000-0000-0000-0000-000000000003");

  private final TeacherRepository teacherRepository = Mockito.mock(TeacherRepository.class);
  private final TeacherValidator teacherValidator = new TeacherValidator(teacherRepository);

  private Teacher createTeacher(UUID id) {
    return Teacher.builder().id(id).build();
  }

  @Test
  void should_return_teachers_when_all_exist() {
    var toky = createTeacher(TEACHER_TOKY);
    var yume = createTeacher(TEACHER_YUME);
    when(teacherRepository.findAllById(List.of(TEACHER_TOKY, TEACHER_YUME)))
        .thenReturn(List.of(toky, yume));

    var result = teacherValidator.checkTeachersExist(List.of(TEACHER_TOKY, TEACHER_YUME));

    assertEquals(List.of(toky, yume), result);
  }

  @Test
  void should_throw_not_found_when_some_teachers_missing() {
    var unknownTeacher = UUID.fromString("99999999-9999-9999-9999-999999999998");
    when(teacherRepository.findAllById(List.of(TEACHER_TOKY, unknownTeacher)))
        .thenReturn(List.of(createTeacher(TEACHER_TOKY)));

    var exception =
        assertThrows(
            NotFoundException.class,
            () -> teacherValidator.checkTeachersExist(List.of(TEACHER_TOKY, unknownTeacher)));

    assertTrue(exception.getMessage().contains(unknownTeacher.toString()));
  }

  @Test
  void should_deduplicate_requested_ids() {
    var toky = createTeacher(TEACHER_TOKY);
    when(teacherRepository.findAllById(List.of(TEACHER_TOKY))).thenReturn(List.of(toky));

    var result = teacherValidator.checkTeachersExist(List.of(TEACHER_TOKY, TEACHER_TOKY));

    verify(teacherRepository).findAllById(List.of(TEACHER_TOKY));
    assertEquals(List.of(toky), result);
  }
}
