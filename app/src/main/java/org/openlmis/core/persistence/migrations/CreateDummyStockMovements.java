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

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.Migration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreateDummyStockMovements implements Migration {

    DbUtil dbUtil;

    public CreateDummyStockMovements() {
        dbUtil = new DbUtil();
    }

    @Override
    public void up(SQLiteDatabase db, ConnectionSource connectionSource) {
        createStockMovementItems();
    }

    private void createStockMovementItems() {
        final List<StockMovementItem> list = new ArrayList<>();
        StockCard stockCard = new StockCard();
        stockCard.setId(1L);
        for (int i = 0; i < 500; i++) {
            StockMovementItem stockMovementItem = new StockMovementItem();
            stockMovementItem.setMovementQuantity(i);
            stockMovementItem.setReason("reason" + i);
            stockMovementItem.setMovementDate(new Date());
            stockMovementItem.setMovementType(StockMovementItem.MovementType.RECEIVE);
            stockMovementItem.setStockCard(stockCard);

            list.add(stockMovementItem);
        }

        try {
            dbUtil.withDaoAsBatch(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, Void>() {
                @Override
                public Void operate(Dao<StockMovementItem, String> dao) throws SQLException {
                    for (StockMovementItem item : list) {
                        dao.create(item);
                    }
                    return null;
                }
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void down(SQLiteDatabase db, ConnectionSource connectionSource) {

    }
}
