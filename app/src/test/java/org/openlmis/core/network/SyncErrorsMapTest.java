package org.openlmis.core.network;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;

@RunWith(LMISTestRunner.class)
public class SyncErrorsMapTest {

  @Test
  public void shouldReturnAppErrorWhenServerReturnsProgramConfigError() {
    String appDisplayError = SyncErrorsMap
        .getDisplayErrorMessageBySyncErrorMessage("Program configuration missing");
    assertEquals(LMISTestApp.getContext().getString(R.string.period_configuration_missing),
        appDisplayError);
  }

  @Test
  public void shouldReturnAppErrorWhenServerReturnsInvalidProductError() {
    String appDisplayError = SyncErrorsMap
        .getDisplayErrorMessageBySyncErrorMessage("Invalid product codes [01A01]");
    assertEquals(
        "Sync failed due attempted submission of invalid product in form [01A01]. Please contact system administrator.",
        appDisplayError);
  }

  @Test
  public void shouldReturnAppErrorWhenServerReturnsPreviousNotFilledError() {
    String appDisplayError = SyncErrorsMap
        .getDisplayErrorMessageBySyncErrorMessage("Please finish all R&R of previous period(s)");
    assertEquals(LMISTestApp.getContext().getString(R.string.rnr_previous_not_filled),
        appDisplayError);
  }

  @Test
  public void shouldReturnAppErrorWhenServerReturnsUserUnAuthorizedError() {
    String appDisplayError = SyncErrorsMap
        .getDisplayErrorMessageBySyncErrorMessage("User does not have permission");
    assertEquals(LMISTestApp.getContext().getString(R.string.unauthorized_operation),
        appDisplayError);
  }

  @Test
  public void shouldReturnAppErrorWhenServerReturnsInternalError() {
    String appDisplayError = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage(
        "Sync failed due to technical problem on the server. Please contact system administrator.");
    assertEquals(LMISTestApp.getContext().getString(R.string.sync_server_error), appDisplayError);
  }

  @Test
  public void shouldReturnAppDataErrorWhenServerReturnsOtherError() {
    String appDisplayError = SyncErrorsMap
        .getDisplayErrorMessageBySyncErrorMessage("Something else");
    assertEquals(LMISTestApp.getContext().getString(R.string.sync_server_error), appDisplayError);
  }

  @Test
  public void shouldReturnAppRnrPeriodDuplicateErrorWhenServerReturnsPeriodDuplicateError() {
    String appDisplayError = SyncErrorsMap
        .getDisplayErrorMessageBySyncErrorMessage("RnR for this period has been submitted");
    assertEquals(LMISTestApp.getContext().getString(R.string.error_rnr_period_duplicate),
        appDisplayError);
  }

  @Test
  public void shouldReturnAppRnrPeriodInvalidErrorWhenServerReturnsPeriodInvalidError() {
    String appDisplayError = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage(
        "Submitted period is not next period in schedule");
    assertEquals(LMISTestApp.getContext().getString(R.string.error_rnr_period_invalid),
        appDisplayError);
  }

  @Test
  public void shouldReturnAppRnrFieldMandatoryNegativeErrorWhenServerReturnsRnrFieldMandatoryNegativeError() {
    String appDisplayError = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage(
        "product's field is negative or null, please validate movements");
    assertEquals(LMISTestApp.getContext().getString(R.string.error_rnr_field_mandatory_negative),
        appDisplayError);
  }

  @Test
  public void shouldReturnAppRnrValidationEquationNotEqualErrorWhenServerReturnsRnrValidationEquationNotEqualError() {
    String appDisplayError = SyncErrorsMap
        .getDisplayErrorMessageBySyncErrorMessage("product quantity is not match");
    assertEquals(
        LMISTestApp.getContext().getString(R.string.error_rnr_validation_equation_not_equal),
        appDisplayError);
  }

  @Test
  public void shouldReturnAppRnrReportStartDateInvalidErrorWhenServerReturnsRnrReportStartDateInvalidError() {
    String appDisplayError = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage(
        "The report submit date must be later than the facility's reports start date");
    assertEquals(LMISTestApp.getContext().getString(R.string.error_rnr_report_start_date_invalid),
        appDisplayError);
  }

  @Test
  public void shouldReturnOrderNumberNotExist() {
    String appDisplayError = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage(
        "Sync failed. This order number does not exist. Please confirm whether this order number is correct.");
    assertEquals(LMISTestApp.getContext().getString(R.string.error_pod_order_number_not_exist),
        appDisplayError);
  }

}