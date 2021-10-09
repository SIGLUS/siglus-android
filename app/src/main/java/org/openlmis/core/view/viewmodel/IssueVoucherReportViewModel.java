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
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Program;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
public class IssueVoucherReportViewModel {

  private Pod pod;
  private Program program;
  List<MultiItemEntity> viewModels;

  public IssueVoucherReportViewModel(Pod pod) {
       updateProductViewModels(pod);
  }

  public List<IssueVoucherReportProductViewModel> getProductViewModels() {
    List<MultiItemEntity> productViewModels = new ArrayList<>();
    productViewModels.addAll(viewModels);
    productViewModels.remove(viewModels.size() - 1);
    return FluentIterable.from(productViewModels)
        .transform(productViewModel -> (IssueVoucherReportProductViewModel) productViewModel).toList();
  }

  public void updateProductViewModels(Pod pod) {
    this.pod = pod;
    if (viewModels == null) {
      viewModels = new ArrayList<>();
    }
    viewModels.clear();
    viewModels.addAll(FluentIterable.from(pod.getPodProductItemsWrapper())
        .transform(podProductItem ->
            new IssueVoucherReportProductViewModel(podProductItem, pod.getOrderStatus(), pod.isLocal(), pod.isDraft()))
        .toList());
    // TODO total
    viewModels.add(new IssueVoucherReportSummaryViewModel(pod, BigDecimal.valueOf(10)));
  }

  public OrderStatus getPodStatus() {
    return pod.getOrderStatus();
  }

  public boolean getIsLocal() {
    return pod.isLocal();
  }


}
