package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.util.List;

public class ProgramDataFormRepository {

    private final GenericDao<ProgramDataForm> genericDao;
    private final Context context;

    @Inject
    DbUtil dbUtil;

    @Inject
    ProgramRepository programRepository;

    @Inject
    public ProgramDataFormRepository(Context context) {
        genericDao = new GenericDao<>(ProgramDataForm.class, context);
        this.context = context;
    }

    public void save(ProgramDataForm form) throws LMISException {
        genericDao.create(form);
    }

    public List<ProgramDataForm> listAll() throws LMISException {
        return genericDao.queryForAll();
    }
}
