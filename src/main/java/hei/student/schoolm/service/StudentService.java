package hei.student.schoolm.service;

import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.dto.TranscriptDto;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.mapper.StudentMapper;
import hei.student.schoolm.model.*;
import hei.student.schoolm.util.Fraction;
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
    var pair = semesterPair(semester);
    var studentTrack = group.getTrack();

    var courseDtos =
        filterCourses(group.getCourses(), pair, studentTrack).stream()
            .map(course -> toCourseGradeDto(course, student, semester))
            .toList();
    var status =
        courseDtos.stream()
                .anyMatch(courseDto -> courseDto.getStatus() == TranscriptStatus.INCOMPLET)
            ? TranscriptStatus.INCOMPLET
            : TranscriptStatus.COMPLET;

    return TranscriptDto.builder()
        .studentId(student.getId())
        .studentRef(student.getReference())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .groupRef(group.getRef())
        .cohortRef(group.getCohort().getRef())
        .academicYear(academicYear(semester, entryYear))
        .semesters(pair)
        .courses(courseDtos)
        .status(status)
        .build();
  }

  public Semester computeSemester(Year entryYear, int month, int year) {
    int semesterNumber = ((year - entryYear.getValue()) * 12 + (month - 10)) / 6 + 1;
    if (semesterNumber < 1 || semesterNumber > MAX_SEMESTER_NUMBER) {
      throw new BadRequestException("Date is out of the valid semester range");
    }
    return Semester.values()[semesterNumber - 1];
  }

  public List<Semester> semesterPair(Semester semester) {
    var firstIndex = semester.ordinal() / 2 * 2;
    return List.of(Semester.values()[firstIndex], Semester.values()[firstIndex + 1]);
  }

  public List<Course> filterCourses(List<Course> courses, List<Semester> pair, Track studentTrack) {
    if (courses == null) {
      return List.of();
    }

    return courses.stream()
        .filter(course -> pair.contains(course.getSemester()))
        .filter(course -> course.getTrack() == Track.COMMON || course.getTrack() == studentTrack)
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
