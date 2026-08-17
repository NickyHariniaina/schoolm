package hei.student.schoolm.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public final class Student extends User {
  private static final BigDecimal PASSING_GRADE = BigDecimal.TEN;

  private String reference;
  private Group group;

  public boolean validate(Course course) {
    var finalGrade = course.finalGradeFor(this);
    return finalGrade != null && finalGrade.compareTo(PASSING_GRADE) >= 0;
  }
}
