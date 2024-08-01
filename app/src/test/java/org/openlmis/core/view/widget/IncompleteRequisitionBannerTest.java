package org.openlmis.core.view.widget;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.view.View;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.builder.ReportTypeBuilder;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.service.RequisitionPeriodService;
import androidx.test.core.app.ApplicationProvider;
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
    RoboGuice.overrideApplicationInjector(ApplicationProvider.getApplicationContext(), binder -> {
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
  public void shouldShowOneIncompleteReportBanner() throws LMISException {
    // given
    List<ReportTypeForm> reportTypeForms = new ArrayList<>();
    reportTypeForms.add(ReportTypeForm.builder()
        .code("ML")
        .name("Malaria")
        .build());
    when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(true);
    when(requisitionPeriodService.getIncompleteReports()).thenReturn(reportTypeForms);

    // when
    incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

    // then
    assertEquals("Your Malaria has not been completed",
        incompleteRequisitionBanner.txMissedRequisition.getText().toString());
  }

  @Test
  public void shouldShowMultipleIncompleteReportBanner() throws LMISException {
    // given
    List<ReportTypeForm> reportTypeForms = new ArrayList<>();
    ReportTypeForm report1 = ReportTypeForm.builder()
        .code("ML")
        .name("Malaria")
        .build();
    ReportTypeForm report2 = ReportTypeForm.builder()
        .code("TARV")
        .name("MMIA")
        .build();
    reportTypeForms.add(report1);
    reportTypeForms.add(report2);
    when(requisitionPeriodService.hasMissedPeriod(anyString())).thenReturn(true);
    when(requisitionPeriodService.getIncompleteReports()).thenReturn(reportTypeForms);

    // when
    incompleteRequisitionBanner = new IncompleteRequisitionBanner(LMISTestApp.getContext());

    // then
    assertEquals("Your Malaria, MMIA have not been completed",
        incompleteRequisitionBanner.txMissedRequisition.getText().toString());
  }

}