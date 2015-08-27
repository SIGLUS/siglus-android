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

package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockRepository {
    @Inject
    DbUtil dbUtil;

    GenericDao<StockCard> genericDao;
    GenericDao<StockMovementItem> stockItemGenericDao;

    @Inject
    ProductRepository productRepository;

    @Inject
    ProgramRepository programRepository;

    @Inject
    public StockRepository(Context context) {
        genericDao = new GenericDao<>(StockCard.class, context);
        stockItemGenericDao = new GenericDao<>(StockMovementItem.class, context);
    }


    public void batchSave(final List<StockCard> stockCards) {
        try {
            dbUtil.withDaoAsBatch(StockCard.class, new DbUtil.Operation<StockCard, Object>() {
                @Override
                public Object operate(Dao<StockCard, String> dao) throws SQLException {
                    for (StockCard stockCard : stockCards) {
                        dao.createOrUpdate(stockCard);
                    }
                    return null;
                }
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }


    public void save(final StockCard stockCard) {
        try {
            genericDao.create(stockCard);
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    public void saveStockItem(final StockMovementItem stockMovementItem) throws LMISException {
        dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, Void>() {
            @Override
            public Void operate(Dao<StockMovementItem, String> dao) throws SQLException {
                dao.create(stockMovementItem);
                return null;
            }
        });
    }

    public List<StockCard> list() throws LMISException {
        return genericDao.queryForAll();
    }

    public List<StockCard> list(String programCode) throws LMISException {
        ArrayList<StockCard> stockCards = new ArrayList<>();
        Program program = programRepository.queryByCode(programCode);
        if (program != null) {
            List<Product> products = productRepository.queryProducts(program.getId());
            if (products != null) {
                for (Product product : products) {
                    StockCard stockCard = queryStockCardByProductId(product.getId());
                    if (stockCard != null) {
                        stockCards.add(stockCard);
                    }
                }
            }
        }
        return stockCards;
    }

    private StockCard queryStockCardByProductId(final long productId) throws LMISException {
        return dbUtil.withDao(StockCard.class, new DbUtil.Operation<StockCard, StockCard>() {
            @Override
            public StockCard operate(Dao<StockCard, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("product_id", productId).queryForFirst();
            }
        });
    }

    public List<StockMovementItem> listLastFive(final long stockCardId) throws LMISException{
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
               return dao.queryBuilder().limit(5L).orderBy("createdAt", true).where().eq("stockCard_id", stockCardId).query();
            }
        });
    }


    public List<StockMovementItem> queryStockItems(final StockCard stockCard, final Date startDate, final Date endDate) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("stockCard_id", stockCard.getId()).and().ge("createdAt", startDate).and().le("createdAt", endDate).query();
            }
        });
    }

    public StockCard queryStockCardById(final long id) throws LMISException{
        return dbUtil.withDao(StockCard.class, new DbUtil.Operation<StockCard, StockCard>() {
            @Override
            public StockCard operate(Dao<StockCard, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("id", id).queryForFirst();
            }
        });
    }
}
