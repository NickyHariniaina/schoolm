package hei.student.schoolm.validator;

import static hei.student.schoolm.utils.GroupTestUtils.createGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.repository.GroupRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GroupValidatorTest {
  private static final UUID GROUP_L1_EL_01 =
      UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID GROUP_L1_EL_02 =
      UUID.fromString("00000000-0000-0000-0000-000000000002");

  private final GroupRepository groupRepository = Mockito.mock(GroupRepository.class);
  private final GroupValidator groupValidator = new GroupValidator(groupRepository);

  @Test
  void should_return_groups_when_all_exist() {
    var group1 = createGroup(GROUP_L1_EL_01);
    var group2 = createGroup(GROUP_L1_EL_02);
    when(groupRepository.findAllById(List.of(GROUP_L1_EL_01, GROUP_L1_EL_02)))
        .thenReturn(List.of(group1, group2));

    var result = groupValidator.checkGroupsExist(List.of(GROUP_L1_EL_01, GROUP_L1_EL_02));

    assertEquals(List.of(group1, group2), result);
  }

  @Test
  void should_throw_not_found_when_some_groups_missing() {
    var unknownGroup = UUID.fromString("99999999-9999-9999-9999-999999999998");
    when(groupRepository.findAllById(List.of(GROUP_L1_EL_01, unknownGroup)))
        .thenReturn(List.of(createGroup(GROUP_L1_EL_01)));

    var exception =
        assertThrows(
            NotFoundException.class,
            () -> groupValidator.checkGroupsExist(List.of(GROUP_L1_EL_01, unknownGroup)));

    assertTrue(exception.getMessage().contains(unknownGroup.toString()));
  }

  @Test
  void should_deduplicate_requested_ids() {
    var group = createGroup(GROUP_L1_EL_01);
    when(groupRepository.findAllById(List.of(GROUP_L1_EL_01))).thenReturn(List.of(group));

    var result = groupValidator.checkGroupsExist(List.of(GROUP_L1_EL_01, GROUP_L1_EL_01));

    verify(groupRepository).findAllById(List.of(GROUP_L1_EL_01));
    assertEquals(List.of(group), result);
  }
}