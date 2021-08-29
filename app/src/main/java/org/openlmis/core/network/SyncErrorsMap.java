/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.network;

import android.content.Context;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;

public final class SyncErrorsMap {

  public static final String ERROR_POD_ORDER_DOSE_NOT_EXIST = "This order number does not exist";
  private static final String PROGRAM_CONFIG_ERROR = "Program configuration missing";
  private static final String INVALID_PRODUCT_CODES = "Invalid product codes";
  private static final String PREVIOUS_FORM_NOT_FILLED =
      "Please finish all R&R of previous period(s)";
  private static final String USER_UNAUTHORIZED = "User does not have permission";
  private static final String ERROR_RNR_PERIOD_DUPLICATE = "RnR for this period has been submitted";
  private static final String ERROR_RNR_PERIOD_INVALID =
      "Submitted period is not next period in schedule";
  private static final String ERROR_RNR_FIELD_MANDATORY_NEGATIVE =
      "product's field is negative or null, please validate movements";
  private static final String ERROR_RNR_VALIDATION_EQUATION_NOT_EQUAL =
      "product quantity is not match";
  private static final String ERROR_RNR_REPORT_START_DATE_INVALID =
      "The report submit date must be later than the facility's reports start date";

  private SyncErrorsMap() {

  }

  public static String getDisplayErrorMessageBySyncErrorMessage(String errorMessage) {
    if (errorMessage == null) {
      return null;
    }
    Context context = LMISApp.getContext();
    if (errorMessage.equals(context.getString(R.string.hint_network_error))) {
      return null;
    }
    if (errorMessage.contains(PROGRAM_CONFIG_ERROR)) {
      return context.getString(R.string.period_configuration_missing);
    }
    if (errorMessage.contains(INVALID_PRODUCT_CODES)) {
      String[] errorString = errorMessage.split(" ");
      return context.getString(R.string.product_code_invalid, errorString[errorString.length - 1]);
    }
    if (errorMessage.contains(PREVIOUS_FORM_NOT_FILLED)) {
      return context.getString(R.string.rnr_previous_not_filled);
    }
    if (errorMessage.contains(USER_UNAUTHORIZED)) {
      return context.getString(R.string.unauthorized_operation);
    }
    if (errorMessage.contains(context.getString(R.string.sync_server_error))) {
      return context.getString(R.string.sync_server_error);
    }

    if (errorMessage.contains(ERROR_RNR_PERIOD_DUPLICATE)) {
      return context.getString(R.string.error_rnr_period_duplicate);
    }
    if (errorMessage.contains(ERROR_RNR_PERIOD_INVALID)) {
      return context.getString(R.string.error_rnr_period_invalid);
    }
    if (errorMessage.contains(ERROR_RNR_FIELD_MANDATORY_NEGATIVE)
        || errorMessage.contains(ERROR_RNR_VALIDATION_EQUATION_NOT_EQUAL)
        || errorMessage.contains(ERROR_RNR_REPORT_START_DATE_INVALID)) {
      return context.getString(R.string.error_rnr_field_mandatory_negative);
    }
    if (errorMessage.contains(ERROR_POD_ORDER_DOSE_NOT_EXIST)) {
      return context.getString(R.string.error_pod_order_number_not_exist);
    }
    return context.getString(R.string.sync_server_error);
  }
}
