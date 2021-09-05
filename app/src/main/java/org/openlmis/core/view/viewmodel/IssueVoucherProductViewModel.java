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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.core.enumeration.IssueVoucherValidationType;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.PodProductLotItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueVoucherProductViewModel implements MultiItemEntity {

  public static final int TYPE_EDIT = 1;
  public static final int TYPE_DONE = 2;
  private final List<IssueVoucherLotViewModel> lotViewModels = new ArrayList<>();
  private boolean done;
  private Product product;
  private StockCard stockCard;
  private IssueVoucherValidationType validationType;
  private boolean shouldShowError;

  public IssueVoucherProductViewModel(Product product) {
    this.product = product;
  }

  public IssueVoucherProductViewModel(StockCard stockCard) {
    this.stockCard = stockCard;
    this.product = stockCard.getProduct();
    lotViewModels.addAll(FluentIterable.from(stockCard.getLotOnHandListWrapper())
        .filter(lotOnHand -> Objects.requireNonNull(lotOnHand).getQuantityOnHand() != null
            && lotOnHand.getQuantityOnHand() > 0)
        .transform(IssueVoucherLotViewModel::build)
        .toList());
  }

  @Override
  public int getItemType() {
    return done ? TYPE_DONE : TYPE_EDIT;
  }

  public boolean validate() {
    if (validProduct() && isAllLotValid()) {
      setDone(true);
      return true;
    } else {
      setDone(false);
      return false;
    }
  }

  public void setDone(boolean isDone) {
    this.done = isDone;
    for (IssueVoucherLotViewModel lotViewModel : lotViewModels) {
      lotViewModel.setDone(isDone);
    }
  }

  public boolean validProduct() {
    if (lotViewModels.isEmpty()) {
      validationType = IssueVoucherValidationType.NO_LOT;
      return false;
    } else {
      boolean isALlLotBlank = true;
      for (IssueVoucherLotViewModel lotViewModel : lotViewModels) {
        if (!lotViewModel.isLotAllBlank()) {
          isALlLotBlank = false;
        }
        if (lotViewModel.validateLot()) {
          lotViewModel.setValid(true);
          continue;
        }
        lotViewModel.setValid(false);
      }
      if (isALlLotBlank) {
        validationType = IssueVoucherValidationType.ALL_LOT_BLANK;
        return false;
      }
      validationType = IssueVoucherValidationType.VALID;
      return true;
    }
  }

  private boolean isAllLotValid() {
    for (IssueVoucherLotViewModel lotViewModel : lotViewModels) {
      if (!lotViewModel.isValid()) {
        return false;
      }
    }
    return true;
  }

  public PodProductItem from() {
    return PodProductItem.builder()
        .product(product)
        .podProductLotItemsWrapper(buildPodProductLotItems())
        .build();
  }

  private List<PodProductLotItem> buildPodProductLotItems() {
    return FluentIterable.from(lotViewModels)
        .transform(IssueVoucherLotViewModel::from)
        .toList();
  }

}
