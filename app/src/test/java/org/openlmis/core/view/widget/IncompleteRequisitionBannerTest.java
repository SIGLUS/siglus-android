package org.openlmis.core.view.widget;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.view.View;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.builder.ReportTypeBuilder;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.utils.Constants;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class IncompleteRequisitionBannerTest {

  protected IncompleteRequisitionBanner incompleteRequisitionBanner;
  RequisitionPeriodService requisitionPeriodService;
  ReportTypeFormRepository mockReportTypeFormRepository;

  @Before
  public void setUp() throws Exception {
    requisitionPeriodService = mock(RequisitionPeriodService.class);
    mockReportTypeFormRepository = mock(ReportTypeFormRepository.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, binder -> {
      binder.bind(RequisitionPeriodService.class).toInstance(requisitionPeriodService);
      binder.bind(ReportTypeFormRepository.class).toInstance(mockReportTypeFormRepository);
    });
    ReportTypeBuilder reportTypeFormBuilder = new ReportTypeBuilder();

    when(mockReportTypeFormRepository.getReportType(Program.TARV_CODE)).
        thenReturn(reportTypeFormBuilder.getMMIAReportTypeForm());
    when(mockReportTypeFormRepository.getReportType(Program.VIA_CODE)).
        thenReturn(reportTypeFormBuilder.getMMIAReportTypeForm());
  }

  @Test
  public void shouldNotShowBannerWhenThereIsMissedRequisition() throws LMISException {
    // given
    when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(false);

    // when
    incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

    // then
    assertEquals(View.GONE, incompleteRequisitionBanner.getVisibility());
  }


  @Test
  public void shouldShowMultipleMissedMmiaAndViaRequisitionBanner() throws LMISException {
    // given
    when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(true);
    when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Program.VIA_CODE)).thenReturn(2);
    when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Program.TARV_CODE)).thenReturn(2);

    // when
    incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

    // then
    assertEquals("Your VIA and MMIA requisitions have not been completed",
        incompleteRequisitionBanner.txMissedRequisition.getText().toString());
  }

  @Test
  public void shouldShowMultipleMissedViaRequisitionBanner() throws LMISException {
    // give
    when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(true);
    when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.VIA_PROGRAM_CODE)).thenReturn(2);
    when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Constants.MMIA_PROGRAM_CODE)).thenReturn(0);

    // when
    incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

    assertEquals("Your VIA requisition has not been completed",
        incompleteRequisitionBanner.txMissedRequisition.getText().toString());
  }

  @Test
  public void shouldShowMultipleMissedMmiaRequisitionBanner() throws LMISException {
    // given
    when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(true);
    when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Program.VIA_CODE)).thenReturn(0);
    when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Program.TARV_CODE)).thenReturn(2);

    // when
    incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

    // then
    assertEquals("Your MMIA has not been completed",
        incompleteRequisitionBanner.txMissedRequisition.getText().toString());
  }

  @Test
  public void shouldShowSingleMissedViaRequisitionBanner() throws LMISException {
    // given
    Period period = new Period(new DateTime(DateTime.parse("2016-05-18")));
    when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(true);
    when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Program.VIA_CODE)).thenReturn(1);
    when(requisitionPeriodService.getMissedPeriodOffsetMonth(Program.TARV_CODE)).thenReturn(0);
    when(requisitionPeriodService.generateNextPeriod(Program.VIA_CODE, null)).thenReturn(period);

    // when
    incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

    // then
    assertEquals("Your VIA requisition has not been completed",
        incompleteRequisitionBanner.txMissedRequisition.getText().toString());
  }

  @Test
  public void shouldShowSingleMissedMmiaRequisitionBanner() throws LMISException {
    // given
    Period period = new Period(new DateTime(DateTime.parse("2016-05-18")));
    when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(true);
    when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Program.VIA_CODE)).thenReturn(0);
    when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Program.TARV_CODE)).thenReturn(1);
    when(requisitionPeriodService.generateNextPeriod(Program.TARV_CODE, null)).thenReturn(period);

    // when
    incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

    // then
    assertEquals("Your MMIA has not been completed",
        incompleteRequisitionBanner.txMissedRequisition.getText().toString());
  }

  @Test
  public void shouldShowSingleMissedViaAndMmiaRequisitionBanner() throws LMISException {
    // given
    Period period = new Period(new DateTime(DateTime.parse("2016-05-18")));
    when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(true);
    when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Program.VIA_CODE)).thenReturn(1);
    when(requisitionPeriodService.getIncompletePeriodOffsetMonth(Program.TARV_CODE)).thenReturn(1);
    when(requisitionPeriodService.generateNextPeriod(Program.VIA_CODE, null)).thenReturn(period);

    // when
    incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

    // then
    assertEquals("Your VIA and MMIA requisitions have not been completed",
        incompleteRequisitionBanner.txMissedRequisition.getText().toString());
  }
}