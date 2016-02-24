package org.openlmis.core.network;

import android.content.Context;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;

public final class SyncErrorsMap {

    private static final String PROGRAM_CONFIG_ERROR = "Program configuration missing";
    private static final String INVALID_PRODUCT_CODES = "Invalid product codes";
    private static final String PREVIOUS_FORM_NOT_FILLED = "Please finish all R&R of previous period(s)";
    private static final String USER_UNAUTHORIZED = "User does not have permission";
    private static final String DUPLICATE_RNR = "RnR for this period has been submitted";

    private SyncErrorsMap() {

    }

    public static String getDisplayErrorMessageBySyncErrorMessage(String errorMessage){
        if (errorMessage == null){
            return null;
        }
        Context context = LMISApp.getContext();
        if (errorMessage.contains(PROGRAM_CONFIG_ERROR)) {
            return context.getString(R.string.period_configuration_missing);
        }
        if (errorMessage.contains(INVALID_PRODUCT_CODES)){
            String[] errorString = errorMessage.split(" ");
            return context.getString(R.string.product_code_invalid, errorString[errorString.length - 1]);
        }
        if (errorMessage.contains(PREVIOUS_FORM_NOT_FILLED)){
            return context.getString(R.string.rnr_previous_not_filled);
        }
        if (errorMessage.contains(USER_UNAUTHORIZED)){
            return context.getString(R.string.unauthorized_operation);
        }
        if (errorMessage.contains(context.getString(R.string.sync_server_error))){
            return context.getString(R.string.sync_server_error);
        }
        if (errorMessage.contains(DUPLICATE_RNR)) {
            return context.getString(R.string.duplicate_rnr_error);
        }
        return context.getString(R.string.default_sync_data_error);
    }
}
