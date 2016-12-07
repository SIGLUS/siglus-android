package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class ProgramDataFormRepository {

    private final GenericDao<ProgramDataForm> genericDao;
    private final GenericDao<ProgramDataFormItem> programDataFormItemGenericDao;
    private final Context context;

    @Inject
    DbUtil dbUtil;

    @Inject
    ProgramRepository programRepository;

    @Inject
    public ProgramDataFormRepository(Context context) {
        this.context = context;
        genericDao = new GenericDao<>(ProgramDataForm.class, context);
        programDataFormItemGenericDao = new GenericDao<>(ProgramDataFormItem.class, context);
    }

    public void batchCreateOrUpdate(final ProgramDataForm form) throws SQLException {
        TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                genericDao.createOrUpdate(form);
                saveFormItems(form);
                return null;
            }
        });
    }

    public void batchSaveForms(final List<ProgramDataForm> programDataForms) {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (ProgramDataForm programDataForm: programDataForms) {
                        batchCreateOrUpdate(programDataForm);
                    }
                    return null;
                }
            });
        } catch (SQLException e) {
            new LMISException(e).reportToFabric();
        }
    }
    
    private void saveFormItems(final ProgramDataForm form) throws LMISException {
        deleteFormItemsByFormId(form.getId());
        for (ProgramDataFormItem item : form.getProgramDataFormItemListWrapper()) {
            programDataFormItemGenericDao.create(item);
        }
    }

    private void deleteFormItemsByFormId(final long formId) throws LMISException {
        dbUtil.withDao(ProgramDataFormItem.class, new DbUtil.Operation<ProgramDataFormItem, Void>() {
            @Override
            public Void operate(Dao<ProgramDataFormItem, String> dao) throws SQLException, LMISException {
                DeleteBuilder<ProgramDataFormItem, String> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().eq("form_id", formId);
                deleteBuilder.delete();
                return null;
            }
        });
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

    public ProgramDataForm queryById(long formId) throws LMISException {
        return genericDao.getById(String.valueOf(formId));
    }

    public void delete(final ProgramDataForm programDataForm) throws LMISException, SQLException {
        TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                genericDao.delete(programDataForm);
                deleteFormItemsByFormId(programDataForm.getId());
                return null;
            }
        });
    }

    public List<ProgramDataFormItem> listProgramDataItemsByFormId(final long formId) throws LMISException {
        return dbUtil.withDao(ProgramDataFormItem.class, new DbUtil.Operation<ProgramDataFormItem, List<ProgramDataFormItem>>() {
            @Override
            public List<ProgramDataFormItem> operate(Dao<ProgramDataFormItem, String> dao) throws SQLException, LMISException {
                return dao.queryBuilder().where().eq("form_id", formId).query();
            }
        });
    }
}
