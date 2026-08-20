package hei.student.schoolm.service;

import hei.student.schoolm.dto.GroupFlowDto;
import hei.student.schoolm.dto.MoveStudentGroupRequest;
import hei.student.schoolm.model.GroupFlow;
import hei.student.schoolm.model.GroupFlowType;
import hei.student.schoolm.repository.GroupFlowRepository;
import hei.student.schoolm.repository.StudentRepository;
import hei.student.schoolm.util.SecurityUtil;
import hei.student.schoolm.validator.GroupValidator;
import hei.student.schoolm.validator.StudentValidator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupFlowService {
  private final GroupFlowRepository groupFlowRepository;
  private final StudentRepository studentRepository;
  private final StudentValidator studentValidator;
  private final GroupValidator groupValidator;
  private final SecurityUtil securityUtil;

  @Transactional(readOnly = true)
  public List<GroupFlowDto> getHistory(UUID studentId) {
    securityUtil.requireSelfOrAdmin(studentId);
    studentValidator.checkStudentExists(studentId);
    return groupFlowRepository.findByStudentId(studentId).stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<UUID> studentGroupIds(UUID studentId) {
    var student = studentValidator.checkStudentExists(studentId);
    var flowGroupIds = groupFlowRepository.findGroupIdsByStudentId(studentId);
    if (student.getGroup() == null) {
      return flowGroupIds;
    }
    return Stream.concat(Stream.of(student.getGroup().getId()), flowGroupIds.stream())
        .distinct()
        .toList();
  }

  @Transactional
  public GroupFlowDto move(UUID studentId, MoveStudentGroupRequest request) {
    var student = studentValidator.checkStudentExists(studentId);
    var group = groupValidator.checkGroupExists(request.groupId());

    var saved =
        groupFlowRepository.save(
            GroupFlow.builder()
                .student(student)
                .group(group)
                .groupFlowType(GroupFlowType.JOIN)
                .build());

    student.setGroup(group);
    studentRepository.save(student);

    return toDto(saved);
  }

  private GroupFlowDto toDto(GroupFlow flow) {
    return new GroupFlowDto(
        flow.getId(),
        flow.getStudent().getId(),
        flow.getGroup().getId(),
        flow.getGroupFlowType(),
        flow.getCreatedAt());
  }
}
