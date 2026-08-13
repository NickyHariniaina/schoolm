package hei.student.schoolm.mapper;

import hei.student.schoolm.dto.CourseDto;
import hei.student.schoolm.model.Course;
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
    return dto;
  }
}
