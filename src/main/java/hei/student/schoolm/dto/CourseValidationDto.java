package hei.student.schoolm.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseValidationDto {
  private UUID courseId;
  private String ref;
  private String title;
  private int credit;
  private BigDecimal finalGrade;
  private boolean acquired;
}
