package hei.student.schoolm.dto;

import hei.student.schoolm.model.User.Role;
import java.util.UUID;

public record UserResponse(UUID id, String firstName, String lastName, String email, Role role) {}
