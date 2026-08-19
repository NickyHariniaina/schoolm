package hei.student.schoolm.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record AdminResponse(UUID id, String firstName, String lastName, String email) {}
