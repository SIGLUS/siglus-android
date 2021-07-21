package org.openlmis.core.view.holder;

import static org.junit.Assert.assertEquals;

import android.view.LayoutInflater;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.view.activity.DumpFragmentActivity;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.robolectric.Robolectric;

@RunWith(LMISTestRunner.class)
public class BulkEntriesLotMovementViewHolderTest {

  private static final String lotNumber = "lotNumber1";
  private BulkEntriesLotMovementViewHolder viewHolder;
  private DumpFragmentActivity dumpFragmentActivity;

  @Before
  public void setUp() {
    dumpFragmentActivity = Robolectric.buildActivity(DumpFragmentActivity.class).get();
    View itemView = LayoutInflater.from(dumpFragmentActivity)
        .inflate(R.layout.item_bulk_entries_lots_info, null, false);
    viewHolder = new BulkEntriesLotMovementViewHolder(itemView,
        MovementReasonManager.getInstance().buildReasonListForMovementType(MovementType.RECEIVE));
  }

  @Test
  public void shouldShowLotSohWhenLotExisting() {
    // given
    LotMovementViewModel lotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber)
        .lotSoh("100")
        .build();

    // when
    viewHolder.populate(lotMovementViewModel, null);

    // then
    assertEquals("100", viewHolder.getLotStockOnHand().getText());
  }

  @Test
  public void shouldShowNewAddedLotSohTipWhenLotExisting() {
    // given
    LotMovementViewModel lotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber)
        .build();

    // when
    viewHolder.populate(lotMovementViewModel, null);

    // then
    String expectedMessage = LMISTestApp.getInstance().getResources().getString(R.string.label_new_added_lot);
    assertEquals(expectedMessage, viewHolder.getLotSohTip().getText().toString());
  }

}