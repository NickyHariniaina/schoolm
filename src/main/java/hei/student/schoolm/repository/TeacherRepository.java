package hei.student.schoolm.repository;

import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.repository.jpa.JTeacherRepository;
import hei.student.schoolm.repository.mapper.JTeacherMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TeacherRepository {
  private final JTeacherRepository jTeacherRepository;
  private final JTeacherMapper jTeacherMapper;

  public List<Teacher> findAllById(List<UUID> ids) {
    return jTeacherRepository.findAllById(ids).stream().map(jTeacherMapper::toDomain).toList();
  }
}
