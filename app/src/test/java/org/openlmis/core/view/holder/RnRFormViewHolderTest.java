package org.openlmis.core.view.holder;

import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.LayoutInflater;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
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

    @Before
    public void setup() {
        program = new Program();
        program.setProgramCode("MMIA");
        program.setProgramName("MMIA");
    }

    private RnRFormViewHolder getViewHolderByType(int viewType) {
        RnRFormListAdapter mockAdapter = mock(RnRFormListAdapter.class);
        switch (viewType) {
            case RnRFormViewModel.TYPE_GROUP:
                return new RnRFormViewHolder(mockAdapter, LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_rnr_list_type3, null, false));
            case RnRFormViewModel.TYPE_DRAFT:
            case RnRFormViewModel.TYPE_UNSYNC:
                return new RnRFormViewHolder(mockAdapter, LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_rnr_list_type1, null, false));
            case RnRFormViewModel.TYPE_HISTORICAL:
                return new RnRFormViewHolder(mockAdapter, LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_rnr_list_type2, null, false));
        }
        return null;
    }

    @Test
    public void shouldShowDraftStyle() {
        RnRForm form = RnRForm.init(program, DateUtil.today());
        form.setStatus(RnRForm.STATUS.DRAFT);
        RnRFormViewModel viewModel = new RnRFormViewModel(form);

        viewHolder = getViewHolderByType(RnRFormViewModel.TYPE_DRAFT);

        viewHolder.populate(viewModel, "MMIA");

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getPeriod()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_incomplete_requisition, viewModel.getName())));
        assertThat(((ColorDrawable) viewHolder.lyPeriod.getBackground()).getColor(), is(getColorResource(R.color.color_draft_title)));
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
        assertThat(((ColorDrawable) viewHolder.lyPeriod.getBackground()).getColor(), is(getColorResource(R.color.color_error_title)));
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
    }

    @SuppressWarnings("ConstantConditions")
    private String getStringResource(int resId, Object... param) {
        return Html.fromHtml(RuntimeEnvironment.application.getApplicationContext().getResources().getString(resId, param)).toString();
    }

    private int getColorResource(int resId) {
        return RuntimeEnvironment.application.getApplicationContext().getResources().getColor(resId);
    }
}