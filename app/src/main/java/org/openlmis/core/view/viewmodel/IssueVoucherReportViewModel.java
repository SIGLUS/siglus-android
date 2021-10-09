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

  public int getListSize() {
    return viewModels.size();
  }

  public void updateProductViewModels(Pod pod) {
    this.pod = pod;
    List<IssueVoucherReportProductViewModel> productViewModels = FluentIterable.from(pod.getPodProductItemsWrapper())
        .transform(podProductItem ->
            new IssueVoucherReportProductViewModel(podProductItem, pod.getOrderStatus(), pod.isLocal(), pod.isDraft()))
        .toList();
    updateViewModels(productViewModels);
  }

  private BigDecimal calculateTotalValue(List<IssueVoucherReportProductViewModel> productViewModels) {
    BigDecimal totalValue = new BigDecimal("0.00");
    for (IssueVoucherReportProductViewModel productViewModel : productViewModels) {
      for (IssueVoucherReportLotViewModel lotViewModel : productViewModel.getLotViewModelList()) {
        BigDecimal value = lotViewModel.getTotalValue();
        if (value != null) {
          totalValue = totalValue.add(value);
        }
      }
    }
    return totalValue;
  }

  public void removeProductAtPosition(int position) {
    List<IssueVoucherReportProductViewModel> productViewModels = new ArrayList<>(getProductViewModels());
    productViewModels.remove(position);
    updateViewModels(productViewModels);
  }

  public boolean isNeedRemoveProduct(int productPosition) {
    IssueVoucherReportProductViewModel productViewModel = (IssueVoucherReportProductViewModel) viewModels
        .get(productPosition);
    return productViewModel.getLotViewModelList().size() == 1;
  }

  public void removeLotAtPosition(int productPosition, int lotPosition) {
    List<IssueVoucherReportProductViewModel> productViewModels = getProductViewModels();
    IssueVoucherReportProductViewModel productViewModel = productViewModels.get(productPosition);
    List<IssueVoucherReportLotViewModel> existedLots = new ArrayList<>(productViewModel.getLotViewModelList());
    existedLots.remove(lotPosition);
    productViewModel.setLotViewModelList(existedLots);
    updateViewModels(productViewModels);
  }

  public void updateTotalViewModels() {
    IssueVoucherReportSummaryViewModel viewModel = (IssueVoucherReportSummaryViewModel) viewModels
        .get(viewModels.size() - 1);
    viewModel.setTotal(calculateTotalValue(getProductViewModels()));
    viewModels.set(viewModels.size() - 1, viewModel);
  }


  private void updateViewModels(List<IssueVoucherReportProductViewModel> productViewModels) {
    if (viewModels == null) {
      viewModels = new ArrayList<>();
    }
    IssueVoucherReportSummaryViewModel summaryViewModel;
    if (viewModels.size() > 0) {
      summaryViewModel = (IssueVoucherReportSummaryViewModel) viewModels.get(viewModels.size() - 1);
      summaryViewModel.setTotal(calculateTotalValue(productViewModels));
    } else {
      summaryViewModel = new IssueVoucherReportSummaryViewModel(pod, calculateTotalValue(productViewModels));
    }
    viewModels.clear();
    viewModels.addAll(productViewModels);
    viewModels.add(summaryViewModel);
  }

  public OrderStatus getPodStatus() {
    return pod.getOrderStatus();
  }

  public boolean getIsLocal() {
    return pod.isLocal();
  }


}
