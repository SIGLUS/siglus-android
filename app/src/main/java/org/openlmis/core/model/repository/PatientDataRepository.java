package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PatientDataReport;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.roboguice.shaded.goole.common.base.Optional;

import java.sql.SQLException;
import java.util.List;

public class PatientDataRepository {

    @Inject
    DbUtil dbUtil;

    private GenericDao<PatientDataReport> genericDao;

    @Inject
    public PatientDataRepository(Context context) {
        this.genericDao = new GenericDao<>(PatientDataReport.class, context);
    }

    public Optional<PatientDataReport> getFirstMovement() throws LMISException {
        PatientDataReport patientDataReport = (PatientDataReport) dbUtil.withDao(PatientDataReport.class, new DbUtil.Operation<PatientDataReport, Object>() {
            @Override
            public PatientDataReport operate(Dao<PatientDataReport, String> dao) throws SQLException, LMISException {
                PatientDataReport patientDataReport = dao.queryBuilder().orderBy("reportedDate", true).queryForFirst();
                return patientDataReport;
            }
        });
        if (patientDataReport != null) {
            return Optional.of(patientDataReport);
        }
        return Optional.absent();
    }

    public Optional<PatientDataReport> saveMovement(final PatientDataReport patientDataReport) throws LMISException {
        if (patientDataReport.isStatusMissing()) {
            final PatientDataReport existingPatientDataReport = (PatientDataReport) dbUtil.withDao(PatientDataReport.class, new DbUtil.Operation<PatientDataReport, Object>() {
                @Override
                public PatientDataReport operate(Dao<PatientDataReport, String> dao) throws SQLException, LMISException {
                    PatientDataReport queryPatientDataReport = dao.queryBuilder().where()
                            .eq("startDatePeriod", patientDataReport.getStartDatePeriod())
                            .and().eq("endDatePeriod", patientDataReport.getEndDatePeriod())
                            .and().eq("type", patientDataReport.getType()).queryForFirst();
                    return queryPatientDataReport;
                }
            });
            if (existingPatientDataReport == null){
                patientDataReport.setStatusMissing(Boolean.FALSE);
                patientDataReport.setStatusDraft(Boolean.TRUE);
                PatientDataReport patientDataReportSaved = genericDao.create(patientDataReport);
                return Optional.of(patientDataReportSaved);
            }
        }
        return Optional.absent();
    }

    public List<PatientDataReport> getAllMovements() throws LMISException {
        return genericDao.queryForAll();
    }
}
