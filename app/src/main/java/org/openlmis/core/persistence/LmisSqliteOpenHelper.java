/*
 * Copyright (c) 2014, Thoughtworks Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
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

public class LMISSqliteOpenHelper extends OrmLiteSqliteOpenHelper {

    private static final List<Migration> MIGRATIONS = new ArrayList<Migration>() {
        {
            add(new CreateInitTables());
            add(new CreateDummyProducts());
        }
    };
    private static int instanceCount = 0;
    private static LMISSqliteOpenHelper _helperInstance;

    private LMISSqliteOpenHelper(Context context) {
        super(context, "lmis_db", null, MIGRATIONS.size());
        ++instanceCount;
        Log.d("LMISSqliteOpenHelper", "Instance Created : total count : " + instanceCount);
    }

    public static synchronized LMISSqliteOpenHelper getInstance(Context context) {
        if (_helperInstance == null) {
            _helperInstance = new LMISSqliteOpenHelper(context);
        }
        return _helperInstance;
    }

    public static void closeHelper() {
        _helperInstance = null;
        --instanceCount;
        Log.d("LMISSqliteOpenHelper", "Instance Destroyed : total count : " + instanceCount);
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
        for (int currentVersion = oldVersion; currentVersion < newVersion; currentVersion++) {
            Migration migration = MIGRATIONS.get(currentVersion);
            Log.i("DB Migration", "Upgrading migration [" + migration.getClass().getSimpleName() + "]");
            migration.up(database, connectionSource);
        }
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
