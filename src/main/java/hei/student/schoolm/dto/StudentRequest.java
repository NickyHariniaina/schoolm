package hei.student.schoolm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;

@Builder
public record StudentRequest(
    UUID id,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank @Email String email,
    String password,
    UUID groupId) {}
