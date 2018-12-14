package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.apache.commons.lang.ArrayUtils;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.utils.Constants;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
        dbUtil.withDaoAsBatch(ReportTypeForm.class, new DbUtil.Operation<ReportTypeForm, Void>() {
            @Override
            public Void operate(Dao<ReportTypeForm, String> dao) throws SQLException, LMISException {
                for (ReportTypeForm reportTypeForm : reportTypeFormList ){
                    createOrUpdate(reportTypeForm);
                }
                return null;
            }
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
        return dbUtil.withDao(ReportTypeForm.class, new DbUtil.Operation<ReportTypeForm, ReportTypeForm>() {
            @Override
            public ReportTypeForm operate(Dao<ReportTypeForm, String> dao) throws SQLException, LMISException {
                return dao.queryBuilder().where().eq("code", reportTypeCode).queryForFirst();
            }
        });
    }

    protected List<ReportTypeForm> listAll() throws LMISException {
        return genericDao.queryForAll();
    }

    public ReportTypeForm getReportType(final String programCode) throws LMISException {
        String reportTypeCode = getReportTypeCode(programCode);
        return queryByCode(reportTypeCode);
    }

    public String getReportTypeCode(final String programCode) {
        List<Constants.Program> programs =  Arrays.asList(Constants.Program.VIA_PROGRAM,
                Constants.Program.MMIA_PROGRAM,
                Constants.Program.AL_PROGRAM,
                Constants.Program.PTV_PROGRAM,
                Constants.Program.RAPID_TEST_PROGRAM);
        for (Constants.Program program : programs) {
            if (program.getCode().equals(programCode)) {
                return program.getReportType();
            }
        }
        return Constants.Program.VIA_PROGRAM.getReportType();
    }

}
