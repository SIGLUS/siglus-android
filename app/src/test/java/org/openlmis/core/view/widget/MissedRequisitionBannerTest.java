package org.openlmis.core.view.widget;

import android.view.View;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.service.PeriodService;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class MissedRequisitionBannerTest {

    protected MissedRequisitionBanner missedRequisitionBanner;
    PeriodService periodService;

    @Before
    public void setUp() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_missed_requisition_banner, true);
        periodService = mock(PeriodService.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(PeriodService.class).toInstance(periodService);
            }
        });
    }

    @Test
    public void shouldNotShowBannerWhenThereIsMissedRequisition() throws LMISException {
        when(periodService.hasMissedPeriod(anyString())).thenReturn(false);
        missedRequisitionBanner = new MissedRequisitionBanner(LMISTestApp.getContext());

        assertEquals(missedRequisitionBanner.getVisibility(), View.GONE);
    }


    @Test
    public void shouldShowBannerWithMissedMmiaAndViaWhenThereAreMultipleMissedRequisitions() throws LMISException {
        when(periodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(periodService.getMissedPeriodOffsetMonth("VIA")).thenReturn(2);
        when(periodService.getMissedPeriodOffsetMonth("MMIA")).thenReturn(2);
        missedRequisitionBanner = new MissedRequisitionBanner(LMISTestApp.getContext());

        assertThat(missedRequisitionBanner.txMissedRequisition.getText().toString(), is("Your MMIA and VIA requisitions for multiple periods have not been completed"));
    }

    @Test
    public void shouldShowBannerWithViaWhenThereAreMultipleMissedRequisitions() throws LMISException {
        when(periodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(periodService.getMissedPeriodOffsetMonth("VIA")).thenReturn(2);
        when(periodService.getMissedPeriodOffsetMonth("MMIA")).thenReturn(0);
        missedRequisitionBanner = new MissedRequisitionBanner(LMISTestApp.getContext());

        assertThat(missedRequisitionBanner.txMissedRequisition.getText().toString(), is("Your VIA requisitions for multiple periods have not been completed"));
    }

    @Test
    public void shouldShowBannerWithMmiaWhenThereAreMultipleMissedRequisitions() throws LMISException {
        when(periodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(periodService.getMissedPeriodOffsetMonth("VIA")).thenReturn(0);
        when(periodService.getMissedPeriodOffsetMonth("MMIA")).thenReturn(2);
        missedRequisitionBanner = new MissedRequisitionBanner(LMISTestApp.getContext());

        assertThat(missedRequisitionBanner.txMissedRequisition.getText().toString(), is("Your MMIA requisitions for multiple periods have not been completed"));
    }

    @Test
    public void shouldShowBannerWithViaWhenThereIsSingleMissedRequisition() throws LMISException {
        Period period = new Period(new DateTime(DateTime.parse("2016-05-18")));
        when(periodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(periodService.getMissedPeriodOffsetMonth("VIA")).thenReturn(1);
        when(periodService.getMissedPeriodOffsetMonth("MMIA")).thenReturn(0);
        when(periodService.generateNextPeriod("VIA", null)).thenReturn(period);
        missedRequisitionBanner = new MissedRequisitionBanner(LMISTestApp.getContext());

        assertThat(missedRequisitionBanner.txMissedRequisition.getText().toString(), is("Your VIA requisition for April 2016 to May 2016 has not been completed"));
    }

    @Test
    public void shouldShowBannerWithMmiaWhenThereIsSingleMissedRequisition() throws LMISException {
        Period period = new Period(new DateTime(DateTime.parse("2016-05-18")));
        when(periodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(periodService.getMissedPeriodOffsetMonth("VIA")).thenReturn(0);
        when(periodService.getMissedPeriodOffsetMonth("MMIA")).thenReturn(1);
        when(periodService.generateNextPeriod("MMIA", null)).thenReturn(period);
        missedRequisitionBanner = new MissedRequisitionBanner(LMISTestApp.getContext());

        assertThat(missedRequisitionBanner.txMissedRequisition.getText().toString(), is("Your MMIA requisition for April 2016 to May 2016 has not been completed"));
    }

    @Test
    public void shouldShowBannerWithViaAndMmiaWhenThereIsSingleMissedRequisition() throws LMISException {
        Period period = new Period(new DateTime(DateTime.parse("2016-05-18")));
        when(periodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(periodService.getMissedPeriodOffsetMonth("VIA")).thenReturn(1);
        when(periodService.getMissedPeriodOffsetMonth("MMIA")).thenReturn(1);
        when(periodService.generateNextPeriod("VIA", null)).thenReturn(period);
        missedRequisitionBanner = new MissedRequisitionBanner(LMISTestApp.getContext());

        assertThat(missedRequisitionBanner.txMissedRequisition.getText().toString(), is("Your MMIA and VIA requisitions for April 2016 to May 2016 have not been completed"));
    }
}