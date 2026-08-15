package hei.student.schoolm.dto;

import hei.student.schoolm.model.Semester;
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
public class TranscriptDto {
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
}
