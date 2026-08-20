package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.CourseAssignment;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JCourseAssignment;
import hei.student.schoolm.repository.model.JGroup;
import hei.student.schoolm.repository.model.JTeacher;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JCourseAssignmentMapper {
  private final JCourseMapper jCourseMapper;
  private final JGroupMapper jGroupMapper;
  private final JTeacherMapper jTeacherMapper;

  public CourseAssignment toDomain(JCourseAssignment jCourseAssignment) {
    if (jCourseAssignment == null) {
      return null;
    }
    return CourseAssignment.builder()
        .id(jCourseAssignment.getId())
        .course(jCourseMapper.toDomain(jCourseAssignment.getCourse()))
        .group(jGroupMapper.toDomain(jCourseAssignment.getGroup()))
        .teachers(
            jCourseAssignment.getTeachers() == null
                ? List.<Teacher>of()
                : jCourseAssignment.getTeachers().stream().map(jTeacherMapper::toDomain).toList())
        .academicYear(jCourseAssignment.getAcademicYear())
        .semester(jCourseAssignment.getSemester())
        .credits(jCourseAssignment.getCredits())
        .createdAt(jCourseAssignment.getCreatedAt())
        .updatedAt(jCourseAssignment.getUpdatedAt())
        .build();
  }

  public JCourseAssignment toEntity(CourseAssignment courseAssignment) {
    if (courseAssignment == null) {
      return null;
    }
    List<JTeacher> teachers =
        courseAssignment.getTeachers() == null
            ? List.<JTeacher>of()
            : courseAssignment.getTeachers().stream()
                .map(teacher -> JTeacher.builder().id(teacher.getId()).build())
                .collect(Collectors.toList());
    return JCourseAssignment.builder()
        .id(courseAssignment.getId())
        .course(
            courseAssignment.getCourse() == null
                ? null
                : JCourse.builder().id(courseAssignment.getCourse().getId()).build())
        .group(
            courseAssignment.getGroup() == null
                ? null
                : JGroup.builder().id(courseAssignment.getGroup().getId()).build())
        .teachers(teachers)
        .academicYear(courseAssignment.getAcademicYear())
        .semester(courseAssignment.getSemester())
        .credits(courseAssignment.getCredits())
        .createdAt(courseAssignment.getCreatedAt())
        .updatedAt(courseAssignment.getUpdatedAt())
        .build();
  }

  public Course toCourseOnly(CourseAssignment courseAssignment) {
    return courseAssignment.getCourse();
  }
}
