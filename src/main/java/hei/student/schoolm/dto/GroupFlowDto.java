package hei.student.schoolm.dto;

import hei.student.schoolm.model.GroupFlowType;
import java.time.Instant;
import java.util.UUID;

public record GroupFlowDto(
    UUID id, UUID studentId, UUID groupId, GroupFlowType groupFlowType, Instant createdAt) {}
