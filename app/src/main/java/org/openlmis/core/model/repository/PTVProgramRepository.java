package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.Period;
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

    @Inject
    PTVProgramStockInformationRepository ptvProgramStockInformationRepository;

    @Inject
    ServiceDispensationRepository serviceDispensationRepository;

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
                genericDao.create(ptvProgram);
                patientDispensationRepository.save(new ArrayList<>(ptvProgram.getPatientDispensations()));
                ptvProgramStockInformationRepository.save(new ArrayList<>(ptvProgram.getPtvProgramStocksInformation()));
                for (PTVProgramStockInformation ptvProgramStockInformation: ptvProgram.getPtvProgramStocksInformation()) {
                    serviceDispensationRepository.save(new ArrayList<>(ptvProgramStockInformation.getServiceDispensations()));
                }
                return ptvProgram;
            }
        });
    }

    public List<PTVProgram> getAll() throws LMISException {
        return genericDao.queryForAll();
    }

    public PTVProgram getByPeriod(final Period period) throws LMISException {
        return dbUtil.withDao(PTVProgram.class, new DbUtil.Operation<PTVProgram, PTVProgram>() {
            @Override
            public PTVProgram operate(Dao<PTVProgram, String> dao) throws SQLException, LMISException {
                return dao.queryBuilder().where().eq("startPeriod", period.getBegin().toDate()).and().eq("endPeriod", period.getEnd().toDate()).queryForFirst();
            }
        });
    }

    public PTVProgram getFirstMovement() throws LMISException {
        return dbUtil.withDao(PTVProgram.class, new DbUtil.Operation<PTVProgram, PTVProgram>() {
            @Override
            public PTVProgram operate(Dao<PTVProgram, String> dao) throws SQLException, LMISException {
                PTVProgram ptvProgramDataReport = dao.queryBuilder().orderBy("createdAt", true).queryForFirst();
                return ptvProgramDataReport;
            }
        });
    }
}
