package hei.student.schoolm.repository;

import hei.student.schoolm.model.Group;
import hei.student.schoolm.repository.jpa.JGroupRepository;
import hei.student.schoolm.repository.mapper.JGroupMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class GroupRepository {
  private final JGroupRepository jGroupRepository;
  private final JGroupMapper jGroupMapper;

  public List<Group> findAllById(List<UUID> ids) {
    return jGroupRepository.findAllById(ids).stream().map(jGroupMapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public List<Group> findAllByIdWithCourses(List<UUID> ids) {
    return jGroupRepository.findAllById(ids).stream()
        .map(jGroupMapper::toDomainWithCourses)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<Group> findAllByCohortId(UUID cohortId) {
    return jGroupRepository.findAllByCohortId(cohortId).stream()
        .map(jGroupMapper::toDomainWithCourses)
        .toList();
  }

  @Transactional(readOnly = true)
  public Optional<Group> findById(UUID id) {
    return jGroupRepository.findById(id).map(jGroupMapper::toDomainWithCourses);
  }
}
