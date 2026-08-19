package hei.student.schoolm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AdminRequest(
    @NotBlank String firstName, @NotBlank String lastName, @NotBlank @Email String email) {}
