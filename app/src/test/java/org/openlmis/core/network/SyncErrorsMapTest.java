package org.openlmis.core.network;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;

import static org.junit.Assert.*;

@RunWith(LMISTestRunner.class)
public class SyncErrorsMapTest {

    @Test
    public void shouldReturnAppErrorWhenServerReturnsDuplicateError() {
        String appDisplayError = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage("RnR for this period has been submitted");
        assertEquals(LMISTestApp.getContext().getString(R.string.duplicate_rnr_error), appDisplayError);
    }

    @Test
    public void shouldReturnAppErrorWhenServerReturnsProgramConfigError() {
        String appDisplayError = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage("Program configuration missing");
        assertEquals(LMISTestApp.getContext().getString(R.string.period_configuration_missing), appDisplayError);
    }

    @Test
    public void shouldReturnAppErrorWhenServerReturnsInvalidProductError() {
        String appDisplayError = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage("Invalid product codes [01A01]");
        assertEquals("Sync failed due attempted submission of invalid product in form [01A01]. Please contact system administrator.", appDisplayError);
    }

    @Test
    public void shouldReturnAppErrorWhenServerReturnsPreviousNotFilledError() {
        String appDisplayError = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage("Please finish all R&R of previous period(s)");
        assertEquals(LMISTestApp.getContext().getString(R.string.rnr_previous_not_filled), appDisplayError);
    }

    @Test
    public void shouldReturnAppErrorWhenServerReturnsUserUnAuthorizedError() {
        String appDisplayError = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage("User does not have permission");
        assertEquals(LMISTestApp.getContext().getString(R.string.unauthorized_operation), appDisplayError);
    }

    @Test
    public void shouldReturnAppErrorWhenServerReturnsInternalError() {
        String appDisplayError = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage("Sync failed due to technical problem on the server. Please contact system administrator.");
        assertEquals(LMISTestApp.getContext().getString(R.string.sync_server_error), appDisplayError);
    }

    @Test
    public void shouldReturnAppDataErrorWhenServerReturnsOtherError() {
        String appDisplayError = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage("Something else");
        assertEquals("Something else", appDisplayError);
    }

    @Test
    public void shouldReturnAppPeriodErrorWhenServerReturnsPeriodError() {
        String appDisplayError = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage("Submitted period is not next period in schedule.");
        assertEquals(LMISTestApp.getContext().getString(R.string.period_mismatch_error), appDisplayError);
    }

}