package org.openlmis.core.view.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import com.google.inject.AbstractModule;
import java.util.Date;
import org.hamcrest.MatcherAssert;
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
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowToast;
import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class RnRFormListActivityTest {

  private RnRFormListActivity rnRFormListActivity;
  private RnRFormListPresenter mockedPresenter;
  private Intent intent;
  private ActivityController<RnRFormListActivity> activityController;

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
    intent.putExtra(Constants.PARAM_PROGRAM_CODE, Constants.Program.MMIA_PROGRAM);
    activityController = Robolectric.buildActivity(RnRFormListActivity.class, intent);
    rnRFormListActivity = activityController.create().get();
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldSetMmiaTitleAndProgramCodeWhenProgramCodeIsMmia() {
    assertThat(rnRFormListActivity.getTitle())
        .isEqualTo(rnRFormListActivity.getResources().getString(R.string.mmia_list));
    verify(mockedPresenter).setProgramCode(Constants.MMIA_PROGRAM_CODE);
  }

  @Test
  public void shouldSetViaTitleAndProgramCodeWhenProgramCodeIsVia() {

    intent.putExtra(Constants.PARAM_PROGRAM_CODE, Constants.Program.VIA_PROGRAM);
    rnRFormListActivity = Robolectric.buildActivity(RnRFormListActivity.class, intent).create()
        .get();

    assertThat(rnRFormListActivity.getTitle())
        .isEqualTo(rnRFormListActivity.getResources().getString(R.string.requisition_list));
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

    rnRFormListActivity.getRnRFormSubscriber().onNext(
        newArrayList(RnRFormViewModel.buildNormalRnrViewModel(form),
            RnRFormViewModel.buildNormalRnrViewModel(form)));

    assertThat(rnRFormListActivity.listView.getAdapter().getItemCount()).isEqualTo(2);
  }

  @Test
  public void shouldStartPhysicalInventoryWhenBtnClickedWithUncompleteInventory() throws Exception {
    View view = mock(View.class);

    RnRFormViewModel viewModel = generateRnRFormViewModel("MMIA",
        RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD);
    rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel, view);

    Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

    assertNotNull(nextStartedIntent);
    assertEquals(nextStartedIntent.getComponent().getClassName(),
        PhysicalInventoryActivity.class.getName());
  }

  @Test
  public void shouldSelectPeriodWhenBtnClickedWithSelectClosePeriod() throws Exception {
    View view = mock(View.class);

    RnRFormViewModel viewModel = generateRnRFormViewModel("MMIA",
        RnRFormViewModel.TYPE_INVENTORY_DONE);
    rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel, view);

    Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

    assertNotNull(nextStartedIntent);
    assertEquals(nextStartedIntent.getComponent().getClassName(),
        SelectPeriodActivity.class.getName());
    assertEquals(nextStartedIntent.getStringExtra(Constants.PARAM_PROGRAM_CODE), "MMIA");
  }

  @Test
  public void shouldStartSelectPeriodPageWhenBtnClickedWithTypeMissedPeriod() throws Exception {
    View view = mock(View.class);

    RnRFormViewModel viewModel = generateRnRFormViewModel("MMIA",
        RnRFormViewModel.TYPE_FIRST_MISSED_PERIOD);
    rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel, view);

    Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

    assertNotNull(nextStartedIntent);
    assertEquals(nextStartedIntent.getComponent().getClassName(),
        SelectPeriodActivity.class.getName());
    assertEquals(nextStartedIntent.getStringExtra(Constants.PARAM_PROGRAM_CODE), "MMIA");
  }

  @Test
  public void shouldStartMMIAHistoryWhenBtnClickedWithTypeHistory() throws Exception {
    View view = mock(View.class);

    RnRFormViewModel viewModel = generateRnRFormViewModel("MMIA",
        RnRFormViewModel.TYPE_SYNCED_HISTORICAL);
    viewModel.setId(999L);
    rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel, view);

    Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

    assertNotNull(nextStartedIntent);
    assertEquals(nextStartedIntent.getComponent().getClassName(),
        MMIARequisitionActivity.class.getName());
    assertEquals(nextStartedIntent.getLongExtra(Constants.PARAM_FORM_ID, 0), 999L);
  }

  @Test
  public void shouldStartVIAHistoryWhenBtnClickedWithTypeHistory() throws Exception {
    View view = mock(View.class);

    intent.putExtra(Constants.PARAM_PROGRAM_CODE, Constants.Program.VIA_PROGRAM);
    rnRFormListActivity = Robolectric.buildActivity(RnRFormListActivity.class, intent).create()
        .get();

    RnRFormViewModel viewModel = generateRnRFormViewModel("ESS_MEDS",
        RnRFormViewModel.TYPE_SYNCED_HISTORICAL);
    viewModel.setId(999L);
    rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel, view);

    Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

    assertNotNull(nextStartedIntent);
    assertEquals(nextStartedIntent.getComponent().getClassName(),
        VIARequisitionActivity.class.getName());
    assertEquals(nextStartedIntent.getLongExtra(Constants.PARAM_FORM_ID, 0), 999L);
  }

  @Test
  public void shouldStartMMIAEditPageWhenBtnClickedWithTypeUnauthorized() throws Exception {
    View view = mock(View.class);

    RnRFormViewModel viewModel = generateRnRFormViewModel("MMIA", RnRFormViewModel.TYPE_DRAFT);
    rnRFormListActivity.rnRFormItemClickListener.clickBtnView(viewModel, view);

    Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

    assertNotNull(nextStartedIntent);
    assertEquals(nextStartedIntent.getComponent().getClassName(),
        MMIARequisitionActivity.class.getName());
    assertEquals(nextStartedIntent.getLongExtra(Constants.PARAM_FORM_ID, 0), 0L);
  }

  @Test
  public void shouldNotLoadSameFormIdAfterLoadedViaHistoryForm() throws Exception {
    View view = mock(View.class);

    RnRFormViewModel historyViewModel = generateRnRFormViewModel("MMIA",
        RnRFormViewModel.TYPE_SYNCED_HISTORICAL);
    historyViewModel.setId(1L);
    rnRFormListActivity.rnRFormItemClickListener.clickBtnView(historyViewModel, view);

    Intent startedIntentWhenIsHistory = ShadowApplication.getInstance().getNextStartedActivity();

    assertNotNull(startedIntentWhenIsHistory);
    assertEquals(startedIntentWhenIsHistory.getComponent().getClassName(),
        MMIARequisitionActivity.class.getName());
    assertEquals(1L, startedIntentWhenIsHistory.getLongExtra(Constants.PARAM_FORM_ID, 0));

    RnRFormViewModel defaultViewModel = generateRnRFormViewModel("MMIA",
        RnRFormViewModel.TYPE_DRAFT);
    rnRFormListActivity.rnRFormItemClickListener.clickBtnView(defaultViewModel, view);

    Intent startedIntentWhenIsDefault = ShadowApplication.getInstance().getNextStartedActivity();

    assertNotNull(startedIntentWhenIsDefault);
    assertEquals(startedIntentWhenIsDefault.getComponent().getClassName(),
        MMIARequisitionActivity.class.getName());
    assertEquals(0L, startedIntentWhenIsDefault.getLongExtra(Constants.PARAM_FORM_ID, 0));
  }

  @Test
  public void shouldGoToRequisitionPageWhenInvokeOnActivityResultWithSelectPeriodRequestCode()
      throws Exception {
    Intent data = new Intent();
    Date inventoryDate = new Date();
    data.putExtra(Constants.PARAM_SELECTED_INVENTORY_DATE, inventoryDate);
    data.putExtra(Constants.PARAM_IS_MISSED_PERIOD, true);

    intent.putExtra(Constants.PARAM_PROGRAM_CODE, Constants.Program.VIA_PROGRAM);
    rnRFormListActivity = Robolectric.buildActivity(RnRFormListActivity.class, intent).create()
        .get();

    rnRFormListActivity
        .onActivityResult(Constants.REQUEST_SELECT_PERIOD_END, Activity.RESULT_OK, data);

    Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();

    assertNotNull(nextStartedIntent);
    assertEquals(nextStartedIntent.getComponent().getClassName(),
        VIARequisitionActivity.class.getName());

    assertTrue(nextStartedIntent.getBooleanExtra(Constants.PARAM_IS_MISSED_PERIOD, false));
    assertEquals(nextStartedIntent.getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE), inventoryDate);
  }

  @Test
  public void shouldRefreshUIWhenInvokeOnActivityResultWithRNRListRequestCode() throws Exception {
    rnRFormListActivity
        .onActivityResult(Constants.REQUEST_FROM_RNR_LIST_PAGE, Activity.RESULT_OK, new Intent());

    verify(mockedPresenter, times(2)).loadRnRFormList();
  }

  @Test
  public void shouldShowToastWhenDateNotInEmergencyDate() throws Exception {
    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-05-18 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
    rnRFormListActivity.checkAndGotoEmergencyPage();
    MatcherAssert.assertThat(ShadowToast.getTextOfLatestToast(),
        is("You are not allowed to create an emergency between 18th and 25th, please submit request using the monthly requisition form."));
  }

  @Test
  public void shouldShowToastWhenHasMissed() throws Exception {
    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-05-17 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());

    Observable<Boolean> value = Observable.create(new Observable.OnSubscribe<Boolean>() {
      @Override
      public void call(Subscriber<? super Boolean> subscriber) {
        subscriber.onNext(true);
      }
    });

    when(mockedPresenter.hasMissedPeriod()).thenReturn(value);
    rnRFormListActivity.checkAndGotoEmergencyPage();
    MatcherAssert.assertThat(ShadowToast.getTextOfLatestToast(),
        is("You are not allowed to create an emergency requisition until you complete all your previous monthly requisitions."));
  }

  @Test
  public void shouldGotoEmergencyPage() throws Exception {
    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-05-17 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());

    Observable<Boolean> value = Observable.create(new Observable.OnSubscribe<Boolean>() {
      @Override
      public void call(Subscriber<? super Boolean> subscriber) {
        subscriber.onNext(false);
      }
    });
    when(mockedPresenter.hasMissedPeriod()).thenReturn(value);

    rnRFormListActivity.checkAndGotoEmergencyPage();

    ShadowActivity shadowActivity = shadowOf(rnRFormListActivity);
    Intent startedIntent = shadowActivity.getNextStartedActivity();
    ShadowIntent shadowIntent = shadowOf(startedIntent);
    MatcherAssert.assertThat(shadowIntent.getIntentClass().getCanonicalName(),
        equalTo(SelectEmergencyProductsActivity.class.getName()));
  }

  private RnRFormViewModel generateRnRFormViewModel(String programCode, int viewModelType) {
    return new RnRFormViewModel(new Period(new DateTime()), programCode, viewModelType);
  }
}