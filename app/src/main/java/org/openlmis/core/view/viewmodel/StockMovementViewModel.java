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


import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;

import lombok.Data;

@Data
public class StockMovementViewModel {

    String movementDate;
    String reason;
    String documentNo;
    String received;
    String negativeAdjustment;
    String positiveAdjustment;
    String issued;
    String stockExistence;


    public StockMovementViewModel(StockMovementItem item){
        this.movementDate = DateUtil.formatDate(item.getCreatedAt());
        this.documentNo = item.getDocumentNumber();
        this.reason = item.getReason();

        switch (item.getMovementType()){
            case RECEIVE:
                this.received = String.valueOf(item.getAmount());
                break;
            case ISSUE:
                this.issued = String.valueOf(item.getAmount());
                break;
            case NEGATIVE_ADJUST:
                this.negativeAdjustment = String.valueOf(item.getAmount());
                break;
            case POSITIVE_ADJUST:
                this.positiveAdjustment = String.valueOf(item.getAmount());
                break;
            default:
        }
        this.stockExistence = String.valueOf(item.getStockOnHand());
    }
}
