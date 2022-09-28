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

import static com.j256.ormlite.dao.DaoManager.createDao;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import java.sql.SQLException;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;

public class DbUtil {

  @Inject
  private Context context;

  public static <T> Dao<T, String> initialiseDao(Class<T> domainClass) throws SQLException {
    return initialiseDao(LmisSqliteOpenHelper.getInstance(LMISApp.getContext()), domainClass);
  }

  public static <T> Dao<T, String> initialiseDao(SQLiteOpenHelper openHelper, Class<T> domainClass)
      throws SQLException {
    ConnectionSource connectionSource = getConnectionSource(openHelper);
    return createDao(connectionSource, domainClass);
  }

  public static ConnectionSource getConnectionSource(SQLiteOpenHelper openHelper) {
    ConnectionSource connectionSource;
    if (openHelper instanceof LmisSqliteOpenHelper) {
      LmisSqliteOpenHelper helper = (LmisSqliteOpenHelper) openHelper;
      connectionSource = helper.getConnectionSource();
    } else {
      connectionSource = new AndroidConnectionSource(openHelper);
    }
    return connectionSource;
  }

  public <T, K> K withDao(Class<T> domainClass, Operation<T, K> operation) throws LMISException {
    SQLiteOpenHelper openHelper = LmisSqliteOpenHelper.getInstance(context);
    try {
      Dao<T, String> dao = initialiseDao(openHelper, domainClass);
      return operation.operate(dao);
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  public <T, K> K withDao(Context context, Class<T> domainClass, Operation<T, K> operation)
      throws LMISException {
    SQLiteOpenHelper openHelper = LmisSqliteOpenHelper.getInstance(context);
    try {
      Dao<T, String> dao = initialiseDao(openHelper, domainClass);
      return operation.operate(dao);
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  public <T, K> K withDaoAsBatch(Context context, Class<T> domainClass,
      final Operation<T, K> operation) throws LMISException {
    SQLiteOpenHelper openHelper = LmisSqliteOpenHelper.getInstance(context);
    try {
      final Dao<T, String> dao = initialiseDao(openHelper, domainClass);
      return dao.callBatchTasks(() -> operation.operate(dao));
    } catch (Exception e) {
      throw new LMISException(e);
    }
  }

  public <T, K> K withDaoAsBatch(Class<T> domainClass, final Operation<T, K> operation)
      throws LMISException {
    SQLiteOpenHelper openHelper = LmisSqliteOpenHelper.getInstance(context);
    try {
      final Dao<T, String> dao = initialiseDao(openHelper, domainClass);
      return dao.callBatchTasks(() -> operation.operate(dao));
    } catch (Exception e) {
      throw new LMISException(e);
    }
  }

  public interface Operation<T, K> {

    K operate(Dao<T, String> dao) throws SQLException, LMISException;
  }
}
