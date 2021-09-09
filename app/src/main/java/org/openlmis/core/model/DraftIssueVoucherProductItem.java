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

package org.openlmis.core.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.openlmis.core.utils.ListUtil;
import org.openlmis.core.view.viewmodel.IssueVoucherProductViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@DatabaseTable(tableName = "draft_issue_voucher_product_items")
public class DraftIssueVoucherProductItem extends BaseModel {

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  Pod pod;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  Product product;

  @DatabaseField(defaultValue = "false")
  boolean done;

  @ForeignCollectionField(eager = true)
  private ForeignCollection<DraftIssueVoucherProductLotItem> foreignDraftLotItems;

  private List<DraftIssueVoucherProductLotItem> draftLotItemListWrapper;

  public List<DraftIssueVoucherProductLotItem> getDraftLotItemListWrapper() {
    draftLotItemListWrapper = ListUtil.wrapOrEmpty(foreignDraftLotItems, draftLotItemListWrapper);
    return draftLotItemListWrapper;
  }

  public IssueVoucherProductViewModel from() {
    IssueVoucherProductViewModel productViewModel = IssueVoucherProductViewModel.builder()
        .product(product)
        .done(done)
        .productItem(this)
        .build();
    productViewModel.getLotViewModels().addAll(FluentIterable.from(getDraftLotItemListWrapper())
        .transform(DraftIssueVoucherProductLotItem::from)
        .toList());
    Collections.sort(productViewModel.getLotViewModels());
    return productViewModel;
  }

}
