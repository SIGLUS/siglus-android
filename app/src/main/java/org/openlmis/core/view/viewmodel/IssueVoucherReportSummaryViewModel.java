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

import com.chad.library.adapter.base.entity.MultiItemEntity;
import java.math.BigDecimal;
import lombok.Data;
import org.openlmis.core.enumeration.IssueVoucherItemType;
import org.openlmis.core.model.Pod;

@Data
public class IssueVoucherReportSummaryViewModel implements MultiItemEntity {

  private Pod pod;
  private BigDecimal total;

  public IssueVoucherReportSummaryViewModel(Pod pod, BigDecimal total) {
    this.pod = pod;
    this.total = total;
  }

  @Override
  public int getItemType() {
    return IssueVoucherItemType.ISSUE_VOUCHER_PRODUCT_TOTAL.getValue();
  }
}
