package hei.student.schoolm.mapper;

import hei.student.schoolm.dto.CourseDto;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Teacher;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {
  public CourseDto toDto(Course course) {
    CourseDto dto = new CourseDto();
    dto.setId(course.getId());
    dto.setRef(course.getRef());
    dto.setTitle(course.getTitle());
    dto.setCredit(course.getCredit());
    dto.setTrack(course.getTrack().name());
    dto.setSemester(course.getSemester().name());
    dto.setTeacherIds(mapTeacherIds(course));
    return dto;
  }

  private List<UUID> mapTeacherIds(Course course) {
    return course.getTeachers().stream().map(Teacher::getId).toList();
  }
}
