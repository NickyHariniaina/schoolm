package hei.student.schoolm.validator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.repository.GradeRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GradeValidatorTest {
  @Mock private GradeRepository gradeRepository;

  @InjectMocks private GradeValidator gradeValidator;

  @Test
  void should_return_grade_when_exists() {
    var gradeId = UUID.randomUUID();
    var grade = Grade.builder().id(gradeId).value(new BigDecimal("15.0")).build();
    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

    var result = gradeValidator.checkGradeExists(gradeId);

    assertNotNull(result);
    assertEquals(gradeId, result.getId());
    assertEquals(new BigDecimal("15.0"), result.getValue());
  }

  @Test
  void should_throw_NotFound_when_grade_does_not_exist() {
    var gradeId = UUID.randomUUID();
    when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());

    var exception =
        assertThrows(NotFoundException.class, () -> gradeValidator.checkGradeExists(gradeId));
    assertTrue(exception.getMessage().contains("Grade " + gradeId + " not found"));
  }
}
