package hei.student.schoolm.service;

import hei.student.schoolm.dto.CourseGradeDto;
import hei.student.schoolm.dto.ExamGradeDto;
import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.dto.TranscriptDto;
import hei.student.schoolm.dto.TranscriptStatus;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.mapper.StudentMapper;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Exam;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.util.Fraction;
import hei.student.schoolm.validator.GroupValidator;
import hei.student.schoolm.validator.StudentValidator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {
  private static final int MAX_SEMESTER_NUMBER = 6;

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
    var student = studentValidator.checkStudentExists(studentId);
    var group = groupValidator.checkGroupExists(student.getGroup().getId());
    var entryYear = group.getCohort().getEntryYear();
    var semester = resolveSemester(month, year, entryYear);
    var pair = semesterPair(semester);
    var courseDtos =
        filterCourses(group.getCourses(), pair).stream()
            .map(course -> toCourseGradeDto(course, student, semester))
            .toList();
    var status =
        courseDtos.stream().anyMatch(courseDto -> courseDto.getStatus() == TranscriptStatus.INCOMPLET)
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

  public List<Course> filterCourses(List<Course> courses, List<Semester> pair) {
    return courses.stream()
        .filter(course -> pair.contains(course.getSemester()))
        .sorted(
            Comparator.comparingInt((Course course) -> course.getSemester().ordinal())
                .thenComparing(Course::getRef))
        .toList();
  }

  public BigDecimal computeFinalGrade(Course course, Student student) {
    var exams = course.getExams();
    if (exams == null || exams.isEmpty()) {
      return null;
    }
    var gradesByExam =
        course.getGrades() == null
            ? Map.<Exam, BigDecimal>of()
            : course.getGrades().stream()
                .filter(
                    grade ->
                        grade.getStudent() != null
                            && grade.getStudent().getId().equals(student.getId()))
                .collect(Collectors.toMap(Grade::getExam, Grade::getValue, (a, b) -> b));
    var total = BigDecimal.ZERO;
    for (var exam : exams) {
      var value = gradesByExam.getOrDefault(exam, BigDecimal.ZERO);
      total = total.add(BigDecimal.valueOf(value.doubleValue() * exam.getCoefficient().toDouble()));
    }
    return total;
  }

  public TranscriptStatus computeStatus(Course course, Semester currentSemester) {
    if (course.getSemester().ordinal() > currentSemester.ordinal()) {
      return TranscriptStatus.INCOMPLET;
    }
    var exams = course.getExams();
    if (exams == null || exams.isEmpty()) {
      return TranscriptStatus.INCOMPLET;
    }
    var sum =
        exams.stream()
            .map(Exam::getCoefficient)
            .reduce(new Fraction(0, 1), Fraction::add);
    return sum.isOne() ? TranscriptStatus.COMPLET : TranscriptStatus.INCOMPLET;
  }

  private Semester resolveSemester(Integer month, Integer year, Year entryYear) {
    if (month == null && year == null) {
      var today = LocalDate.now();
      return computeSemester(entryYear, today.getMonthValue(), today.getYear());
    }
    if (month == null || year == null) {
      throw new BadRequestException("month and year must both be provided");
    }
    return computeSemester(entryYear, month, year);
  }

  private CourseGradeDto toCourseGradeDto(Course course, Student student, Semester currentSemester) {
    return CourseGradeDto.builder()
        .courseId(course.getId())
        .ref(course.getRef())
        .title(course.getTitle())
        .semester(course.getSemester())
        .credit(course.getCredit())
        .coefficientSum(coefficientSum(course))
        .exams(
            course.getExams() == null
                ? List.of()
                : course.getExams().stream()
                    .map(exam -> toExamGradeDto(exam, course, student))
                    .toList())
        .finalGrade(computeFinalGrade(course, student))
        .status(computeStatus(course, currentSemester))
        .build();
  }

  private ExamGradeDto toExamGradeDto(Exam exam, Course course, Student student) {
    var grade =
        course.getGrades() == null
            ? null
            : course.getGrades().stream()
                .filter(
                    g ->
                        g.getExam().getId().equals(exam.getId())
                            && g.getStudent() != null
                            && g.getStudent().getId().equals(student.getId()))
                .findFirst()
                .orElse(null);
    return ExamGradeDto.builder()
        .examId(exam.getId())
        .date(exam.getDateExam().toString())
        .coefficient(exam.getCoefficient().toDouble())
        .value(grade == null ? null : grade.getValue())
        .build();
  }

  private double coefficientSum(Course course) {
    var exams = course.getExams();
    if (exams == null || exams.isEmpty()) {
      return 0.0;
    }
    return exams.stream()
        .map(exam -> exam.getCoefficient().toDouble())
        .reduce(0.0, Double::sum);
  }

  private String academicYear(Semester semester, Year entryYear) {
    var pairIndex = semester.ordinal() / 2 + 1;
    var startYear = entryYear.getValue() + pairIndex - 1;
    return startYear + "-" + (startYear + 1);
  }
}