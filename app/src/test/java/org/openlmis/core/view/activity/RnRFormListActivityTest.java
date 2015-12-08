package org.openlmis.core.view.activity;


import android.content.Intent;

import com.google.inject.AbstractModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.presenter.RnRFormListPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import roboguice.RoboGuice;
import rx.Observable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
        rnRFormListActivity.getRnRFormSubscriber().onNext(newArrayList(new RnRFormViewModel("hello"), new RnRFormViewModel("world")));

        assertThat(rnRFormListActivity.listView.getAdapter().getItemCount()).isEqualTo(2);
    }
}