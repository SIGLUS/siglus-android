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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.openlmis.core.LMISApp;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;

import java.util.HashSet;
import java.util.Set;

import roboguice.RoboGuice;

@Singleton
public class SharedPreferenceMgr {

    private static SharedPreferenceMgr self;
    public static final String MY_PREFERENCE = "LMISPreference";
    SharedPreferences sharedPreferences;

    public static final String KEY_LAST_SYNCED_TIME_RNR_FORM = "lastSyncedDate";
    public static final String KEY_LAST_SYNCED_TIME_STOCKCARD = "lastSyncedDateStockCard";
    public static final String KEY_LAST_LOGIN_USER = "last_user";
    private static final String KEY_NEEDS_INVENTORY = "init_inventory";
    private static final String KEY_HAS_GET_PRODUCTS = "has_get_products";
    private static final String KEY_HAS_SYNCED_LATEST_MONTH_STOCKMOVEMENTS = "has_get_month_stock_cards_synced";
    private static final String KEY_SHOULD_SYNC_LAST_YEAR = "should_sync_last_year";
    private static final String KEY_IS_REQUISITION_DATA_SYNCED = "is_requisition_data_synced";
    public static final String KEY_STOCK_SYNC_END_TIME = "sync_stock_end_time";
    public static final String KEY_STOCK_SYNC_CURRENT_INDEX = "sync_stock_current_index";
    public static final String KEY_LAST_SYNC_PRODUCT_TIME = "last_sync_product_time";
    public static final String KEY_SHOW_PRODUCT_UPDATE_BANNER = "show_product_update_banner";
    public static final String KEY_PRODUCT_UPDATE_BANNER_TEXT = "product_update_banner_text";

    @Inject
    public SharedPreferenceMgr(Context context) {
        sharedPreferences = context.getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE);
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

    public boolean hasGetProducts() {
        return sharedPreferences.getBoolean(SharedPreferenceMgr.KEY_HAS_GET_PRODUCTS, false);
    }

    public void setHasGetProducts(boolean hasGetProducts) {
        sharedPreferences.edit().putBoolean(SharedPreferenceMgr.KEY_HAS_GET_PRODUCTS, hasGetProducts).apply();
    }

    public boolean isLastMonthStockDataSynced() {
        StockRepository stockRepository = RoboGuice.getInjector(LMISApp.getContext()).getInstance(StockRepository.class);
        return sharedPreferences.getBoolean(SharedPreferenceMgr.KEY_HAS_SYNCED_LATEST_MONTH_STOCKMOVEMENTS, stockRepository.hasStockData());
    }

    public void setLastMonthStockCardDataSynced(boolean isStockCardSynced) {
        sharedPreferences.edit().putBoolean(SharedPreferenceMgr.KEY_HAS_SYNCED_LATEST_MONTH_STOCKMOVEMENTS, isStockCardSynced).apply();
    }

    public boolean shouldSyncLastYearStockData() {
        return sharedPreferences.getBoolean(SharedPreferenceMgr.KEY_SHOULD_SYNC_LAST_YEAR, false);
    }

    public void setShouldSyncLastYearStockCardData(boolean shouldSyncLastYearStockCardData) {
        sharedPreferences.edit().putBoolean(SharedPreferenceMgr.KEY_SHOULD_SYNC_LAST_YEAR, shouldSyncLastYearStockCardData).apply();
    }

    public boolean isRequisitionDataSynced() {
        RnrFormRepository rnrFormRepository = RoboGuice.getInjector(LMISApp.getContext()).getInstance(RnrFormRepository.class);
        return sharedPreferences.getBoolean(SharedPreferenceMgr.KEY_IS_REQUISITION_DATA_SYNCED, rnrFormRepository.hasRequisitionData());
    }

    public void setRequisitionDataSynced(boolean requisitionDataSynced) {
        sharedPreferences.edit().putBoolean(SharedPreferenceMgr.KEY_IS_REQUISITION_DATA_SYNCED, requisitionDataSynced).apply();
    }

    public boolean isNeedsInventory() {
        return sharedPreferences.getBoolean(SharedPreferenceMgr.KEY_NEEDS_INVENTORY, true);
    }

    public void setIsNeedsInventory(boolean isNeedsInventory) {
        sharedPreferences.edit().putBoolean(SharedPreferenceMgr.KEY_NEEDS_INVENTORY, isNeedsInventory).apply();
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

    public void removeShowUpdateBannerTextWhenReactiveProduct(String primaryName){
        Set<String> stringSet = sharedPreferences.getStringSet(KEY_PRODUCT_UPDATE_BANNER_TEXT, new HashSet<String>());
        stringSet.remove(primaryName);
    }

    public Set<String> getShowUpdateBannerTexts() {
        return sharedPreferences.getStringSet(KEY_PRODUCT_UPDATE_BANNER_TEXT, new HashSet<String>());
    }

    public void addShowUpdateBannerText(String productName) {
        Set<String> stringSet = sharedPreferences.getStringSet(KEY_PRODUCT_UPDATE_BANNER_TEXT, new HashSet<String>());
        stringSet.add(productName);
        sharedPreferences.edit().putStringSet(KEY_PRODUCT_UPDATE_BANNER_TEXT, stringSet).apply();
    }
}
