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

import android.text.SpannableStringBuilder;
import lombok.Getter;
import lombok.Setter;
import org.openlmis.core.model.Product;
import org.openlmis.core.utils.TextStyleUtil;

@Getter
public class ProductsToBulkEntriesViewModel {

  private final Product product;
  private final SpannableStringBuilder styledProductName;
  private final String productType;
  private Boolean isAdded;


  @Setter
  private boolean isChecked;

  public ProductsToBulkEntriesViewModel(Product product) {
    this.product = product;
    this.styledProductName = TextStyleUtil.formatStyledProductName(product);
    this.productType = "each";
    this.isChecked = false;
    this.isAdded = false;
  }

}
