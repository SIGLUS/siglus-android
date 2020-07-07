package org.openlmis.core.model.repository;

import android.content.Context;
import android.util.Log;

import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataColumn;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormBasicItem;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.model.ProgramDataFormSignature;
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
    private final GenericDao<ProgramDataFormBasicItem> ProgramDataFormBasicItemGenericDao;
    private final GenericDao<ProgramDataColumn> programDataColumnGenericDao;
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
        programDataColumnGenericDao = new GenericDao<>(ProgramDataColumn.class, context);
        ProgramDataFormBasicItemGenericDao = new GenericDao<>(ProgramDataFormBasicItem.class, context);
    }

    public void batchCreateOrUpdate(final ProgramDataForm form) throws SQLException {
        TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), (Callable<Void>) () -> {
            genericDao.createOrUpdate(form);
            Log.d("---|show items size|---", "" + form.getProgramDataFormItemListWrapper().size());
            saveFormItems(form);
            saveFormBasicItems(form);
            saveSignatures(form.getSignaturesWrapper());
            return null;
        });
    }

    private void saveSignatures(final List<ProgramDataFormSignature> signatures) throws LMISException {
        dbUtil.withDao(ProgramDataFormSignature.class, (DbUtil.Operation<ProgramDataFormSignature, Void>) dao -> {
            for (ProgramDataFormSignature signature : signatures) {
                dao.createOrUpdate(signature);
            }
            return null;
        });
    }

    public void batchSaveForms(final List<ProgramDataForm> programDataForms) {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (ProgramDataForm programDataForm : programDataForms) {
                        batchCreateOrUpdate(programDataForm);
                    }
                    return null;
                }
            });
        } catch (SQLException e) {
            new LMISException(e, "ProgramDataFormRepository.batchSaveForms").reportToFabric();
        }
    }

    private void saveFormItems(final ProgramDataForm form) throws LMISException {
        deleteFormItemsByFormId(form.getId());
        for (ProgramDataFormItem item : form.getProgramDataFormItemListWrapper()) {
            if (item.getProgramDataColumn().getId() == 0) {
                item.setProgramDataColumn(getProgramDataColumnByCode(item.getProgramDataColumn().getCode()));
            }
            programDataFormItemGenericDao.create(item);
        }
    }

    private void saveFormBasicItems(final ProgramDataForm form) throws LMISException {
        deleteFormBasicItems(form.getId());
        for (ProgramDataFormBasicItem item : form.getFormBasicItemListWrapper()) {
            ProgramDataFormBasicItemGenericDao.create(item);
        }
    }

    public ProgramDataColumn getProgramDataColumnByCode(final String columnCode) throws LMISException {
        return dbUtil.withDao(ProgramDataColumn.class, dao -> dao.queryBuilder().where().eq("code", columnCode).queryForFirst());
    }

    private void deleteFormBasicItems(final long formId) throws LMISException {
        dbUtil.withDao(ProgramDataFormBasicItem.class, (DbUtil.Operation<ProgramDataFormBasicItem, Void>) dao -> {
            DeleteBuilder<ProgramDataFormBasicItem, String> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("form_id", formId);
            deleteBuilder.delete();
            return null;
        });
    }

    private void deleteFormItemsByFormId(final long formId) throws LMISException {
        dbUtil.withDao(ProgramDataFormItem.class, (DbUtil.Operation<ProgramDataFormItem, Void>) dao -> {
            DeleteBuilder<ProgramDataFormItem, String> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("form_id", formId);
            deleteBuilder.delete();
            return null;
        });
    }

    public List<ProgramDataForm> listByProgramCode(String programCode) throws LMISException {
        final Program program = programRepository.queryByCode(programCode);
        if (program == null) {
            return Collections.emptyList();
        }

        return dbUtil.withDao(ProgramDataForm.class, dao -> dao.queryBuilder().where().eq("program_id", program.getId()).query());
    }

    public ProgramDataForm queryById(long formId) throws LMISException {
        return genericDao.getById(String.valueOf(formId));
    }

    public void delete(final ProgramDataForm programDataForm) throws SQLException {
        TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), (Callable<Void>) () -> {
            genericDao.delete(programDataForm);
            deleteFormItemsByFormId(programDataForm.getId());
            return null;
        });
    }

    public List<ProgramDataFormItem> listProgramDataItemsByFormId(final long formId) throws LMISException {
        return dbUtil.withDao(ProgramDataFormItem.class, dao -> dao.queryBuilder().where().eq("form_id", formId).query());
    }
}
