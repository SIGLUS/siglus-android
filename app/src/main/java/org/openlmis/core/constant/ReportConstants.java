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

package org.openlmis.core.constant;

public class ReportConstants {

  private ReportConstants() {
  }

  // treatment table
  public static final String KEY_TREATMENT_ADULT_TABLE = "table_treatment_adult_key";
  public static final String KEY_TREATMENT_ADULT_SENSITIVE_INTENSIVE = "table_treatment_adult_key_sensitive_intensive";
  public static final String KEY_TREATMENT_ADULT_SENSITIVE_MAINTENANCE =
      "table_treatment_adult_key_sensitive_maintenance";
  public static final String KEY_TREATMENT_ADULT_MR_INDUCTION = "table_treatment_adult_key_mr_induction";
  public static final String KEY_TREATMENT_ADULT_MR_INTENSIVE = "table_treatment_adult_key_mr_intensive";
  public static final String KEY_TREATMENT_ADULT_MR_MAINTENANCE = "table_treatment_adult_key_mr_maintenance";
  public static final String KEY_TREATMENT_ADULT_XR_INDUCTION = "table_treatment_adult_key_xr_induction";
  public static final String KEY_TREATMENT_ADULT_XR_MAINTENANCE = "table_treatment_adult_key_xr_maintenance";

  public static final String KEY_TREATMENT_PEDIATRIC_TABLE = "table_treatment_pediatric_key";
  public static final String KEY_TREATMENT_PEDIATRIC_SENSITIVE_INTENSIVE =
      "table_treatment_pediatric_key_sensitive_intensive";
  public static final String KEY_TREATMENT_PEDIATRIC_SENSITIVE_MAINTENANCE =
      "table_treatment_pediatric_key_sensitive_maintenance";
  public static final String KEY_TREATMENT_PEDIATRIC_MR_INDUCTION = "table_treatment_pediatric_key_mr_induction";
  public static final String KEY_TREATMENT_PEDIATRIC_MR_INTENSIVE = "table_treatment_pediatric_key_mr_intensive";
  public static final String KEY_TREATMENT_PEDIATRIC_MR_MAINTENANCE = "table_treatment_pediatric_key_mr_maintenance";
  public static final String KEY_TREATMENT_PEDIATRIC_XR_INDUCTION = "table_treatment_pediatric_key_xr_intensive";
  public static final String KEY_TREATMENT_PEDIATRIC_XR_MAINTENANCE = "table_treatment_pediatric_key_xr_maintenance";

  // pharmacy product table
  public static final String KEY_PHARMACY_PRODUCT_TABLE = "table_pharmacy_product_key";
  public static final String KEY_PHARMACY_PRODUCT_ISONIAZIDA_100 = "table_pharmacy_product_key_isoniazida_100_mg";
  public static final String KEY_PHARMACY_PRODUCT_ISONIAZIDA_300 = "table_pharmacy_product_key_isoniazida_300_mg";
  public static final String KEY_PHARMACY_PRODUCT_LEVOFLOXACINA_100 = "table_pharmacy_product_key_levofloxacina_100_mg";
  public static final String KEY_PHARMACY_PRODUCT_LEVOFLOXACINA_250 = "table_pharmacy_product_key_levofloxacina_250_mg";
  public static final String KEY_PHARMACY_PRODUCT_RIFAPENTINA_300 =
      "table_pharmacy_product_key_rifapentina_300_mg_isoniazida_300_mg";
  public static final String KEY_PHARMACY_PRODUCT_RIFAPENTINA_150 = "table_pharmacy_product_key_rifapentina_150_mg";
  public static final String KEY_PHARMACY_PRODUCT_PIRIDOXINA_25 = "table_pharmacy_product_key_piridoxina_25_mg";
  public static final String KEY_PHARMACY_PRODUCT_PIRIDOXINA_50 = "table_pharmacy_product_key_piridoxina_50_mg";

  // mmtb age group table
  public static final String KEY_SERVICE_ADULT = "table_age_group_service_key_adult";
  public static final String KEY_SERVICE_LESS_THAN_25 = "table_age_group_service_key_child_less_than_25kg";
  public static final String KEY_SERVICE_MORE_THAN_25 = "table_age_group_service_key_child_more_than_25kg";
  public static final String KEY_AGE_GROUP_TREATMENT = "table_age_group_header_key_treatment";
  public static final String KEY_AGE_GROUP_PROPHYLAXIS = "table_age_group_header_key_prophylaxis";

  // mmtb new patient table
  public static final String KEY_NEW_PATIENT_TABLE = "table_new_patients_key";
  public static final String KEY_NEW_ADULT_SENSITIVE = "table_new_patients_key_new_adult_sensitive";
  public static final String KEY_NEW_ADULT_MR = "table_new_patients_key_new_adult_mr";
  public static final String KEY_NEW_ADULT_XR = "table_new_patients_key_new_adult_xr";
  public static final String KEY_NEW_CHILD_SENSITIVE = "table_new_patients_key_new_child_sensitive";
  public static final String KEY_NEW_CHILD_MR = "table_new_patients_key_new_child_mr";
  public static final String KEY_NEW_CHILD_XR = "table_new_patients_key_new_child_xr";
  public static final String KEY_NEW_PATIENT_TOTAL = "table_new_patients_key_total";

  // mmtb prophylaxis table
  public static final String KEY_PROPHYLAXIS_TABLE = "table_prophylaxis_key";
  public static final String KEY_START_PHASE = "table_prophylaxis_key_initial";
  public static final String KEY_CONTINUE_PHASE = "table_prophylaxis_key_continuous_maintenance";
  public static final String KEY_FINAL_PHASE = "table_prophylaxis_key_final_last_dismissal";
  public static final String KEY_PROPHYLAXIS_TABLE_TOTAL = "table_prophylaxis_key_total";

  // mmtb type of dispensation of prophylactics table
  public static final String KEY_TYPE_OF_DISPENSATION_TABLE = "table_prophylactics_key";
  public static final String KEY_FREQUENCY_MONTHLY = "table_prophylactics_key_monthly";
  public static final String KEY_FREQUENCY_QUARTERLY = "table_prophylactics_key_trimenstral";
  public static final String KEY_FREQUENCY_TOTAL = "table_prophylactics_key_total";

}
