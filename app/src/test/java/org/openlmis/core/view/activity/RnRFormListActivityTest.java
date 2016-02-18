package org.openlmis.core.view.activity;


import android.app.Activity;
import android.content.Intent;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.presenter.RnRFormListPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowToast;

import java.util.Date;

import roboguice.RoboGuice;
import rx.Observable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

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
        intent.putExtra(Constants.PARAM_PROGRAM_CODE, MMIARepository.MMIA_PROGRAM_CODE);
        rnRFormListActivity = Robolectric.buildActivity(RnRFormListActivity.class).withIntent(intent).create().get();
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldSetMmiaTitleAndProgramCodeWhenProgramCodeIsMmia() {
        assertThat(rnRFormListActivity.getTitle()).isEqualTo(rnRFormListActivity.getResources().getString(R.string.title_mmia_list));
        verify(mockedPresenter).setProgramCode(MMIARepository.MMIA_PROGRAM_CODE);
    }

    @Test
    public void shouldSetViaTitleAndProgramCodeWhenProgramCodeIsVia() {

        intent.putExtra(Constants.PARAM_PROGRAM_CODE, VIARepository.VIA_PROGRAM_CODE);
        rnRFormListActivity = Robolectric.buildActivity(RnRFormListActivity.class).withIntent(intent).create().get();

        assertThat(rnRFormListActivity.getTitle()).isEqualTo(rnRFormListActivity.getResources().getString(R.string.title_requisition_list));
        verify(mockedPresenter).setProgramCode(VIARepository.VIA_PROGRAM_CODE);
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
        RnRFormViewModel viewModel = generateRnRFormViewModel("MMIA", RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel);

        Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(nextStartedIntent);
        assertEquals(nextStartedIntent.getComponent().getClassName(), InventoryActivity.class.getName());
        assertTrue(nextStartedIntent.getBooleanExtra(Constants.PARAM_IS_PHYSICAL_INVENTORY, false));
    }

    @Test
    public void shouldSelectPeriodWhenBtnClickedWithSelectClosePeriod() throws Exception {
        RnRFormViewModel viewModel = generateRnRFormViewModel("MMIA", RnRFormViewModel.TYPE_SELECT_CLOSE_OF_PERIOD);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel);

        Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(nextStartedIntent);
        assertEquals(nextStartedIntent.getComponent().getClassName(), SelectPeriodActivity.class.getName());
        assertEquals(nextStartedIntent.getStringExtra(Constants.PARAM_PROGRAM_CODE), "MMIA");
    }

    @Test
    public void shouldStartMMIAHistoryWhenBtnClickedWithTypeHistory() throws Exception {
        RnRFormViewModel viewModel = generateRnRFormViewModel("MMIA", RnRFormViewModel.TYPE_HISTORICAL);
        viewModel.setId(999L);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel);

        Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(nextStartedIntent);
        assertEquals(nextStartedIntent.getComponent().getClassName(), MMIARequisitionActivity.class.getName());
        assertEquals(nextStartedIntent.getLongExtra(Constants.PARAM_FORM_ID, 0), 999L);
    }

    @Test
    public void shouldStartVIAHistoryWhenBtnClickedWithTypeHistory() throws Exception {
        intent.putExtra(Constants.PARAM_PROGRAM_CODE, VIARepository.VIA_PROGRAM_CODE);
        rnRFormListActivity = Robolectric.buildActivity(RnRFormListActivity.class).withIntent(intent).create().get();

        RnRFormViewModel viewModel = generateRnRFormViewModel("ESS_MEDS", RnRFormViewModel.TYPE_HISTORICAL);
        viewModel.setId(999L);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel);

        Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(nextStartedIntent);
        assertEquals(nextStartedIntent.getComponent().getClassName(), VIARequisitionActivity.class.getName());
        assertEquals(nextStartedIntent.getLongExtra(Constants.PARAM_FORM_ID, 0), 999L);
    }

    @Test
    public void shouldStartMMIAEditPageWhenBtnClickedWithTypeUnauthorized() throws Exception {
        RnRFormViewModel viewModel = generateRnRFormViewModel("MMIA", RnRFormViewModel.TYPE_UN_AUTHORIZED);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel);

        Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(nextStartedIntent);
        assertEquals(nextStartedIntent.getComponent().getClassName(), MMIARequisitionActivity.class.getName());
        assertEquals(nextStartedIntent.getLongExtra(Constants.PARAM_FORM_ID, 0), 0L);
    }

    @Test
    public void shouldNotLoadSameFormIdAfterLoadedViaHistoryForm() throws Exception {
        RnRFormViewModel historyViewModel = generateRnRFormViewModel("MMIA", RnRFormViewModel.TYPE_HISTORICAL);
        historyViewModel.setId(1L);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(historyViewModel);

        Intent startedIntentWhenIsHistory = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(startedIntentWhenIsHistory);
        assertEquals(startedIntentWhenIsHistory.getComponent().getClassName(), MMIARequisitionActivity.class.getName());
        assertEquals(1L, startedIntentWhenIsHistory.getLongExtra(Constants.PARAM_FORM_ID, 0));

        RnRFormViewModel defaultViewModel = generateRnRFormViewModel("MMIA", RnRFormViewModel.TYPE_UN_AUTHORIZED);
        rnRFormListActivity.rnRFormItemClickListener.clickBtnView(defaultViewModel);

        Intent startedIntentWhenIsDefault = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(startedIntentWhenIsDefault);
        assertEquals(startedIntentWhenIsDefault.getComponent().getClassName(), MMIARequisitionActivity.class.getName());
        assertEquals(0L, startedIntentWhenIsDefault.getLongExtra(Constants.PARAM_FORM_ID, 0));
    }

    @Test
    public void shouldGoToRequisitionPageWhenInvokeOnActivityResultWithSelectPeriodRequestCode() throws Exception {
        rnRFormListActivity.onActivityResult(Constants.REQUEST_SELECT_PERIOD_END, Activity.RESULT_OK, new Intent());

        Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

        assertNotNull(nextStartedIntent);
        assertEquals(nextStartedIntent.getComponent().getClassName(), MMIARequisitionActivity.class.getName());
    }

    @Test
    public void shouldRefreshUIWhenInvokeOnActivityResultWithRNRListRequestCode() throws Exception {
        rnRFormListActivity.onActivityResult(Constants.REQUEST_FROM_RNR_LIST_PAGE, Activity.RESULT_OK, new Intent());

        verify(mockedPresenter, times(2)).loadRnRFormList();
    }

    private RnRFormViewModel generateRnRFormViewModel(String programCode, int viewModelType) {
        return new RnRFormViewModel(new Period(new DateTime()), programCode, viewModelType);
    }
}