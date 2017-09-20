package org.openlmis.core.googleAnalytics;

public enum ScreenName {
    HomeScreen("Home Screen"),
    InventoryScreen("Inventory Screen"),
    KitStockCardOverviewScreen("Kit StockCard Overview Screen"),
    LoginScreen("Login Screen"),
    RapidTestScreen("Rapid Test Screen"),
    MMIARequisitionScreen("MMIA Requisition Screen"),
    VIARequisitionScreen("VIA Requisition Screen"),
    RnRFormHistoryScreen("RnR Form History Screen"),
    StockCardOverviewScreen("StockCard Overview Screen"),
    StockCardMovementScreen("StockCard Movement Screen"),
    StockCardNewMovementScreen("StockCard New Movement Screen"),
    StockCardMovementHistoryScreen("StockCard Movement History Screen"),
    ArchivedDrugsListScreen("Archived Drugs List Screen"),
    SelectPeriodScreen("Select Period Screen"),
    SelectUnpackKitNumberScreen("Select Unpack Kit Number Screen"),
    UnpackKitScreen("Unpack Kit Screen"),
    SelectRegimeProductScreen("Select Regime Products Screen"),
    SelectEmergencyProductsScreen("Select Emergency Products Screen"),
    AddDrugsToVIAScreen("Add Drugs to VIA Classica Screen"),
    RapidTestReportFormScreen("Rapid Test Report Form"),
    PatientDataReportFormScreen("Patient Data Report"),
    AllDrugsMovementHistoryScreen("All Drugs Movement History Screen");

    private final String screenName;

    ScreenName(String ScreenName) {
        this.screenName = ScreenName;
    }

    public String getScreenName() {
        return this.screenName;
    }
}
