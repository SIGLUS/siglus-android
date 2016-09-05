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
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.Period;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.sql.SQLException;
import java.util.List;

public class InventoryRepository {
    final Context context;
    GenericDao<Inventory> genericDao;
    GenericDao<DraftInventory> draftInventoryGenericDao;
    GenericDao<DraftLotItem> draftLotItemGenericDao;

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
                        if (draftLotItem.isNewAdded()) {
                            lotRepository.createOrUpdateLot(draftLotItem.getLot());
                        } else {
                            Lot lot = lotRepository.getLotByLotNumberAndProductId(draftLotItem.getLot().getLotNumber(), draftLotItem.getLot().getProduct().getId());
                            draftLotItem.setLot(lot);
                        }
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

                    lotRepository.deleteLots(FluentIterable.from(draftLotItemGenericDao.queryForAll()).filter(new Predicate<DraftLotItem>() {
                        @Override
                        public boolean apply(DraftLotItem draftLotItem) {
                            return draftLotItem.isNewAdded();
                        }
                    }).transform(new Function<DraftLotItem, Lot>() {
                        @Override
                        public Lot apply(DraftLotItem draftLotItem) {
                            return draftLotItem.getLot();
                        }
                    }).toList());
                    TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getConnectionSource(), DraftLotItem.class);
                }
                return null;
            }
        });

    }
}
