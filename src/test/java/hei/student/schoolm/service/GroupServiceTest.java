package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.validator.GroupValidator;
import java.time.Year;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID COHORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Mock private GroupValidator groupValidator;
  @InjectMocks private GroupService groupService;

  @Test
  void should_return_group_cohort() {
    var cohort = Cohort.builder().id(COHORT_ID).ref("P24").entryYear(Year.of(2024)).build();
    var group = Group.builder().id(GROUP_ID).ref("L1-EL-01").cohort(cohort).build();
    when(groupValidator.checkGroupExists(GROUP_ID)).thenReturn(group);

    var result = groupService.getCohort(GROUP_ID);

    assertEquals(cohort, result);
  }
}
