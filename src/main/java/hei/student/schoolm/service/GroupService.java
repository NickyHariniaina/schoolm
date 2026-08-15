package hei.student.schoolm.service;

import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.validator.GroupValidator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {
  private final GroupValidator groupValidator;

  public Cohort getCohort(UUID groupId) {
    var group = groupValidator.checkGroupExists(groupId);
    return group.getCohort();
  }
}
