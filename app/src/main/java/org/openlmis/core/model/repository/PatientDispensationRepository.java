package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PatientDispensation;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

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
        return dbUtil.withDao(PatientDispensation.class, dao -> {
            List<PatientDispensation> patientDispensations = dao.queryBuilder().where().eq("ptvProgramId", id).query();
            return patientDispensations;
        });
    }
}
