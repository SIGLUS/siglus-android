package org.openlmis.core.view.widget;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LMISTestRunner.class)
public class ViaReportConsultationNumberViewTest {

    private ViaReportConsultationNumberView view;

    @Before
    public void setUp() throws Exception {
        view = new ViaReportConsultationNumberView(LMISTestApp.getContext());
    }

    @Test
    public void shouldShowErrorWhenConsultationNumbersIsEmpty() throws Exception {
        view.editText.setText("");
        view.validate();
        assertThat(view.editText.getError().toString()).isEqualTo("Invalid Input");
    }
}