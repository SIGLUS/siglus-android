package org.openlmis.core.view.holder;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.RequisitionBuilder;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.robolectric.RuntimeEnvironment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(LMISTestRunner.class)
public class RequisitionFormViewHolderTest {

    private RequisitionFormViewHolder viewHolder;

    @Before
    public void setup() {
        View itemView = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_requisition_body, null, false);
        viewHolder = new RequisitionFormViewHolder(itemView);
    }

    @Test
    public void shouldHighLightRequestAmount(){
        viewHolder.populate(RequisitionBuilder.buildFakeRequisitionViewModel(), RnRForm.STATUS.DRAFT);

        int bgReqColor = ((ColorDrawable)viewHolder.requestAmount.getBackground()).getColor();
        int bgAprColor = ((ColorDrawable)viewHolder.approvedAmount.getBackground()).getColor();

        assertThat(bgReqColor, is(RuntimeEnvironment.application.getApplicationContext().getResources().getColor(R.color.white)));
        assertThat(bgAprColor, is(RuntimeEnvironment.application.getApplicationContext().getResources().getColor(android.R.color.transparent)));
        assertThat(viewHolder.requestAmount.isEnabled(), is(true));
        assertThat(viewHolder.approvedAmount.isEnabled(), is(false));
    }


    @Test
    public void shouldHighLightApprovedAmount(){
        viewHolder.populate(RequisitionBuilder.buildFakeRequisitionViewModel(), RnRForm.STATUS.SUBMITTED);

        int bgReqColor = ((ColorDrawable) viewHolder.requestAmount.getBackground()).getColor();
        int bgAprColor = ((ColorDrawable) viewHolder.approvedAmount.getBackground()).getColor();

        assertThat(bgReqColor, is(RuntimeEnvironment.application.getApplicationContext().getResources().getColor(android.R.color.transparent)));
        assertThat(bgAprColor, is(RuntimeEnvironment.application.getApplicationContext().getResources().getColor(R.color.white)));
        assertThat(viewHolder.requestAmount.isEnabled(), is(false));
        assertThat(viewHolder.approvedAmount.isEnabled(), is(true));
    }

    @Test
    public void shouldUpdateApprovedAmountWhenRequestAmountChanged(){
        RequisitionFormItemViewModel viewModel = RequisitionBuilder.buildFakeRequisitionViewModel();
        viewHolder.populate(viewModel, RnRForm.STATUS.DRAFT);
        viewHolder.requestAmount.setText("123");

        assertThat(viewModel.getRequestAmount(), is("123"));
        assertThat(viewModel.getApprovedAmount(), is("123"));
        assertThat(viewHolder.requestAmount.getText().toString(), is("123"));
    }


    @Test
    public void shouldUpdateApprovedAmountModelWhenTextChanged(){
        RequisitionFormItemViewModel viewModel = RequisitionBuilder.buildFakeRequisitionViewModel();
        viewHolder.populate(viewModel, RnRForm.STATUS.SUBMITTED);

        viewHolder.approvedAmount.setText("123");

        assertThat(viewModel.getRequestAmount(), is("0"));
        assertThat(viewModel.getApprovedAmount(), is("123"));
    }
}