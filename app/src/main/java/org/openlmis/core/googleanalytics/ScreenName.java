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

package org.openlmis.core.googleanalytics;

public enum ScreenName {
  HOME_SCREEN("Home Screen"),
  INVENTORY_SCREEN("Inventory Screen"),
  KIT_STOCK_CARD_OVERVIEW_SCREEN("Kit StockCard Overview Screen"),
  LOGIN_SCREEN("Login Screen"),
  RAPID_TEST_SCREEN("Rapid Test Screen"),
  MMIA_REQUISITION_SCREEN("MMIA Requisition Screen"),
  AL_REQUISITION_SCREEN("AL Requisition Screen"),
  PTV_REQUISITION_SCREEN("PTV Requisition Screen"),
  VIA_REQUISITION_SCREEN("VIA Requisition Screen"),
  ISSUE_VOUCHER_REPORT_SCREEN("Issue voucher Report Screen"),
  RN_R_FORM_HISTORY_SCREEN("RnR Form History Screen"),
  STOCK_CARD_OVERVIEW_SCREEN("StockCard Overview Screen"),
  STOCK_CARD_MOVEMENT_SCREEN("StockCard Movement Screen"),
  STOCK_CARD_NEW_MOVEMENT_SCREEN("StockCard New Movement Screen"),
  STOCK_CARD_MOVEMENT_HISTORY_SCREEN("StockCard Movement History Screen"),
  ARCHIVED_DRUGS_LIST_SCREEN("Archived Drugs List Screen"),
  SELECT_PERIOD_SCREEN("Select Period Screen"),
  SELECT_UNPACK_KIT_NUMBER_SCREEN("Select Unpack Kit Number Screen"),
  UNPACK_KIT_SCREEN("Unpack Kit Screen"),
  SELECT_REGIME_PRODUCT_SCREEN("Select Regime Products Screen"),
  SELECT_EMERGENCY_PRODUCTS_SCREEN("Select Emergency Products Screen"),
  ADD_DRUGS_TO_VIA_SCREEN("Add Drugs to VIA Classica Screen"),
  RAPID_TEST_REPORT_FORM_SCREEN("Rapid Test Report Form"),
  MALARIA_DATA_REPORT_FORM_SCREEN("Patient Data Report"),
  ALL_DRUGS_MOVEMENT_HISTORY_SCREEN("All Drugs Movement History Screen"),
  ISSUE_VOUCHER_AND_POD("Issue Voucher and PoD List Screen"),
  BULK_ENTRIES_SCREEN("Bulk Entries Screen"),
  ADD_PRODUCT_TO_BULK_ENTRIES_SCREEN("Add Products To Bulk Entries Screen"),
  REQUISITION_SCREEN("Requisition Screen"),
  BULK_ISSUE_CHOOSE_DESTINATION("Bulk Issue Choose Destination Screen"),
  BULK_ISSUE("Bulk Issue Screen"),
  STOCK_MOVEMENT_DETAIL_HISTORY_SCREEN("Stock Movement Detail History Screen"),
  ISSUE_VOUCHER_INPUT_ORDER_NUMBER_SCREEN("Issue Voucher Input Order number Screen"),
  ISSUE_VOUCHER_DRAFT_SCREEN("Issue Voucher Draft Screen"),
  ISSUE_VOUCHER_REMOT_SCREEN("Issue Voucher Draft Screen"),
  EDIT_ORDER_NUMBER_SCREEN("Edit Order Number Screen"),;

  private final String name;

  ScreenName(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
