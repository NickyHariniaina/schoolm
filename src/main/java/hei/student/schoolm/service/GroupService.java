package hei.student.schoolm.service;

import hei.student.schoolm.dto.GroupRequest;
import hei.student.schoolm.dto.GroupResponse;
import hei.student.schoolm.dto.StudentResponse;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.repository.GroupRepository;
import hei.student.schoolm.repository.StudentRepository;
import hei.student.schoolm.validator.GroupValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {
  private final GroupRepository groupRepository;
  private final StudentRepository studentRepository;
  private final CohortService cohortService;
  private final GroupValidator groupValidator;

  @Transactional(readOnly = true)
  public List<GroupResponse> getAll(UUID cohortId) {
    var groups =
        cohortId == null ? groupRepository.findAll() : groupRepository.findAllByCohortId(cohortId);
    return groups.stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public GroupResponse getById(UUID id) {
    return toResponse(getEntityOrThrow(id));
  }

  @Transactional
  public GroupResponse upsert(GroupRequest request) {
    var group =
        request.id() == null
            ? Group.builder().id(UUID.randomUUID()).build()
            : getEntityOrThrow(request.id());
    group.setRef(request.ref().toUpperCase());
    group.setTrack(request.track());
    group.setCohort(cohortService.getEntityOrThrow(request.cohortId()));

    var saved = groupRepository.save(group);
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<StudentResponse> getStudents(UUID groupId) {
    getEntityOrThrow(groupId);
    return studentRepository.findAllByGroupId(groupId).stream()
        .map(this::toStudentResponse)
        .toList();
  }

  public Cohort getCohort(UUID groupId) {
    var group = groupValidator.checkGroupExists(groupId);
    return group.getCohort();
  }

  public Group getEntityOrThrow(UUID id) {
    return groupRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Group not found: " + id));
  }

  private GroupResponse toResponse(Group group) {
    return new GroupResponse(
        group.getId(),
        group.getCohort() == null ? null : group.getCohort().getId(),
        group.getCohort() == null ? null : group.getCohort().getRef(),
        group.getRef(),
        group.getTrack());
  }

  private StudentResponse toStudentResponse(Student student) {
    return new StudentResponse(
        student.getId(),
        student.getReference(),
        student.getFirstName(),
        student.getLastName(),
        student.getEmail(),
        student.getGroup() == null ? null : student.getGroup().getId(),
        student.getGroup() == null ? null : student.getGroup().getRef());
  }
}
