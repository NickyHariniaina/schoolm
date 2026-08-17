package hei.student.schoolm.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateGradeRequest(
    @NotNull(message = "Grade value is required")
        @DecimalMin(value = "0.0", message = "Grade must be at least 0")
        @DecimalMax(value = "20.0", message = "Grade must be at most 20")
        BigDecimal value,
    @NotBlank(message = "Change reason is required") String changeReason) {}
