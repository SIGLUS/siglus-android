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

package org.openlmis.core.googleAnalytics;

public enum TrackerActions {
  SELECT_STOCK_CARD("Select Stock Card"),
  SELECT_REASON("Select Reason"),
  SELECT_MOVEMENT_DATE("Select Movement Date"),
  SELECT_COMPLETE("Select Complete"),
  SELECT_APPROVE("Select Approve"),
  SELECT_MMIA("Select MMIA"),
  SELECT_VIA("Select VIA"),
  SELECT_PTV("Select PTV"),
  SELECT_AL("Select AL"),
  CREATE_RNR("Create RnR Form"),
  SELECT_PERIOD("Select Period"),
  SUBMIT_RNR("First Time Approve"),
  AUTHORISE_RNR("Second Time Approve"),
  SELECT_INVENTORY("Select Inventory"),
  COMPLETE_INVENTORY("Complete Inventory"),
  APPROVE_INVENTORY("Approve Inventory"),
  NETWORK_CONNECTED("Network Connected"),
  NETWORK_DISCONNECTED("Network Disconnected"),
  SWITCH_POWER_ON("Tablet Power On"),
  SWITCH_POWER_OFF("Tablet Power Off");

  private final String trackerAction;

  TrackerActions(String trackerAction) {
    this.trackerAction = trackerAction;
  }

  public String getString() {
    return this.trackerAction;
  }
}
