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
import android.text.Html;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(LMISTestRunner.class)
public class RnRFormListAdapterTest {

    private RnRFormListAdapter adapter;
    private Program program;
    private List<RnRFormViewModel> data;

    @Before
    public void setup() {
        data = new ArrayList<>();
        program = new Program();
        program.setProgramCode("MMIA");
        program.setProgramName("MMIA");
        adapter = new RnRFormListAdapter(RuntimeEnvironment.application, "MMIA", data);

    }


    @Test
    public void shouldShowDraftStyle(){
        RnRForm form = RnRForm.init(program, DateUtil.today());
        form.setStatus(RnRForm.STATUS.DRAFT);
        RnRFormViewModel viewModel = new RnRFormViewModel(form);


        RnRFormListAdapter.ViewHolder viewHolder = createAdapterItem(viewModel);

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getPeriod()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_incomplete_requisition, viewModel.getName())));
        assertThat(((ColorDrawable)viewHolder.lyPeriod.getBackground()).getColor(), is(getColorResource(R.color.color_draft_title)));
    }

    @Test
    public void shouldShowUnSyncedStyle(){
        RnRForm form = RnRForm.init(program, DateUtil.today());
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        form.setSynced(false);
        RnRFormViewModel viewModel = new RnRFormViewModel(form);

        RnRFormListAdapter.ViewHolder viewHolder = createAdapterItem(viewModel);

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getPeriod()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_unsynced_requisition, viewModel.getName())));
        assertThat(((ColorDrawable)viewHolder.lyPeriod.getBackground()).getColor(), is(getColorResource(R.color.color_error_title)));
    }


    @Test
    public void shouldShowCompleteStyle(){
        RnRForm form = RnRForm.init(program, DateUtil.today());
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        form.setSynced(true);
        RnRFormViewModel viewModel = new RnRFormViewModel(form);

        RnRFormListAdapter.ViewHolder viewHolder = createAdapterItem(viewModel);

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getPeriod()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_submitted_message, viewModel.getName(), viewModel.getSyncedDate())));
        assertThat(viewHolder.btnView.getText().toString(), is(getStringResource(R.string.btn_view_requisition, viewModel.getName())));
    }

    private RnRFormListAdapter.ViewHolder createAdapterItem(RnRFormViewModel viewModel) {
        data.clear();
        data.add(viewModel);

        adapter.notifyDataSetChanged();

        RnRFormListAdapter.ViewHolder viewHolder = adapter.onCreateViewHolder(null, adapter.getItemViewType(0));
        adapter.onBindViewHolder(viewHolder, 0);
        return viewHolder;
    }


    @SuppressWarnings("ConstantConditions")
    private String getStringResource(int resId, Object... param){
        return Html.fromHtml(RuntimeEnvironment.application.getApplicationContext().getResources().getString(resId, param)).toString();
    }

    private int getColorResource(int resId){
        return RuntimeEnvironment.application.getApplicationContext().getResources().getColor(resId);
    }
}
