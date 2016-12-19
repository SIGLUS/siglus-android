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
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.utils.Constants;
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
    RequisitionPeriodService requisitionPeriodService;

    @Before
    public void setUp() throws Exception {
        requisitionPeriodService = mock(RequisitionPeriodService.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(RequisitionPeriodService.class).toInstance(requisitionPeriodService);
            }
        });
    }

    @Test
    public void shouldNotShowBannerWhenThereIsMissedRequisition() throws LMISException {
        when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(false);
        incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

        assertEquals(incompleteRequisitionBanner.getVisibility(), View.GONE);
    }


    @Test
    public void shouldShowMultipleMissedMmiaAndViaRequisitionBanner() throws LMISException {
        when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.VIA_PROGRAM_CODE)).thenReturn(2);
        when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.MMIA_PROGRAM_CODE)).thenReturn(2);
        incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

        assertThat(incompleteRequisitionBanner.txMissedRequisition.getText().toString(), is("Your MMIA and VIA requisitions for multiple periods have not been completed"));
    }

    @Test
    public void shouldShowMultipleMissedViaRequisitionBanner() throws LMISException {
        when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.VIA_PROGRAM_CODE)).thenReturn(2);
        when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.MMIA_PROGRAM_CODE)).thenReturn(0);
        incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

        assertThat(incompleteRequisitionBanner.txMissedRequisition.getText().toString(), is("Your VIA requisitions for multiple periods have not been completed"));
    }

    @Test
    public void shouldShowMultipleMissedMmiaRequisitionBanner() throws LMISException {
        when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.VIA_PROGRAM_CODE)).thenReturn(0);
        when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.MMIA_PROGRAM_CODE)).thenReturn(2);
        incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

        assertThat(incompleteRequisitionBanner.txMissedRequisition.getText().toString(), is("Your MMIA for multiple periods have not been completed"));
    }

    @Test
    public void shouldShowSingleMissedViaRequisitionBanner() throws LMISException {
        Period period = new Period(new DateTime(DateTime.parse("2016-05-18")));
        when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.VIA_PROGRAM_CODE)).thenReturn(1);
        when(requisitionPeriodService.getMissedPeriodOffsetMonth(Constants.MMIA_PROGRAM_CODE)).thenReturn(0);
        when(requisitionPeriodService.generateNextPeriod(Constants.VIA_PROGRAM_CODE, null)).thenReturn(period);
        incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

        assertThat(incompleteRequisitionBanner.txMissedRequisition.getText().toString(), is("Your VIA requisition for April 2016 to May 2016 has not been completed"));
    }

    @Test
    public void shouldShowSingleMissedMmiaRequisitionBanner() throws LMISException {
        Period period = new Period(new DateTime(DateTime.parse("2016-05-18")));
        when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.VIA_PROGRAM_CODE)).thenReturn(0);
        when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.MMIA_PROGRAM_CODE)).thenReturn(1);
        when(requisitionPeriodService.generateNextPeriod(Constants.MMIA_PROGRAM_CODE, null)).thenReturn(period);
        incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

        assertThat(incompleteRequisitionBanner.txMissedRequisition.getText().toString(), is("Your MMIA for April 2016 to May 2016 has not been completed"));
    }

    @Test
    public void shouldShowSingleMissedViaAndMmiaRequisitionBanner() throws LMISException {
        Period period = new Period(new DateTime(DateTime.parse("2016-05-18")));
        when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(true);
        when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.VIA_PROGRAM_CODE)).thenReturn(1);
        when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.MMIA_PROGRAM_CODE)).thenReturn(1);
        when(requisitionPeriodService.generateNextPeriod(Constants.VIA_PROGRAM_CODE, null)).thenReturn(period);
        incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

        assertThat(incompleteRequisitionBanner.txMissedRequisition.getText().toString(), is("Your MMIA and VIA requisitions for April 2016 to May 2016 have not been completed"));
    }
}