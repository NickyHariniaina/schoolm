package hei.student.schoolm.model;

import hei.student.schoolm.util.Fraction;
import java.time.Instant;
import java.time.LocalDate;
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
public class Exam {
  private UUID id;
  private Course course;
  private LocalDate dateExam;
  private List<Grade> grades;
  private Fraction coefficient;
  private Instant createdAt;
  private Instant updatedAt;
}
