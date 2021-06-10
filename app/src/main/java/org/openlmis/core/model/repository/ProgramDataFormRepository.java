/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.model.repository;

import static org.openlmis.core.utils.Constants.RAPID_TEST_CODE;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataColumn;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormBasicItem;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.model.ProgramDataFormSignature;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

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
    TransactionManager
        .callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
            (Callable<Void>) () -> {
              genericDao.createOrUpdate(form);
              Log.d("---|show items size|---",
                  "" + form.getProgramDataFormItemListWrapper().size());
              saveFormItems(form);
              Log.d("---|show items size|---", "" + form.getFormBasicItemListWrapper().size());
              saveFormBasicItems(form);
              saveSignatures(form.getSignaturesWrapper());
              return null;
            });
  }

  private void saveSignatures(final List<ProgramDataFormSignature> signatures)
      throws LMISException {
    dbUtil.withDao(ProgramDataFormSignature.class,
        (DbUtil.Operation<ProgramDataFormSignature, Void>) dao -> {
          for (ProgramDataFormSignature signature : signatures) {
            dao.createOrUpdate(signature);
          }
          return null;
        });
  }

  public void batchSaveForms(final List<ProgramDataForm> programDataForms) {
    try {
      TransactionManager
          .callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
              new Callable<Void>() {
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
        item.setProgramDataColumn(
            getProgramDataColumnByCode(item.getProgramDataColumn().getCode()));
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

  public ProgramDataColumn getProgramDataColumnByCode(final String columnCode)
      throws LMISException {
    return dbUtil.withDao(ProgramDataColumn.class,
        dao -> dao.queryBuilder().where().eq("code", columnCode).queryForFirst());
  }

  private void deleteFormBasicItems(final long formId) throws LMISException {
    dbUtil.withDao(ProgramDataFormBasicItem.class,
        (DbUtil.Operation<ProgramDataFormBasicItem, Void>) dao -> {
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

    return dbUtil.withDao(ProgramDataForm.class,
        dao -> dao.queryBuilder().orderBy("periodBegin", true).where()
            .eq("program_id", program.getId()).query());
  }

  public ProgramDataForm queryById(long formId) throws LMISException {
    return genericDao.getById(String.valueOf(formId));
  }

  public void delete(final ProgramDataForm programDataForm) throws SQLException {
    TransactionManager
        .callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
            (Callable<Void>) () -> {
              genericDao.delete(programDataForm);
              deleteFormItemsByFormId(programDataForm.getId());
              return null;
            });
  }

  public List<ProgramDataFormItem> listProgramDataItemsByFormId(final long formId)
      throws LMISException {
    return dbUtil.withDao(ProgramDataFormItem.class,
        dao -> dao.queryBuilder().where().eq("form_id", formId).query());
  }

  public void deleteProgramDirtyData(List<String> productCodeList) {
    String deleteProgramDataItems = "DELETE FROM program_data_items "
        + "WHERE form_id=(SELECT id FROM program_data_forms WHERE synced=0);";
    String deleteProgramDataFromSignatures = "DELETE FROM program_data_form_signatures "
        + "WHERE form_id=(SELECT id FROM program_data_forms WHERE synced=0);";
    String deleteProgramDataBasicItems = "DELETE FROM program_data_Basic_items";
    String deleteProgramDataForms = "DELETE FROM program_data_forms WHERE synced=0;";
    Cursor getProgramByProductCodeCursor = null;
    String getProgramByProductCode = null;
    for (String productCode : productCodeList) {
      getProgramByProductCode = "SELECT * FROM programs "
          + "WHERE programCode IN (SELECT programCode FROM product_programs WHERE productCode='"
          + productCode + "');";
      getProgramByProductCodeCursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
          .getWritableDatabase().rawQuery(getProgramByProductCode, null);
      while (getProgramByProductCodeCursor.moveToNext()) {
        if (getProgramByProductCodeCursor
            .getString(getProgramByProductCodeCursor.getColumnIndexOrThrow("programCode"))
            .equals(Constants.RAPID_TEST_OLD_CODE)) {
          LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
              .execSQL(deleteProgramDataItems);
          LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
              .execSQL(deleteProgramDataFromSignatures);
          LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
              .execSQL(deleteProgramDataBasicItems);
          LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
              .execSQL(deleteProgramDataForms);
        }
      }
    }
    if (getProgramByProductCodeCursor != null && !getProgramByProductCodeCursor.isClosed()) {
      getProgramByProductCodeCursor.close();
    }
  }

  public boolean hasOldDate() {
    List<ProgramDataForm> list = null;
    try {
      list = listByProgramCode(RAPID_TEST_CODE);
    } catch (LMISException e) {
      new LMISException(e, "RnrFormRepository.hasOldDate").reportToFabric();
    }
    Date dueDateShouldDataLivedInDB = DateUtil.dateMinusMonth(DateUtil.getCurrentDate(),
        SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData());

    if (list != null && list.size() > 0) {
      for (ProgramDataForm programDataForm : list) {
        if (programDataForm.getPeriodEnd().before(dueDateShouldDataLivedInDB)) {
          return true;
        }
      }
    }
    return false;
  }

  public void deleteOldData() {
    String dueDateShouldDataLivedInDB = DateUtil
        .formatDate(DateUtil.dateMinusMonth(DateUtil.getCurrentDate(),
            SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData()),
            DateUtil.DB_DATE_FORMAT);

    String rawSqlDeleteProgramDataItems = "DELETE FROM program_data_items "
        + "WHERE form_id IN (SELECT id FROM program_data_forms WHERE periodEnd < '"
        + dueDateShouldDataLivedInDB + "' );";
    String rawSqlDeleteSignatures = "DELETE FROM program_data_form_signatures "
        + "WHERE form_id IN (SELECT id FROM program_data_forms WHERE periodEnd < '"
        + dueDateShouldDataLivedInDB + "' );";
    String rawSqlDeleteProgramBasicItems = "DELETE FROM program_data_Basic_items "
        + "WHERE form_id IN (SELECT id FROM program_data_forms WHERE periodEnd < '"
        + dueDateShouldDataLivedInDB + "' );";
    String rawSqlDeleteProgramDataForms = "DELETE FROM program_data_forms "
        + "WHERE periodEnd < '" + dueDateShouldDataLivedInDB + "'; ";

    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(rawSqlDeleteProgramDataItems);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(rawSqlDeleteProgramBasicItems);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(rawSqlDeleteSignatures);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(rawSqlDeleteProgramDataForms);
  }
}
