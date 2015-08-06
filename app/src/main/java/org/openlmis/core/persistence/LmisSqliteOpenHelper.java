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
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

import org.openlmis.core.persistence.migrations.CreateDummyProducts;
import org.openlmis.core.persistence.migrations.CreateInitTables;

import java.util.ArrayList;
import java.util.List;

public final class LmisSqliteOpenHelper extends OrmLiteSqliteOpenHelper {

    private static final List<Migration> MIGRATIONS = new ArrayList<Migration>() {
        {
            add(new CreateInitTables());
            add(new CreateDummyProducts());
        }
    };
    private static int instanceCount = 0;
    private static LmisSqliteOpenHelper _helperInstance;

    private LmisSqliteOpenHelper(Context context) {
        super(context, "lmis_db", null, MIGRATIONS.size());
        ++instanceCount;
        Log.d("LmisSqliteOpenHelper", "Instance Created : total count : " + instanceCount);
    }

    public static synchronized LmisSqliteOpenHelper getInstance(Context context) {
        if (_helperInstance == null) {
            _helperInstance = new LmisSqliteOpenHelper(context);
        }
        return _helperInstance;
    }

    public static void closeHelper() {
        _helperInstance = null;
        --instanceCount;
        Log.d("LmisSqliteOpenHelper", "Instance Destroyed : total count : " + instanceCount);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        for (Migration migration : MIGRATIONS) {
            Log.i("DB Creation", "Upgrading migration [" + migration.getClass().getSimpleName() + "]");
            migration.up(database, connectionSource);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
//        for (int currentVersion = oldVersion; currentVersion < newVersion; currentVersion++) {
//            Migration migration = MIGRATIONS.get(currentVersion);
//            Log.i("DB Migration", "Upgrading migration [" + migration.getClass().getSimpleName() + "]");
//            migration.up(database, connectionSource);
//        }

        onCreate(database, connectionSource);
    }

    @Override
    public void onDowngrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        for (int currentVersion = newVersion - 1; currentVersion >= oldVersion; currentVersion--) {
            Migration migration = MIGRATIONS.get(currentVersion);
            Log.i("DB Migration", "Downgrading migration [" + migration.getClass().getSimpleName() + "]");
            migration.down(database, connectionSource);
        }
    }

    @Override
    public void close() {
        super.close();
        closeHelper();
    }
}
