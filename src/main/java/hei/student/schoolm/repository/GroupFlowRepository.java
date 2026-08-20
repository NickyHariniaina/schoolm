package hei.student.schoolm.repository;

import hei.student.schoolm.model.GroupFlow;
import hei.student.schoolm.repository.jpa.JGroupFlowRepository;
import hei.student.schoolm.repository.mapper.JGroupFlowMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class GroupFlowRepository {
  private final JGroupFlowRepository repository;
  private final JGroupFlowMapper mapper;

  @Transactional
  public GroupFlow save(GroupFlow groupFlow) {
    return mapper.toDomain(repository.save(mapper.toEntity(groupFlow)));
  }

  @Transactional(readOnly = true)
  public List<GroupFlow> findByStudentId(UUID studentId) {
    return repository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<UUID> findGroupIdsByStudentId(UUID studentId) {
    return repository.findByStudentId(studentId).stream()
        .map(flow -> flow.getGroup().getId())
        .distinct()
        .toList();
  }

  @Transactional
  public void deleteAllByStudentId(UUID studentId) {
    repository.deleteAllByStudentId(studentId);
  }
}
