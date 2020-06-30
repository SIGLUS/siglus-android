package org.openlmis.core.view.fragment;

import android.view.View;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.PTVRequisitionPresenter;
import org.openlmis.core.view.activity.PTVRequisitionActivity;
import org.openlmis.core.view.widget.PTVTestRnrForm;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class PTVRequisitionFragmentTest {

    private PTVRequisitionPresenter ptvRequisitionPresenter;
    private PTVTestRnrForm ptvTable;
    private PTVRequisitionFragment ptvRequisitionFragment;
    private SharedPreferenceMgr sharedPreferenceMgr;

    @Before
    public void setUp() {
        ptvRequisitionPresenter = mock(PTVRequisitionPresenter.class);
        ptvTable = mock(PTVTestRnrForm.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyModule());
        ptvRequisitionFragment = getPtvRequisitionFragment();
        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
    }

    @Test
    public void shouldShowSaveAndCompleteButtonWhenFormIsEditable() {
        ptvRequisitionFragment.initUI();

        assertThat(ptvRequisitionFragment.actionPanelView.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void shouldNotRemoveRnrFormWhenGoBack() {
        ptvRequisitionFragment.onBackPressed();
        verify(ptvRequisitionPresenter, never()).deleteDraft();
    }

    @Test
    public void shouldTestAA() {
//        sharedPreferenceMgr.setShouldSyncLastYearStockCardData(false);
        when(sharedPreferenceMgr.shouldSyncLastYearStockData()).thenReturn(false);
        ptvRequisitionFragment = getPtvRequisitionFragment();
        ptvRequisitionFragment.initUI();
        assertThat(ptvRequisitionFragment.actionPanelView.getVisibility()).isEqualTo(View.VISIBLE);
    }

    private class MyModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(PTVRequisitionPresenter.class).toInstance(ptvRequisitionPresenter);
        }
    }

    private PTVRequisitionFragment getPtvRequisitionFragment() {
        PTVRequisitionActivity ptvRequisitionActivity = Robolectric.buildActivity(PTVRequisitionActivity.class).create().get();
        PTVRequisitionFragment fragment = (PTVRequisitionFragment) ptvRequisitionActivity.getFragmentManager().findFragmentById(R.id.fragment_requisition);

        return fragment;
    }

}
