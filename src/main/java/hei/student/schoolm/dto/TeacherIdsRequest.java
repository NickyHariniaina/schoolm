package hei.student.schoolm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherIdsRequest {
  @NotEmpty private List<@NotNull UUID> teacherIds;
}
