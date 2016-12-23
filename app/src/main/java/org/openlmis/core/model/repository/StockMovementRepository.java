package org.openlmis.core.model.repository;

import android.content.Context;
import android.database.Cursor;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.Lists;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockMovementRepository {
    @Inject
    DbUtil dbUtil;
    @Inject
    Context context;

    @Inject
    private LotRepository lotRepository;

    GenericDao<StockMovementItem> genericDao;

    @Inject
    public StockMovementRepository(Context context) {
        genericDao = new GenericDao<>(StockMovementItem.class, context);
    }

    public void create(StockMovementItem stockMovementItem) throws LMISException {
        genericDao.create(stockMovementItem);
    }

    public List<StockMovementItem> listUnSynced() throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("synced", false).query();
            }
        });
    }

    protected void batchCreateOrUpdateStockMovementsAndLotInfo(final List<StockMovementItem> stockMovementItems) throws LMISException {
        dbUtil.withDaoAsBatch(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, Void>() {
            @Override
            public Void operate(Dao<StockMovementItem, String> dao) throws SQLException, LMISException {
                for (StockMovementItem stockMovementItem : stockMovementItems) {
                    updateDateTimeIfEmpty(stockMovementItem);
                    dao.createOrUpdate(stockMovementItem);
                    lotRepository.batchCreateLotsAndLotMovements(stockMovementItem.getLotMovementItemListWrapper());
                }
                return null;
            }
        });
    }

    private void updateDateTimeIfEmpty(StockMovementItem stockMovementItem) {
        if (stockMovementItem.getCreatedTime() == null) {
            stockMovementItem.setCreatedTime(new Date());
        }
        stockMovementItem.setCreatedAt(new Date());
        stockMovementItem.setUpdatedAt(new Date());
    }


    public void batchCreateStockMovementItemAndLotItems(final StockMovementItem stockMovementItem) throws LMISException {
        stockMovementItem.setCreatedTime(new Date(LMISApp.getInstance().getCurrentTimeMillis()));
        create(stockMovementItem);

        lotRepository.batchCreateLotsAndLotMovements(stockMovementItem.getLotMovementItemListWrapper());
        lotRepository.batchCreateLotsAndLotMovements(stockMovementItem.getNewAddedLotMovementItemListWrapper());
    }

    public void batchCreateOrUpdateStockMovementsAndLotMovements(final List<StockMovementItem> stockMovementItems) throws LMISException {
        dbUtil.withDaoAsBatch(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, Void>() {
            @Override
            public Void operate(Dao<StockMovementItem, String> dao) throws SQLException, LMISException {
                for (StockMovementItem stockMovementItem : stockMovementItems) {
                    updateDateTimeIfEmpty(stockMovementItem);
                    dao.createOrUpdate(stockMovementItem);
                    for (LotMovementItem lotMovementItem : stockMovementItem.getLotMovementItemListWrapper()) {
                        lotRepository.createLotMovementItem(lotMovementItem);
                    }
                }
                return null;
            }
        });
    }

    public StockMovementItem getFirstStockMovement() throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, StockMovementItem>() {
            @Override
            public StockMovementItem operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return dao.queryBuilder()
                        .orderBy("movementDate", true)
                        .orderBy("createdTime", true)
                        .queryForFirst();
            }
        });
    }

    public List<String> queryStockMovementDatesByProgram(final String programCode) {
        String rawSql = "SELECT movementDate FROM stock_items s1 "
                + "JOIN stock_cards s2 ON s1.stockCard_id = s2.id "
                + "JOIN products p1 ON s2.product_id = p1.id "
                + "JOIN product_programs p2 ON p2.productCode = p1.code "
                + "JOIN programs p3 ON p2.programCode = p3.programCode "
                + "WHERE p1.isActive = 1 AND p1.isArchived = 0 AND p2.isActive = 1 AND p3.programCode = '" + programCode + "' "
                + "OR p3.parentCode = '" + programCode + "'";
        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
        List<String> movementDates = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                movementDates.add(cursor.getString(cursor.getColumnIndexOrThrow("movementDate")));
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }

        return movementDates;
    }

    public Date queryEarliestStockMovementDateByProgram(final String programCode) {
        Date earliestDate = null;

        for (String movementDate : queryStockMovementDatesByProgram(programCode)) {
            Date date = DateUtil.parseString(movementDate, DateUtil.DB_DATE_FORMAT);
            if (earliestDate == null || date.before(earliestDate)) {
                earliestDate = date;
            }
        }
        return earliestDate;
    }

    public StockMovementItem queryFirstStockMovementItem(final StockCard stockCard) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, StockMovementItem>() {
            @Override
            public StockMovementItem operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return dao.queryBuilder()
                        .orderBy("movementDate", true)
                        .orderBy("createdTime", true)
                        .where()
                        .eq("stockCard_id", stockCard.getId())
                        .queryForFirst();
            }
        });
    }

    public List<StockMovementItem> queryStockItemsHistory(final long stockCardId, final long startIndex, final long maxRows) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return dao.queryBuilder().offset(startIndex).limit(maxRows).orderBy("movementDate", true).orderBy("createdTime", true).orderBy("id", true).where().eq("stockCard_id", stockCardId).query();
            }
        });
    }

    public List<StockMovementItem> queryStockItemsByPeriodDates(final StockCard stockCard, final Date periodBeginDate, final Date periodEndDate) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return dao.queryBuilder()
                        .orderBy("movementDate", true)
                        .orderBy("createdTime", true)
                        .where()
                        .eq("stockCard_id", stockCard.getId())
                        .and().gt("createdTime", periodBeginDate)//difference from the api above
                        .and().le("createdTime", periodEndDate)
                        .query();
            }
        });
    }

    public List<StockMovementItem> queryStockMovementsByTimeRange(final long stockCardId, final Date startDate, final Date endDate) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return dao.queryBuilder()
                        .orderBy("movementDate", true)
                        .orderBy("createdTime", true)
                        .where()
                        .eq("stockCard_id", stockCardId)
                        .and().ge("movementDate", startDate)
                        .and().le("movementDate", endDate)
                        .query();
            }
        });
    }

    public List<StockMovementItem> listLastFiveStockMovements(final long stockCardId) throws LMISException {
        return dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, List<StockMovementItem>>() {
            @Override
            public List<StockMovementItem> operate(Dao<StockMovementItem, String> dao) throws SQLException {
                return Lists.reverse(dao.queryBuilder().limit(5L).orderBy("movementDate", false).orderBy("createdTime", false).orderBy("id", false).where().eq("stockCard_id", stockCardId).query());
            }
        });
    }
}