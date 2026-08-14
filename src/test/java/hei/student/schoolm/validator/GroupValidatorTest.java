package hei.student.schoolm.validator;

import static hei.student.schoolm.utils.StudentTestUtils.createGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.repository.GroupRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupValidatorTest {
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Mock private GroupRepository groupRepository;
  @InjectMocks private GroupValidator groupValidator;

  @Test
  void should_return_group_when_exists() {
    var group = createGroup(GROUP_ID, "L1-EL-01");
    when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.of(group));

    var result = groupValidator.checkGroupExists(GROUP_ID);

    assertEquals(group, result);
  }

  @Test
  void should_throw_not_found_when_group_missing() {
    when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.empty());

    var exception =
        assertThrows(NotFoundException.class, () -> groupValidator.checkGroupExists(GROUP_ID));

    assertTrue(exception.getMessage().contains(GROUP_ID.toString()));
  }
}