package org.openlmis.core.training;

import android.content.res.AssetManager;
import android.os.Environment;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class TrainingEnvironmentHelper {
    private static TrainingEnvironmentHelper helper;

    public static TrainingEnvironmentHelper getInstance() {
        if (helper == null) {
            helper = new TrainingEnvironmentHelper();
        }
        return helper;
    }

    public void setUpData() {
        setUpDataForTrainingEnvironment();
        setSyncedForTrainingEnvironment();
    }


    private void setSyncedForTrainingEnvironment() {
        SharedPreferenceMgr.getInstance().setLastSyncProductTime(String.valueOf(LMISApp.getInstance().getCurrentTimeMillis()));
        SharedPreferenceMgr.getInstance().setLastMonthStockCardDataSynced(true);
        SharedPreferenceMgr.getInstance().setRequisitionDataSynced(true);
        SharedPreferenceMgr.getInstance().setRapidTestsDataSynced(true);
        SharedPreferenceMgr.getInstance().setRnrLastSyncTime();
        SharedPreferenceMgr.getInstance().setShouldSyncLastYearStockCardData(false);
        SharedPreferenceMgr.getInstance().setKeyHasCopiedTrainingDb(true);
    }

    private void setUpDataForTrainingEnvironment() {
        File currentDB = new File(Environment.getDataDirectory(), "//data//" + LMISApp.getContext().getApplicationContext().getPackageName() + "//databases//lmis_db");
        try {
            AssetManager assetManager = LMISApp.getContext().getAssets();
            InputStream inputStream = assetManager.open("lmis_training.db");
            FileUtil.copyInputStreamToFile(inputStream, currentDB);
            TrainingSqliteOpenHelper.getInstance(LMISApp.getContext()).updateTimeInDB();
            SharedPreferenceMgr.getInstance().setKeyHasCopiedTrainingDb(true);
        } catch (IOException | SQLException e) {
            new LMISException(e).reportToFabric();
        }
    }
}
