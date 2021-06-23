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

package org.openlmis.core.training;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import java.sql.SQLException;
import java.util.Date;
import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;


public final class TrainingSqliteOpenHelper extends OrmLiteSqliteOpenHelper {

  private static final Date TRAINING_ANCHOR_DATE = DateUtil
      .parseString("2017-02-14", DateUtil.DB_DATE_FORMAT);
  public static final String DATE_TIME_SUFFIX = ".000000";
  public static final String APP_ENVIRONMENT_TRAINING = "org.clintonhealthaccess.lmismoz.training";
  private final int monthOffsetFromAnchor;

  private DatabaseConnection dbConnection;

  private TrainingSqliteOpenHelper(Context context) {
    super(context, "lmis_db", null, LmisSqliteOpenHelper.getDBVersion());
    monthOffsetFromAnchor = DateUtil
        .calculateDateMonthOffset(TRAINING_ANCHOR_DATE, DateUtil.getCurrentDate());
  }

  @SuppressWarnings({"squid:S2095"})
  private synchronized void getConnection() throws SQLException {
    if (null == dbConnection) {
      dbConnection = new TrainingSqliteOpenHelper(LMISApp.getContext()).getConnectionSource()
          .getReadWriteConnection();
    }
  }

  @Override
  public void onOpen(SQLiteDatabase db) {
    super.onOpen(db);
    db.disableWriteAheadLogging();
  }

  @Override
  public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
    // do nothing
  }

  @Override
  public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion,
      int newVersion) {
    // do nothing
  }

  @Override
  public void close() {
    super.close();
    getWritableDatabase().close();
  }

  public void updateTimeInDB() throws SQLException {
    getConnection();
    updateLotExpirationDate();
    updateProgramDataFromPeriodsAndSubmitTime();
    updateRnRFormPeriods();
    updateStockMovementItemMovementDate();
  }

  private void updateStockMovementItemMovementDate() throws SQLException {
    String sql =
        "UPDATE stock_items SET movementDate = date(movementDate, '+" + monthOffsetFromAnchor
            + " months')";
    dbConnection.update(sql, null, null);
  }

  private void updateRnRFormPeriods() throws SQLException {
    String sql = "UPDATE rnr_forms "
        + "SET periodBegin = datetime(periodBegin, '+" + monthOffsetFromAnchor + " months') || "
        + DATE_TIME_SUFFIX + ","
        + "periodEnd = datetime(periodEnd, '+" + monthOffsetFromAnchor + " months') || "
        + DATE_TIME_SUFFIX;
    dbConnection.update(sql, null, null);
  }

  private void updateProgramDataFromPeriodsAndSubmitTime() throws SQLException {
    String sql = "UPDATE program_data_forms "
        + "SET periodBegin = datetime(periodBegin, '+" + monthOffsetFromAnchor + " months') || "
        + DATE_TIME_SUFFIX + ","
        + "periodEnd = datetime(periodEnd, '+" + monthOffsetFromAnchor + " months') ||"
        + DATE_TIME_SUFFIX;
    dbConnection.update(sql, null, null);
  }


  private void updateLotExpirationDate() throws SQLException {
    String sql = "UPDATE lots SET expirationDate = date(expirationDate, '+" + monthOffsetFromAnchor
        + " months')";
    dbConnection.update(sql, null, null);
  }

  public static TrainingSqliteOpenHelper getInstance(Context context) {
    return new TrainingSqliteOpenHelper(context);
  }
}
