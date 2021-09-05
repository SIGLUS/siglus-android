package org.openlmis.core.view.adapter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import android.view.LayoutInflater;
import android.view.View;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.view.adapter.IssueVoucherLotAdapter.IssueVoucherLotViewHolder;
import org.openlmis.core.view.viewmodel.IssueVoucherLotViewModel;

@RunWith(LMISTestRunner.class)
public class IssueVoucherLotAdapterTest {

  private IssueVoucherLotAdapter adapter;
  private IssueVoucherLotViewModel mockViewModel;

  @Before
  public void setup(){
    adapter = new IssueVoucherLotAdapter();
    LMISTestApp.getContext().setTheme(R.style.AppTheme);

    mockViewModel = Mockito.mock(IssueVoucherLotViewModel.class);
    LotOnHand mockLotOnHand = Mockito.mock(LotOnHand.class);
    Lot mockLot = Mockito.mock(Lot.class);
    when(mockLotOnHand.getLot()).thenReturn(mockLot);
    when(mockLotOnHand.getQuantityOnHand()).thenReturn(1L);
    when(mockLot.getLotNumber()).thenReturn("LotNumber");
    when(mockLot.getExpirationDate()).thenReturn(new Date());
  }

  @Test
  public void testConvertEditType() {
    // given
    IssueVoucherLotViewHolder holder = adapter.new IssueVoucherLotViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.item_issue_voucher_lot_edit, null));
    when(mockViewModel.isDone()).thenReturn(false);
    when(mockViewModel.isNewAdd()).thenReturn(true);
    // when
    adapter.convert(holder, mockViewModel);

    // when
    assertEquals(View.VISIBLE, holder.getView(R.id.iv_del).getVisibility());
  }
}