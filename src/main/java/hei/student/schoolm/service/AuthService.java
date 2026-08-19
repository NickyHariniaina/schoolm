package hei.student.schoolm.service;

import hei.student.schoolm.dto.AuthResponse;
import hei.student.schoolm.dto.LoginRequest;
import hei.student.schoolm.dto.UserResponse;
import hei.student.schoolm.endpoint.rest.security.JwtService;
import hei.student.schoolm.exception.UnauthorizedException;
import hei.student.schoolm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  public AuthResponse login(LoginRequest request) {
    var user =
        userRepository
            .findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

    if (user.password() == null || !passwordEncoder.matches(request.password(), user.password())) {
      throw new UnauthorizedException("Invalid credentials");
    }

    var token = jwtService.generateToken(user.id(), user.email(), user.role());
    var userResponse =
        new UserResponse(user.id(), user.firstName(), user.lastName(), user.email(), user.role());
    return new AuthResponse(token, userResponse);
  }
}
