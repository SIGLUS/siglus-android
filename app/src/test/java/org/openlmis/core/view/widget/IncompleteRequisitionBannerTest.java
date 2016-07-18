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
public class IncompleteRequisitionBannerTest {

    protected IncompleteRequisitionBanner incompleteRequisitionBanner;
    PeriodService periodService;

    @Before
    public void setUp() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_incomplete_requisition_banner, true);
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
        incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

        assertEquals(incompleteRequisitionBanner.getVisibility(), View.GONE);
    }


    @Test
    public void shouldShowMultipleMissedMmiaAndViaRequisitionBanner() throws LMISException {
        when(periodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(periodService.getIncompletePeriodOffsetMonth("VIA")).thenReturn(2);
        when(periodService.getIncompletePeriodOffsetMonth("MMIA")).thenReturn(2);
        incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

        assertThat(incompleteRequisitionBanner.txMissedRequisition.getText().toString(), is("Your MMIA and VIA requisitions for multiple periods have not been completed"));
    }

    @Test
    public void shouldShowMultipleMissedViaRequisitionBanner() throws LMISException {
        when(periodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(periodService.getIncompletePeriodOffsetMonth("VIA")).thenReturn(2);
        when(periodService.getIncompletePeriodOffsetMonth("MMIA")).thenReturn(0);
        incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

        assertThat(incompleteRequisitionBanner.txMissedRequisition.getText().toString(), is("Your VIA requisitions for multiple periods have not been completed"));
    }

    @Test
    public void shouldShowMultipleMissedMmiaRequisitionBanner() throws LMISException {
        when(periodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(periodService.getIncompletePeriodOffsetMonth("VIA")).thenReturn(0);
        when(periodService.getIncompletePeriodOffsetMonth("MMIA")).thenReturn(2);
        incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

        assertThat(incompleteRequisitionBanner.txMissedRequisition.getText().toString(), is("Your MMIA requisitions for multiple periods have not been completed"));
    }

    @Test
    public void shouldShowSingleMissedViaRequisitionBanner() throws LMISException {
        Period period = new Period(new DateTime(DateTime.parse("2016-05-18")));
        when(periodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(periodService.getIncompletePeriodOffsetMonth("VIA")).thenReturn(1);
        when(periodService.getMissedPeriodOffsetMonth("MMIA")).thenReturn(0);
        when(periodService.generateNextPeriod("VIA", null)).thenReturn(period);
        incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

        assertThat(incompleteRequisitionBanner.txMissedRequisition.getText().toString(), is("Your VIA requisition for April 2016 to May 2016 has not been completed"));
    }

    @Test
    public void shouldShowSingleMissedMmiaRequisitionBanner() throws LMISException {
        Period period = new Period(new DateTime(DateTime.parse("2016-05-18")));
        when(periodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(periodService.getIncompletePeriodOffsetMonth("VIA")).thenReturn(0);
        when(periodService.getIncompletePeriodOffsetMonth("MMIA")).thenReturn(1);
        when(periodService.generateNextPeriod("MMIA", null)).thenReturn(period);
        incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

        assertThat(incompleteRequisitionBanner.txMissedRequisition.getText().toString(), is("Your MMIA requisition for April 2016 to May 2016 has not been completed"));
    }

    @Test
    public void shouldShowSingleMissedViaAndMmiaRequisitionBanner() throws LMISException {
        Period period = new Period(new DateTime(DateTime.parse("2016-05-18")));
        when(periodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(periodService.getIncompletePeriodOffsetMonth("VIA")).thenReturn(1);
        when(periodService.getIncompletePeriodOffsetMonth("MMIA")).thenReturn(1);
        when(periodService.generateNextPeriod("VIA", null)).thenReturn(period);
        incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

        assertThat(incompleteRequisitionBanner.txMissedRequisition.getText().toString(), is("Your MMIA and VIA requisitions for April 2016 to May 2016 have not been completed"));
    }
}