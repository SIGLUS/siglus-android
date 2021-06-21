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

package org.openlmis.core.network.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
@NoArgsConstructor
public class StockMovementEntry {

  String processedDate;
  String signature;
  String occurred;
  String documentationNo;
  String productCode;
  String type;
  long soh;
  long quantity;
  List<LotMovementEntry> lotEventList = new ArrayList<>();

  public StockMovementEntry(StockMovementItem stockMovementItem) {
    this.setProcessedDate(new DateTime(stockMovementItem.getCreatedTime())
            .toString(ISODateTimeFormat.dateTime()));
    this.setSignature(stockMovementItem.getSignature());
    this.setOccurred(DateUtil.formatDate(stockMovementItem.getMovementDate(), DateUtil.DB_DATE_FORMAT));
    this.setDocumentationNo(stockMovementItem.getDocumentNumber());
    this.setProductCode(stockMovementItem.getStockCard().getProduct().getCode());
    this.setType(stockMovementItem.getMovementType().toString());
    this.setSoh(stockMovementItem.getStockOnHand());
    this.setQuantity(stockMovementItem.getMovementQuantity());
    if (stockMovementItem.getLotMovementItemListWrapper() != null) {
      lotEventList.addAll(FluentIterable.from(stockMovementItem.getLotMovementItemListWrapper())
          .transform(lotMovementItem -> new LotMovementEntry(lotMovementItem)).toList());
    }
  }
}
