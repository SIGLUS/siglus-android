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

import java.util.List;
import lombok.Data;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;

@Data
public class BulkEntriesViewModel extends InventoryViewModel {

  private boolean done;

  private Product product;

  private Long quantity;

  private List<LotMovementViewModel> lotMovementViewModels;


  public BulkEntriesViewModel(Product product) {
    super(product);
    this.product = product;
  }

  public BulkEntriesViewModel(StockCard stockCard) {
    super(stockCard);
    this.product = stockCard.getProduct();
  }

  public BulkEntriesViewModel(Product product, boolean done,
      Long quantity,
      List<LotMovementViewModel> lotMovementViewModels) {
    super(product);
    this.done = done;
    this.product = product;
    this.quantity = quantity;
    this.lotMovementViewModels = lotMovementViewModels;
  }
}
