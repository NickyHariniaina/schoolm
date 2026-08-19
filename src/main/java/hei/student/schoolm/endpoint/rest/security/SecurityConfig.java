package hei.student.schoolm.endpoint.rest.security;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final RequestMappingHandlerMapping handlerMapping;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/auth/**",
                        "/ping",
                        "/health/**",
                        "/error",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-ui",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/doc/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico")
                    .permitAll()
                    .requestMatchers("/ui/login", "/ui/logout")
                    .permitAll()
                    .requestMatchers("/", "/cohorts/{ref}/graduates/download")
                    .hasRole("ADMIN")
                    .requestMatchers(GET, "/cohorts/{ref}/graduates")
                    .hasRole("ADMIN")
                    .requestMatchers(GET, "/cohorts/*/graduates")
                    .hasRole("ADMIN")
                    .requestMatchers(GET, "/course-assignments/curriculum-status")
                    .hasRole("ADMIN")
                    .requestMatchers(PUT, "/cohorts")
                    .hasRole("ADMIN")
                    .requestMatchers(PUT, "/groups")
                    .hasRole("ADMIN")
                    .requestMatchers(GET, "/groups", "/groups/*", "/groups/*/students")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(PUT, "/teachers")
                    .hasRole("ADMIN")
                    .requestMatchers(DELETE, "/teachers/*")
                    .hasRole("ADMIN")
                    .requestMatchers(GET, "/teachers", "/teachers/*")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(PUT, "/course-assignments/**")
                    .hasRole("ADMIN")
                    .requestMatchers(DELETE, "/course-assignments/**")
                    .hasRole("ADMIN")
                    .requestMatchers(PUT, "/students/**")
                    .hasRole("ADMIN")
                    .requestMatchers(PUT, "/students")
                    .hasRole("ADMIN")
                    .requestMatchers(DELETE, "/students/*")
                    .hasRole("ADMIN")
                    .requestMatchers(GET, "/students")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(GET, "/students/*")
                    .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                    .requestMatchers(PUT, "/courses/**")
                    .hasRole("ADMIN")
                    .requestMatchers(DELETE, "/courses/*")
                    .hasRole("ADMIN")
                    .requestMatchers(PUT, "/grades/**")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(GET, "/grades/*/history")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(GET, "/courses", "/courses/*")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(GET, "/courses/*/exams")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(PUT, "/exams")
                    .hasRole("ADMIN")
                    .requestMatchers(GET, "/exams/*")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(DELETE, "/exams/*")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        GET,
                        "/cohorts",
                        "/cohorts/*",
                        "/course-assignments",
                        "/course-assignments/*",
                        "/students/*/group-flows")
                    .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                    .requestMatchers(
                        GET, "/students/*/semester-validation", "/students/*/graduate-transcript")
                    .authenticated()
                    .requestMatchers(POST, "/students/*/transcript/email")
                    .authenticated()
                    .anyRequest()
                    .authenticated())
        .httpBasic(Customizer.withDefaults())
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                    (request, response, authException) -> {
                      // A request that does not map to any controller is a non-existent endpoint,
                      // so answer 404 rather than leaking an auth challenge for it.
                      boolean mapsToHandler;
                      try {
                        mapsToHandler = handlerMapping.getHandler(request) != null;
                      } catch (Exception ignored) {
                        mapsToHandler = true;
                      }
                      if (!mapsToHandler) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                      }
                      // Keep the WWW-Authenticate header so browsers prompt for credentials
                      // when opening the /ui pages without a bearer token.
                      response.setHeader("WWW-Authenticate", "Basic realm=\"hei\"");
                      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    }))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
