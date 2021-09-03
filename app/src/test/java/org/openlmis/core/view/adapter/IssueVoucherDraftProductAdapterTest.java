package org.openlmis.core.view.adapter;


import static org.junit.Assert.assertEquals;
import static org.openlmis.core.view.viewmodel.BulkIssueProductViewModel.TYPE_EDIT;

import android.view.LayoutInflater;
import android.widget.TextView;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.adapter.IssueVoucherDraftProductAdapter.IssueVoucherProductViewHolder;
import org.openlmis.core.view.viewmodel.IssueVoucherProductViewModel;

@RunWith(LMISTestRunner.class)
public class IssueVoucherDraftProductAdapterTest {

  private IssueVoucherDraftProductAdapter adapter;
  private IssueVoucherProductViewModel mockProductViewModel;

  @Before
  public void setup() {
    adapter = new IssueVoucherDraftProductAdapter();
    LMISTestApp.getContext().setTheme(R.style.AppTheme);
    mockProductViewModel = Mockito.mock(IssueVoucherProductViewModel.class);
    StockCard mockStockCard = Mockito.mock(StockCard.class);
    Mockito.when(mockStockCard.getProduct()).thenReturn(ProductBuilder.buildAdultProduct());
    Mockito.when(mockProductViewModel.getStockCard()).thenReturn(mockStockCard);
    Mockito.when(mockProductViewModel.getProduct()).thenReturn(ProductBuilder.buildAdultProduct());
    Mockito.when(mockProductViewModel.getItemType()).thenReturn(TYPE_EDIT);
  }

  @Test
  public void testConvert() {
    // given
    IssueVoucherProductViewHolder holder = adapter.new IssueVoucherProductViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.item_issue_voucher_draft_edit, null));

    // when
    adapter.convert(holder, mockProductViewModel);
    RobolectricUtils.waitLooperIdle();

    // when
    TextView tvProductTitle = holder.getView(R.id.tv_product_title);
    assertEquals("Primary product name [productCode]", tvProductTitle.getText().toString());
  }

  @Test
  public void shouldValidateTrue() {
    // given
    IssueVoucherProductViewModel mockProductViewModel = Mockito.mock(IssueVoucherProductViewModel.class);
    Mockito.when(mockProductViewModel.validate()).thenReturn(true);
    adapter.setList(Collections.singletonList(mockProductViewModel));

    // then
    Assert.assertEquals(-1, adapter.validateAll());
  }

  @Test
  public void shouldValidateFalse() {
    // given
    IssueVoucherProductViewModel mockProductViewModel = Mockito.mock(IssueVoucherProductViewModel.class);
    Mockito.when(mockProductViewModel.validate()).thenReturn(false);
    adapter.setList(Collections.singletonList(mockProductViewModel));

    // then
    Assert.assertEquals(0, adapter.validateAll());
  }

}