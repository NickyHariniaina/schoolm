package hei.student.schoolm.service;

import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.dto.TranscriptDto;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.mapper.StudentMapper;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.validator.GroupValidator;
import hei.student.schoolm.validator.StudentValidator;
import java.time.LocalDate;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {
  private final StudentValidator studentValidator;
  private final GroupValidator groupValidator;
  private final StudentMapper studentMapper;

  @Transactional(readOnly = true)
  public SemesterValidationDto getStudentSemesterValidation(UUID studentId, Semester semester) {
    var student = studentValidator.checkStudentExists(studentId);
    var group = groupValidator.checkGroupExists(student.getGroup().getId());
    var courses =
        group.getCourses() == null
            ? List.<Course>of()
            : group.getCourses().stream()
                .filter(course -> course.getSemester() == semester)
                .sorted(
                    Comparator.comparingInt((Course course) -> course.getSemester().ordinal())
                        .thenComparing(Course::getRef))
                .toList();
    return studentMapper.toSemesterValidationDto(student, group, semester, courses);
  }

  public Group getGroup(UUID studentId) {
    var student = studentValidator.checkStudentExists(studentId);
    return student.getGroup();
  }

  public TranscriptDto getTranscript(UUID studentId, Integer month, Integer year) {
    if (month != null && (month < 1 || month > 12)) {
      throw new BadRequestException("month must be between 1 and 12");
    }
    var student = studentValidator.checkStudentExists(studentId);
    var group = groupValidator.checkGroupExists(student.getGroup().getId());
    var entryYear = group.getCohort().getEntryYear();
    var semester = resolveSemester(month, year, entryYear);
    var courses = filterCourses(group.getCourses(), semester.pair());
    return studentMapper.toTranscriptDto(student, group, semester, courses);
  }

  public List<Course> filterCourses(List<Course> courses, List<Semester> pair) {
    return courses.stream()
        .filter(course -> pair.contains(course.getSemester()))
        .sorted(
            Comparator.comparingInt((Course course) -> course.getSemester().ordinal())
                .thenComparing(Course::getRef))
        .toList();
  }

  private Semester resolveSemester(Integer month, Integer year, Year entryYear) {
    if (month == null && year == null) {
      var today = LocalDate.now();
      return Semester.from(entryYear, today.getMonthValue(), today.getYear());
    }
    if (month == null || year == null) {
      throw new BadRequestException("month and year must both be provided");
    }
    return Semester.from(entryYear, month, year);
  }
}
