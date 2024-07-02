package org.openlmis.core.view.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.builder.RnRFormBuilder;

public class RnRFormViewModelTest {

  private RnRFormViewModel rnRFormViewModel;

  @Before
  public void setUp() throws Exception {
    rnRFormViewModel = new RnRFormViewModel(mock(Period.class), "programCode", -1);
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