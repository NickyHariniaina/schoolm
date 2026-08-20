package hei.student.schoolm.validator;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.repository.GroupRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupValidator {
  private final GroupRepository repository;

  public List<Group> checkGroupsExist(List<UUID> groupIds) {
    var uniqueIds = groupIds.stream().distinct().toList();
    var groups = repository.findAllById(uniqueIds);
    var foundIds = groups.stream().map(Group::getId).collect(Collectors.toSet());
    var missing = uniqueIds.stream().filter(id -> !foundIds.contains(id)).toList();
    if (!missing.isEmpty()) {
      throw new NotFoundException("Group(s) not found: " + missing);
    }
    return groups;
  }

  public Group checkGroupExists(UUID groupId) {
    return repository
        .findById(groupId)
        .orElseThrow(() -> new NotFoundException("Group " + groupId + " not found"));
  }
}
