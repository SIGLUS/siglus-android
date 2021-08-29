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

package org.openlmis.core.view.viewmodel;

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.network.SyncErrorsMap;
import org.openlmis.core.utils.Constants.Program;
import org.openlmis.core.utils.DateUtil;

@Data
@Builder
public class IssueVoucherListViewModel implements Comparable<IssueVoucherListViewModel> {

  private static final ArrayList<String> PROGRAM_INDEX = new ArrayList<>();

  static {
    for (Program value : Program.values()) {
      PROGRAM_INDEX.add(value.getCode());
    }
  }

  private Pod pod;

  private String programName;

  private SyncError syncError;

  public String getOrderNumber() {
    return pod.isRequisitionIsEmergency() ? "EMERGENCY " + pod.getOrderCode() : pod.getOrderCode();
  }

  public String getOrderSupplyFacilityName() {
    return pod.getOrderSupplyFacilityName();
  }

  public String getReportingPeriod() {
    Date requisitionActualStartDate = pod.getRequisitionActualStartDate();
    Date requisitionActualEndDate = pod.getRequisitionActualEndDate();
    if (requisitionActualStartDate == null || requisitionActualEndDate == null) {
      return "";
    }
    String startDate = DateUtil.formatDate(requisitionActualStartDate, DateUtil.SIMPLE_DATE_FORMAT);
    String endDate = DateUtil.formatDate(requisitionActualEndDate, DateUtil.SIMPLE_DATE_FORMAT);
    return startDate + " - " + endDate;
  }

  public String getShippedDate() {
    Date shippedDate = pod.getShippedDate();
    if (shippedDate == null) {
      return "";
    }
    return DateUtil.formatDate(shippedDate, DateUtil.SIMPLE_DATE_FORMAT);
  }

  public boolean isIssueVoucher() {
    return OrderStatus.SHIPPED == pod.getOrderStatus();
  }

  public boolean shouldShowOperationIcon() {
    return pod.isLocal() && (isIssueVoucher()
        || (!isIssueVoucher() && StringUtils.contains(getErrorMsg(), SyncErrorsMap.ERROR_POD_ORDER_DOSE_NOT_EXIST)));
  }

  @Nullable
  public String getErrorMsg() {
    if (!pod.isSynced() && syncError == null) {
      return LMISApp.getContext().getResources().getString(R.string.error_pod_not_sync);
    } else if (syncError != null) {
      return SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage(syncError.getErrorMessage());
    } else {
      return null;
    }
  }

  @Override
  public int compareTo(IssueVoucherListViewModel another) {
    boolean currentIsLocalAndUnSync = pod.isLocal() && !pod.isSynced();
    boolean anotherIsLocalAndUnSync = another.getPod().isLocal() && !another.getPod().isSynced();
    if (currentIsLocalAndUnSync && anotherIsLocalAndUnSync) {
      return sortByProgram(another);
    } else if (currentIsLocalAndUnSync) {
      return -1;
    } else if (anotherIsLocalAndUnSync) {
      return 1;
    } else {
      long shippedTime = pod.getShippedDate().getTime();
      long anotherShippedTime = another.getPod().getShippedDate().getTime();
      return Long.compare(anotherShippedTime, shippedTime);
    }
  }

  private int sortByProgram(IssueVoucherListViewModel another) {
    int programIndex = PROGRAM_INDEX.indexOf(pod.getRequisitionProgramCode());
    int anotherProgramIndex = PROGRAM_INDEX.indexOf(another.getPod().getRequisitionProgramCode());
    return Integer.compare(anotherProgramIndex, programIndex);
  }
}
