package hei.student.schoolm.dto;

import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Track;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CourseRequest(
    UUID id,
    @NotBlank String ref,
    @NotBlank String title,
    @Min(1) int credit,
    @NotNull Track track,
    @NotNull Semester semester) {}
