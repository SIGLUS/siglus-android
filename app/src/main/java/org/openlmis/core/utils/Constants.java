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

package org.openlmis.core.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.roboguice.shaded.goole.common.collect.ImmutableList;

public final class Constants {

  public enum Program {
    MMIA_PROGRAM(MMIA_PROGRAM_CODE, R.string.mmia_list, MMIA_REPORT),
    VIA_PROGRAM(VIA_PROGRAM_CODE, R.string.requisition_list, VIA_REPORT),
    AL_PROGRAM(AL_PROGRAM_CODE, R.string.label_al_name, AL_REPORT),
    RAPID_TEST_PROGRAM(RAPID_TEST_PROGRAM_CODE, R.string.title_rapid_test_reports, RAPID_REPORT),
    MMTB_PROGRAM(MMTB_PROGRAM_CODE, R.string.title_mmtb_reports, MMTB_REPORT);


    private final String code;
    private final String reportType;
    private final int title;

    Program(String code, int title, String reportType) {
      this.code = code;
      this.title = title;
      this.reportType = reportType;
    }

    public String getCode() {
      return code;
    }

    public String getReportType() {
      return reportType;
    }

    public int getTitle() {
      return title;
    }
  }

  // Don't change these program codes!!!
  public static final String MMIA_PROGRAM_CODE = "T";
  public static final String VIA_PROGRAM_CODE = "VC";
  public static final String AL_PROGRAM_CODE = "ML";
  public static final String RAPID_TEST_PROGRAM_CODE = "TR";
  public static final String MMTB_PROGRAM_CODE = "TB";
  public static final List<Constants.Program> PROGRAMS = Collections
      .unmodifiableList(Arrays.asList(Constants.Program.VIA_PROGRAM,
          Constants.Program.MMIA_PROGRAM,
          Constants.Program.AL_PROGRAM,
          Constants.Program.RAPID_TEST_PROGRAM,
          Constants.Program.RAPID_TEST_PROGRAM));

  //Don't change these reportTypes codes!!!
  public static final String MMIA_REPORT = "MMIA";
  public static final String VIA_REPORT = "VIA";
  public static final String RAPID_REPORT = "TEST_KIT";
  public static final String AL_REPORT = "MALARIA";
  public static final String MMTB_REPORT = "MMTB";

  public static final String EXISTENT_STOCK = "existentStock";
  public static final String TREATMENTS_ATTENDED = "treatmentsAttended";

  public static final Map<String, String> REGIMEN_CODE_TO_ADDITIONAL_PRODUCT;

  static {
    Map<String, String> map = new HashMap<>();
    map.put("1x6", "08O05");
    map.put("2x6", "08O05Z");
    map.put("3x6", "08O05Y");
    map.put("4x6", "08O05X");
    REGIMEN_CODE_TO_ADDITIONAL_PRODUCT = Collections.unmodifiableMap(map);
  }

  public static final Map<String, String> REGIMEN_INFORMATION_TO_REGIMEN_CODE;

  static {
    Map<String, String> map = new HashMap<>();
    map.put("existentStock_08O05", "AL STOCK Malaria 1x6");
    map.put("existentStock_08O05Z", "AL STOCK Malaria 2x6");
    map.put("existentStock_08O05Y", "AL STOCK Malaria 3x6");
    map.put("existentStock_08O05X", "AL STOCK Malaria 4x6");
    map.put("treatmentsAttended_08O05", "AL US/APE Malaria 1x6");
    map.put("treatmentsAttended_08O05Z", "AL US/APE Malaria 2x6");
    map.put("treatmentsAttended_08O05Y", "AL US/APE Malaria 3x6");
    map.put("treatmentsAttended_08O05X", "AL US/APE Malaria 4x6");
    REGIMEN_INFORMATION_TO_REGIMEN_CODE = Collections.unmodifiableMap(map);
  }


  // Intent Params
  public static final String PARAM_STOCK_CARD_ID = "stockCardId";
  public static final String PARAM_STOCK_CARD_ID_ARRAY = "stockCardIdArray";
  public static final String PARAM_STOCK_NAME = "stockName";
  public static final String PARAM_IS_ACTIVATED = "productIsActivated";
  public static final String PARAM_IS_PHYSICAL_INVENTORY = "isPhysicalInventory";
  public static final String PARAM_IS_ADD_NEW_DRUG = "isAddNewDrug";
  public static final String PARAM_KIT_CODE = "kitCode";
  public static final String PARAM_KIT_NUM = "kitNum";
  public static final String PARAM_KIT_NAME = "kitName";
  public static final String PARAM_PROGRAM_CODE = "programCode";
  public static final String PARAM_FORM_ID = "formId";
  public static final String PARAM_PREVIOUS_FORM = "previousForm";
  public static final String PARAM_IS_FROM_ARCHIVE = "isFromArchive";
  public static final String PARAM_IS_KIT = "isKit";
  public static final String PARAM_SELECTED_INVENTORY_DATE = "selectedInventoryDate";
  public static final String PARAM_IS_MISSED_PERIOD = "isMissedPeriod";
  public static final String PARAM_PERIOD_END_MONTH = "period_end_month";
  public static final String PARAM_PERIOD = "period";
  public static final String PARAM_USERNAME = "username";
  public static final String PARAM_PASSWORD = "password";
  public static final String PARAM_CUSTOM_REGIMEN = "customRegimen";
  public static final String PARAM_SELECTED_EMERGENCY = "selected_emergency";
  public static final String PARAM_PERIOD_BEGIN = "periodBegin";
  public static final String PARAM_ADDED_DRUGS_TO_VIA = "addedDrugsToVIA";
  public static final String PARAM_ADDED_DRUG_CODES_IN_VIA = "addedDrugsInVIA";
  public static final String PARAM_MOVEMENT_TYPE = "movementType";
  public static final String PARAM_MSG_CONFIRM_GENERATE_LOT_NUMBER
      = "confirmGenerateLotNumberMessage";
  public static final String PARAM_ISSUE_VOUCHER_FORM_ID  = "issueVoucherFormId";
  public static final String PARAM_ISSUE_VOUCHER_OR_POD  = "issueVoucherOrPod";
  public static final String PARAM_POD = "pod";
  public static final String PARAM_ISSUE_VOUCHER = "issueVoucher";
  public static final String PARAM_IS_ELECTRONIC_ISSUE_VOUCHER = "isElectronicIssueVoucher";

  // Broadcast Intent Filter
  public static final String INTENT_FILTER_DELETED_PRODUCT =
      LMISApp.getContext().getPackageName() + ".deleted.product";
  // sync within AndroidManifest.xml
  public static final String INTENT_FILTER_ONE_MONTHLY_TASK =
      LMISApp.getContext().getPackageName() + ".one.monthly.task";

  // error define
  public static final String SYNC_MOVEMENT_ERROR = "sync_movement_error";

  // PTV
  public static final String PTV_PRODUCT_FIRST_CODE = "08S40";
  public static final String PTV_PRODUCT_SECOND_CODE = "08S15";
  public static final String PTV_PRODUCT_THIRD_CODE = "08S22";
  public static final String PTV_PRODUCT_FOURTH_CODE = "08S17";
  public static final String PTV_PRODUCT_FIFTH_CODE = "08S23";

  public static final long DEFAULT_FORM_ID = 0;

  public static final String ENTRIES = "Entries";
  public static final String LOSSES_AND_ADJUSTMENTS = "Losses and Adjustments";
  public static final String REQUISITIONS = "Requisitions";
  public static final String FINAL_STOCK = "Final Stock";
  public static final String TOTAL = "Total";
  public static final List<String> KIT_PRODUCTS = ImmutableList.of("26A01", "26A02", "26B01", "26B02");

  public static final String IS_USER_TRIGGERED_SYCED = "isUserTriggered";

  public static final String ATTR_TABLE_DISPENSED_DS5 = "dispensed_ds5";
  public static final String ATTR_TABLE_DISPENSED_DS4 = "dispensed_ds4";
  public static final String ATTR_TABLE_DISPENSED_DS3 = "dispensed_ds3";
  public static final String ATTR_TABLE_DISPENSED_DS2 = "dispensed_ds2";
  public static final String ATTR_TABLE_DISPENSED_DS1 = "dispensed_ds1";
  public static final String ATTR_TABLE_DISPENSED_DS = "dispensed_ds";
  public static final String ATTR_TABLE_DISPENSED_DT2 = "dispensed_dt2";
  public static final String ATTR_TABLE_DISPENSED_DT1 = "dispensed_dt1";
  public static final String ATTR_TABLE_DISPENSED_DT = "dispensed_dt";
  public static final String ATTR_TABLE_DISPENSED_DM = "dispensed_dm";

  // auth parameter
  public static final String GRANT_TYPE = "password";

  // basic auth
  public static final String BASIC_AUTH = "Basic dXNlci1jbGllbnQ6Y2hhbmdlbWU=";
  public static final String AUTHORIZATION = "Authorization";
  public static final String USER_NAME = "UserName";
  public static final String FACILITY_CODE = "FacilityCode";
  public static final String FACILITY_NAME = "FacilityName";
  public static final String UNIQUE_ID = "UniqueId";
  public static final String DEVICE_INFO = "DeviceInfo";
  public static final String VERSION_CODE = "VersionCode";
  public static final String ANDROID_SDK_VERSION = "AndroidSDKVersion";

  // event
  public static final String REFRESH_BACKGROUND_EVENT = "refresh background event";
  public static final String REFRESH_ISSUE_VOUCHER_LIST = "refresh issue voucher list";

  // from page
  public static final String FROM_BULK_INITIAL_PAGE = "from bulk initial page";
  public static final String FROM_BULK_ENTRIES_PAGE = "from bulk entries page";

  public static final int STOCK_CARD_MAX_SYNC_MONTH = 12;

  public static final String  DEFAULT_REASON_FOR_NO_AMOUNT_LOT = "INTERMEDIATE_WAREHOUSE";

  public static final String SIGLUS_API_ERROR_NOT_ANDROID = "siglusapi.error.notAndroidUser";

  public static final String SIGLUS_API_ERROR_NOT_REGISTERED_DEVICE = "siglusapi.error.notRegisteredDevice";

  public static final String LOGIN_ACTIVITY = "LoginActivity";

  public static final String VIRTUAL_LOT_NUMBER = "virtual lot number";

  public static final String SIGLUS_API_ORDER_NUMBER_NOT_EXIST = "This order number does not exist";

  public static final String SIGLUS_API_ORDER_NUMBER_NOT_EXIST_IN_PORTUGUESE = "Este número de pedido não existe";

  public static final String SERVER_FAILED_MESSAGE_IN_ENGLISH =
      "Sync failed due to technical problem on the server. Please contact system administrator.";

  public static final String SERVER_FAILED_MESSAGE_IN_PORTUGUESE =
      "Sincronização falhou devido à problemas técnicos no servidor. Entre em contacto com administrador do sistema.";

  private Constants() {

  }
}
