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
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.persistence.migrations.AddALToRegimen;
import org.openlmis.core.persistence.migrations.AddActiveColumnToProductTable;
import org.openlmis.core.persistence.migrations.AddActiveToRegimes;
import org.openlmis.core.persistence.migrations.AddCategoryColumnToProductPrograms;
import org.openlmis.core.persistence.migrations.AddCmmTable;
import org.openlmis.core.persistence.migrations.AddCreatedTimeToStockMovement;
import org.openlmis.core.persistence.migrations.AddCustomAmountAndTotalToRnrFormTable;
import org.openlmis.core.persistence.migrations.AddDoneColumnToDraftInventoryTable;
import org.openlmis.core.persistence.migrations.AddEmergencyColumnToRnr;
import org.openlmis.core.persistence.migrations.AddFacilityIdToUser;
import org.openlmis.core.persistence.migrations.AddInventoryTable;
import org.openlmis.core.persistence.migrations.AddIsArchivedToProduct;
import org.openlmis.core.persistence.migrations.AddIsBasicColumnToProductsTable;
import org.openlmis.core.persistence.migrations.AddIsCustomColumnToRegime;
import org.openlmis.core.persistence.migrations.AddIsEmergencyColumnToProgram;
import org.openlmis.core.persistence.migrations.AddIsHIVColumnToProductsTable;
import org.openlmis.core.persistence.migrations.AddIsKitColumnToProduct;
import org.openlmis.core.persistence.migrations.AddLastReportEndTimeToReportType;
import org.openlmis.core.persistence.migrations.AddLotMovementItemsReasonAndDocumentNumber;
import org.openlmis.core.persistence.migrations.AddLotMovementItemsTable;
import org.openlmis.core.persistence.migrations.AddLotOnHandTable;
import org.openlmis.core.persistence.migrations.AddLotsTable;
import org.openlmis.core.persistence.migrations.AddLowStockAvgColumnToStockCardTable;
import org.openlmis.core.persistence.migrations.AddMalariaSignature;
import org.openlmis.core.persistence.migrations.AddManualAddColumnToRnrFormItemsTable;
import org.openlmis.core.persistence.migrations.AddNewPrograms;
import org.openlmis.core.persistence.migrations.AddParentCodeToProgramTable;
import org.openlmis.core.persistence.migrations.AddProgramToRegimen;
import org.openlmis.core.persistence.migrations.AddRapidTestColumnsTemplate;
import org.openlmis.core.persistence.migrations.AddRapidTestProgram;
import org.openlmis.core.persistence.migrations.AddRegimeDisplayOrder;
import org.openlmis.core.persistence.migrations.AddRegimePharmacy;
import org.openlmis.core.persistence.migrations.AddRequestedColumnToStockItems;
import org.openlmis.core.persistence.migrations.AddRnrBaseInfoItem;
import org.openlmis.core.persistence.migrations.AddServiceItemTable;
import org.openlmis.core.persistence.migrations.AddSignatureFieldInStockMovementItemTable;
import org.openlmis.core.persistence.migrations.AddSubmittedDateToRnRForm;
import org.openlmis.core.persistence.migrations.AddSyncErrorsMessageTable;
import org.openlmis.core.persistence.migrations.AddSyncTagToStockMovementItem;
import org.openlmis.core.persistence.migrations.AddVersionCodeToProgramProductTable;
import org.openlmis.core.persistence.migrations.ChangeMalariaTreatments;
import org.openlmis.core.persistence.migrations.ChangeMovementReasonToCode;
import org.openlmis.core.persistence.migrations.ChangeProgramTableName;
import org.openlmis.core.persistence.migrations.ConvertEssMedsToVIAProgram;
import org.openlmis.core.persistence.migrations.CreateAdditionalProductProgramTable;
import org.openlmis.core.persistence.migrations.CreateBulkEntriesDraftTables;
import org.openlmis.core.persistence.migrations.CreateBulkIssueDraftTables;
import org.openlmis.core.persistence.migrations.CreateDirtyDataProductTable;
import org.openlmis.core.persistence.migrations.CreateDraftInventoryTable;
import org.openlmis.core.persistence.migrations.CreateDraftLotMovementTable;
import org.openlmis.core.persistence.migrations.CreateDummyRegimes;
import org.openlmis.core.persistence.migrations.CreateInitTables;
import org.openlmis.core.persistence.migrations.CreateInitialInventoryDraftTables;
import org.openlmis.core.persistence.migrations.CreateKitProductsTable;
import org.openlmis.core.persistence.migrations.CreateMalariaTreatments;
import org.openlmis.core.persistence.migrations.CreatePTVProgramSchema;
import org.openlmis.core.persistence.migrations.CreatePatientDataReportTable;
import org.openlmis.core.persistence.migrations.CreatePodProductItemTable;
import org.openlmis.core.persistence.migrations.CreatePodProductLotItemTable;
import org.openlmis.core.persistence.migrations.CreatePodTable;
import org.openlmis.core.persistence.migrations.CreateProductProgramsTable;
import org.openlmis.core.persistence.migrations.CreateProgramBasicDataFormTable;
import org.openlmis.core.persistence.migrations.CreateProgramDataColumnsTable;
import org.openlmis.core.persistence.migrations.CreateProgramDataFormSignatureTable;
import org.openlmis.core.persistence.migrations.CreateProgramDataFormTable;
import org.openlmis.core.persistence.migrations.CreateProgramDataItemsTable;
import org.openlmis.core.persistence.migrations.CreateRegimeShortCodeTable;
import org.openlmis.core.persistence.migrations.CreateRegimeThreeLineTable;
import org.openlmis.core.persistence.migrations.CreateReportTypeTable;
import org.openlmis.core.persistence.migrations.CreateRnRFormSignature;
import org.openlmis.core.persistence.migrations.CreateServiceTable;
import org.openlmis.core.persistence.migrations.CreateUsageColumnsMapTable;
import org.openlmis.core.persistence.migrations.CreateTestConsumptionLineItemsTable;
import org.openlmis.core.persistence.migrations.DeletePrograms;
import org.openlmis.core.persistence.migrations.DeleteReportTypes;
import org.openlmis.core.persistence.migrations.SetQuantityOfStockMovementForInitialInventory;
import org.openlmis.core.persistence.migrations.UpdateAvgColumn;
import org.openlmis.core.persistence.migrations.UpdateCategoryColumnForMMIAProducts;
import org.openlmis.core.persistence.migrations.UpdateCreateTimeAndUpdateTime;
import org.openlmis.core.persistence.migrations.UpdateCustomRegimes;
import org.openlmis.core.persistence.migrations.UpdateKitProductUnSynced;
import org.openlmis.core.persistence.migrations.UpdateProductsFalseValueToZero;
import org.openlmis.core.persistence.migrations.UpdateProgramDataFormTable;
import org.openlmis.core.persistence.migrations.UpdateRapidTestCode;
import org.openlmis.core.persistence.migrations.UpdateRapidTestColumnsTemplate;
import org.openlmis.core.persistence.migrations.UpdateRegimeShortCodeTable;
import org.openlmis.core.persistence.migrations.UpdateRegimenType;
import org.openlmis.core.persistence.migrations.UpdateReportType;
import org.openlmis.core.persistence.migrations.UpdateStockCardProductType;
import org.openlmis.core.persistence.migrations.UpdateStockCardSOHStatus;
import org.openlmis.core.persistence.migrations.UpdateUsageColumnsMap;


public final class LmisSqliteOpenHelper extends OrmLiteSqliteOpenHelper {

  private static final String TAG = LmisSqliteOpenHelper.class.getSimpleName();

  private static final List<Migration> MIGRATIONS;
  private static int instanceCount = 0;
  private static LmisSqliteOpenHelper helperInstance;

  static {
    MIGRATIONS = new ArrayList<>();
    MIGRATIONS.add(new CreateInitTables());
    MIGRATIONS.add(new CreateDummyRegimes());
    MIGRATIONS.add(new AddSignatureFieldInStockMovementItemTable());
    MIGRATIONS.add(new AddFacilityIdToUser());
    MIGRATIONS.add(new AddSyncTagToStockMovementItem());
    MIGRATIONS.add(new ChangeMovementReasonToCode());
    MIGRATIONS.add(new AddSubmittedDateToRnRForm());
    MIGRATIONS.add(new SetQuantityOfStockMovementForInitialInventory());
    MIGRATIONS.add(new CreateRnRFormSignature());
    MIGRATIONS.add(new CreateDraftInventoryTable());
    MIGRATIONS.add(new AddIsArchivedToProduct());
    MIGRATIONS.add(new AddCreatedTimeToStockMovement());
    MIGRATIONS.add(new AddSyncErrorsMessageTable());
    MIGRATIONS.add(new AddActiveColumnToProductTable());
    MIGRATIONS.add(new AddIsKitColumnToProduct());
    MIGRATIONS.add(new CreateKitProductsTable());
    MIGRATIONS.add(new UpdateProductsFalseValueToZero());
    MIGRATIONS.add(new UpdateCreateTimeAndUpdateTime());
    MIGRATIONS.add(new AddInventoryTable());
    MIGRATIONS.add(new AddParentCodeToProgramTable());
    MIGRATIONS.add(new UpdateRegimenType());
    MIGRATIONS.add(new AddIsCustomColumnToRegime());
    MIGRATIONS.add(new CreateRegimeShortCodeTable());
    MIGRATIONS.add(new ChangeProgramTableName());
    MIGRATIONS.add(new CreateProductProgramsTable());
    MIGRATIONS.add(new AddIsEmergencyColumnToProgram());
    MIGRATIONS.add(new AddNewPrograms());
    MIGRATIONS.add(new ConvertEssMedsToVIAProgram());
    MIGRATIONS.add(new AddEmergencyColumnToRnr());
    MIGRATIONS.add(new AddCategoryColumnToProductPrograms());
    MIGRATIONS.add(new AddLowStockAvgColumnToStockCardTable());
    MIGRATIONS.add(new UpdateCategoryColumnForMMIAProducts());
    MIGRATIONS.add(new AddCmmTable());
    MIGRATIONS.add(new UpdateAvgColumn());
    MIGRATIONS.add(new AddRequestedColumnToStockItems());
    MIGRATIONS.add(new AddManualAddColumnToRnrFormItemsTable());
    MIGRATIONS.add(new AddLotsTable());
    MIGRATIONS.add(new AddLotMovementItemsTable());
    MIGRATIONS.add(new AddLotOnHandTable());
    MIGRATIONS.add(new CreateDraftLotMovementTable());
    MIGRATIONS.add(new CreateProgramDataFormTable());
    MIGRATIONS.add(new AddRapidTestProgram());
    MIGRATIONS.add(new CreateProgramDataColumnsTable());
    MIGRATIONS.add(new AddRapidTestColumnsTemplate());
    MIGRATIONS.add(new CreateProgramDataItemsTable());
    MIGRATIONS.add(new CreateProgramDataFormSignatureTable());
    MIGRATIONS.add(new AddDoneColumnToDraftInventoryTable());
    MIGRATIONS.add(new AddIsBasicColumnToProductsTable());
    MIGRATIONS.add(new CreatePatientDataReportTable());
    MIGRATIONS.add(new CreateMalariaTreatments());
    MIGRATIONS.add(new ChangeMalariaTreatments());
    MIGRATIONS.add(new CreatePTVProgramSchema());
    MIGRATIONS.add(new AddMalariaSignature());
    MIGRATIONS.add(new UpdateProgramDataFormTable());
    MIGRATIONS.add(new UpdateStockCardProductType());
    MIGRATIONS.add(new UpdateCustomRegimes());
    MIGRATIONS.add(new AddIsHIVColumnToProductsTable());
    MIGRATIONS.add(new AddALToRegimen());
    MIGRATIONS.add(new CreateProgramBasicDataFormTable());
    MIGRATIONS.add(new CreateReportTypeTable());
    MIGRATIONS.add(new CreateServiceTable());
    MIGRATIONS.add(new AddCustomAmountAndTotalToRnrFormTable());
    MIGRATIONS.add(new AddServiceItemTable());
    MIGRATIONS.add(new AddProgramToRegimen());
    MIGRATIONS.add(new UpdateRapidTestColumnsTemplate());
    MIGRATIONS.add(new UpdateRapidTestCode());
    MIGRATIONS.add(new UpdateReportType());
    MIGRATIONS.add(new AddRegimePharmacy());
    MIGRATIONS.add(new AddRnrBaseInfoItem());
    MIGRATIONS.add(new CreateRegimeThreeLineTable());
    MIGRATIONS.add(new AddRegimeDisplayOrder());
    MIGRATIONS.add(new UpdateRegimeShortCodeTable());
    MIGRATIONS.add(new AddActiveToRegimes());
    MIGRATIONS.add(new UpdateKitProductUnSynced());
    MIGRATIONS.add(new CreateInitialInventoryDraftTables());
    MIGRATIONS.add(new CreateDirtyDataProductTable());
    MIGRATIONS.add(new AddVersionCodeToProgramProductTable());
    MIGRATIONS.add(new AddLastReportEndTimeToReportType());
    MIGRATIONS.add(new DeletePrograms());
    MIGRATIONS.add(new DeleteReportTypes());
    MIGRATIONS.add(new UpdateStockCardSOHStatus());
    MIGRATIONS.add(new AddLotMovementItemsReasonAndDocumentNumber());
    MIGRATIONS.add(new CreateBulkEntriesDraftTables());
    MIGRATIONS.add(new CreateBulkIssueDraftTables());
    MIGRATIONS.add(new CreatePodTable());
    MIGRATIONS.add(new CreatePodProductTable());
    MIGRATIONS.add(new CreatePodLotItemTable());
    MIGRATIONS.add(new CreateTestConsumptionLineItemsTable());
    MIGRATIONS.add(new CreateUsageColumnsMapTable());
    MIGRATIONS.add(new UpdateUsageColumnsMap());
    MIGRATIONS.add(new CreateAdditionalProductProgramTable());
  }

  private LmisSqliteOpenHelper(Context context) {
    super(context, "lmis_db", null, MIGRATIONS.size());
    ++instanceCount;
    Log.d(TAG, "Instance Created : total count : " + instanceCount);
  }

  public static synchronized LmisSqliteOpenHelper getInstance(Context context) {
    if (helperInstance == null) {
      helperInstance = new LmisSqliteOpenHelper(context);
    }
    return helperInstance;
  }

  public static void closeHelper() {
    helperInstance = null;
    --instanceCount;
    Log.d(TAG, "Instance Destroyed : total count : " + instanceCount);
  }

  public static int getDBVersion() {
    return MIGRATIONS.size();
  }

  @Override
  public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
    if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
      for (Migration migration : MIGRATIONS) {
        Log.i(TAG, "Upgrading migration [" + migration.getClass().getSimpleName() + "]");
        migration.setSQLiteDatabase(database);
        migration.up();
      }
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion,
      int newVersion) {
    Log.d(TAG, "onUpgrade oldVersion=" + oldVersion + ",newVersion=" + newVersion);
    for (int currentVersion = oldVersion; currentVersion < newVersion; currentVersion++) {
      Migration migration = MIGRATIONS.get(currentVersion);
      Log.i(TAG, "Upgrading migration [" + migration.getClass().getSimpleName() + "]");
      migration.setSQLiteDatabase(database);
      migration.up();
    }
  }

  @Override
  public void onDowngrade(SQLiteDatabase database, int oldVersion, int newVersion) {
    throw new SQLiteException(
        "Unexpected downgrade happened, old=" + oldVersion + ",new=" + newVersion);
  }

  @Override
  public void close() {
    super.close();
    getWritableDatabase().close();
    closeHelper();
  }

  @Override
  public void onOpen(SQLiteDatabase db) {
    super.onOpen(db);
    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      db.disableWriteAheadLogging();
    }
  }
}
