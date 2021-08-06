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
import android.os.Build;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

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
import org.openlmis.core.persistence.migrations.CreateDraftInventoryTable;
import org.openlmis.core.persistence.migrations.CreateDraftLotMovementTable;
import org.openlmis.core.persistence.migrations.CreateDummyRegimes;
import org.openlmis.core.persistence.migrations.CreateInitTables;
import org.openlmis.core.persistence.migrations.CreateInitialInventoryDraftTables;
import org.openlmis.core.persistence.migrations.CreateKitProductsTable;
import org.openlmis.core.persistence.migrations.CreateMalariaTreatments;
import org.openlmis.core.persistence.migrations.CreatePTVProgramSchema;
import org.openlmis.core.persistence.migrations.CreatePatientDataReportTable;
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
import org.openlmis.core.persistence.migrations.CreateDirtyDataProductTable;
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
import org.openlmis.core.persistence.migrations.UpdateReportType;
import org.openlmis.core.persistence.migrations.UpdateStockCardProductType;
import org.openlmis.core.persistence.migrations.UpdateRegimenType;

import java.util.ArrayList;
import java.util.List;

public final class LmisSqliteOpenHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = LmisSqliteOpenHelper.class.getSimpleName();

    private static final List<Migration> MIGRATIONS = new ArrayList<Migration>() {
        {
            add(new CreateInitTables());
            add(new CreateDummyRegimes());
            add(new AddSignatureFieldInStockMovementItemTable());
            add(new AddFacilityIdToUser());
            add(new AddSyncTagToStockMovementItem());
            add(new ChangeMovementReasonToCode());
            add(new AddSubmittedDateToRnRForm());
            add(new SetQuantityOfStockMovementForInitialInventory());
            add(new CreateRnRFormSignature());
            add(new CreateDraftInventoryTable());
            add(new AddIsArchivedToProduct());
            add(new AddCreatedTimeToStockMovement());
            add(new AddSyncErrorsMessageTable());
            add(new AddActiveColumnToProductTable());
            add(new AddIsKitColumnToProduct());
            add(new CreateKitProductsTable());
            add(new UpdateProductsFalseValueToZero());
            add(new UpdateCreateTimeAndUpdateTime());
            add(new AddInventoryTable());
            add(new AddParentCodeToProgramTable());
            add(new UpdateRegimenType());
            add(new AddIsCustomColumnToRegime());
            add(new CreateRegimeShortCodeTable());
            add(new ChangeProgramTableName());
            add(new CreateProductProgramsTable());
            add(new AddIsEmergencyColumnToProgram());
            add(new AddNewPrograms());
            add(new ConvertEssMedsToVIAProgram());
            add(new AddEmergencyColumnToRnr());
            add(new AddCategoryColumnToProductPrograms());
            add(new AddLowStockAvgColumnToStockCardTable());
            add(new UpdateCategoryColumnForMMIAProducts());
            add(new AddCmmTable());
            add(new UpdateAvgColumn());
            add(new AddRequestedColumnToStockItems());
            add(new AddManualAddColumnToRnrFormItemsTable());
            add(new AddLotsTable());
            add(new AddLotMovementItemsTable());
            add(new AddLotOnHandTable());
            add(new CreateDraftLotMovementTable());
            add(new CreateProgramDataFormTable());
            add(new AddRapidTestProgram());
            add(new CreateProgramDataColumnsTable());
            add(new AddRapidTestColumnsTemplate());
            add(new CreateProgramDataItemsTable());
            add(new CreateProgramDataFormSignatureTable());
            add(new AddDoneColumnToDraftInventoryTable());
            add(new AddIsBasicColumnToProductsTable());
            add(new CreatePatientDataReportTable());
            add(new CreateMalariaTreatments());
            add(new ChangeMalariaTreatments());
            add(new CreatePTVProgramSchema());
            add(new AddMalariaSignature());
            add(new UpdateProgramDataFormTable());
            add(new UpdateStockCardProductType());
            add(new UpdateCustomRegimes());
            add(new AddIsHIVColumnToProductsTable());
            add(new AddALToRegimen());
            add(new CreateProgramBasicDataFormTable());
            add(new CreateReportTypeTable());
            add(new CreateServiceTable());
            add(new AddCustomAmountAndTotalToRnrFormTable());
            add(new AddServiceItemTable());
            add(new AddProgramToRegimen());
            add(new UpdateRapidTestColumnsTemplate());
            add(new UpdateRapidTestCode());
            add(new UpdateReportType());
            add(new AddRegimePharmacy());
            add(new AddRnrBaseInfoItem());
            add(new CreateRegimeThreeLineTable());
            add(new AddRegimeDisplayOrder());
            add(new UpdateRegimeShortCodeTable());
            add(new AddActiveToRegimes());
            add(new UpdateKitProductUnSynced());
            add(new CreateInitialInventoryDraftTables());
            add(new CreateDirtyDataProductTable());
            add(new AddVersionCodeToProgramProductTable());
            add(new AddLastReportEndTimeToReportType());
        }
    };
    private static int instanceCount = 0;
    private static LmisSqliteOpenHelper _helperInstance;

    private LmisSqliteOpenHelper(Context context) {
        super(context, "lmis_db", null, MIGRATIONS.size());
        ++instanceCount;
        Log.d(TAG, "Instance Created : total count : " + instanceCount);
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
        Log.d(TAG, "Instance Destroyed : total count : " + instanceCount);
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
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
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
        throw new RuntimeException("Unexpected downgrade happened, old=" + oldVersion + ",new=" + newVersion);
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

    public static int getDBVersion() {
        return MIGRATIONS.size();
    }
}
