package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftInitialInventory;
import org.openlmis.core.model.DraftInitialInventoryLotItem;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.DraftLotItem;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

import java.sql.SQLException;
import java.util.List;

public class InventoryRepository {
    final Context context;
    GenericDao<Inventory> genericDao;
    GenericDao<DraftInventory> draftInventoryGenericDao;
    GenericDao<DraftLotItem> draftLotItemGenericDao;

    GenericDao<DraftInitialInventory> draftInitialInventoryGenericDao;
    GenericDao<DraftInitialInventoryLotItem> draftInitialInventoryLotItemGenericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    public InventoryRepository(Context context) {
        genericDao = new GenericDao<>(Inventory.class, context);
        draftInventoryGenericDao = new GenericDao<>(DraftInventory.class, context);
        draftLotItemGenericDao = new GenericDao<>(DraftLotItem.class, context);
        draftInitialInventoryGenericDao = new GenericDao<>(DraftInitialInventory.class, context);
        draftInitialInventoryLotItemGenericDao = new GenericDao<>(DraftInitialInventoryLotItem.class, context);
        this.context = context;
    }

    public void save(Inventory inventory) {
        try {
            genericDao.create(inventory);
        } catch (LMISException e) {
            new LMISException(e, "InventoryRepository.save").reportToFabric();
        }
    }

    public List<Inventory> queryPeriodInventory(final Period period) throws LMISException {
        return dbUtil.withDao(Inventory.class, new DbUtil.Operation<Inventory, List<Inventory>>() {
            @Override
            public List<Inventory> operate(Dao<Inventory, String> dao) throws SQLException {
                return dao.queryBuilder().orderBy("updatedAt", false)
                        .where()
                        .between("updatedAt",
                                period.getInventoryBegin().toDate(),
                                period.getInventoryEnd().toDate())
                        .query();
            }
        });
    }

    public void createDraft(final DraftInventory draftInventory) throws LMISException {
        dbUtil.withDaoAsBatch(DraftInventory.class, new DbUtil.Operation<DraftInventory, Object>() {
            @Override
            public Object operate(Dao<DraftInventory, String> dao) throws LMISException {
                draftInventoryGenericDao.createOrUpdate(draftInventory);
                for (DraftLotItem draftLotItem : draftInventory.getDraftLotItemListWrapper()) {
                    draftLotItemGenericDao.createOrUpdate(draftLotItem);
                }
                return null;
            }
        });
    }

    public List<DraftInventory> queryAllDraft() throws LMISException {
        return draftInventoryGenericDao.queryForAll();
    }

    public void clearDraft() throws LMISException {
        dbUtil.withDaoAsBatch(DraftInventory.class, new DbUtil.Operation<DraftInventory, Object>() {
            @Override
            public Object operate(Dao<DraftInventory, String> dao) throws SQLException, LMISException {
                TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getConnectionSource(), DraftInventory.class);
                TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getConnectionSource(), DraftLotItem.class);
                return null;
            }
        });

    }

    public void createInitialDraft(final DraftInitialInventory draftInitialInventory) throws LMISException {
        dbUtil.withDaoAsBatch(DraftInitialInventory.class, dao -> {
            draftInitialInventoryGenericDao.createOrUpdate(draftInitialInventory);
            for (DraftInitialInventoryLotItem draftInitialInventoryLotItem : draftInitialInventory.getDraftLotItemListWrapper()) {
                draftInitialInventoryLotItemGenericDao.createOrUpdate(draftInitialInventoryLotItem);
            }
            return null;
        });
    }

    public List<DraftInitialInventory> queryAllInitialDraft() throws LMISException {
        return draftInitialInventoryGenericDao.queryForAll();
    }

    public void clearInitialDraft() throws LMISException {
        dbUtil.withDaoAsBatch(DraftInitialInventory.class, dao -> {
            TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
                    .getConnectionSource(), DraftInitialInventory.class);
            TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
                    .getConnectionSource(), DraftInitialInventoryLotItem.class);
            return null;
        });

    }
}
