package hei.student.schoolm.model;

import hei.student.schoolm.model.User.Role;
import java.util.UUID;

public record AuthUser(
    UUID id, String firstName, String lastName, String email, String password, Role role) {}
