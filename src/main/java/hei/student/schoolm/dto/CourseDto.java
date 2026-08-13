package hei.student.schoolm.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
  private UUID id;
  private String ref;
  private String title;
  private int credit;
  private String track;
  private String semester;
  private List<UUID> teacherIds;
}
