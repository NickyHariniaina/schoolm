package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.repository.model.JGroup;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class JGroupMapper {
  private final JCohortMapper jCohortMapper;
  private final JCourseMapper jCourseMapper;

  public JGroupMapper(JCohortMapper jCohortMapper, @Lazy JCourseMapper jCourseMapper) {
    this.jCohortMapper = jCohortMapper;
    this.jCourseMapper = jCourseMapper;
  }

  public Group toDomain(JGroup jGroup) {
    return Group.builder()
        .id(jGroup.getId())
        .ref(jGroup.getRef())
        .track(jGroup.getTrack())
        .cohort(jGroup.getCohort() == null ? null : jCohortMapper.toDomain(jGroup.getCohort()))
        .createdAt(jGroup.getCreatedAt())
        .updatedAt(jGroup.getUpdatedAt())
        .build();
  }

  public Group toDomainWithCourses(JGroup jGroup) {
    var group = toDomain(jGroup);
    var courses =
        jGroup.getCourses() == null
            ? List.<Course>of()
            : jGroup.getCourses().stream().map(jCourseMapper::toDomainWithoutGroups).toList();
    group.setCourses(courses);
    return group;
  }

  public JGroup toEntity(Group group) {
    return JGroup.builder()
        .id(group.getId())
        .ref(group.getRef())
        .track(group.getTrack())
        .cohort(group.getCohort() == null ? null : jCohortMapper.toEntity(group.getCohort()))
        .createdAt(group.getCreatedAt())
        .updatedAt(group.getUpdatedAt())
        .build();
  }
}
