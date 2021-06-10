package org.openlmis.core.view.fragment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.inject.AbstractModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.presenter.RapidTestReportFormPresenter;
import org.openlmis.core.view.activity.RapidTestReportFormActivity;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class RapidTestReportFormFragmentTest {

  private RapidTestReportFormPresenter presenter;
  private RapidTestReportFormFragment rapidTestReportFormFragment;

  @Before
  public void setUp() {
    presenter = mock(RapidTestReportFormPresenter.class);
    rapidTestReportFormFragment = mock(RapidTestReportFormFragment.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyModule());
  }

//    @Test
//    public void shouldShowSaveAndCompleteButtonWhenFormIsEditable() {
//
////        Observable<RapidTestReportViewModel> loadViewModel = Observable.create(new Observable.OnSubscribe<RapidTestReportViewModel>() {
////            @Override
////            public void call(Subscriber<? super RapidTestReportViewModel> subscriber) {
////                subscriber.onCompleted();
////            }
////        });
////        TestSubscriber
////        TestObserver<RapidTestReportViewModel> loadViewModel =  TestObserver.class;
////        when(presenter.loadViewModel(0, null)).thenReturn(loadViewModel);
//
//        TestSubscriber subscriber = new TestSubscriber<>();
//        Observable<RapidTestReportViewModel> observable = presenter.loadViewModel(0, null);
//        observable.subscribe(subscriber);
//
//        subscriber.awaitTerminalEvent();
//
//        rapidTestReportFormFragment = getRapidTestRequisitionFragment();
//        rapidTestReportFormFragment.updateUI();
//        assertThat(rapidTestReportFormFragment.actionPanelView.getVisibility()).isEqualTo(View.VISIBLE);
//    }

  @Test
  public void shouldNotRemoveRnrFormWhenGoBack() {
    rapidTestReportFormFragment.onBackPressed();
    verify(presenter, never()).deleteDraft();
  }

  private class MyModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(RapidTestReportFormPresenter.class).toInstance(presenter);
    }
  }

  private RapidTestReportFormFragment getRapidTestRequisitionFragment() {
    RapidTestReportFormActivity rapidTestRequisitionActivity = Robolectric
        .buildActivity(RapidTestReportFormActivity.class).create().get();
    RapidTestReportFormFragment fragment = (RapidTestReportFormFragment) rapidTestRequisitionActivity
        .getFragmentManager().findFragmentById(R.id.fragment_requisition);

    return fragment;
  }
}
