package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Cmm;
import org.openlmis.core.persistence.GenericDao;

import java.util.List;

public class CmmRepository {
    private GenericDao<Cmm> cmmDao;

    @Inject
    public CmmRepository(Context context) {
        cmmDao = new GenericDao<>(Cmm.class, context);
    }

    public void save(Cmm cmm) throws LMISException {
        cmmDao.createOrUpdate(cmm);
    }

    public List<Cmm> list() throws LMISException {
        return cmmDao.queryForAll();
    }
}
