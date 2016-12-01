package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.DraftLotItem;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class InventoryRepository {
    final Context context;
    GenericDao<Inventory> genericDao;
    GenericDao<DraftInventory> draftInventoryGenericDao;
    GenericDao<DraftLotItem> draftLotItemGenericDao;

    private final Comparator<Inventory> inventoryComparator = new Comparator<Inventory>() {
        @Override
        public int compare(Inventory i1, Inventory i2) {
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            c1.setTime(i1.getUpdatedAt());
            c2.setTime(i2.getUpdatedAt());
            return c1.get(Calendar.DAY_OF_YEAR) - c2.get(Calendar.DAY_OF_YEAR);
        }
    };

    @Inject
    DbUtil dbUtil;
    @Inject
    private LotRepository lotRepository;

    @Inject
    public InventoryRepository(Context context) {
        genericDao = new GenericDao<>(Inventory.class, context);
        draftInventoryGenericDao = new GenericDao<>(DraftInventory.class, context);
        draftLotItemGenericDao = new GenericDao<>(DraftLotItem.class, context);
        this.context = context;
    }

    public void save(Inventory inventory) {
        try {
            genericDao.create(inventory);
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }



    public List<Inventory> queryPeriodInventory(final Period period) throws LMISException {
        return dbUtil.withDao(Inventory.class, new DbUtil.Operation<Inventory, List<Inventory>>() {
            @Override
            public List<Inventory> operate(Dao<Inventory, String> dao) throws SQLException {
                if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
                    List<Inventory> inventories = dao.queryBuilder().orderBy("updatedAt", false).where().between("updatedAt", period.getBegin().toDate(), period.getEnd().toDate()).query();
                    TreeSet<Inventory> treeSet = new TreeSet<>(new Comparator<Inventory>() {
                        @Override
                        public int compare(Inventory i1, Inventory i2) {
                            Calendar c1 = Calendar.getInstance();
                            Calendar c2 = Calendar.getInstance();
                            c1.setTime(i1.getUpdatedAt());
                            c2.setTime(i2.getUpdatedAt());
                            return c1.get(Calendar.DAY_OF_YEAR) - c2.get(Calendar.DAY_OF_YEAR);
                        }
                    });
                    treeSet.addAll(inventories);
                    return new ArrayList<>(treeSet);
                }
                return dao.queryBuilder().orderBy("updatedAt", false).where().between("updatedAt", period.getInventoryBegin().toDate(), period.getInventoryEnd().toDate()).query();
            }
        });
    }

    public void createDraft(final DraftInventory draftInventory) throws LMISException {
        dbUtil.withDaoAsBatch(DraftInventory.class, new DbUtil.Operation<DraftInventory, Object>() {
            @Override
            public Object operate(Dao<DraftInventory, String> dao) throws SQLException, LMISException {
                draftInventoryGenericDao.create(draftInventory);
                if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
                    for (DraftLotItem draftLotItem : draftInventory.getDraftLotItemListWrapper()) {
                        draftLotItemGenericDao.createOrUpdate(draftLotItem);
                    }
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
                if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
                    TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getConnectionSource(), DraftLotItem.class);
                }
                return null;
            }
        });

    }
}
