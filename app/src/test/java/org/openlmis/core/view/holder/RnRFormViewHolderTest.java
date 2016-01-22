package org.openlmis.core.view.holder;

import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.adapter.RnRFormListAdapter;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.robolectric.RuntimeEnvironment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

@RunWith(LMISTestRunner.class)
public class RnRFormViewHolderTest {

    private Program program;
    private RnRFormViewHolder viewHolder;
    private RnRFormListAdapter mockAdapter;

    @Before
    public void setup() {
        mockAdapter = mock(RnRFormListAdapter.class);
        program = new Program();
        program.setProgramCode("MMIA");
        program.setProgramName("MMIA");
    }

    private RnRFormViewHolder getViewHolderByType(int viewType) {
        if (viewType == RnRFormViewModel.TYPE_UNSYNC) {
            return new RnRFormViewHolder(mockAdapter, LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_rnr_card_unsync, null, false));
        } else {
            return new RnRFormViewHolder(mockAdapter, LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_rnr_card, null, false));
        }
    }

    @Test
    public void shouldShowDraftStyle() {
        RnRForm form = RnRForm.init(program, DateUtil.today());
        form.setStatus(RnRForm.STATUS.DRAFT);
        RnRFormViewModel viewModel = new RnRFormViewModel(form);

        viewHolder = getViewHolderByType(RnRFormViewModel.TYPE_UN_AUTHORIZED);

        viewHolder.populate(viewModel, "MMIA");

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getPeriod()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_incomplete_requisition, viewModel.getName())));
        assertThat(((ColorDrawable) viewHolder.txPeriod.getBackground()).getColor(), is(getColorResource(R.color.color_draft_title)));
        assertThat(viewHolder.btnView.getText().toString(), is(getStringResource(R.string.btn_view_incomplete_requisition, viewModel.getName())));
        assertThat(viewHolder.ivDelete.getVisibility(), is(View.GONE));
    }

    @Test
    public void shouldShowDraftStyleWhenToggleOff() {
        RnRForm form = RnRForm.init(program, DateUtil.today());
        form.setStatus(RnRForm.STATUS.DRAFT);
        RnRFormViewModel viewModel = new RnRFormViewModel(form);

        viewHolder = new RnRFormViewHolder(mockAdapter, LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_rnr_card_unsync, null, false));

        viewHolder.populateOld(viewModel, "MMIA");

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getPeriod()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_incomplete_requisition_old, viewModel.getName())));
        assertThat(((ColorDrawable) viewHolder.txPeriod.getBackground()).getColor(), is(getColorResource(R.color.color_draft_title)));
    }

    @Test
    public void shouldShowUnSyncedStyle() {
        RnRForm form = RnRForm.init(program, DateUtil.today());
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        form.setSynced(false);
        RnRFormViewModel viewModel = new RnRFormViewModel(form);

        viewHolder = getViewHolderByType(RnRFormViewModel.TYPE_UNSYNC);
        viewHolder.populate(viewModel, "MMIA");

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getPeriod()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_unsynced_requisition, viewModel.getName())));
        assertThat(((ColorDrawable) viewHolder.txPeriod.getBackground()).getColor(), is(getColorResource(R.color.color_red)));
    }

    @Test
    public void shouldShowUnCompleteInventory() {
        RnRFormViewModel viewModel = new RnRFormViewModel(new Period(new DateTime()), "MMIA", RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY);

        viewHolder = getViewHolderByType(RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY);
        viewHolder.populate(viewModel, "MMIA");

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getPeriod()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_uncompleted_physical_inventory_message, viewModel.getName())));
        assertThat(((ColorDrawable) viewHolder.txPeriod.getBackground()).getColor(), is(getColorResource(R.color.color_draft_title)));
        assertThat(viewHolder.btnView.getText().toString(), is(getStringResource(R.string.btn_view_uncompleted_physical_inventory, viewModel.getName())));
        assertThat(viewHolder.ivDelete.getVisibility(), is(View.GONE));
    }

    @Test
    public void shouldShowCompletedInventory() {
        RnRFormViewModel viewModel = new RnRFormViewModel(new Period(new DateTime()), "MMIA", RnRFormViewModel.TYPE_COMPLETED_INVENTORY);

        viewHolder = getViewHolderByType(RnRFormViewModel.TYPE_COMPLETED_INVENTORY);
        viewHolder.populate(viewModel, "MMIA");

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getPeriod()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_completed_physical_inventory_message, viewModel.getName())));
        assertThat(((ColorDrawable) viewHolder.txPeriod.getBackground()).getColor(), is(getColorResource(R.color.color_draft_title)));
        assertThat(viewHolder.btnView.getText().toString(), is(getStringResource(R.string.btn_view_completed_physical_inventory, viewModel.getName())));
        assertThat(viewHolder.ivDelete.getVisibility(), is(View.GONE));
    }

    @Test
    public void shouldShowCompleteStyle() {
        RnRForm form = RnRForm.init(program, DateUtil.today());
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        form.setSynced(true);
        RnRFormViewModel viewModel = new RnRFormViewModel(form);
        viewHolder = getViewHolderByType(RnRFormViewModel.TYPE_HISTORICAL);

        viewHolder.populate(viewModel, "MMIA");

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getPeriod()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_submitted_message, viewModel.getName(), viewModel.getSyncedDate())));
        assertThat(viewHolder.btnView.getText().toString(), is(getStringResource(R.string.btn_view_requisition, viewModel.getName())));
        assertThat(viewHolder.ivDelete.getVisibility(), is(View.VISIBLE));
    }

    @SuppressWarnings("ConstantConditions")
    private String getStringResource(int resId, Object... param) {
        return Html.fromHtml(RuntimeEnvironment.application.getApplicationContext().getResources().getString(resId, param)).toString();
    }

    private int getColorResource(int resId) {
        return RuntimeEnvironment.application.getApplicationContext().getResources().getColor(resId);
    }
}