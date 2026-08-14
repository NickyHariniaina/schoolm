package hei.student.schoolm.utils;

import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.repository.model.JGroup;
import java.util.UUID;

public final class GroupTestUtils {
  private GroupTestUtils() {}

  public static JGroup createJGroup(UUID id, String ref, Track track) {
    return JGroup.builder().id(id).ref(ref).track(track).build();
  }

  public static Group createGroup(UUID id) {
    return Group.builder().id(id).build();
  }

  public static Group createGroup(UUID id, String ref, Track track) {
    return Group.builder().id(id).ref(ref).track(track).build();
  }
}
