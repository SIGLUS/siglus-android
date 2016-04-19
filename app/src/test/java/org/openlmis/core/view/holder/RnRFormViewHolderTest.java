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
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

@RunWith(LMISTestRunner.class)
public class RnRFormViewHolderTest {

    private Program program;
    private RnRFormViewHolder viewHolder;
    private RnRFormViewHolder.RnRFormItemClickListener mockedListener;

    @Before
    public void setup() {
        mockedListener = mock(RnRFormViewHolder.RnRFormItemClickListener.class);
        program = new Program();
        program.setProgramCode("MMIA");
        program.setProgramName("MMIA");
    }

    private RnRFormViewHolder getViewHolderByType(int viewType) {
        if (viewType == RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL || viewType == RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY) {
            return new RnRFormViewHolder(LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_rnr_card_disable, null, false), mockedListener);
        } else {
            return new RnRFormViewHolder(LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_rnr_card, null, false), mockedListener);
        }
    }

    @Test
    public void shouldShowDraftStyle() {
        RnRForm form = RnRForm.init(program, DateUtil.today());
        form.setStatus(RnRForm.STATUS.DRAFT);
        RnRFormViewModel viewModel = RnRFormViewModel.buildNormalRnrViewModel(form);

        viewHolder = getViewHolderByType(RnRFormViewModel.TYPE_CREATED_BUT_UNCOMPLETED);

        viewHolder.populate(viewModel);

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getTitle()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_incomplete_requisition, viewModel.getName())));
        assertThat(((ColorDrawable) viewHolder.txPeriod.getBackground()).getColor(), is(getColorResource(R.color.color_draft_title)));
        assertThat(viewHolder.btnView.getText().toString(), is(getStringResource(R.string.btn_view_incomplete_requisition, viewModel.getName())));
        assertThat(viewHolder.ivDelete.getVisibility(), is(View.GONE));
    }

    @Test
    public void shouldShowUnSyncedStyle() {
        RnRForm form = RnRForm.init(program, DateUtil.today());
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        form.setSynced(false);
        RnRFormViewModel viewModel = RnRFormViewModel.buildNormalRnrViewModel(form);

        viewHolder = getViewHolderByType(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
        viewHolder.populate(viewModel);

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getTitle()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_unsynced_requisition, viewModel.getName())));
        assertThat(((ColorDrawable) viewHolder.txPeriod.getBackground()).getColor(), is(getColorResource(R.color.color_red)));
    }

    @Test
    public void shouldShowUnCompleteInventory() {
        RnRFormViewModel viewModel = new RnRFormViewModel(new Period(new DateTime()), "MMIA", RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD);

        viewHolder = getViewHolderByType(RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD);
        viewHolder.populate(viewModel);

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getTitle()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_uncompleted_physical_inventory_message, viewModel.getName())));
        assertThat(((ColorDrawable) viewHolder.txPeriod.getBackground()).getColor(), is(getColorResource(R.color.color_draft_title)));
        assertThat(viewHolder.btnView.getText().toString(), is(getStringResource(R.string.btn_view_uncompleted_physical_inventory, viewModel.getName())));
        assertThat(viewHolder.ivDelete.getVisibility(), is(View.GONE));
    }

    @Test
    public void shouldShowCompletedInventory() {
        RnRFormViewModel viewModel = new RnRFormViewModel(new Period(new DateTime()), "MMIA", RnRFormViewModel.TYPE_CLOSE_OF_PERIOD_SELECTED);

        viewHolder = getViewHolderByType(RnRFormViewModel.TYPE_CLOSE_OF_PERIOD_SELECTED);
        viewHolder.populate(viewModel);

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getTitle()));
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
        RnRFormViewModel viewModel = RnRFormViewModel.buildNormalRnrViewModel(form);
        viewHolder = getViewHolderByType(RnRFormViewModel.TYPE_SYNCED_HISTORICAL);

        viewHolder.populate(viewModel);

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getTitle()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_submitted_message, viewModel.getName(), viewModel.getSyncedTime())));
        assertThat(viewHolder.btnView.getText().toString(), is(getStringResource(R.string.btn_view_requisition, viewModel.getName())));
        assertThat(viewHolder.ivDelete.getVisibility(), is(View.VISIBLE));
        assertNull(viewHolder.tvDrugCount);
    }

    @Test
    public void shouldShowTvDrugCountWhenIsHistoricalEmergency() throws SQLException {
        RnRForm form = RnRForm.init(program, DateUtil.today());
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        form.setSynced(true);
        form.setSubmittedTime(DateUtil.parseString("2016-01-22 11:33:44", DateUtil.DATE_TIME_FORMAT));
        form.setEmergency(true);
        DbUtil.initialiseDao(RnRForm.class).assignEmptyForeignCollection(form, "rnrFormItemList");
        RnRFormViewModel viewModel = RnRFormViewModel.buildEmergencyViewModel(form);
        viewHolder = getViewHolderByType(RnRFormViewModel.TYPE_SYNCED_HISTORICAL);

        viewHolder.populate(viewModel);

        assertThat(viewHolder.tvDrugCount.getVisibility(), is(View.VISIBLE));
        assertThat(viewHolder.tvDrugCount.getText().toString(), is("0 drugs ordered"));
        assertTrue(viewHolder.txPeriod.getText().toString().equals("Emergency Requisition – 22 Jan 11:33"));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_submitted_message, "Emergency requisition balancete", viewModel.getSyncedTime())));
        assertThat(viewHolder.btnView.getText().toString(), is(getStringResource(R.string.btn_view_requisition, "Emergency requisition balancete")));
        assertThat(viewHolder.ivDelete.getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void shouldShowCanNotDoPhysicalInventoryType() {
        RnRFormViewModel viewModel = new RnRFormViewModel(Period.of(DateUtil.today()), program.getProgramCode(), RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY);
        viewHolder = getViewHolderByType(RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY);

        viewHolder.populate(viewModel);

        assertThat(viewHolder.txPeriod.getText().toString(), is(viewModel.getTitle()));
        assertThat(viewHolder.txMessage.getText().toString(), is(getStringResource(R.string.label_can_not_create_rnr, DateUtil.getMonthAbbrByDate(viewModel.getPeriodEndMonth().toDate()))));
    }

    @SuppressWarnings("ConstantConditions")
    private String getStringResource(int resId, Object... param) {
        return Html.fromHtml(RuntimeEnvironment.application.getApplicationContext().getResources().getString(resId, param)).toString();
    }

    private int getColorResource(int resId) {
        return RuntimeEnvironment.application.getApplicationContext().getResources().getColor(resId);
    }
}