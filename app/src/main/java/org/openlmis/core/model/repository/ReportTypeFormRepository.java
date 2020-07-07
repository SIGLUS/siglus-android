package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.utils.Constants;

import java.util.List;

public class ReportTypeFormRepository {

    GenericDao<ReportTypeForm> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    public ReportTypeFormRepository(Context context) {
        genericDao = new GenericDao<>(ReportTypeForm.class, context);
    }

    public void batchCreateOrUpdateReportTypes(final List<ReportTypeForm> reportTypeFormList) throws LMISException {
        dbUtil.withDaoAsBatch(ReportTypeForm.class, (DbUtil.Operation<ReportTypeForm, Void>) dao -> {
            for (ReportTypeForm reportTypeForm : reportTypeFormList) {
                createOrUpdate(reportTypeForm);
            }
            return null;
        });
    }

    public void createOrUpdate(ReportTypeForm reportTypeForm) throws LMISException {
        ReportTypeForm existingReportType = queryByCode(reportTypeForm.getCode());
        if (existingReportType == null) {
            genericDao.create(reportTypeForm);
        } else {
            reportTypeForm.setId(existingReportType.getId());
            genericDao.update(reportTypeForm);
        }
    }

    public ReportTypeForm queryByCode(final String reportTypeCode) throws LMISException {
        return dbUtil.withDao(ReportTypeForm.class, dao -> dao.queryBuilder().where().eq("code", reportTypeCode).queryForFirst());
    }

    public List<ReportTypeForm> listAll() throws LMISException {
        return genericDao.queryForAll();
    }

    public ReportTypeForm getReportType(final String programCode) throws LMISException {
        String reportTypeCode = getReportTypeCode(programCode);
        return queryByCode(reportTypeCode);
    }

    public String getReportTypeCode(final String programCode) {
        for (Constants.Program program : Constants.PROGRAMES) {
            if (program.getCode().equals(programCode)) {
                return program.getReportType();
            }
        }
        return Constants.Program.VIA_PROGRAM.getReportType();
    }

}
