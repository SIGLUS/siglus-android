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


import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.openlmis.core.utils.DateUtil;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "stock_items")
public class StockMovementItem extends BaseModel {

    public enum MovementType {
        RECEIVE("RECEIVE"),
        ISSUE("ISSUE"),
        POSITIVE_ADJUST("POSITIVE_ADJUST"),
        NEGATIVE_ADJUST("NEGATIVE_ADJUST"),
        PHYSICAL_INVENTORY("PHYSICAL_INVENTORY");

        private final String value;

        MovementType(String receive) {
            this.value = receive;
        }

        @Override
        public String toString() {
            return value;
        }


    }

    @DatabaseField
    String documentNumber;

    @DatabaseField
    long movementQuantity;

    @DatabaseField
    String reason;

    @DatabaseField
    MovementType movementType;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    StockCard stockCard;

    @DatabaseField
    long stockOnHand = -1;

    @DatabaseField
    String signature;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DateUtil.DB_DATE_FORMAT)
    private java.util.Date movementDate;

    @DatabaseField
    private boolean synced = false;
}
