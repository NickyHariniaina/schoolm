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
public class Course {
  private UUID id;
  private String ref;
  private String title;
  private int credit;
  private Track track;
  private Semester semester;
  private Instant createdAt;
  private Instant updatedAt;
  private List<Teacher> teachers;
  private List<Group> groups;
  private List<Exam> exams;
  private List<Grade> grades;

  public List<UUID> teacherIds() {
    return teachers == null ? List.of() : teachers.stream().map(Teacher::getId).toList();
  }
}
