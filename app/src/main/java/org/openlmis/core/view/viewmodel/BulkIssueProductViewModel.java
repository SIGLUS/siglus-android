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
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.openlmis.core.model.Product;

@Data
@RequiredArgsConstructor(staticName = "create")
public class BulkIssueProductViewModel implements MultiItemEntity {

  public static final int TYPE_EDIT = 1;

  public static final int TYPE_DONE = 2;

  private boolean done;

  @NonNull
  private Product product;

  private Long requested;

  @NonNull
  private List<BulkIssueLotViewModel> lotViewModels;

  @Override
  public int getItemType() {
    return done ? TYPE_DONE : TYPE_EDIT;
  }
}
