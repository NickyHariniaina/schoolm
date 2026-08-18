package hei.student.schoolm.dto;

import java.math.BigDecimal;

public record GraduateEntry(
    int rank, String studentRef, String firstName, String lastName, BigDecimal average) {}
