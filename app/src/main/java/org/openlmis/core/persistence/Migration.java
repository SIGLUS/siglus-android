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

import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class Migration {

    public static final String DIR_MIGRATION = "migrations";
    public static final String TAG = "Migration";

    protected SQLiteDatabase db;

    public abstract void up();

    public void setSQLiteDatabase(SQLiteDatabase db) {
        this.db = db;
    }

    protected void execSQL(String sql) {
        if (db != null) {
            Log.d("Migration", "exec sql :" + sql);
            db.execSQL(sql);
        }
    }

    protected void execSQLScript(String filename) {
        AssetManager manager = LMISApp.getContext().getResources().getAssets();

        String path = DIR_MIGRATION + File.separator + filename;
        db.beginTransaction();
        try {
            InputStream io = manager.open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(io));

            String line = reader.readLine();
            while (line != null) {
                String cmd = line.trim();
                if (!StringUtils.isEmpty(cmd)) {
                    execSQL(cmd);
                }
                line = reader.readLine();
            }
            reader.close();

            db.setTransactionSuccessful();
        } catch (IOException e) {
            new LMISException(e).reportToFabric();
            throw new RuntimeException("Invalid migration file :" + filename);
        } finally {
            db.endTransaction();
        }
    }
}
