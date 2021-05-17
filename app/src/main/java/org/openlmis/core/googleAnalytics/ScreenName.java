package org.openlmis.core.googleAnalytics;

public enum ScreenName {
    HOME_SCREEN("Home Screen"),
    INVENTORY_SCREEN("Inventory Screen"),
    KIT_STOCK_CARD_OVERVIEW_SCREEN("Kit StockCard Overview Screen"),
    LOGIN_SCREEN("Login Screen"),
    RAPID_TEST_SCREEN("Rapid Test Screen"),
    MMIA_REQUISITION_SCREEN("MMIA Requisition Screen"),
    AL_REQUISITION_SCREEN("AL Requisition Screen"),
    PTV_REQUISITION_SCREEN("PTV Requisition Screen"),
    VIA_REQUISITION_SCREEN("VIA Requisition Screen"),
    RN_R_FORM_HISTORY_SCREEN("RnR Form History Screen"),
    STOCK_CARD_OVERVIEW_SCREEN("StockCard Overview Screen"),
    STOCK_CARD_MOVEMENT_SCREEN("StockCard Movement Screen"),
    STOCK_CARD_NEW_MOVEMENT_SCREEN("StockCard New Movement Screen"),
    STOCK_CARD_MOVEMENT_HISTORY_SCREEN("StockCard Movement History Screen"),
    ARCHIVED_DRUGS_LIST_SCREEN("Archived Drugs List Screen"),
    SELECT_PERIOD_SCREEN("Select Period Screen"),
    SELECT_UNPACK_KIT_NUMBER_SCREEN("Select Unpack Kit Number Screen"),
    UNPACK_KIT_SCREEN("Unpack Kit Screen"),
    SELECT_REGIME_PRODUCT_SCREEN("Select Regime Products Screen"),
    SELECT_EMERGENCY_PRODUCTS_SCREEN("Select Emergency Products Screen"),
    ADD_DRUGS_TO_VIA_SCREEN("Add Drugs to VIA Classica Screen"),
    RAPID_TEST_REPORT_FORM_SCREEN("Rapid Test Report Form"),
    MALARIA_DATA_REPORT_FORM_SCREEN("Patient Data Report"),
    ALL_DRUGS_MOVEMENT_HISTORY_SCREEN("All Drugs Movement History Screen");

    private final String name;

    ScreenName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
