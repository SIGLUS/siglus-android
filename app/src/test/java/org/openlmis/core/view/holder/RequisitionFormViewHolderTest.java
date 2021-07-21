package org.openlmis.core.view.holder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import android.view.LayoutInflater;
import android.view.View;
import androidx.fragment.app.Fragment;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.builder.RequisitionBuilder;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.activity.DumpFragmentActivity;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.openlmis.core.view.viewmodel.RnRFormItemAdjustmentViewModel;
import org.robolectric.Robolectric;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class RequisitionFormViewHolderTest {

  private RequisitionFormViewHolder viewHolder;
  private RequisitionFormItemViewModel viewModel;
  private DumpFragmentActivity dummyActivity;

  @Before
  public void setup() {
    dummyActivity = Robolectric.setupActivity(DumpFragmentActivity.class);
    View itemView = LayoutInflater.from(dummyActivity)
        .inflate(R.layout.item_requisition_body, null, false);
    viewHolder = new RequisitionFormViewHolder(itemView);
    viewModel = RequisitionBuilder.buildFakeRequisitionViewModel();
  }

  @Test
  public void shouldHighLightRequestAmount() {
    // when
    viewHolder.populate(viewModel, Status.DRAFT);
    // then
    assertThat(viewHolder.requestAmount.isEnabled(), is(true));
    assertThat(viewHolder.approvedAmount.isEnabled(), is(false));
  }


  @Test
  public void shouldHighLightApprovedAmount() {
    // when
    viewHolder.populate(viewModel, Status.SUBMITTED);
    // then
    assertThat(viewHolder.requestAmount.isEnabled(), is(false));
    assertThat(viewHolder.approvedAmount.isEnabled(), is(true));
  }

  @Test
  public void shouldUpdateApprovedAmountWhenRequestAmountChanged() {

    viewHolder.populate(viewModel, Status.DRAFT);
    viewHolder.requestAmount.setText("123");

    assertThat(viewModel.getRequestAmount(), is("123"));
    assertThat(viewModel.getApprovedAmount(), is("123"));
    assertThat(viewHolder.requestAmount.getText().toString(), is("123"));
  }

  @Test
  public void shouldUpdateApprovedAmountModelWhenTextChanged() {
    viewHolder.populate(viewModel, Status.SUBMITTED);

    viewHolder.approvedAmount.setText("123");

    assertThat(viewModel.getRequestAmount(), is("0"));
    assertThat(viewModel.getApprovedAmount(), is("123"));
  }

  @Test
  public void shouldOnlyShowAdjustIconWhenAdjustmentQuantityIsNotZero() throws Exception {
    viewHolder.populate(viewModel, Status.DRAFT);

    assertThat(viewHolder.adjustTheoreticalIcon.getVisibility(), is(View.GONE));

    viewModel.setAdjustmentViewModels(Arrays.asList(generateAdjustmentViewModel()));
    viewHolder.populate(viewModel, Status.DRAFT);

    assertThat(viewHolder.adjustTheoreticalIcon.getVisibility(), is(View.VISIBLE));
  }


  @Test
  public void shouldShowPopTipsTotalReminderIconClicked() {
    viewModel.setAdjustmentViewModels(Arrays.asList(generateAdjustmentViewModel()));
    viewHolder.populate(viewModel, Status.DRAFT);

    viewHolder.adjustTheoreticalIcon.performClick();

    RobolectricUtils.waitLooperIdle();

    Fragment dialogFragment = dummyActivity.getSupportFragmentManager()
        .findFragmentByTag("adjustmentTheoreticalDialog");

    assertNotNull(dialogFragment);
  }

  private RnRFormItemAdjustmentViewModel generateAdjustmentViewModel() {
    RnRFormItemAdjustmentViewModel adjustmentViewModel = new RnRFormItemAdjustmentViewModel();
    adjustmentViewModel.setKitName("US kit");
    adjustmentViewModel.setKitStockOnHand(100L);
    adjustmentViewModel.setQuantity(2);
    return adjustmentViewModel;
  }
}