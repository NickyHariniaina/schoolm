package hei.student.schoolm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record TeacherIdsRequest(@NotEmpty List<@NotNull UUID> teacherIds) {}
