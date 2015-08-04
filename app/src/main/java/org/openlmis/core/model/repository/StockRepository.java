package org.openlmis.core.model.repository;

import android.content.Context;
import android.util.Log;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.apache.commons.lang3.time.DateUtils;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.utils.DateUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StockRepository {
    @Inject
    DbUtil dbUtil;

    GenericDao<StockCard> genericDao;
    GenericDao<StockItem> stockItemGenericDao;

    enum MOVEMENTTYPE {
        RECEIVE("Receive"),
        ISSUE("Issue"),
        POSADJUST("Positive Adjustment"),
        NEGADJUST("Negative Adjustment");

        MOVEMENTTYPE(String name){

        }
    }

    @Inject
    public StockRepository(Context context) {
        genericDao = new GenericDao<>(StockCard.class,context);
        stockItemGenericDao = new GenericDao<>(StockItem.class, context);
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

    public void saveStockItems(final ArrayList<StockItem> stockItems) throws LMISException{
        dbUtil.withDaoAsBatch(StockItem.class, new DbUtil.Operation<StockItem, Void>() {
            @Override
            public Void operate(Dao<StockItem, String> dao) throws SQLException {

                for (StockItem item : stockItems){
                    dao.create(item);
                }
                return null;
            }
        });
    }

    public List<StockCard> list() throws LMISException{
        return genericDao.queryForAll();
    }

    public List<StockCard> list(String programCode) throws LMISException{
        //TODO
        return new ArrayList<>();
    }

    public List<StockItem> listStockItems() throws LMISException {
        return stockItemGenericDao.queryForAll();
    }

    public List<StockItem> queryStockItems(final StockCard stockCard, final Date startDate, final Date endDate) throws LMISException {
        return dbUtil.withDao(StockItem.class, new DbUtil.Operation<StockItem, List<StockItem>>() {
            @Override
            public List<StockItem> operate(Dao<StockItem, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("stockCard_id", stockCard.getId()).and().ge("createdAt", startDate).and().le("createdAt", endDate).query();
            }
        });
    }

    public int sum(final String movementType, final StockCard stockCard, final Date startDate, final Date endDate) throws LMISException {
        return dbUtil.withDao(StockItem.class, new DbUtil.Operation<StockItem, Integer>() {
            @Override
            public Integer operate(Dao<StockItem, String> dao) throws SQLException {

                String query = "select sum(amount) from stock_items where stockCard_id=" + stockCard.getId()
                        + " and movementType='" + movementType
                        + "' and createdAt<='" + DateUtil.formatDate(endDate) + "' and createdAt>='" + DateUtil.formatDate(startDate) + "'";

                Log.d("StockRepository", query);
                return (int) dao.queryRawValue(query);
            }
        });
    }

}
