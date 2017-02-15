package org.openlmis.core.training;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;

import java.sql.SQLException;
import java.util.Date;

public class TrainingSqliteOpenHelper extends OrmLiteSqliteOpenHelper {
    private static final Date trainingAnchorDate = DateUtil.parseString("2017-02-14", DateUtil.DB_DATE_FORMAT);
    public static final String DATE_TIME_SUFFIX = ".000000";
    private final int monthOffsetFromAnchor;

    private DatabaseConnection dbConnection;

    private TrainingSqliteOpenHelper(Context context) {
        super(context, "lmis_db", null, LmisSqliteOpenHelper.getDBVersion());
        monthOffsetFromAnchor = DateUtil.calculateDateMonthOffset(trainingAnchorDate, new Date());
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }

    public void updateTimeInDB() throws SQLException {
        dbConnection = new TrainingSqliteOpenHelper(LMISApp.getContext()).getConnectionSource().getReadWriteConnection();
        updateLotExpirationDate();
        updateProgramDataFromPeriodsAndSubmitTime();
        updateRnRFormPeriods();
        updateStockMovementItemMovementDate();
    }

    private void updateStockMovementItemMovementDate() throws SQLException {
        String sql = "UPDATE stock_items SET movementDate = date(movementDate, '+" + monthOffsetFromAnchor + " months')";
        dbConnection.update(sql, null, null);
    }

    private void updateRnRFormPeriods() throws SQLException {
        String sql = "UPDATE rnr_forms "
                + "SET periodBegin = datetime(periodBegin, '+" + monthOffsetFromAnchor + " months') || " + DATE_TIME_SUFFIX + ","
                + "periodEnd = datetime(periodEnd, '+" + monthOffsetFromAnchor + " months') || " + DATE_TIME_SUFFIX;
        dbConnection.update(sql, null, null);
    }

    private void updateProgramDataFromPeriodsAndSubmitTime() throws SQLException {
        String sql = "UPDATE program_data_forms "
                + "SET submittedTime = datetime(submittedTime, '+" + monthOffsetFromAnchor + " months') || " + DATE_TIME_SUFFIX + ","
                + "periodBegin = datetime(periodBegin, '+" + monthOffsetFromAnchor + " months') || " + DATE_TIME_SUFFIX + ","
                + "periodEnd = datetime(periodEnd, '+" + monthOffsetFromAnchor + " months') ||" + DATE_TIME_SUFFIX;
        dbConnection.update(sql, null, null);
    }


    private void updateLotExpirationDate() throws SQLException {
        String sql = "UPDATE lots SET expirationDate = date(expirationDate, '+" + monthOffsetFromAnchor + " months')";
        dbConnection.update(sql, null, null);
    }

    public static TrainingSqliteOpenHelper getInstance(Context context) {
        return new TrainingSqliteOpenHelper(context);
    }
}
