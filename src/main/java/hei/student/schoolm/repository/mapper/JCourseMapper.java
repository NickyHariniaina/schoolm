package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Exam;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JGroup;
import hei.student.schoolm.repository.model.JTeacher;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JCourseMapper {
  private final JTeacherMapper jTeacherMapper;
  private final JGroupMapper jGroupMapper;
  private final JExamMapper jExamMapper;

  public Course toDomain(JCourse jCourse) {
    return toDomain(jCourse, true);
  }

  public Course toDomainWithoutGroups(JCourse jCourse) {
    return toDomain(jCourse, false);
  }

  private Course toDomain(JCourse jCourse, boolean withGroups) {
    var teachers =
        jCourse.getTeachers() == null
            ? List.<Teacher>of()
            : jCourse.getTeachers().stream().map(jTeacherMapper::toDomain).toList();
    var groups =
        withGroups && jCourse.getGroups() != null
            ? jCourse.getGroups().stream().map(jGroupMapper::toDomain).toList()
            : List.<Group>of();
    var exams =
        jCourse.getExams() == null
            ? List.<Exam>of()
            : jCourse.getExams().stream().map(jExamMapper::toDomain).toList();
    var grades =
        exams.stream()
            .flatMap(
                exam -> exam.getGrades() == null ? Stream.<Grade>of() : exam.getGrades().stream())
            .toList();
    return Course.builder()
        .id(jCourse.getId())
        .ref(jCourse.getRef())
        .title(jCourse.getTitle())
        .credit(jCourse.getCredit())
        .track(jCourse.getTrack())
        .semester(jCourse.getSemester())
        .createdAt(jCourse.getCreatedAt())
        .updatedAt(jCourse.getUpdatedAt())
        .teachers(teachers)
        .groups(groups)
        .exams(exams)
        .grades(grades)
        .build();
  }

  public JCourse toEntity(Course course) {
    var teachers =
        course.getTeachers() == null
            ? List.<JTeacher>of()
            : course.getTeachers().stream().map(jTeacherMapper::toEntity).toList();
    var groups =
        course.getGroups() == null
            ? List.<JGroup>of()
            : course.getGroups().stream().map(jGroupMapper::toEntity).toList();
    return JCourse.builder()
        .id(course.getId())
        .ref(course.getRef())
        .title(course.getTitle())
        .credit(course.getCredit())
        .track(course.getTrack())
        .semester(course.getSemester())
        .teachers(teachers)
        .groups(groups)
        .build();
  }
}
