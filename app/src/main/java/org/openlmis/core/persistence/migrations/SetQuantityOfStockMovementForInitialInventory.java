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

package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class SetQuantityOfStockMovementForInitialInventory extends Migration{

    @Override
    public void down() {
        execSQL("UPDATE stock_items SET movementQuantity = 0 WHERE id IN"
                + " (SELECT id FROM (SELECT * FROM stock_items ORDER BY id DESC) si GROUP BY stockCard_id)");
    }

    @Override
    public void up() {
        execSQL("UPDATE stock_items SET movementQuantity = stockOnHand WHERE id IN"
                + " (SELECT id FROM (SELECT * FROM stock_items ORDER BY id DESC) si GROUP BY stockCard_id)");
    }
}
