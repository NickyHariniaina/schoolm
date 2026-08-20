package hei.student.schoolm.service;

import hei.student.schoolm.dto.GradeDto;
import hei.student.schoolm.dto.GradeHistoryDto;
import hei.student.schoolm.dto.GradeRequest;
import hei.student.schoolm.dto.UpdateGradeRequest;
import hei.student.schoolm.exception.ForbiddenException;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Exam;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.repository.GradeRepository;
import hei.student.schoolm.repository.jpa.JGradeHistoryRepository;
import hei.student.schoolm.repository.mapper.JGradeMapper;
import hei.student.schoolm.repository.model.JGradeHistory;
import hei.student.schoolm.util.SecurityUtil;
import hei.student.schoolm.validator.CourseValidator;
import hei.student.schoolm.validator.ExamValidator;
import hei.student.schoolm.validator.GradeValidator;
import hei.student.schoolm.validator.StudentValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradeService {
  private final GradeRepository gradeRepository;
  private final GradeValidator gradeValidator;
  private final JGradeHistoryRepository gradeHistoryRepository;
  private final JGradeMapper mapper;
  private final SecurityUtil securityUtil;
  private final ExamValidator examValidator;
  private final StudentValidator studentValidator;
  private final CourseValidator courseValidator;

  @Transactional(readOnly = true)
  public List<GradeDto> getGradesByExamId(UUID examId) {
    var exam = examValidator.checkExamExists(examId);
    requireTeacherTeachesCourse(exam.getCourse());
    return gradeRepository.findAllByExamId(examId).stream().map(mapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public GradeDto getGradeById(UUID gradeId) {
    var grade = gradeValidator.checkGradeExists(gradeId);
    requireTeacherTeaches(grade);
    return mapper.toDto(grade);
  }

  @Transactional
  public List<GradeDto> upsertGrades(UUID examId, List<GradeRequest> requests) {
    var exam = examValidator.checkExamExists(examId);
    requireTeacherTeachesCourse(exam.getCourse());
    return requests.stream().map(request -> saveGrade(exam, request)).map(mapper::toDto).toList();
  }

  private Grade saveGrade(Exam exam, GradeRequest request) {
    if (request.id() == null) {
      var student = studentValidator.checkStudentExists(request.studentId());
      var grade =
          Grade.builder()
              .id(UUID.randomUUID())
              .student(student)
              .exam(exam)
              .value(request.value())
              .build();
      return gradeRepository.save(grade);
    }

    var grade = gradeValidator.checkGradeExists(request.id());
    if (!grade.getExam().getId().equals(exam.getId())) {
      throw new ForbiddenException("grade does not belong to this exam");
    }
    if (request.changeReason() == null || request.changeReason().isBlank()) {
      throw new hei.student.schoolm.exception.BadRequestException(
          "changeReason is required when updating a grade");
    }
    recordHistory(grade, request.value(), request.changeReason());
    grade.setValue(request.value());
    grade.setChangeReason(request.changeReason());
    return gradeRepository.save(grade);
  }

  @Transactional
  public void delete(UUID gradeId) {
    var grade = gradeValidator.checkGradeExists(gradeId);
    requireTeacherTeaches(grade);
    gradeHistoryRepository.deleteAllByGradeId(gradeId);
    gradeRepository.deleteById(gradeId);
  }

  @Transactional
  public GradeDto updateGrade(UUID gradeId, UpdateGradeRequest request) {
    var grade = gradeValidator.checkGradeExists(gradeId);
    requireTeacherTeaches(grade);
    var oldValue = grade.getValue();

    recordHistory(grade, request.value(), request.changeReason());

    grade.setValue(request.value());
    grade.setChangeReason(request.changeReason());
    var updatedGrade = gradeRepository.save(grade);

    return mapper.toDto(updatedGrade);
  }

  private void recordHistory(Grade grade, java.math.BigDecimal newValue, String changeReason) {
    var history =
        JGradeHistory.builder()
            .id(UUID.randomUUID())
            .gradeId(grade.getId())
            .studentId(grade.getStudent().getId())
            .examId(grade.getExam().getId())
            .oldValue(grade.getValue())
            .newValue(newValue)
            .changeReason(changeReason)
            .build();
    gradeHistoryRepository.save(history);
  }

  @Transactional(readOnly = true)
  public List<GradeHistoryDto> getGradeHistory(UUID gradeId) {

    var grade = gradeValidator.checkGradeExists(gradeId);
    requireTeacherTeaches(grade);

    var history = gradeHistoryRepository.findAllByGradeIdOrderByChangedAtDesc(gradeId);

    return history.stream().map(mapper::toHistoryDto).toList();
  }

  private void requireTeacherTeaches(Grade grade) {
    if (securityUtil.isTeacher()) {
      var teacherId = securityUtil.getCurrentUserIdOrThrow();
      var teaches = grade.getExam().getCourse().teacherIds().contains(teacherId);
      if (!teaches) {
        throw new ForbiddenException("You may only manage grades for courses you teach");
      }
    }
  }

  private void requireTeacherTeachesCourse(Course course) {
    if (securityUtil.isTeacher()) {
      var teacherId = securityUtil.getCurrentUserIdOrThrow();
      var loadedCourse =
          course.getId() == null ? course : courseValidator.checkCourseExists(course.getId());
      var teaches = loadedCourse.teacherIds().contains(teacherId);
      if (!teaches) {
        throw new ForbiddenException("You may only manage grades for courses you teach");
      }
    }
  }
}
