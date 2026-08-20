package hei.student.schoolm.dto;

import hei.student.schoolm.model.Track;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GroupRequest(
    UUID id, @NotNull UUID cohortId, @NotBlank String ref, @NotNull Track track) {}
