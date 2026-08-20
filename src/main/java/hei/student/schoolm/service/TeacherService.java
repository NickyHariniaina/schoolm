package hei.student.schoolm.service;

import hei.student.schoolm.dto.TeacherRequest;
import hei.student.schoolm.dto.TeacherResponse;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.exception.ConflictException;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.model.User;
import hei.student.schoolm.repository.CourseRepository;
import hei.student.schoolm.repository.TeacherRepository;
import hei.student.schoolm.util.SecurityUtil;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherService {
  private final TeacherRepository teacherRepository;
  private final CourseRepository courseRepository;
  private final PasswordEncoder passwordEncoder;
  private final SecurityUtil securityUtil;

  @Transactional(readOnly = true)
  public List<TeacherResponse> getAll() {
    return teacherRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public TeacherResponse getById(UUID id) {
    securityUtil.requireSelfOrStaff(id);
    return toResponse(getEntityOrThrow(id));
  }

  @Transactional
  public TeacherResponse upsert(TeacherRequest request) {
    if (request.id() == null) {
      return create(request);
    }
    return update(request);
  }

  private TeacherResponse create(TeacherRequest request) {
    if (request.password() == null || request.password().isBlank()) {
      throw new BadRequestException("password is required when creating a teacher");
    }

    var teacher =
        Teacher.builder()
            .id(UUID.randomUUID())
            .email(request.email())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .role(User.Role.TEACHER)
            .password(passwordEncoder.encode(request.password()))
            .build();

    var saved = teacherRepository.save(teacher);
    return toResponse(saved);
  }

  private TeacherResponse update(TeacherRequest request) {
    var teacher = getEntityOrThrow(request.id());
    teacher.setEmail(request.email());
    teacher.setFirstName(request.firstName());
    teacher.setLastName(request.lastName());
    if (request.password() != null && !request.password().isBlank()) {
      teacher.setPassword(passwordEncoder.encode(request.password()));
    }

    var saved = teacherRepository.save(teacher);
    return toResponse(saved);
  }

  @Transactional
  public void delete(UUID id) {
    if (!securityUtil.isAdmin()) {
      throw new hei.student.schoolm.exception.ForbiddenException(
          "Only an admin can delete a teacher");
    }
    getEntityOrThrow(id);
    if (courseRepository.existsByTeacherId(id)) {
      throw new ConflictException("Teacher is still assigned to a course");
    }
    teacherRepository.deleteById(id);
  }

  public Teacher getEntityOrThrow(UUID id) {
    return teacherRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Teacher not found: " + id));
  }

  private TeacherResponse toResponse(Teacher teacher) {
    return new TeacherResponse(
        teacher.getId(), teacher.getFirstName(), teacher.getLastName(), teacher.getEmail());
  }
}
