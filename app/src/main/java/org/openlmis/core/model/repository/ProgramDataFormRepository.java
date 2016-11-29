package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.Collections;
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

    public List<ProgramDataForm> listByProgramCode(String programCode) throws LMISException {
        final Program program = programRepository.queryByCode(programCode);
        if (program == null) {
            return Collections.emptyList();
        }

        return dbUtil.withDao(ProgramDataForm.class, new DbUtil.Operation<ProgramDataForm, List<ProgramDataForm>>() {
            @Override
            public List<ProgramDataForm> operate(Dao<ProgramDataForm, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("program_id", program.getId()).query();
            }
        });
    }
}
