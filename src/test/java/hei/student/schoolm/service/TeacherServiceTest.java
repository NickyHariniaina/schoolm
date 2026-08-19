package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.dto.TeacherRequest;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.exception.ConflictException;
import hei.student.schoolm.exception.ForbiddenException;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.model.User;
import hei.student.schoolm.repository.CourseRepository;
import hei.student.schoolm.repository.TeacherRepository;
import hei.student.schoolm.util.SecurityUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {
  private static final UUID TEACHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Mock TeacherRepository teacherRepository;
  @Mock CourseRepository courseRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock SecurityUtil securityUtil;
  @InjectMocks TeacherService teacherService;

  @Test
  void should_list_all_teachers() {
    when(teacherRepository.findAll()).thenReturn(List.of(buildTeacher()));

    var result = teacherService.getAll();

    assertEquals(1, result.size());
    assertEquals("John", result.get(0).firstName());
    assertEquals("john.doe@hei.school", result.get(0).email());
  }

  @Test
  void should_get_teacher_by_id() {
    when(securityUtil.isAdmin()).thenReturn(true);
    when(teacherRepository.findById(TEACHER_ID)).thenReturn(Optional.of(buildTeacher()));

    var result = teacherService.getById(TEACHER_ID);

    assertEquals(TEACHER_ID, result.id());
    assertEquals("John", result.firstName());
  }

  @Test
  void should_throw_when_teacher_not_found() {
    when(securityUtil.isAdmin()).thenReturn(true);
    when(teacherRepository.findById(TEACHER_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> teacherService.getById(TEACHER_ID));
  }

  @Test
  void should_create_teacher_with_encoded_password() {
    when(passwordEncoder.encode("secret")).thenReturn("encoded");
    when(teacherRepository.save(any(Teacher.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, Teacher.class));
    var request = new TeacherRequest(null, "John", "Doe", "john.doe@hei.school", "secret");

    var result = teacherService.upsert(request);

    assertEquals("john.doe@hei.school", result.email());
    var captor = ArgumentCaptor.forClass(Teacher.class);
    verify(teacherRepository).save(captor.capture());
    assertEquals("encoded", captor.getValue().getPassword());
    assertEquals(User.Role.TEACHER, captor.getValue().getRole());
  }

  @Test
  void should_require_password_when_creating_teacher() {
    var request = new TeacherRequest(null, "John", "Doe", "john.doe@hei.school", null);

    assertThrows(BadRequestException.class, () -> teacherService.upsert(request));
    verify(teacherRepository, never()).save(any());
  }

  @Test
  void should_update_teacher_without_changing_password() {
    var existing = buildTeacher();
    existing.setPassword("old-encoded");
    when(teacherRepository.findById(TEACHER_ID)).thenReturn(Optional.of(existing));
    when(teacherRepository.save(any(Teacher.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, Teacher.class));
    var request = new TeacherRequest(TEACHER_ID, "Jane", "Doe", "jane.doe@hei.school", null);

    var result = teacherService.upsert(request);

    assertEquals("Jane", result.firstName());
    assertEquals("jane.doe@hei.school", result.email());
    var captor = ArgumentCaptor.forClass(Teacher.class);
    verify(teacherRepository).save(captor.capture());
    assertEquals("old-encoded", captor.getValue().getPassword());
  }

  @Test
  void should_update_password_when_provided() {
    var existing = buildTeacher();
    when(teacherRepository.findById(TEACHER_ID)).thenReturn(Optional.of(existing));
    when(passwordEncoder.encode("newpass")).thenReturn("new-encoded");
    when(teacherRepository.save(any(Teacher.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, Teacher.class));
    var request = new TeacherRequest(TEACHER_ID, "John", "Doe", "john.doe@hei.school", "newpass");

    teacherService.upsert(request);

    var captor = ArgumentCaptor.forClass(Teacher.class);
    verify(teacherRepository).save(captor.capture());
    assertEquals("new-encoded", captor.getValue().getPassword());
  }

  @Test
  void should_throw_when_updating_missing_teacher() {
    when(teacherRepository.findById(TEACHER_ID)).thenReturn(Optional.empty());
    var request = new TeacherRequest(TEACHER_ID, "John", "Doe", "john.doe@hei.school", null);

    assertThrows(NotFoundException.class, () -> teacherService.upsert(request));
  }

  @Test
  void should_delete_teacher_as_admin() {
    when(securityUtil.isAdmin()).thenReturn(true);
    when(teacherRepository.findById(TEACHER_ID)).thenReturn(Optional.of(buildTeacher()));
    when(courseRepository.existsByTeacherId(TEACHER_ID)).thenReturn(false);

    teacherService.delete(TEACHER_ID);

    verify(teacherRepository).deleteById(TEACHER_ID);
  }

  @Test
  void should_reject_delete_when_not_admin() {
    when(securityUtil.isAdmin()).thenReturn(false);

    assertThrows(ForbiddenException.class, () -> teacherService.delete(TEACHER_ID));
    verify(teacherRepository, never()).deleteById(any());
  }

  @Test
  void should_reject_delete_when_assigned_to_course() {
    when(securityUtil.isAdmin()).thenReturn(true);
    when(teacherRepository.findById(TEACHER_ID)).thenReturn(Optional.of(buildTeacher()));
    when(courseRepository.existsByTeacherId(TEACHER_ID)).thenReturn(true);

    assertThrows(ConflictException.class, () -> teacherService.delete(TEACHER_ID));
    verify(teacherRepository, never()).deleteById(any());
  }

  private Teacher buildTeacher() {
    return Teacher.builder()
        .id(TEACHER_ID)
        .firstName("John")
        .lastName("Doe")
        .email("john.doe@hei.school")
        .role(User.Role.TEACHER)
        .build();
  }
}
