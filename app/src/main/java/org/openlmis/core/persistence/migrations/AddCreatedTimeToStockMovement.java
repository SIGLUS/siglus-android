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


import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.Migration;

import java.util.List;


public class AddCreatedTimeToStockMovement extends Migration {
    GenericDao<StockMovementItem> stockItemGenericDao;


    MovementReasonManager reasonManager;
    DbUtil dbUtil;

    public AddCreatedTimeToStockMovement() {
        stockItemGenericDao = new GenericDao<>(StockMovementItem.class, LMISApp.getContext());
        reasonManager = MovementReasonManager.getInstance();
        dbUtil = new DbUtil();
    }

    @Override
    public void up() {
        execSQL("ALTER TABLE 'stock_items' ADD COLUMN createdTime VARCHAR");
        execSQL("CREATE INDEX `stock_items_created_time_idx` ON `stock_items` ( `createdTime` )");
        try {
            initCreatedTime();
        } catch (LMISException e) {
            new LMISException(e, "AddCreatedTimeToStockMovement,up").reportToFabric();
        }
    }

    private void initCreatedTime() throws LMISException {
        List<StockMovementItem> itemList = stockItemGenericDao.queryForAll();
        if (itemList == null || itemList.size() == 0) {
            return;
        }
        for (StockMovementItem item : itemList) {
            item.setCreatedTime(item.getCreatedAt());
        }
        updateStockMovementItems(itemList);
    }

    private void updateStockMovementItems(final List<StockMovementItem> stockMovementItems) throws LMISException {
        dbUtil.withDaoAsBatch(LMISApp.getContext(), StockMovementItem.class, (DbUtil.Operation<StockMovementItem, Void>) dao -> {
            for (StockMovementItem stockMovementItem : stockMovementItems) {
                dao.update(stockMovementItem);
            }
            return null;
        });
    }

}
