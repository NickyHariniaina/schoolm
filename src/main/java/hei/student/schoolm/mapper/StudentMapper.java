package hei.student.schoolm.mapper;

import hei.student.schoolm.dto.CourseGradeDto;
import hei.student.schoolm.dto.CourseValidationDto;
import hei.student.schoolm.dto.ExamGradeDto;
import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.dto.TranscriptDto;
import hei.student.schoolm.dto.TranscriptStatus;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Exam;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Student;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

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
    return CourseValidationDto.builder()
        .courseId(course.getId())
        .ref(course.getRef())
        .title(course.getTitle())
        .credit(course.getCredit())
        .finalGrade(course.finalGradeFor(student))
        .acquired(student.validate(course))
        .build();
  }

  public TranscriptDto toTranscriptDto(
      Student student, Group group, Semester currentSemester, List<Course> courses) {
    var courseDtos =
        courses.stream().map(course -> toCourseGradeDto(course, student, currentSemester)).toList();
    var status =
        courseDtos.stream()
                .anyMatch(courseDto -> courseDto.getStatus() == TranscriptStatus.INCOMPLET)
            ? TranscriptStatus.INCOMPLET
            : TranscriptStatus.COMPLET;
    var entryYear = group.getCohort().getEntryYear();

    return TranscriptDto.builder()
        .studentId(student.getId())
        .studentRef(student.getReference())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .groupRef(group.getRef())
        .cohortRef(group.getCohort().getRef())
        .academicYear(currentSemester.academicYear(entryYear))
        .semesters(currentSemester.pair())
        .courses(courseDtos)
        .status(status)
        .build();
  }

  private CourseGradeDto toCourseGradeDto(
      Course course, Student student, Semester currentSemester) {
    return CourseGradeDto.builder()
        .courseId(course.getId())
        .ref(course.getRef())
        .title(course.getTitle())
        .semester(course.getSemester())
        .credit(course.getCredit())
        .coefficientSum(course.coefficientSum().toDouble())
        .exams(
            course.getExams() == null
                ? List.of()
                : course.getExams().stream()
                    .map(exam -> toExamGradeDto(exam, course, student))
                    .toList())
        .finalGrade(course.finalGradeFor(student))
        .status(
            course.isCompleteFor(currentSemester)
                ? TranscriptStatus.COMPLET
                : TranscriptStatus.INCOMPLET)
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
}
