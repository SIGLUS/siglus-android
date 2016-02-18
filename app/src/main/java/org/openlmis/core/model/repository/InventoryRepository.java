package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.List;

public class InventoryRepository {
    final Context context;
    GenericDao<Inventory> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    public InventoryRepository(Context context) {
        genericDao = new GenericDao<>(Inventory.class, context);
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
}
