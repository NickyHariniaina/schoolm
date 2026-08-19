package hei.student.schoolm.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ExamDto(
    UUID id,
    UUID courseId,
    LocalDate dateExam,
    int coefNumerator,
    int coefDenominator,
    Instant createdAt,
    Instant updatedAt) {}
