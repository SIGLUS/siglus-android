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

import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;

import java.util.HashMap;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StockMovementEntry {
    String facilityId;
    String productCode;
    long quantity;
    String reasonName;
    String occurred;
    String referenceNumber;
    String type;
    HashMap<String,String> customProps = new HashMap<>();

    public  StockMovementEntry(StockMovementItem stockMovementItem, String facilityId) {
        this.setProductCode(stockMovementItem.getStockCard().getProduct().getCode());
        this.setQuantity(stockMovementItem.getMovementQuantity());
        this.setReasonName(stockMovementItem.getReason());
        this.setFacilityId(facilityId);
        this.setType("ADJUSTMENT");
        this.setOccurred(DateUtil.formatDate(stockMovementItem.getMovementDate(), DateUtil.DB_DATE_FORMAT));
        this.setReferenceNumber(stockMovementItem.getDocumentNumber());
        this.getCustomProps().put("expirationDates", stockMovementItem.getStockCard().getExpireDates());
    }
}
