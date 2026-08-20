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
  private final JGroupRepository repository;
  private final JGroupMapper mapper;

  public List<Group> findAllById(List<UUID> ids) {
    return repository.findAllById(ids).stream().map(mapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public List<Group> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public List<Group> findAllByIdWithCourses(List<UUID> ids) {
    return repository.findAllById(ids).stream().map(mapper::toDomainWithCourses).toList();
  }

  @Transactional(readOnly = true)
  public List<Group> findAllByCohortId(UUID cohortId) {
    return repository.findAllByCohortId(cohortId).stream()
        .map(mapper::toDomainWithCourses)
        .toList();
  }

  @Transactional(readOnly = true)
  public Optional<Group> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomainWithCourses);
  }

  @Transactional
  public Group save(Group group) {
    return mapper.toDomain(repository.save(mapper.toEntity(group)));
  }
}
