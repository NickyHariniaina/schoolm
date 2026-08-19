package hei.student.schoolm.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GradeRequest(
    UUID id,
    @NotNull UUID studentId,
    @NotNull
        @DecimalMin(value = "0.0", message = "Grade must be at least 0")
        @DecimalMax(value = "20.0", message = "Grade must be at most 20")
        BigDecimal value,
    String changeReason) {}
