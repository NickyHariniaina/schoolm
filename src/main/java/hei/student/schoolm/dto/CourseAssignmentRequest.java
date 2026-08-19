package hei.student.schoolm.dto;

import hei.student.schoolm.model.Semester;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CourseAssignmentRequest(
    UUID id,
    @NotNull(message = "Course id is required") UUID courseId,
    @NotNull(message = "Group id is required") UUID groupId,
    @NotEmpty(message = "At least one teacher is required") List<@NotNull UUID> teacherIds,
    @NotNull(message = "Academic year is required")
        @Min(value = 2000, message = "Academic year must be at least 2000")
        @Max(value = 2100, message = "Academic year must be at most 2100")
        Integer academicYear,
    @NotNull(message = "Semester is required") Semester semester,
    @NotNull(message = "Credits are required")
        @Min(value = 1, message = "Credits must be at least 1")
        Integer credits) {}
