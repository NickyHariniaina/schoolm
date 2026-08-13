package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JTeacher;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JCourseMapper {
  private final JTeacherMapper jTeacherMapper;

  public Course toDomain(JCourse jCourse) {
    List<Teacher> teachers =
        jCourse.getTeachers() == null
            ? List.of()
            : jCourse.getTeachers().stream().map(jTeacherMapper::toDomain).toList();
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
        .build();
  }

  public JCourse toEntity(Course course) {
    List<JTeacher> teachers =
        course.getTeachers() == null
            ? List.of()
            : course.getTeachers().stream().map(jTeacherMapper::toEntity).toList();
    return JCourse.builder()
        .id(course.getId())
        .ref(course.getRef())
        .title(course.getTitle())
        .credit(course.getCredit())
        .track(course.getTrack())
        .semester(course.getSemester())
        .teachers(teachers)
        .build();
  }
}
