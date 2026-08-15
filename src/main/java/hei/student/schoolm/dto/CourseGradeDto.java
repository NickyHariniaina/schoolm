package hei.student.schoolm.dto;

import hei.student.schoolm.model.Semester;
import java.math.BigDecimal;
import java.util.List;
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
public class CourseGradeDto {
  private UUID courseId;
  private String ref;
  private String title;
  private Semester semester;
  private int credit;
  private double coefficientSum;
  private List<ExamGradeDto> exams;
  private BigDecimal finalGrade;
  private TranscriptStatus status;
}
