package org.openlmis.core.persistence.migrations;


import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonData;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.robolectric.annotation.Config;

@RunWith(LMISTestRunner.class)
public class ChangeMovementReasonToCodeTest extends LMISRepositoryUnitTest {

  ChangeMovementReasonToCode migrate;

  @Before
  public void setUp() throws Exception {
    migrate = new ChangeMovementReasonToCode();
    migrate.stockItemGenericDao = spy(migrate.stockItemGenericDao);
    migrate.dbUtil = mock(DbUtil.class);
  }


  @Test
  public void shouldSetDefaultCodeWhenCannotFindACodeForReason() {
    StockMovementItem item = new StockMovementItem();
    item.setMovementType(MovementReasonManager.MovementType.ISSUE);
    item.setReason("cannot find a code");

    migrate.setDefaultReasonCode(item);
    assertThat(item.getReason(), is(MovementReasonManager.DEFAULT_ISSUE));

    item.setMovementType(MovementReasonManager.MovementType.RECEIVE);
    migrate.setDefaultReasonCode(item);
    assertThat(item.getReason(), is(MovementReasonManager.DEFAULT_RECEIVE));

    item.setMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST);
    migrate.setDefaultReasonCode(item);
    assertThat(item.getReason(), is(MovementReasonManager.DEFAULT_POSITIVE_ADJUSTMENT));

    item.setMovementType(MovementReasonManager.MovementType.NEGATIVE_ADJUST);
    migrate.setDefaultReasonCode(item);
    assertThat(item.getReason(), is(MovementReasonManager.DEFAULT_NEGATIVE_ADJUSTMENT));

    item.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
    migrate.setDefaultReasonCode(item);
    assertThat(item.getReason(), is(MovementReasonManager.INVENTORY));

    item.setReason("physicalInventoryPositive");
    migrate.setDefaultReasonCode(item);
    assertThat(item.getReason(), is(MovementReasonManager.INVENTORY_POSITIVE));

    item.setReason("physicalInventoryNegative");
    migrate.setDefaultReasonCode(item);
    assertThat(item.getReason(), is(MovementReasonManager.INVENTORY_NEGATIVE));

  }


  @Test
  public void shouldSetDefaultReason() {
    StockMovementItem item = new StockMovementItem();
    Throwable e = null;
    item.setMovementType(MovementReasonManager.MovementType.DEFAULT);
    try {
      migrate.setDefaultReasonCode(item);
    } catch (Throwable ex) {
      e = ex;
    }
    assertTrue(e.getMessage().startsWith("Invalid MovementType "));
  }

  @Test
  @Config(qualifiers = "pt-port")
  public void shouldReplaceLegacyReasonDataToReasonCodePT() throws LMISException {
    MovementReasonManager.getInstance().refresh();
    testMigrate(MovementReasonData.PT_TYPE_TO_DESC_LIST);
  }


  @Test
  @Config(qualifiers = "en-port")
  public void shouldReplaceLegacyReasonDataToReasonCodeEN() throws LMISException {
    MovementReasonManager.getInstance().refresh();
    testMigrate(MovementReasonData.PT_TYPE_TO_DESC_LIST);
  }

  private void testMigrate(EnumMap<MovementType, ArrayList<String>> movementTypeToDescList) throws LMISException {
    ArrayList<StockMovementItem> stockMovementItems = new ArrayList<>();

    for (Entry<MovementType, ArrayList<String>> entry : movementTypeToDescList.entrySet()) {
      for (String desc : entry.getValue()) {
        StockMovementItem item = new StockMovementItem();
        item.setMovementType(entry.getKey());
        item.setReason(desc);
        stockMovementItems.add(item);
      }
    }
    doReturn(stockMovementItems).when(migrate.stockItemGenericDao).queryForAll();
    migrate.up();
    for (StockMovementItem newItem : stockMovementItems) {
      assertThat(newItem.getReason().isEmpty(), is(false));
    }
  }
}