package hei.student.schoolm.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MoveStudentGroupRequest(@NotNull(message = "Group id is required") UUID groupId) {}
