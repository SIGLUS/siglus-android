package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.joda.time.DateTime;
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
        if(!patientDataReport.isStatusComplete() && !patientDataReport.isStatusSynced()){
            final PatientDataReport existingPatientDataReport = getPatientDataReportByPeriodAndType(patientDataReport.getStartDatePeriod(), patientDataReport.getEndDatePeriod(), patientDataReport.getType());
            if (existingPatientDataReport == null){
                patientDataReport.setStatusMissing(Boolean.FALSE);
                patientDataReport.setStatusDraft(Boolean.TRUE);
                patientDataReport.setReportedDate(new DateTime());
                PatientDataReport patientDataReportSaved = genericDao.create(patientDataReport);
                return Optional.of(patientDataReportSaved);
            }else {
                patientDataReport.setId(existingPatientDataReport.getId());
                if(!patientDataReport.isStatusComplete()){
                    patientDataReport.setStatusMissing(Boolean.FALSE);
                    patientDataReport.setStatusDraft(Boolean.TRUE);
                }
                patientDataReport.setReportedDate(new DateTime());
                PatientDataReport patientDataReportModified = genericDao.createOrUpdate(patientDataReport);
                return Optional.of(patientDataReportModified);
            }
        }
        return  Optional.absent();
    }

     public PatientDataReport getPatientDataReportByPeriodAndType(final DateTime beginDate, final DateTime endDate, final String type) throws LMISException {
        return (PatientDataReport) dbUtil.withDao(PatientDataReport.class, new DbUtil.Operation<PatientDataReport, Object>() {
            @Override
            public PatientDataReport operate(Dao<PatientDataReport, String> dao) throws SQLException, LMISException {
                PatientDataReport queryPatientDataReport = dao.queryBuilder().where()
                        .eq("startDatePeriod", beginDate)
                        .and().eq("endDatePeriod", endDate)
                        .and().eq("type", type).queryForFirst();
                return queryPatientDataReport;
            }
        });
    }


    public List<PatientDataReport> getAllMovements() throws LMISException {
        return genericDao.queryForAll();
    }
}
