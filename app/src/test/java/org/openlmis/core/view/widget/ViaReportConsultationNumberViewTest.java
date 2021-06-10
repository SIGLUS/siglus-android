package org.openlmis.core.view.widget;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;

@RunWith(LMISTestRunner.class)
public class ViaReportConsultationNumberViewTest {

  private ViaReportConsultationNumberView view;

  @Before
  public void setUp() throws Exception {
    view = new ViaReportConsultationNumberView(LMISTestApp.getContext());
  }

  @Test
  public void shouldShowErrorWhenConsultationNumbersIsEmpty() throws Exception {
    view.etExternalConsultationsPerformed.setText("");
    view.validate();
    assertThat(view.etExternalConsultationsPerformed.getError().toString())
        .isEqualTo("Invalid Input");
  }
}