/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.view.adapter;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.RequisitionBuilder;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(LMISTestRunner.class)
public class RequisitionFormAdapterTest {


    private RequisitionFormAdapter adapter;
    private LayoutInflater inflater;
    private RequisitionFormAdapter.ViewHolder viewHolder;


    @Before
    public void setup() {
        ArrayList<RequisitionFormItemViewModel> data = new ArrayList<>();
        adapter = new RequisitionFormAdapter(RuntimeEnvironment.application, data, false);
        inflater = LayoutInflater.from(RuntimeEnvironment.application);
        viewHolder = new RequisitionFormAdapter.ViewHolder(inflater.inflate(R.layout.item_requisition_body,null, false),false);
    }


    @Test
    public void shouldHighLightRequestAmount(){
        adapter.setStatus(RnRForm.STATUS.DRAFT);
        adapter.onBindViewHolder(viewHolder, RequisitionBuilder.buildFakeRequisitionViewModel());


        int bgReqColor = ((ColorDrawable)viewHolder.requestAmount.getBackground()).getColor();
        int bgAprColor = ((ColorDrawable)viewHolder.approvedAmount.getBackground()).getColor();

        assertThat(bgReqColor, is(RuntimeEnvironment.application.getApplicationContext().getResources().getColor(R.color.white)));
        assertThat(bgAprColor, is(RuntimeEnvironment.application.getApplicationContext().getResources().getColor(android.R.color.transparent)));
        assertThat(viewHolder.requestAmount.isEnabled(), is(true));
        assertThat(viewHolder.approvedAmount.isEnabled(), is(false));
    }


    @Test
    public void shouldHighLightApprovedAmount(){
        adapter.setStatus(RnRForm.STATUS.SUBMITTED);
        adapter.onBindViewHolder(viewHolder, RequisitionBuilder.buildFakeRequisitionViewModel());

        int bgReqColor = ((ColorDrawable) viewHolder.requestAmount.getBackground()).getColor();
        int bgAprColor = ((ColorDrawable) viewHolder.approvedAmount.getBackground()).getColor();

        assertThat(bgReqColor, is(RuntimeEnvironment.application.getApplicationContext().getResources().getColor(android.R.color.transparent)));
        assertThat(bgAprColor, is(RuntimeEnvironment.application.getApplicationContext().getResources().getColor(R.color.white)));
        assertThat(viewHolder.requestAmount.isEnabled(), is(false));
        assertThat(viewHolder.approvedAmount.isEnabled(), is(true));
    }

    @Test
    public void shouldUpdateApprovedAmountWhenRequestAmountChanged(){
        viewHolder = new RequisitionFormAdapter.ViewHolder(inflater.inflate(R.layout.item_requisition_body,null, false),false);
        adapter.setStatus(RnRForm.STATUS.DRAFT);
        RequisitionFormItemViewModel viewModel = RequisitionBuilder.buildFakeRequisitionViewModel();
        adapter.onBindViewHolder(viewHolder, viewModel);

        viewHolder.requestAmount.setText("123");

        assertThat(viewModel.getRequestAmount(), is("123"));
        assertThat(viewModel.getApprovedAmount(), is("123"));
        assertThat(viewHolder.requestAmount.getText().toString(), is("123"));
    }


    @Test
    public void shouldUpdateApprovedAmountModelWhenTextChanged(){
        viewHolder = new RequisitionFormAdapter.ViewHolder(inflater.inflate(R.layout.item_requisition_body,null, false),false);
        adapter.setStatus(RnRForm.STATUS.SUBMITTED);

        RequisitionFormItemViewModel viewModel = RequisitionBuilder.buildFakeRequisitionViewModel();
        adapter.onBindViewHolder(viewHolder, viewModel);

        viewHolder.approvedAmount.setText("123");

        assertThat(viewModel.getRequestAmount(), is("0"));
        assertThat(viewModel.getApprovedAmount(), is("123"));
    }


}
