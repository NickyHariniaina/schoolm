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
public class GroupFlow {
  private UUID id;
  private Student student;
  private Group group;
  private GroupFlowType groupFlowType;
  private Instant createdAt;
}
