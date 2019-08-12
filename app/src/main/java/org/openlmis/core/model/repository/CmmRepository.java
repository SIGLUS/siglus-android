package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Cmm;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.sql.SQLException;
import java.util.List;

public class CmmRepository {

    @Inject
    DbUtil dbUtil;

    private GenericDao<Cmm> cmmDao;

    @Inject
    public CmmRepository(Context context) {
        cmmDao = new GenericDao<>(Cmm.class, context);
    }

    public void save(final Cmm cmm) throws LMISException {
        Cmm sameCardSamePeriodCmm = dbUtil.withDao(Cmm.class, new DbUtil.Operation<Cmm, Cmm>() {
            @Override
            public Cmm operate(Dao<Cmm, String> dao) throws SQLException, LMISException {
                return dao.queryBuilder()
                        .where().eq("stockCard_id", cmm.getStockCard().getId())
                        .and().eq("periodBegin", cmm.getPeriodBegin())
                        .and().eq("periodEnd", cmm.getPeriodEnd())
                        .queryForFirst();
            }
        });

        if (sameCardSamePeriodCmm != null) {
            cmm.setId(sameCardSamePeriodCmm.getId());
        }
        cmmDao.createOrUpdate(cmm);
    }

    public List<Cmm> list() throws LMISException {
        return cmmDao.queryForAll();
    }

    public List<Cmm> listUnsynced() throws LMISException {
        return FluentIterable.from(cmmDao.queryForAll()).filter(new Predicate<Cmm>() {
            @Override
            public boolean apply(Cmm cmm) {
                return !cmm.isSynced();
            }
        }).toList();
    }
}
