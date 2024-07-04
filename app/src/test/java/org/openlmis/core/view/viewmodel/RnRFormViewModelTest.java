package org.openlmis.core.view.viewmodel;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.builder.RnRFormBuilder;

@RunWith(LMISTestRunner.class)
public class RnRFormViewModelTest {

  private RnRFormViewModel rnRFormViewModel;

  @Before
  public void setUp() throws Exception {
    rnRFormViewModel = new RnRFormViewModel(new Period(DateTime.now()), "programCode", -1);
  }

  @Test
  public void setTypeWithForm_shouldReturnRejectedTypeWhenStatusIsRejected() {
    // given
    RnRForm rnRForm = new RnRFormBuilder().setStatus(Status.REJECTED).build();
    // when
    rnRFormViewModel.setType(rnRForm);
    // then
    assertEquals(61, rnRFormViewModel.getType());
  }
}