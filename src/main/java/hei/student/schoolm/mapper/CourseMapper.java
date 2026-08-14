package hei.student.schoolm.mapper;

import hei.student.schoolm.dto.CourseDto;
import hei.student.schoolm.model.Course;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {
  public CourseDto toDto(Course course) {
    var dto = new CourseDto();
    dto.setId(course.getId());
    dto.setRef(course.getRef());
    dto.setTitle(course.getTitle());
    dto.setCredit(course.getCredit());
    dto.setTrack(course.getTrack().name());
    dto.setSemester(course.getSemester().name());
    dto.setTeacherIds(course.teacherIds());
    dto.setGroupIds(course.groupIds());
    return dto;
  }

  public List<CourseDto> toDtoList(List<Course> courses) {
    if (courses == null) {
      return List.of();
    }
    return courses.stream().map(this::toDto).collect(Collectors.toList());
  }
}
