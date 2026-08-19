package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.GroupFlow;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.repository.model.JGroup;
import hei.student.schoolm.repository.model.JGroupFlow;
import hei.student.schoolm.repository.model.JStudent;
import org.springframework.stereotype.Component;

@Component
public class JGroupFlowMapper {
  public GroupFlow toDomain(JGroupFlow jGroupFlow) {
    if (jGroupFlow == null) {
      return null;
    }
    return GroupFlow.builder()
        .id(jGroupFlow.getId())
        .student(
            jGroupFlow.getStudent() == null
                ? null
                : Student.builder().id(jGroupFlow.getStudent().getId()).build())
        .group(
            jGroupFlow.getGroup() == null
                ? null
                : Group.builder().id(jGroupFlow.getGroup().getId()).build())
        .groupFlowType(jGroupFlow.getGroupFlowType())
        .createdAt(jGroupFlow.getCreatedAt())
        .build();
  }

  public JGroupFlow toEntity(GroupFlow groupFlow) {
    if (groupFlow == null) {
      return null;
    }
    return JGroupFlow.builder()
        .id(groupFlow.getId())
        .student(
            groupFlow.getStudent() == null
                ? null
                : JStudent.builder().id(groupFlow.getStudent().getId()).build())
        .group(
            groupFlow.getGroup() == null
                ? null
                : JGroup.builder().id(groupFlow.getGroup().getId()).build())
        .groupFlowType(groupFlow.getGroupFlowType())
        .createdAt(groupFlow.getCreatedAt())
        .build();
  }
}
