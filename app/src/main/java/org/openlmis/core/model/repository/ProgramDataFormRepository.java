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

import static org.openlmis.core.constant.FieldConstants.PERIOD_BEGIN;
import static org.openlmis.core.constant.FieldConstants.PROGRAM_CODE;
import static org.openlmis.core.constant.FieldConstants.PROGRAM_ID;
import static org.openlmis.core.utils.Constants.RAPID_TEST_PROGRAM_CODE;

import android.content.Context;
import android.database.Cursor;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

@SuppressWarnings("squid:S1874")
public class ProgramDataFormRepository {

  private static final String WHERE_PERIOD_END =
      "WHERE form_id IN (SELECT id FROM program_data_forms WHERE periodEnd < '";
  private static final String END_STRING = "' );";
  private final Context context;

  @Inject
  DbUtil dbUtil;

  @Inject
  ProgramRepository programRepository;

  @Inject
  public ProgramDataFormRepository(Context context) {
    this.context = context;
  }

  public List<ProgramDataForm> listByProgramCode(String programCode) throws LMISException {
    final Program program = programRepository.queryByCode(programCode);
    if (program == null) {
      return Collections.emptyList();
    }
    return dbUtil.withDao(ProgramDataForm.class,
        dao -> dao.queryBuilder()
            .orderBy(PERIOD_BEGIN, true)
            .where().eq(PROGRAM_ID, program.getId())
            .query());
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
            .getString(getProgramByProductCodeCursor.getColumnIndexOrThrow(PROGRAM_CODE))
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
      list = listByProgramCode(RAPID_TEST_PROGRAM_CODE);
    } catch (LMISException e) {
      new LMISException(e, "RnrFormRepository.hasOldDate").reportToFabric();
    }
    Date dueDateShouldDataLivedInDB = DateUtil.dateMinusMonth(DateUtil.getCurrentDate(),
        SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData());

    if (list != null && !list.isEmpty()) {
      for (ProgramDataForm programDataForm : list) {
        if (programDataForm.getPeriodEnd().before(dueDateShouldDataLivedInDB)) {
          return true;
        }
      }
    }
    return false;
  }

  public void deleteOldData() {
    String dueDateShouldDataLivedInDB = DateUtil.formatDate(DateUtil.dateMinusMonth(DateUtil.getCurrentDate(),
        SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData()),
        DateUtil.DB_DATE_FORMAT);

    String rawSqlDeleteProgramDataItems = "DELETE FROM program_data_items "
        + WHERE_PERIOD_END
        + dueDateShouldDataLivedInDB + END_STRING;
    String rawSqlDeleteSignatures = "DELETE FROM program_data_form_signatures "
        + WHERE_PERIOD_END
        + dueDateShouldDataLivedInDB + END_STRING;
    String rawSqlDeleteProgramBasicItems = "DELETE FROM program_data_Basic_items "
        + WHERE_PERIOD_END
        + dueDateShouldDataLivedInDB + END_STRING;
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
