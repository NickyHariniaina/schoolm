package hei.student.schoolm.dto;

import hei.student.schoolm.model.Semester;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranscriptPdfDto {
  private UUID studentId;
  private String studentRef;
  private String firstName;
  private String lastName;
  private String groupRef;
  private String cohortRef;
  private String academicYear;
  private List<Semester> semesters;
  private List<CourseGradeDto> courses;
  private TranscriptStatus status;
  private Double average;
  private Integer totalCredits;
  private Integer acquiredCredits;
}
