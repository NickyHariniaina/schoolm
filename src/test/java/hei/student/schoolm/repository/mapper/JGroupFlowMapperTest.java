package hei.student.schoolm.repository.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.GroupFlow;
import hei.student.schoolm.model.GroupFlowType;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.repository.model.JGroup;
import hei.student.schoolm.repository.model.JGroupFlow;
import hei.student.schoolm.repository.model.JStudent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JGroupFlowMapperTest {
  private static final UUID FLOW_ID = UUID.fromString("00000000-0000-0000-0000-000000000801");
  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000802");
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000803");

  private final JGroupFlowMapper mapper = new JGroupFlowMapper();

  @Test
  void toDomain_maps_fields_and_reduces_relations_to_ids() {
    var jFlow =
        JGroupFlow.builder()
            .id(FLOW_ID)
            .student(JStudent.builder().id(STUDENT_ID).build())
            .group(JGroup.builder().id(GROUP_ID).build())
            .groupFlowType(GroupFlowType.JOIN)
            .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
            .build();

    GroupFlow flow = mapper.toDomain(jFlow);

    assertEquals(FLOW_ID, flow.getId());
    assertEquals(STUDENT_ID, flow.getStudent().getId());
    assertEquals(GROUP_ID, flow.getGroup().getId());
    assertEquals(GroupFlowType.JOIN, flow.getGroupFlowType());
    assertEquals(Instant.parse("2025-01-01T00:00:00Z"), flow.getCreatedAt());
  }

  @Test
  void toDomain_maps_null_relations() {
    var jFlow = JGroupFlow.builder().id(FLOW_ID).groupFlowType(GroupFlowType.LEAVE).build();

    GroupFlow flow = mapper.toDomain(jFlow);

    assertNull(flow.getStudent());
    assertNull(flow.getGroup());
  }

  @Test
  void toDomain_returns_null_when_input_null() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  void toEntity_maps_fields_and_reduces_relations_to_ids() {
    var flow =
        GroupFlow.builder()
            .id(FLOW_ID)
            .student(Student.builder().id(STUDENT_ID).build())
            .group(Group.builder().id(GROUP_ID).build())
            .groupFlowType(GroupFlowType.JOIN)
            .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
            .build();

    JGroupFlow jFlow = mapper.toEntity(flow);

    assertEquals(FLOW_ID, jFlow.getId());
    assertEquals(STUDENT_ID, jFlow.getStudent().getId());
    assertEquals(GROUP_ID, jFlow.getGroup().getId());
    assertEquals(GroupFlowType.JOIN, jFlow.getGroupFlowType());
  }

  @Test
  void toEntity_maps_null_relations() {
    var flow = GroupFlow.builder().id(FLOW_ID).groupFlowType(GroupFlowType.LEAVE).build();

    JGroupFlow jFlow = mapper.toEntity(flow);

    assertNull(jFlow.getStudent());
    assertNull(jFlow.getGroup());
  }

  @Test
  void toEntity_returns_null_when_input_null() {
    assertNull(mapper.toEntity(null));
  }
}
