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
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;


public final class TrainingSqliteOpenHelper extends OrmLiteSqliteOpenHelper {

  public static final String DATE_TIME_SUFFIX = ".000000";
  public static final String TIMEZONE_SUFFIX = "'T15:35:24.455Z'";
  public static final String MONTH_FIELD = " months') || ";
  public static final String MONTH = " months')";
  private static final Date TRAINING_ANCHOR_DATE = DateUtil.parseString("2021-07-18", DateUtil.DB_DATE_FORMAT);
  private int monthOffsetFromAnchor;
  private DatabaseConnection dbConnection;
  private static TrainingSqliteOpenHelper trainingSqliteOpenHelper;

  private TrainingSqliteOpenHelper(Context context) {
    super(context, "lmis_db", null, LmisSqliteOpenHelper.getDBVersion());
    monthOffsetFromAnchor = DateUtil.calculateDateMonthOffset(TRAINING_ANCHOR_DATE, DateUtil.getCurrentDate());
  }

  public static synchronized TrainingSqliteOpenHelper getInstance(Context context) {
    if (null == trainingSqliteOpenHelper) {
      trainingSqliteOpenHelper = new TrainingSqliteOpenHelper(context);
    }
    return trainingSqliteOpenHelper;
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
  public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
    // do nothing
  }

  @Override
  public void close() {
    super.close();
    getWritableDatabase().close();
    closeHelper();
  }

  public static void closeHelper() {
    trainingSqliteOpenHelper = null;
  }

  public void updateTimeInDB() throws SQLException {
    dbConnection = trainingSqliteOpenHelper.getConnectionSource().getReadWriteConnection();
    updateLotExpirationDate();
    updateProgramDataFromPeriodsAndSubmitTime();
    updateRnRFormPeriodsAndSubmittedTime();
    updateStockMovementItemMovementDate();
    updateStockMovementItemCreatedTime();
    updateInventory();
    updatePodReportPeriodAndShippedDate();
  }

  private void updateInventory() throws SQLException {
    String sql = "UPDATE inventory SET createdAt = datetime(createdAt, '+" + monthOffsetFromAnchor + " months'), "
        + "updatedAt = datetime(updatedAt, '+" + monthOffsetFromAnchor + MONTH;
    dbConnection.update(sql, null, null);
  }

  private void updateStockMovementItemMovementDate() throws SQLException {
    String sql = "UPDATE stock_items SET movementDate = date(movementDate, '+" + monthOffsetFromAnchor + MONTH;
    dbConnection.update(sql, null, null);
  }

  private void updateStockMovementItemCreatedTime() throws SQLException {
    String sql =
        "UPDATE stock_items SET createdTime = strftime('%Y-%m-%d %H:%M:%S:000',createdTime, '+" + monthOffsetFromAnchor
            + MONTH;
    dbConnection.update(sql, null, null);
  }

  private void updateRnRFormPeriodsAndSubmittedTime() throws SQLException {
    String sql = "UPDATE rnr_forms "
        + "SET periodBegin = datetime(periodBegin, '+" + monthOffsetFromAnchor + MONTH_FIELD + DATE_TIME_SUFFIX
        + ","
        + "periodEnd = datetime(periodEnd, '+" + monthOffsetFromAnchor + MONTH_FIELD + DATE_TIME_SUFFIX
        + ","
        + "submittedTime = dateTime(submittedTime, '+" + monthOffsetFromAnchor + MONTH_FIELD + DATE_TIME_SUFFIX;
    dbConnection.update(sql, null, null);
  }

  private void updateProgramDataFromPeriodsAndSubmitTime() throws SQLException {
    String sql = "UPDATE program_data_forms "
        + "SET periodBegin = datetime(periodBegin, '+" + monthOffsetFromAnchor + MONTH_FIELD + DATE_TIME_SUFFIX
        + ","
        + "periodEnd = datetime(periodEnd, '+" + monthOffsetFromAnchor + MONTH_FIELD + DATE_TIME_SUFFIX;
    dbConnection.update(sql, null, null);
  }

  private void updatePodReportPeriodAndShippedDate() throws SQLException {
    String sql = "UPDATE pods "
        + "SET shippedDate = date(shippedDate, '+" + monthOffsetFromAnchor + MONTH
        + ","
        + "requisitionActualStartDate = date(requisitionActualStartDate, '+" + monthOffsetFromAnchor + MONTH
        + ","
        + "requisitionActualEndDate = date(requisitionActualEndDate, '+" + monthOffsetFromAnchor + MONTH
        + ","
        + "receivedDate = date(receivedDate, '+" + monthOffsetFromAnchor + MONTH
        + ","
        + "processedDate = date(processedDate, '+" + monthOffsetFromAnchor + MONTH_FIELD + TIMEZONE_SUFFIX;
    dbConnection.update(sql, null, null);

  }

  private void updateLotExpirationDate() throws SQLException {
    String sql = "UPDATE lots SET expirationDate = date(expirationDate, '+" + monthOffsetFromAnchor + MONTH;
    dbConnection.update(sql, null, null);
  }
}
