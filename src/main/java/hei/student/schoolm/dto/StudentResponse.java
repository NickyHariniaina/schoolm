package hei.student.schoolm.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record StudentResponse(
    UUID id,
    String reference,
    String firstName,
    String lastName,
    String email,
    UUID groupId,
    String groupRef) {}
