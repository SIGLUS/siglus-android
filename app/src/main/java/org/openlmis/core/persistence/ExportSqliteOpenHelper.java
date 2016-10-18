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

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }

    public static void removePrivateUserInfo(Context context) throws SQLException {
        ExportSqliteOpenHelper exportSqliteOpenHelper = new ExportSqliteOpenHelper(context);

        String updateSQL = "UPDATE users "
                + "SET username = id , "
                + "password = '123456' ";

        exportSqliteOpenHelper.getConnectionSource().getReadWriteConnection().update(updateSQL, null, null);
    }
}
