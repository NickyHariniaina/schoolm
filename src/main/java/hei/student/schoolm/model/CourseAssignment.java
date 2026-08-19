package hei.student.schoolm.model;

import java.time.Instant;
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
public class CourseAssignment {
  private UUID id;
  private Course course;
  private Group group;
  private List<Teacher> teachers;
  private int academicYear;
  private Semester semester;
  private int credits;
  private Instant createdAt;
  private Instant updatedAt;

  public List<UUID> teacherIds() {
    return teachers == null ? List.of() : teachers.stream().map(Teacher::getId).toList();
  }
}
