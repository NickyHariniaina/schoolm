package hei.student.schoolm.mapper;

import hei.student.schoolm.dto.CourseDto;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JTeacher;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {
  public CourseDto toDto(JCourse course) {
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

  private List<UUID> mapTeacherIds(JCourse course) {
    return course.getTeachers().stream().map(JTeacher::getId).toList();
  }
}
