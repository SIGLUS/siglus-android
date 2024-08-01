package org.openlmis.core.manager;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openlmis.core.manager.MovementReasonData.EN_TYPE_TO_DESC_LIST;
import static org.openlmis.core.manager.MovementReasonData.PT_TYPE_TO_DESC_LIST;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.MovementReasonNotFoundException;
import org.openlmis.core.manager.MovementReasonManager.MovementReason;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.persistence.migrations.ChangeMovementReasonToCode;
import androidx.test.core.app.ApplicationProvider;
import org.robolectric.annotation.Config;
import roboguice.RoboGuice;


@RunWith(LMISTestRunner.class)
public class MovementReasonManagerTest {

  MovementReasonManager reasonManager;

  @Before
  public void setUp() throws Exception {
    reasonManager = RoboGuice.getInjector(ApplicationProvider.getApplicationContext()).getInstance(MovementReasonManager.class);
  }

  @Test
  @Config(qualifiers = "en-port")
  public void shouldGetRightCodeFromEnglishReason() throws Exception {
    for (Entry<MovementType, ArrayList<String>> entry : EN_TYPE_TO_DESC_LIST.entrySet()) {
      for (String desc : entry.getValue()) {
        String code = reasonManager.queryByDesc(entry.getKey(), desc).getCode();
        assertThat(StringUtils.isEmpty(code), is(false));
      }
    }
  }

  @Test
  @Config(qualifiers = "pt-port")
  public void shouldGetRightCodeForPTReason() throws Exception {
    for (Entry<MovementType, ArrayList<String>> entry : PT_TYPE_TO_DESC_LIST.entrySet()) {
      for (String desc : entry.getValue()) {
        String code = reasonManager.queryByDesc(entry.getKey(), desc).getCode();
        assertThat(StringUtils.isEmpty(code), is(false));
      }
    }
  }

  @Test
  public void shouldNotDisplayPhysicalInventoryAndDefaultReasonOnMenu()
      throws MovementReasonNotFoundException {
    assertThat(reasonManager.queryByCode(MovementType.PHYSICAL_INVENTORY, MovementReasonManager.INVENTORY)
        .canBeDisplayOnMovementMenu(), is(false));
    assertThat(
        reasonManager.queryByCode(MovementType.ISSUE, ChangeMovementReasonToCode.DEFAULT_ISSUE).canBeDisplayOnMovementMenu(),
        is(false));
    assertThat(reasonManager.queryByCode(MovementType.RECEIVE, ChangeMovementReasonToCode.DEFAULT_RECEIVE)
        .canBeDisplayOnMovementMenu(), is(false));
    assertThat(
        reasonManager.queryByCode(MovementType.NEGATIVE_ADJUST, ChangeMovementReasonToCode.DEFAULT_NEGATIVE_ADJUSTMENT)
            .canBeDisplayOnMovementMenu(), is(false));
    assertThat(
        reasonManager.queryByCode(MovementType.POSITIVE_ADJUST, ChangeMovementReasonToCode.DEFAULT_POSITIVE_ADJUSTMENT)
            .canBeDisplayOnMovementMenu(), is(false));
    assertThat(
        reasonManager.queryByCode(MovementType.POSITIVE_ADJUST, MovementReasonManager.DONATION)
            .canBeDisplayOnMovementMenu(),
        is(true));
    assertThat(
        reasonManager.queryByCode(MovementType.ISSUE, MovementReasonManager.UNPACK_KIT).canBeDisplayOnMovementMenu(),
        is(false));
    assertThat(
        reasonManager.queryByCode(MovementType.NEGATIVE_ADJUST, MovementReasonManager.EXPIRED_RETURN_TO_SUPPLIER_AND_DISCARD).canBeDisplayOnMovementMenu(),
        is(false));
  }

  @Test
  public void shouldDisplayOnMovementReasonMenu() throws MovementReasonNotFoundException {
    for (Entry<MovementType, ArrayList<String>> entry : EN_TYPE_TO_DESC_LIST.entrySet()) {
      for (String desc : entry.getValue()) {
        boolean displayMenu = !(desc.equalsIgnoreCase("Inventory")
            || desc.equals("Unpack kit"));
        assertThat(reasonManager.queryByDesc(entry.getKey(), desc).canBeDisplayOnMovementMenu(), is(displayMenu));
      }
    }
  }

  @Test
  public void shouldReturnPositiveRejectionReasons_whenBuildReasonListForRejectionIsCalledWithTrue() {
    // when
    List<MovementReason> movementReasons = reasonManager.buildReasonListForRejection(true);
    // then
    assertEquals(2, movementReasons.size());

    ArrayList<String> positiveRejectionReasonCodes = newArrayList("EXCESS", "LOT_NOT_SPECIFIED");
    for (MovementReason movementReason : movementReasons) {
      assertTrue(positiveRejectionReasonCodes.contains(movementReason.code));
    }
  }

  @Test
  public void shouldReturnNegativeRejectionReasons_whenBuildReasonListForRejectionIsCalledWithFalse() {
    // when
    List<MovementReason> movementReasons = reasonManager.buildReasonListForRejection(false);
    // then
    assertEquals(5, movementReasons.size());

    ArrayList<String> positiveRejectionReasonCodes = newArrayList(
        "DAMAGED", "INSUFFICIENT", "EXPIRED", "UNEATABLE", "INCORRECT_BATCH"
    );
    for (MovementReason movementReason : movementReasons) {
      assertTrue(positiveRejectionReasonCodes.contains(movementReason.code));
    }
  }
}