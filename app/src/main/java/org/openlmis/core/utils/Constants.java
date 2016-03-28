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
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RegimeProduct;

import java.util.ArrayList;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public final class Constants {

    // Don't change these program codes!!!
    public static final String MMIA_PROGRAM_CODE = "MMIA";
    public static final String VIA_PROGRAM_CODE = "VIA";
    public static final String ESS_PRORGRAM_CODE = "ESS_MEDS";

    // Intent Params
    public static final String PARAM_STOCK_CARD_ID = "stockCardId";
    public static final String PARAM_STOCK_NAME = "stockName";
    public static final String PARAM_IS_ACTIVATED = "productIsActivated";
    public static final String PARAM_IS_PHYSICAL_INVENTORY = "isPhysicalInventory";
    public static final String PARAM_IS_ADD_NEW_DRUG = "isAddNewDrug";
    public static final String PARAM_KIT_CODE = "kitCode";
    public static final String PARAM_KIT_NUM = "kitNum";
    public static final String PARAM_KIT_NAME = "kitName";
    public static final String PARAM_PROGRAM_CODE = "programCode";
    public static final String PARAM_FORM_ID = "formId";
    public static final String PARAM_IS_FROM_ARCHIVE = "isFromArchive";
    public static final String PARAM_IS_KIT = "isKit";
    public static final String PARAM_SELECTED_INVENTORY_DATE = "selectedInventoryDate";
    public static final String PARAM_IS_MISSED_PERIOD = "isMissedPeriod";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_CUSTOM_REGIMEN = "customRegimen";

    // Request Params
    public static final int REQUEST_FROM_STOCK_LIST_PAGE = 100;
    public static final int REQUEST_UNPACK_KIT = 200;
    public static final int REQUEST_FROM_RNR_LIST_PAGE = 300;
    public static final int REQUEST_SELECT_PERIOD_END = 400;

    // Broadcast Intent Filter
    public static final String INTENT_FILTER_SET_SYNC_DATA = LMISApp.getContext().getPackageName() + ".sync_data";

    private Constants() {

    }

    public static ArrayList<RegimeProduct> getRegimeProducts() throws LMISException {
        return newArrayList(
                new RegimeProduct("3TC 150mg", "Lamivudina 150mg"),
                new RegimeProduct("3TC 300 mg", "Lamivudina 300mg"),
                new RegimeProduct("AZT 300 mg", "zidovudina 300mg"),
                new RegimeProduct("NVP 200 mg", "Nevirapina 200mg"),
                new RegimeProduct("TDF 300 mg", "Tenofovir 300mg"),
                new RegimeProduct("EFV 600 mg", "Efavirenze 600mg"),
                new RegimeProduct("Lpv/r 200/50mg", "Lopinavir/Ritonavir 200/50 mg"),
                new RegimeProduct("ABC 300mg", "Abacavir 300mg"),
                new RegimeProduct("D4T 30mg", "Stavudina 30mg"),
                new RegimeProduct("3TC30mg", "Lamivudina 30mg"),
                new RegimeProduct("D4T 6mg", "Stavudina 6mg"),
                new RegimeProduct("AZT60mg", "Zidovudina60mg"),
                new RegimeProduct("NVP 50mg", "Nevirapina 50mg"),
                new RegimeProduct("Lpv/r 100/25mg", "Lopinavir/Ritonavir 100/25mg"),
                new RegimeProduct("Lpv/r 80/20mL Solucao oral", "Lopinavir/Ritonavir 80/20mL Solucao oral"),
                new RegimeProduct("EFV 200mg", "Efavirenze 200mg"),
                new RegimeProduct("EFV 50mg", "Efavirenze 50mg"),
                new RegimeProduct("ABC60mg", "Abacavir 60mg"),
                new RegimeProduct("NVP 50mg/5ml sol oral", "Nevirapina 50mg/5ml sol oral"),
                new RegimeProduct("AZT 50mg/5ml sol oral", "zidovudina 50mg/5ml sol oral"));
    }
}
