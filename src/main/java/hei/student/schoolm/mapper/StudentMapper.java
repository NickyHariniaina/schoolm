package hei.student.schoolm.mapper;

import hei.student.schoolm.dto.CourseValidationDto;
import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Student;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {
  private static final BigDecimal PASSING_GRADE = BigDecimal.TEN;

  public SemesterValidationDto toSemesterValidationDto(
      Student student, Group group, Semester semester, List<Course> courses) {
    var courseDtos =
        courses.stream().map(course -> toCourseValidationDto(course, student)).toList();
    var totalCredits = courseDtos.stream().mapToInt(CourseValidationDto::getCredit).sum();
    var acquiredCredits =
        courseDtos.stream()
            .filter(CourseValidationDto::isAcquired)
            .mapToInt(CourseValidationDto::getCredit)
            .sum();
    var validated =
        !courseDtos.isEmpty() && courseDtos.stream().allMatch(CourseValidationDto::isAcquired);

    return SemesterValidationDto.builder()
        .studentId(student.getId())
        .studentRef(student.getReference())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .groupRef(group.getRef())
        .semester(semester)
        .totalCredits(totalCredits)
        .acquiredCredits(acquiredCredits)
        .validated(validated)
        .courses(courseDtos)
        .build();
  }

  private CourseValidationDto toCourseValidationDto(Course course, Student student) {
    var finalGrade = computeFinalGrade(course, student);
    var acquired = finalGrade != null && finalGrade.compareTo(PASSING_GRADE) >= 0;
    return CourseValidationDto.builder()
        .courseId(course.getId())
        .ref(course.getRef())
        .title(course.getTitle())
        .credit(course.getCredit())
        .finalGrade(finalGrade)
        .acquired(acquired)
        .build();
  }

  private BigDecimal computeFinalGrade(Course course, Student student) {
    var exams = course.getExams();
    if (exams == null || exams.isEmpty()) {
      return null;
    }
    var gradesByExamId =
        course.getGrades() == null
            ? Map.<UUID, BigDecimal>of()
            : course.getGrades().stream()
                .filter(
                    grade ->
                        grade.getStudent() != null
                            && grade.getStudent().getId().equals(student.getId()))
                .collect(Collectors.toMap(g -> g.getExam().getId(), Grade::getValue, (a, b) -> b));
    var total = BigDecimal.ZERO;
    for (var exam : exams) {
      var value = gradesByExamId.getOrDefault(exam.getId(), BigDecimal.ZERO);
      total = total.add(BigDecimal.valueOf(value.doubleValue() * exam.getCoefficient().toDouble()));
    }
    return total;
  }
}
