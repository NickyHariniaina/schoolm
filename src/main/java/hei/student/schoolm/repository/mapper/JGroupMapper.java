package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.model.Group;
import hei.student.schoolm.repository.model.JGroup;
import org.springframework.stereotype.Component;

@Component
public class JGroupMapper {
  public Group toDomain(JGroup jGroup) {
    return Group.builder()
        .id(jGroup.getId())
        .ref(jGroup.getRef())
        .track(jGroup.getTrack())
        .createdAt(jGroup.getCreatedAt())
        .updatedAt(jGroup.getUpdatedAt())
        .build();
  }

  public JGroup toEntity(Group group) {
    return JGroup.builder()
        .id(group.getId())
        .ref(group.getRef())
        .track(group.getTrack())
        .createdAt(group.getCreatedAt())
        .updatedAt(group.getUpdatedAt())
        .build();
  }
}
