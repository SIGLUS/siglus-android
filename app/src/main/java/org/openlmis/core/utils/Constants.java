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

package org.openlmis.core.utils;

import org.openlmis.core.LMISApp;

public final class Constants {
    // Intent Params
    public static final String PARAM_STOCK_CARD_ID = "stockCardId";
    public static final String PARAM_STOCK_NAME = "stockName";
    public static final String PARAM_IS_ACTIVATED = "productIsActivated";
    public static final String PARAM_IS_PHYSICAL_INVENTORY = "isPhysicalInventory";
    public static final String PARAM_IS_ADD_NEW_DRUG = "isAddNewDrug";
    public static final String PARAM_KIT_CODE = "kitCode";
    public static final String PARAM_KIT_NUM = "kitNum";
    public static final String PARAM_PROGRAM_CODE = "programCode";
    public static final String PARAM_FORM_ID = "formId";
    public static final String PARAM_IS_FROM_ARCHIVE = "isFromArchive";
    public static final String PARAM_IS_KIT = "isKit";
    public static final String PARAM_SELECTED_INVENTORY = "selectedInventory";

    // Request Params
    public static final int REQUEST_FROM_STOCK_LIST_PAGE = 100;
    public static final int REQUEST_UNPACK_KIT = 200;
    public static final int REQUEST_FROM_RNR_LIST_PAGE = 300;
    public static final int REQUEST_SELECT_PERIOD_END = 400;
    public static final String INTENT_FILTER_SET_SYNCED_TIME = LMISApp.getContext().getPackageName()+".set_synced_time";

    private Constants(){

    }
}
