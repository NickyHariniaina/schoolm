package hei.student.schoolm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ExamRequest(
    UUID id,
    @NotNull UUID courseId,
    @NotNull LocalDate dateExam,
    @Positive int coefNumerator,
    @Positive int coefDenominator) {}
