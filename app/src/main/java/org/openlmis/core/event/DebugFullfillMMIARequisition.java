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

package org.openlmis.core.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Data
public class DebugFullfillMMIARequisition {
  int issuesAmount;
  int adjustmentAmount;
  int inventoryAmount;
  int patientAmount;
  int communityPharmacyAmount;
  int patientTypeAmount;
  int patientAgeAmount;
  int prophylaxyAmount;
  int totalPatientAmount;
  int totalMonthAmount;

  public DebugFullfillMMIARequisition() {
    this.setAllAmountDefault();
  }

  public void setAllAmountDefault() {
    this.issuesAmount = 1;
    this.adjustmentAmount = 1;
    this.inventoryAmount = 1;
    this.patientAmount = 1;
    this.communityPharmacyAmount = 1;
    this.patientTypeAmount = 1;
    this.patientAgeAmount = 1;
    this.prophylaxyAmount = 1;
    this.totalPatientAmount = 1;
    this.totalMonthAmount = 1;
  }
}
