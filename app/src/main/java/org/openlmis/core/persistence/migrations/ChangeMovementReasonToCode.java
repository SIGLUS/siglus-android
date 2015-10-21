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



import com.j256.ormlite.dao.Dao;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.MovementReasonNotFoundException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.Migration;


import java.sql.SQLException;
import java.util.List;



public class ChangeMovementReasonToCode extends Migration{

    GenericDao<StockMovementItem> stockItemGenericDao;


    MovementReasonManager reasonManager;
    private DbUtil dbUtil;

    public ChangeMovementReasonToCode(){
        stockItemGenericDao = new GenericDao<>(StockMovementItem.class, LMISApp.getContext());
        reasonManager = new MovementReasonManager(LMISApp.getContext());
        dbUtil = new DbUtil();
    }

    @Override
    public void down() {

    }

    @Override
    public void up() {
        try {
            List<StockMovementItem> itemList = stockItemGenericDao.queryForAll();
            if (itemList == null || itemList.size() == 0){
                return;
            }

            for (StockMovementItem item : itemList){
                try {
                    String newReasonCode = reasonManager.queryForCode(item.getReason());
                    item.setReason(newReasonCode);
                }catch (MovementReasonNotFoundException e){
                    setDefaultReasonCode(item);
                }
            }
            updateStockMovementItems(itemList);

        }catch (LMISException e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    protected void setDefaultReasonCode(StockMovementItem item) {
        if ("physicalInventoryPositive".equalsIgnoreCase(item.getReason())){
            item.setReason("INVENTORY_POSITIVE");
        }else if ("physicalInventoryNegative".equalsIgnoreCase(item.getReason())){
            item.setReason("INVENTORY_NEGATIVE");
        }else {
            switch (item.getMovementType()){
                case ISSUE:
                    item.setReason("DEFAULT_ISSUE");
                    break;
                case POSITIVE_ADJUST:
                    item.setReason("DEFAULT_POSITIVE_ADJUSTMENT");
                    break;
                case NEGATIVE_ADJUST:
                    item.setReason("DEFAULT_NEGATIVE_ADJUSTMENT");
                    break;
                case RECEIVE:
                    item.setReason("DEFAULT_RECEIVE");
                    break;
                case PHYSICAL_INVENTORY:
                    item.setReason("INVENTORY");
                    break;
                default:
                    throw new RuntimeException("Invalid MovementType :" + item.getMovementType());
            }
        }
    }

    private void updateStockMovementItems(final List<StockMovementItem> stockMovementItems) throws LMISException {
        dbUtil.withDaoAsBatch(LMISApp.getContext(), StockMovementItem.class, new DbUtil.Operation<StockMovementItem, Void>() {
            @Override
            public Void operate(Dao<StockMovementItem, String> dao) throws SQLException {
                for (StockMovementItem stockMovementItem : stockMovementItems) {
                    dao.update(stockMovementItem);
                }
                return null;
            }
        });
    }
}
