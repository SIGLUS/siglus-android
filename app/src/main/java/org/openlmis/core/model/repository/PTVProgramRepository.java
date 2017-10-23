package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class PTVProgramRepository {

    private Context context;

    @Inject
    DbUtil dbUtil;

    @Inject
    PatientDispensationRepository patientDispensationRepository;

    private GenericDao<PTVProgram> genericDao;


    @Inject
    public PTVProgramRepository(Context context) {
        this.genericDao = new GenericDao<>(PTVProgram.class, context);
        this.context = context;
    }

    public PTVProgram save(final PTVProgram ptvProgram) throws LMISException, SQLException {
        ConnectionSource connectionSource = DbUtil.getConnectionSource(LmisSqliteOpenHelper.getInstance(context));
        return TransactionManager.callInTransaction(connectionSource, new Callable<PTVProgram>() {
            @Override
            public PTVProgram call() throws LMISException {
                PTVProgram ptvProgramSaved = genericDao.create(ptvProgram);
                patientDispensationRepository.save(new ArrayList<>(ptvProgram.getPatientDispensations()));
                return ptvProgramSaved;
            }
        });
    }

    public List<PTVProgram> getAll() throws LMISException {
        return genericDao.queryForAll();
    }
}
