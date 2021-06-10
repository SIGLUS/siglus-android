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

import android.content.res.AssetManager;
import android.os.Environment;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.utils.FileUtil;

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
    SharedPreferenceMgr.getInstance()
        .setLastSyncProductTime(String.valueOf(LMISApp.getInstance().getCurrentTimeMillis()));
    SharedPreferenceMgr.getInstance().setLastMonthStockCardDataSynced(true);
    SharedPreferenceMgr.getInstance().setRequisitionDataSynced(true);
    SharedPreferenceMgr.getInstance().setRapidTestsDataSynced(true);
    SharedPreferenceMgr.getInstance().setRnrLastSyncTime();
    SharedPreferenceMgr.getInstance().setShouldSyncLastYearStockCardData(false);
    SharedPreferenceMgr.getInstance().setKeyHasCopiedTrainingDb(true);
  }

  private void setUpDataForTrainingEnvironment() {
    File currentDB = new File(Environment.getDataDirectory(),
        "//data//" + LMISApp.getContext().getApplicationContext().getPackageName()
            + "//databases//lmis_db");
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
