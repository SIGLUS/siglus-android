package org.openlmis.core.view.holder;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.builder.RequisitionBuilder;
import org.openlmis.core.view.activity.VIARequisitionActivity;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.openlmis.core.view.viewmodel.RnRFormItemAdjustmentViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowAlertDialog;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(LMISTestRunner.class)
public class RequisitionFormViewHolderTest {

    private RequisitionFormViewHolder viewHolder;
    private RequisitionFormItemViewModel viewModel;

    @Before
    public void setup() {
        VIARequisitionActivity viaRequisitionActivity = Robolectric.buildActivity(VIARequisitionActivity.class).create().get();
        View itemView = LayoutInflater.from(viaRequisitionActivity).inflate(R.layout.item_requisition_body, null, false);
        viewHolder = new RequisitionFormViewHolder(itemView);
        viewModel = RequisitionBuilder.buildFakeRequisitionViewModel();
    }

    @Test
    public void shouldHighLightRequestAmount() {
        viewHolder.populate(viewModel, RnRForm.STATUS.DRAFT);

        int bgReqColor = ((ColorDrawable) viewHolder.requestAmount.getBackground()).getColor();
        int bgAprColor = ((ColorDrawable) viewHolder.approvedAmount.getBackground()).getColor();

        assertThat(bgReqColor, is(RuntimeEnvironment.application.getApplicationContext().getResources().getColor(R.color.color_white)));
        assertThat(bgAprColor, is(RuntimeEnvironment.application.getApplicationContext().getResources().getColor(android.R.color.transparent)));
        assertThat(viewHolder.requestAmount.isEnabled(), is(true));
        assertThat(viewHolder.approvedAmount.isEnabled(), is(false));
    }


    @Test
    public void shouldHighLightApprovedAmount() {
        viewHolder.populate(viewModel, RnRForm.STATUS.SUBMITTED);

        int bgReqColor = ((ColorDrawable) viewHolder.requestAmount.getBackground()).getColor();
        int bgAprColor = ((ColorDrawable) viewHolder.approvedAmount.getBackground()).getColor();

        assertThat(bgReqColor, is(RuntimeEnvironment.application.getApplicationContext().getResources().getColor(android.R.color.transparent)));
        assertThat(bgAprColor, is(RuntimeEnvironment.application.getApplicationContext().getResources().getColor(R.color.color_white)));
        assertThat(viewHolder.requestAmount.isEnabled(), is(false));
        assertThat(viewHolder.approvedAmount.isEnabled(), is(true));
    }

    @Test
    public void shouldUpdateApprovedAmountWhenRequestAmountChanged() {

        viewHolder.populate(viewModel, RnRForm.STATUS.DRAFT);
        viewHolder.requestAmount.setText("123");

        assertThat(viewModel.getRequestAmount(), is("123"));
        assertThat(viewModel.getApprovedAmount(), is("123"));
        assertThat(viewHolder.requestAmount.getText().toString(), is("123"));
    }

    @Test
    public void shouldUpdateApprovedAmountModelWhenTextChanged() {
        viewHolder.populate(viewModel, RnRForm.STATUS.SUBMITTED);

        viewHolder.approvedAmount.setText("123");

        assertThat(viewModel.getRequestAmount(), is("0"));
        assertThat(viewModel.getApprovedAmount(), is("123"));
    }

    @Test
    public void shouldOnlyShowAdjustIconWhenAdjustmentQuantityIsNotZero() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_theoretical, true);
        viewHolder.populate(viewModel, RnRForm.STATUS.DRAFT);

        assertThat(viewHolder.adjustTheoreticalIcon.getVisibility(), is(View.GONE));

        viewModel.setAdjustmentViewModels(Arrays.asList(generateAdjustmentViewModel()));
        viewHolder.populate(viewModel, RnRForm.STATUS.DRAFT);

        assertThat(viewHolder.adjustTheoreticalIcon.getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void shouldShowPopTipsTotalReminderIconClicked() {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_theoretical, true);

        viewModel.setAdjustmentViewModels(Arrays.asList(generateAdjustmentViewModel()));
        viewHolder.populate(viewModel, RnRForm.STATUS.DRAFT);

        viewHolder.adjustTheoreticalIcon.performClick();
        AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(alertDialog);
    }

    private RnRFormItemAdjustmentViewModel generateAdjustmentViewModel() {
        RnRFormItemAdjustmentViewModel adjustmentViewModel = new RnRFormItemAdjustmentViewModel();
        adjustmentViewModel.setKitName("US kit");
        adjustmentViewModel.setKitStockOnHand(100L);
        adjustmentViewModel.setQuantity(2);
        return adjustmentViewModel;
    }
}