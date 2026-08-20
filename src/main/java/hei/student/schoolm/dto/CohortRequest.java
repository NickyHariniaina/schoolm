package hei.student.schoolm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CohortRequest(UUID id, @NotBlank String ref, @NotNull Integer entryYear) {}
