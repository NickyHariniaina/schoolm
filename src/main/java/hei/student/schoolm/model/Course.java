package hei.student.schoolm.model;

import java.time.Instant;
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

  public enum Track {
    COMMON,
    EL,
    TN
  }

  public enum Semester {
    S1,
    S2,
    S3,
    S4,
    S5,
    S6
  }

}
