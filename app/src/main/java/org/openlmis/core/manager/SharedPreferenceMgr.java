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

package org.openlmis.core.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import roboguice.RoboGuice;

@Singleton
public class SharedPreferenceMgr {

    private static SharedPreferenceMgr self;
    public static final String MY_PREFERENCE = "LMISPreference";
    SharedPreferences sharedPreferences;

    private static final String KEY_LAST_SYNCED_TIME_RNR_FORM = "lastSyncedDate";
    private static final String KEY_LAST_SYNCED_TIME_STOCKCARD = "lastSyncedDateStockCard";
    public static final String KEY_LAST_LOGIN_USER = "last_user";
    public static final String KEY_USER_FACILITY = "user_facility";
    public static final String KEY_USER_FACILITY_ID = "user_facility_id";
    private static final String KEY_NEEDS_INVENTORY = "init_inventory";
    private static final String KEY_HAS_SYNCED_LATEST_MONTH_STOCKMOVEMENTS = "has_get_month_stock_cards_synced";
    private static final String KEY_SHOULD_SYNC_LAST_YEAR = "should_sync_last_year";
    private static final String KEY_IS_SYNCING_LAST_YEAR = "is_syncing_last_year";
    private static final String KEY_IS_REQUISITION_DATA_SYNCED = "is_requisition_data_synced";
    public static final String KEY_STOCK_SYNC_END_TIME = "sync_stock_end_time";
    public static final String KEY_STOCK_SYNC_CURRENT_INDEX = "sync_stock_current_index";
    public static final String KEY_LAST_SYNC_PRODUCT_TIME = "last_sync_product_time";
    public static final String KEY_LAST_SYNC_SERVICE_TIME = "last_sync_service_time";
    public static final String KEY_SHOW_PRODUCT_UPDATE_BANNER = "show_product_update_banner";
    public static final String KEY_PRODUCT_UPDATE_BANNER_TEXT = "product_update_banner_text";
    public static final String LATEST_PHYSICAL_INVENTORY_TIME = "latest_physical_inventory_time";
    public static final String LAST_MOVEMENT_HANDSHAKE_DATE = "last_movement_handshake_date";
    public static final String KEY_ENABLE_QA_DEBUG = "enable_qa_debug";
    public static final String LATEST_UPDATE_LOW_STOCK_AVG_TIME = "latest_update_low_stock_avg_time";
    public static final String LATEST_MONTHLY_CHECK_DIRTY_DATA_TIME = "last_monthly_check_dirty_data_time";
    public static final String KEY_HAS_LOT_INFO = "has_lot_info";
    public static final String KEY_HAS_DELETED_OLD_STOCK_MOVEMENT = "has_deleted_old_stock_movement";
    public static final String KEY_HAS_DELETED_OLD_RNR = "has_deleted_old_rnr";
    public static final String KEY_HAS_SYNCED_DOWN_RAPID_TESTS = "syncedRapidTests";
    public static final String LATEST_SYNCED_DOWN_REPORT_TYPE = "syncedReport";
    public static final String MONTH_OFFSET_DEFINED_OLD_DATA = "month_offset_that_defined_old_data";
    public static final String KEY_STOCK_CARD_LAST_YEAR_SYNC_ERROR = "stock_card_last_year_sync_error";
    public static final String KEY_STOCK_MOVENENT_SYNC_ERROR = "stock_movement_sync_error";
    public static final String KEY_HAS_COPIED_TRAINING_DB = "has_copied_training_db";
    public static final String KEY_LAST_UPDATE = "last_update";
    public static final String KEY_MD5_TIME = "md5_time";
    public static final String KEY_MD5_KEY = "md5";
    public static final String KEY_UPDATE_FILE = "updateFile";
    public static final String KEY_SILENT_FAILED = "silent_failed";
    public static final String KEY_DOWNLOADED_LATEST_VERSIONCODE = "downloaded_latest_versioncode";
    public static final String KEY_DELETED_THREE_PRODUCT = "deleted_three_products";
    public static final String KEY_DELETED_MOVEMENT_ITEM = "deleted_movement_line";
    public static final String KEY_DELETED_PRODUCT_TIME = "deleted_product_time";
    public static final String KEY_KEEP_MOVEMENT_LINE = "keep_movement_line";

    final int MONTH_OFFSET = 13;
    protected StockRepository stockRepository;

    @Inject
    public SharedPreferenceMgr(Context context) {
        sharedPreferences = context.getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE);
        stockRepository = RoboGuice.getInjector(context).getInstance(StockRepository.class);
        self = this;
    }

    public static SharedPreferenceMgr getInstance() {
        return self;
    }

    public SharedPreferences getPreference() {
        return sharedPreferences;
    }

    public boolean hasSyncedVersion() {
        return sharedPreferences.getBoolean(UserInfoMgr.getInstance().getVersion(), false);
    }

    public void setSyncedVersion(boolean hasUpdated) {
        sharedPreferences.edit().putBoolean(UserInfoMgr.getInstance().getVersion(), hasUpdated).apply();
    }

    public boolean isLastMonthStockDataSynced() {
        return sharedPreferences.getBoolean(SharedPreferenceMgr.KEY_HAS_SYNCED_LATEST_MONTH_STOCKMOVEMENTS, stockRepository.hasStockData());
    }

    public void setLastMonthStockCardDataSynced(boolean isStockCardSynced) {
        sharedPreferences.edit().putBoolean(SharedPreferenceMgr.KEY_HAS_SYNCED_LATEST_MONTH_STOCKMOVEMENTS, isStockCardSynced).apply();
    }

    public boolean shouldSyncLastYearStockData() {
        return sharedPreferences.getBoolean(SharedPreferenceMgr.KEY_SHOULD_SYNC_LAST_YEAR, true);
    }

    public void setShouldSyncLastYearStockCardData(boolean shouldSyncLastYearStockCardData) {
        sharedPreferences.edit().putBoolean(SharedPreferenceMgr.KEY_SHOULD_SYNC_LAST_YEAR, shouldSyncLastYearStockCardData).apply();
    }

    public boolean isSyncingLastYearStockCards() {
        return sharedPreferences.getBoolean(SharedPreferenceMgr.KEY_IS_SYNCING_LAST_YEAR, false);
    }

    public void setIsSyncingLastYearStockCards(boolean isSyncingLastYearStockCards) {
        sharedPreferences.edit().putBoolean(SharedPreferenceMgr.KEY_IS_SYNCING_LAST_YEAR, isSyncingLastYearStockCards).apply();
    }

    public boolean isRequisitionDataSynced() {
        RnrFormRepository rnrFormRepository = RoboGuice.getInjector(LMISApp.getContext()).getInstance(RnrFormRepository.class);
        return sharedPreferences.getBoolean(SharedPreferenceMgr.KEY_IS_REQUISITION_DATA_SYNCED, rnrFormRepository.hasRequisitionData());
    }

    public boolean hasDeletedOldStockMovement() {
        return sharedPreferences.getBoolean(SharedPreferenceMgr.KEY_HAS_DELETED_OLD_STOCK_MOVEMENT, false);
    }

    public boolean hasDeletedOldRnr() {
        return sharedPreferences.getBoolean(SharedPreferenceMgr.KEY_HAS_DELETED_OLD_RNR, false);
    }

    public void setRequisitionDataSynced(boolean requisitionDataSynced) {
        sharedPreferences.edit().putBoolean(SharedPreferenceMgr.KEY_IS_REQUISITION_DATA_SYNCED, requisitionDataSynced).apply();
    }

    public void setReportTypesData(List<ReportTypeForm> reportTypeFormList) {
        Gson gson = new GsonBuilder().setDateFormat(DateUtil.DATE_TIME_FORMAT).create();
        String json = gson.toJson(reportTypeFormList);

        sharedPreferences.edit().putString(SharedPreferenceMgr.LATEST_SYNCED_DOWN_REPORT_TYPE, json).apply();
    }

    public List<ReportTypeForm> getReportTypesData() {
        String json = sharedPreferences.getString(SharedPreferenceMgr.LATEST_SYNCED_DOWN_REPORT_TYPE, null);
        if (json != null) {
            Gson gson = new GsonBuilder().setDateFormat(DateUtil.DATE_TIME_FORMAT).create();
            Type type = new TypeToken<List<ReportTypeForm>>() {
            }.getType();
            return gson.fromJson(json, type);
        }
        return null;

    }

    public boolean isNeedsInventory() {
        return sharedPreferences.getBoolean(SharedPreferenceMgr.KEY_NEEDS_INVENTORY, true);
    }

    public String getLastLoginUser() {
        return sharedPreferences.getString(SharedPreferenceMgr.KEY_LAST_LOGIN_USER, StringUtils.EMPTY);
    }

    public void setLastLoginUser(String lastLoginUser) {
        sharedPreferences.edit().putString(SharedPreferenceMgr.KEY_LAST_LOGIN_USER, lastLoginUser).apply();
    }

    public String getCurrentUserFacility() {
        return sharedPreferences.getString(SharedPreferenceMgr.KEY_USER_FACILITY, StringUtils.EMPTY);
    }

    public void setCurrentUserFacility(String userFacility) {
        sharedPreferences.edit().putString(SharedPreferenceMgr.KEY_USER_FACILITY, userFacility).apply();
    }

    public void setIsNeedsInventory(boolean isNeedsInventory) {
        sharedPreferences.edit().putBoolean(SharedPreferenceMgr.KEY_NEEDS_INVENTORY, isNeedsInventory).apply();
    }

    public String getLastSyncServiceTime() {
        return sharedPreferences.getString(KEY_LAST_SYNC_SERVICE_TIME, null);
    }

    public void setKeyLastSyncServiceTime(String lastSyncProductTime) {
        sharedPreferences.edit().putString(KEY_LAST_SYNC_SERVICE_TIME, lastSyncProductTime).apply();
    }

    public String getLastSyncProductTime() {
        return sharedPreferences.getString(KEY_LAST_SYNC_PRODUCT_TIME, null);
    }

    public void setLastSyncProductTime(String lastSyncProductTime) {
        sharedPreferences.edit().putString(KEY_LAST_SYNC_PRODUCT_TIME, lastSyncProductTime).apply();
    }

    public boolean isNeedShowProductsUpdateBanner() {
        return sharedPreferences.getBoolean(KEY_SHOW_PRODUCT_UPDATE_BANNER, false);
    }

    public void setIsNeedShowProductsUpdateBanner(boolean isNeedShowUpdateBanner, String primaryName) {
        sharedPreferences.edit().putBoolean(KEY_SHOW_PRODUCT_UPDATE_BANNER, isNeedShowUpdateBanner).apply();
        if (isNeedShowUpdateBanner) {
            addShowUpdateBannerText(primaryName);
        } else {
            sharedPreferences.edit().remove(KEY_PRODUCT_UPDATE_BANNER_TEXT).apply();
        }
    }

    public void removeShowUpdateBannerTextWhenReactiveProduct(String primaryName) {
        Set<String> stringSet = sharedPreferences.getStringSet(KEY_PRODUCT_UPDATE_BANNER_TEXT, new HashSet<String>());
        stringSet.remove(primaryName);
        if (stringSet.size() == 0) {
            sharedPreferences.edit().putBoolean(KEY_SHOW_PRODUCT_UPDATE_BANNER, false).apply();
        }
    }

    public Set<String> getShowUpdateBannerTexts() {
        return sharedPreferences.getStringSet(KEY_PRODUCT_UPDATE_BANNER_TEXT, new HashSet<String>());
    }

    public void addShowUpdateBannerText(String productName) {
        Set<String> stringSet = sharedPreferences.getStringSet(KEY_PRODUCT_UPDATE_BANNER_TEXT, new HashSet<String>());
        stringSet.add(productName);
        sharedPreferences.edit().putStringSet(KEY_PRODUCT_UPDATE_BANNER_TEXT, stringSet).apply();
    }

    public String getLatestPhysicInventoryTime() {
        return sharedPreferences.getString(LATEST_PHYSICAL_INVENTORY_TIME, "1970-01-01 08:00:00");
    }

    public void setLatestPhysicInventoryTime(String latestPhysicInventoryTime) {
        sharedPreferences.edit().putString(LATEST_PHYSICAL_INVENTORY_TIME, latestPhysicInventoryTime).apply();
    }

    public long getRnrLastSyncTime() {
        return sharedPreferences.getLong(KEY_LAST_SYNCED_TIME_RNR_FORM, 0);
    }

    public void setRnrLastSyncTime() {
        sharedPreferences.edit().putLong(KEY_LAST_SYNCED_TIME_RNR_FORM, LMISApp.getInstance().getCurrentTimeMillis()).apply();
    }

    public long getStockLastSyncTime() {
        return sharedPreferences.getLong(KEY_LAST_SYNCED_TIME_STOCKCARD, 0);
    }

    public int getMonthOffsetThatDefinedOldData() {
        return sharedPreferences.getInt(MONTH_OFFSET_DEFINED_OLD_DATA, MONTH_OFFSET);
    }

    public void setStockLastSyncTime() {
        sharedPreferences.edit().putLong(KEY_LAST_SYNCED_TIME_STOCKCARD, LMISApp.getInstance().getCurrentTimeMillis()).apply();
    }

    public void setKeyHasCopiedTrainingDb(boolean hasCopiedTrainingDb) {
        sharedPreferences.edit().putBoolean(KEY_HAS_COPIED_TRAINING_DB, hasCopiedTrainingDb).apply();
    }

    public boolean hasCopiedTrainingDb() {
        return sharedPreferences.getBoolean(KEY_HAS_COPIED_TRAINING_DB, false);
    }

    public boolean hasSyncedUpLatestMovementLastDay() {
        DateTime lastSyncTriggerDate = new DateTime(sharedPreferences.getLong(LAST_MOVEMENT_HANDSHAKE_DATE, 0));
        DateTime currentDate = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
        return currentDate.minusDays(1).isBefore(lastSyncTriggerDate);
    }

    public void setLastMovementHandShakeDateToToday() {
        sharedPreferences.edit().putLong(LAST_MOVEMENT_HANDSHAKE_DATE, LMISApp.getInstance().getCurrentTimeMillis()).apply();
    }

    public void setEnableQaDebug(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_ENABLE_QA_DEBUG, enabled).apply();
    }

    public boolean isQaDebugEnabled() {
        return sharedPreferences.getBoolean(KEY_ENABLE_QA_DEBUG, false);
    }

    public DateTime getLatestUpdateLowStockAvgTime() {
        return new DateTime(sharedPreferences.getLong(LATEST_UPDATE_LOW_STOCK_AVG_TIME, 0));
    }

    public DateTime getLatestMonthlyCheckDirtyDataTime() {
        return new DateTime(sharedPreferences.getLong(LATEST_MONTHLY_CHECK_DIRTY_DATA_TIME, 0));
    }

    public void updateLatestMonthlyCheckDirtyDataTime() {
        sharedPreferences.edit().putLong(LATEST_MONTHLY_CHECK_DIRTY_DATA_TIME, LMISApp.getInstance().getCurrentTimeMillis()).apply();
    }

    public void updateLatestLowStockAvgTime() {
        sharedPreferences.edit().putLong(LATEST_UPDATE_LOW_STOCK_AVG_TIME, LMISApp.getInstance().getCurrentTimeMillis()).apply();
    }

    public void setHasDeletedOldStockMovement(boolean hasDeletedOldStockMovement) {
        sharedPreferences.edit().putBoolean(SharedPreferenceMgr.KEY_HAS_DELETED_OLD_STOCK_MOVEMENT, hasDeletedOldStockMovement).apply();
    }

    public void setHasDeletedOldRnr(boolean hasDeletedOldRnr) {
        sharedPreferences.edit().putBoolean(SharedPreferenceMgr.KEY_HAS_DELETED_OLD_RNR, hasDeletedOldRnr).apply();
    }

    public boolean isRapidTestDataSynced() {
        return sharedPreferences.getBoolean(SharedPreferenceMgr.KEY_HAS_SYNCED_DOWN_RAPID_TESTS, false);
    }

    public void setRapidTestsDataSynced(boolean hasRapidTestsDataSynced) {
        sharedPreferences.edit().putBoolean(SharedPreferenceMgr.KEY_HAS_SYNCED_DOWN_RAPID_TESTS, hasRapidTestsDataSynced).apply();
    }

    public void setStockCardLastYearSyncError(boolean isSyncFailed) {
        sharedPreferences.edit().putBoolean(SharedPreferenceMgr.KEY_STOCK_CARD_LAST_YEAR_SYNC_ERROR, isSyncFailed).apply();
    }

    public boolean isStockCardLastYearSyncError() {
        return sharedPreferences.getBoolean(SharedPreferenceMgr.KEY_STOCK_CARD_LAST_YEAR_SYNC_ERROR, false);
    }

    public String getStockMovementSyncError() {
        return sharedPreferences.getString(SharedPreferenceMgr.KEY_STOCK_MOVENENT_SYNC_ERROR, "");
    }

    public void setStockMovementSyncError(String msg) {
        sharedPreferences.edit().putString(SharedPreferenceMgr.KEY_STOCK_MOVENENT_SYNC_ERROR, msg).apply();
    }

    public long getLastUpdate() {
        return sharedPreferences.getLong(KEY_LAST_UPDATE, 0);
    }

    public void setLastUpdate(Long val) {
        sharedPreferences.edit().putLong(KEY_LAST_UPDATE, val).apply();
    }

    public long getMd5Time() {
        return sharedPreferences.getLong(KEY_MD5_TIME, 0);
    }

    public void setMd5Time(Long val) {
        sharedPreferences.edit().putLong(KEY_MD5_TIME, val).apply();
    }

    public String getMd5Key() {
        return sharedPreferences.getString(KEY_MD5_KEY, "0");
    }

    public void setMd5Key(String val) {
        sharedPreferences.edit().putString(KEY_MD5_KEY, val).apply();
    }

    public String getUpdateFile() {
        return sharedPreferences.getString(KEY_UPDATE_FILE, "");
    }

    public void setUpdateFile(String val) {
        sharedPreferences.edit().putString(KEY_UPDATE_FILE, val).apply();
    }

    public void removeDownloadInfo() {
        sharedPreferences.edit().remove(KEY_UPDATE_FILE)
                .remove(KEY_SILENT_FAILED)
                .remove(KEY_DOWNLOADED_LATEST_VERSIONCODE).apply();
    }

    public void setLastestVersionCode(int val) {
        sharedPreferences.edit().putInt(KEY_DOWNLOADED_LATEST_VERSIONCODE, val).apply();
    }

    public void setDeletedProduct(Set<String> products) {
        Set<String> deletedProductsCode = new HashSet<>();
        if (!products.isEmpty()){
            deletedProductsCode.addAll(getDeletedProduct());
            deletedProductsCode.addAll(products);
        }
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(deletedProductsCode);
        sharedPreferences.edit().putString(KEY_DELETED_THREE_PRODUCT, json).apply();
    }

    public List<String> getDeletedProduct() {
        String json = sharedPreferences.getString(KEY_DELETED_THREE_PRODUCT, null);
        if (json != null) {
            Gson gson = new GsonBuilder().create();
            Type type = new TypeToken<List<String>>() {
            }.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    public void setDeletedMovementItems(List<StockMovementItem> stockMovementItems) {
        Set<StockMovementItem> deleteStockMovementItems = new HashSet<>();
        if (!stockMovementItems.isEmpty()){
            deleteStockMovementItems.addAll(getDeletedMovementItems());
            deleteStockMovementItems.addAll(stockMovementItems);
        }
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(deleteStockMovementItems);
        sharedPreferences.edit().putString(KEY_DELETED_MOVEMENT_ITEM, json).apply();
    }

    public List<StockMovementItem> getDeletedMovementItems() {
            String json = sharedPreferences.getString(KEY_DELETED_MOVEMENT_ITEM, null);
            if (json != null) {
                Gson gson = new GsonBuilder().create();
                Type type = new TypeToken<List<StockMovementItem>>() {
                }.getType();
                return gson.fromJson(json, type);
            }
            return new ArrayList<>();
    }

    public void setKeepMovementItemsMap(HashMap<String,List<StockMovementItem>> keepStockMovementItemsMap){
        HashMap<String,List<StockMovementItem>> keepMovementMap = new HashMap<>();
        if (!keepStockMovementItemsMap.isEmpty()) {
            keepMovementMap.putAll(getKeepMovementItemsMap());
            keepMovementMap.putAll(keepStockMovementItemsMap);
        }
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(keepMovementMap);
        sharedPreferences.edit().putString(KEY_KEEP_MOVEMENT_LINE, json).apply();
    }

    public HashMap<String,List<StockMovementItem>> getKeepMovementItemsMap() {
        String json = sharedPreferences.getString(KEY_KEEP_MOVEMENT_LINE, null);
        if (json != null) {
            Gson gson = new GsonBuilder().create();
            Type type = new TypeToken<HashMap<String,List<StockMovementItem>>>() {
            }.getType();
            return gson.fromJson(json, type);
        }
        return new HashMap<>();
    }



    public void setUserFacilityId(String facilityId) {
        sharedPreferences.edit().putString(KEY_USER_FACILITY_ID, facilityId).apply();
    }

    public String getUserFacilityId() {
        return sharedPreferences.getString(KEY_USER_FACILITY_ID, StringUtils.EMPTY);
    }

    public Date getCheckDataDate() {
        return new Date(sharedPreferences.getLong(KEY_DELETED_PRODUCT_TIME, 0L));
    }

    public void setCheckDataDate(long time) {
        sharedPreferences.edit().putLong(KEY_DELETED_PRODUCT_TIME, time).apply();
    }
}
