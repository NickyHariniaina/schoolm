package hei.student.schoolm.repository;

import hei.student.schoolm.model.Group;
import hei.student.schoolm.repository.jpa.JGroupRepository;
import hei.student.schoolm.repository.mapper.JGroupMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupRepository {
  private final JGroupRepository jGroupRepository;
  private final JGroupMapper jGroupMapper;

  public List<Group> findAllById(List<UUID> ids) {
    return jGroupRepository.findAllById(ids).stream().map(jGroupMapper::toDomain).toList();
  }
}
