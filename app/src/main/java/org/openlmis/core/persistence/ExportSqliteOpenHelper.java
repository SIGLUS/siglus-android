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

package org.openlmis.core.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import java.sql.SQLException;

public final class ExportSqliteOpenHelper extends OrmLiteSqliteOpenHelper {

  private ExportSqliteOpenHelper(Context context) {
    super(context, "lmis_copy", null, LmisSqliteOpenHelper.getDBVersion());
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

  public static void removePrivateUserInfo(Context context) throws SQLException {
    try (ExportSqliteOpenHelper exportSqliteOpenHelper = new ExportSqliteOpenHelper(context)) {
      String updateSQL = "UPDATE users "
          + "SET username = id , "
          + "password = '123456' ";

      exportSqliteOpenHelper.getConnectionSource().getReadWriteConnection()
          .update(updateSQL, null, null);
    }
  }
}
