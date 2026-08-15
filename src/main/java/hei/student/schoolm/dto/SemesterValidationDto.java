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
public class SemesterValidationDto {
  private UUID studentId;
  private String studentRef;
  private String firstName;
  private String lastName;
  private String groupRef;
  private Semester semester;
  private int totalCredits;
  private int acquiredCredits;
  private boolean validated;
  private List<CourseValidationDto> courses;
}
