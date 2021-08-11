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
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.openlmis.core.model.DraftBulkIssueProduct;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
@Builder
public class BulkIssueProductViewModel implements MultiItemEntity, Comparable<BulkIssueProductViewModel> {

  public static final int TYPE_EDIT = 1;

  public static final int TYPE_DONE = 2;

  private boolean done;

  private Product product;

  private Long requested;

  private DraftBulkIssueProduct draftProduct;

  private List<BulkIssueLotViewModel> lotViewModels;

  public static BulkIssueProductViewModel buildFromDraft(DraftBulkIssueProduct draftProduct) {
    return new BulkIssueProductViewModelBuilder()
        .done(draftProduct.isDone())
        .product(draftProduct.getProduct())
        .requested(draftProduct.getRequested())
        .draftProduct(draftProduct)
        .lotViewModels(FluentIterable
            .from(draftProduct.getDraftLotListWrapper())
            .transform(draftLot -> BulkIssueLotViewModel.buildFromDraft(draftProduct.isDone(), draftLot))
            .toSortedList(BulkIssueLotViewModel::compareTo))
        .build();
  }

  public static BulkIssueProductViewModel buildFromProduct(Product product, List<LotOnHand> lotOnHandList) {
    return new BulkIssueProductViewModelBuilder()
        .product(product)
        .lotViewModels(FluentIterable
            .from(lotOnHandList)
            .transform(BulkIssueLotViewModel::buildFromLotOnHand)
            .toSortedList(BulkIssueLotViewModel::compareTo))
        .build();
  }

  public DraftBulkIssueProduct convertToDraft(String documentNumber, String movementReasonCode) {
    if (draftProduct == null) {
      draftProduct = new DraftBulkIssueProduct();
    }
    return draftProduct
        .setDone(done)
        .setMovementReasonCode(movementReasonCode)
        .setProduct(product)
        .setRequested(requested)
        .setDocumentNumber(documentNumber)
        .setDraftLotListWrapper(FluentIterable
            .from(lotViewModels)
            .transform(lotViewModel -> Objects.requireNonNull(lotViewModel).convertToDraft(draftProduct))
            .toList());
  }

  public boolean validate() {
    setDone(true);
    return done;
  }

  public void setDone(boolean isDone) {
    this.done = isDone;
    if (lotViewModels == null) {
      return;
    }
    for (BulkIssueLotViewModel lotViewModel : lotViewModels) {
      lotViewModel.setDone(isDone);
    }
  }

  public List<BulkIssueLotViewModel> getFilteredLotViewModels() {
    return FluentIterable
        .from(lotViewModels)
        .filter(BulkIssueLotViewModel::shouldDisplayAfterDone)
        .toList();
  }

  @Override
  public int getItemType() {
    return done ? TYPE_DONE : TYPE_EDIT;
  }

  @Override
  public int compareTo(BulkIssueProductViewModel o) {
    return product.compareTo(o.getProduct());
  }

  public boolean hasChanged() {
    for (BulkIssueLotViewModel lotViewModel : lotViewModels) {
      if (lotViewModel.hasChanged()) {
        return true;
      }
    }
    // self check
    Long draftRequested = draftProduct == null ? null : draftProduct.getRequested();
    Long currentRequested = requested == null ? null : requested;
    boolean requestedChanged = ObjectUtils.notEqual(draftRequested, currentRequested);
    boolean statusChanged = (draftProduct == null && done) || (draftProduct != null && draftProduct.isDone() != done);
    return requestedChanged || statusChanged;
  }
}
