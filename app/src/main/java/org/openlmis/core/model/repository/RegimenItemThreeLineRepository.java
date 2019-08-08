package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.List;

public class RegimenItemThreeLineRepository {
    GenericDao<RegimenItemThreeLines> genericDao;
    @Inject
    DbUtil dbUtil;

    @Inject
    public RegimenItemThreeLineRepository(Context context) {
        this.genericDao = new GenericDao<>(RegimenItemThreeLines.class, context);
    }

    public void batchCreateOrUpdate(final List<RegimenItemThreeLines> list) throws LMISException {
        dbUtil.withDaoAsBatch(RegimenItemThreeLines.class, new DbUtil.Operation<RegimenItemThreeLines, Void>() {
            @Override
            public Void operate(Dao<RegimenItemThreeLines, String> dao) throws SQLException {
                for (RegimenItemThreeLines item : list) {
                    dao.createOrUpdate(item);
                }
                return null;
            }
        });
    }

    public void deleteRegimeThreeLineItems(final List<RegimenItemThreeLines> itemThreeLinesWrapper) throws LMISException {
        dbUtil.withDao(RegimenItemThreeLines.class, new DbUtil.Operation<RegimenItemThreeLines, Void>() {
            @Override
            public Void operate(Dao<RegimenItemThreeLines, String> dao) throws SQLException {
                dao.delete(itemThreeLinesWrapper);
                return null;
            }
        });
    }

    public void deleteRegimeThreeLineItem(final RegimenItemThreeLines item) throws LMISException {
        dbUtil.withDao(RegimenItemThreeLines.class, new DbUtil.Operation<RegimenItemThreeLines, Void>() {
            @Override
            public Void operate(Dao<RegimenItemThreeLines, String> dao) throws SQLException {
                dao.delete(item);
                return null;
            }
        });
    }

    public List<RegimenItemThreeLines> listAll() throws LMISException {
        return dbUtil.withDao(RegimenItemThreeLines.class, new DbUtil.Operation<RegimenItemThreeLines, List<RegimenItemThreeLines>>() {
            @Override
            public List<RegimenItemThreeLines> operate(Dao<RegimenItemThreeLines, String> dao) throws SQLException {
                return dao.queryForAll();
            }
        });
    }
}
