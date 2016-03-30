package org.openlmis.core.view.activity;


import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.RnRFormListPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowToast;

import java.util.Date;

import roboguice.RoboGuice;
import rx.Observable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class RnRFormListActivityTest {

    private RnRFormListActivity rnRFormListActivity;
    private RnRFormListPresenter mockedPresenter;
    private Intent intent;

    @Before
    public void setUp() {
        mockedPresenter = mock(RnRFormListPresenter.class);

        Observable observable = Observable.just(newArrayList());
        when(mockedPresenter.loadRnRFormList()).thenReturn(observable);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(RnRFormListPresenter.class).toInstance(mockedPresenter);
            }
        });

        intent = new Intent();
        intent.putExtra(Constants.PARAM_PROGRAM_CODE, Constants.MMIA_PROGRAM_CODE);
        rnRFormListActivity = Robolectric.buildActivity(RnRFormListActivity.class).withIntent(intent).create().get();
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldSetMmiaTitleAndProgramCodeWhenProgramCodeIsMmia() {
        assertThat(rnRFormListActivity.getTitle()).isEqualTo(rnRFormListActivity.getResources().getString(R.string.mmia_list));
        verify(mockedPresenter).setProgramCode(Constants.MMIA_PROGRAM_CODE);
    }

    @Test
    public void shouldSetViaTitleAndProgramCodeWhenProgramCodeIsVia() {

        intent.putExtra(Constants.PARAM_PROGRAM_CODE, Constants.VIA_PROGRAM_CODE);
        rnRFormListActivity = Robolectric.buildActivity(RnRFormListActivity.class).withIntent(intent).create().get();

        assertThat(rnRFormListActivity.getTitle()).isEqualTo(rnRFormListActivity.getResources().getString(R.string.requisition_list));
        verify(mockedPresenter).setProgramCode(Constants.VIA_PROGRAM_CODE);
    }

    @Test
    public void shouldShowErrorMsgWhenCalledSubscriberOnError() {
        rnRFormListActivity.getRnRFormSubscriber().onError(new Exception("test exception"));

        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("test exception");
    }

    @Test
    public void shouldLoadDataWhenCalledSubscriberOnNext() throws Exception {
        RnRForm form = new RnRForm();
        Program program = new Program();
        program.setProgramCode("MMIA");
        form.setProgram(program);
        form.setPeriodBegin(new Date());
        form.setPeriodEnd(new Date());

        rnRFormListActivity.getRnRFormSubscriber().onNext(newArrayList(new RnRFormViewModel(form), new RnRFormViewModel(form)));

        assertThat(rnRFormListActivity.listView.getAdapter().getItemCount()).isEqualTo(2);
    }

    @Test
    public void shouldStartPhysicalInventoryWhenBtnClickedWithUncompleteInventory() throws Exception {
        RnRFormViewModel viewModel = generateRnRFormViewModel("MMIA", RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel);

        Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(nextStartedIntent);
        assertEquals(nextStartedIntent.getComponent().getClassName(), InventoryActivity.class.getName());
        assertTrue(nextStartedIntent.getBooleanExtra(Constants.PARAM_IS_PHYSICAL_INVENTORY, false));
    }

    @Test
    public void shouldSelectPeriodWhenBtnClickedWithSelectClosePeriod() throws Exception {
        RnRFormViewModel viewModel = generateRnRFormViewModel("MMIA", RnRFormViewModel.TYPE_INVENTORY_DONE);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel);

        Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(nextStartedIntent);
        assertEquals(nextStartedIntent.getComponent().getClassName(), SelectPeriodActivity.class.getName());
        assertEquals(nextStartedIntent.getStringExtra(Constants.PARAM_PROGRAM_CODE), "MMIA");
    }

    @Test
    public void shouldStartSelectPeriodPageWhenBtnClickedWithTypeMissedPeriod() throws Exception {
        RnRFormViewModel viewModel = generateRnRFormViewModel("MMIA", RnRFormViewModel.TYPE_FIRST_MISSED_PERIOD);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel);

        Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(nextStartedIntent);
        assertEquals(nextStartedIntent.getComponent().getClassName(), SelectPeriodActivity.class.getName());
        assertEquals(nextStartedIntent.getStringExtra(Constants.PARAM_PROGRAM_CODE), "MMIA");
    }

    @Test
    public void shouldStartMMIAHistoryWhenBtnClickedWithTypeHistory() throws Exception {
        RnRFormViewModel viewModel = generateRnRFormViewModel("MMIA", RnRFormViewModel.TYPE_SYNCED_HISTORICAL);
        viewModel.setId(999L);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel);

        Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(nextStartedIntent);
        assertEquals(nextStartedIntent.getComponent().getClassName(), MMIARequisitionActivity.class.getName());
        assertEquals(nextStartedIntent.getLongExtra(Constants.PARAM_FORM_ID, 0), 999L);
    }

    @Test
    public void shouldStartVIAHistoryWhenBtnClickedWithTypeHistory() throws Exception {
        intent.putExtra(Constants.PARAM_PROGRAM_CODE, Constants.VIA_PROGRAM_CODE);
        rnRFormListActivity = Robolectric.buildActivity(RnRFormListActivity.class).withIntent(intent).create().get();

        RnRFormViewModel viewModel = generateRnRFormViewModel("ESS_MEDS", RnRFormViewModel.TYPE_SYNCED_HISTORICAL);
        viewModel.setId(999L);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel);

        Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(nextStartedIntent);
        assertEquals(nextStartedIntent.getComponent().getClassName(), VIARequisitionActivity.class.getName());
        assertEquals(nextStartedIntent.getLongExtra(Constants.PARAM_FORM_ID, 0), 999L);
    }

    @Test
    public void shouldStartMMIAEditPageWhenBtnClickedWithTypeUnauthorized() throws Exception {
        RnRFormViewModel viewModel = generateRnRFormViewModel("MMIA", RnRFormViewModel.TYPE_CREATED_BUT_UNCOMPLETED);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel);

        Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(nextStartedIntent);
        assertEquals(nextStartedIntent.getComponent().getClassName(), MMIARequisitionActivity.class.getName());
        assertEquals(nextStartedIntent.getLongExtra(Constants.PARAM_FORM_ID, 0), 0L);
    }

    @Test
    public void shouldNotLoadSameFormIdAfterLoadedViaHistoryForm() throws Exception {
        RnRFormViewModel historyViewModel = generateRnRFormViewModel("MMIA", RnRFormViewModel.TYPE_SYNCED_HISTORICAL);
        historyViewModel.setId(1L);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(historyViewModel);

        Intent startedIntentWhenIsHistory = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(startedIntentWhenIsHistory);
        assertEquals(startedIntentWhenIsHistory.getComponent().getClassName(), MMIARequisitionActivity.class.getName());
        assertEquals(1L, startedIntentWhenIsHistory.getLongExtra(Constants.PARAM_FORM_ID, 0));

        RnRFormViewModel defaultViewModel = generateRnRFormViewModel("MMIA", RnRFormViewModel.TYPE_CREATED_BUT_UNCOMPLETED);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(defaultViewModel);

        Intent startedIntentWhenIsDefault = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(startedIntentWhenIsDefault);
        assertEquals(startedIntentWhenIsDefault.getComponent().getClassName(), MMIARequisitionActivity.class.getName());
        assertEquals(0L, startedIntentWhenIsDefault.getLongExtra(Constants.PARAM_FORM_ID, 0));
    }

    @Test
    public void shouldGoToRequisitionPageWhenInvokeOnActivityResultWithSelectPeriodRequestCode() throws Exception {
        Intent data = new Intent();
        Date inventoryDate = new Date();
        data.putExtra(Constants.PARAM_SELECTED_INVENTORY_DATE, inventoryDate);
        data.putExtra(Constants.PARAM_IS_MISSED_PERIOD, true);

        intent.putExtra(Constants.PARAM_PROGRAM_CODE, Constants.VIA_PROGRAM_CODE);
        rnRFormListActivity = Robolectric.buildActivity(RnRFormListActivity.class).withIntent(intent).create().get();

        rnRFormListActivity.onActivityResult(Constants.REQUEST_SELECT_PERIOD_END, Activity.RESULT_OK, data);

        Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(nextStartedIntent);
        assertEquals(nextStartedIntent.getComponent().getClassName(), VIARequisitionActivity.class.getName());

        assertTrue(nextStartedIntent.getBooleanExtra(Constants.PARAM_IS_MISSED_PERIOD, false));
        assertEquals(nextStartedIntent.getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE), inventoryDate);
    }

    @Test
    public void shouldRefreshUIWhenInvokeOnActivityResultWithRNRListRequestCode() throws Exception {
        rnRFormListActivity.onActivityResult(Constants.REQUEST_FROM_RNR_LIST_PAGE, Activity.RESULT_OK, new Intent());

        verify(mockedPresenter, times(2)).loadRnRFormList();
    }

    @Test
    public void shouldOnlyShowCreateEmergencyRnRButtonInVIAPage() {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_create_emergency_rnr, true);

        Intent intent = new Intent();
        intent.putExtra(Constants.PARAM_PROGRAM_CODE, Constants.VIA_PROGRAM_CODE);
        rnRFormListActivity = Robolectric.buildActivity(RnRFormListActivity.class).withIntent(intent).create().visible().get();

        MenuItem createEmergencyRnr = shadowOf(rnRFormListActivity).getOptionsMenu().findItem(R.id.action_create_emergency_rnr);
        assertTrue(createEmergencyRnr.isVisible());
    }

    @Test
    public void shouldHideCreateEmergencyRnRButtonInMMIAPage() {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_create_emergency_rnr, true);

        Intent intent = new Intent();
        intent.putExtra(Constants.PARAM_PROGRAM_CODE, Constants.MMIA_PROGRAM_CODE);
        rnRFormListActivity = Robolectric.buildActivity(RnRFormListActivity.class).withIntent(intent).create().visible().get();

        MenuItem createEmergencyRnr = shadowOf(rnRFormListActivity).getOptionsMenu().findItem(R.id.action_create_emergency_rnr);
        assertFalse(createEmergencyRnr.isVisible());
    }

    @Test
    public void shouldOpenEmergencyRnrPageWhenEmergencyActionMenuClicked() {
        rnRFormListActivity.onOptionsItemSelected(new RoboMenuItem(R.id.action_create_emergency_rnr));

        Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(nextStartedIntent);
        assertEquals(nextStartedIntent.getComponent().getClassName(), SelectEmergencyProductsActivity.class.getName());
    }


    private RnRFormViewModel generateRnRFormViewModel(String programCode, int viewModelType) {
        return new RnRFormViewModel(new Period(new DateTime()), programCode, viewModelType);
    }
}