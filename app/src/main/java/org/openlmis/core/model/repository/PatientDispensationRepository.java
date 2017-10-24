package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PatientDispensation;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.List;

public class PatientDispensationRepository {

    @Inject
    DbUtil dbUtil;

    private GenericDao<PatientDispensation> patientDispensationGenericDao;

    @Inject
    public PatientDispensationRepository(Context context) {
        patientDispensationGenericDao = new GenericDao<>(PatientDispensation.class, context);
    }

    public boolean save(List<PatientDispensation> patientDispensations) throws LMISException {
        return patientDispensationGenericDao.create(patientDispensations);
    }

    public List<PatientDispensation> getAllByProgramId(final long id) throws LMISException {
        return dbUtil.withDao(PatientDispensation.class, new DbUtil.Operation<PatientDispensation, List<PatientDispensation>>() {
            @Override
            public List<PatientDispensation> operate(Dao<PatientDispensation, String> dao) throws SQLException, LMISException {
                List<PatientDispensation> patientDispensations = dao.queryBuilder().where().eq("ptvProgramId", id).query();
                return patientDispensations;
            }
        });
    }
}
