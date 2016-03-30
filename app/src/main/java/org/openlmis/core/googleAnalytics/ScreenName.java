package org.openlmis.core.googleAnalytics;

public enum ScreenName {
    HomeScreen("Home Screen"),
    InventoryScreen("Inventory Screen"),
    KitStockCardOverviewScreen("Kit StockCard Overview Screen"),
    LoginScreen("Login Screen"),
    MMIARequisitionScreen("MMIA Requisition Screen"),
    VIARequisitionScreen("VIA Requisition Screen"),
    RnRFormHistoryScreen("RnR Form History Screen"),
    StockCardOverviewScreen("StockCard Overview Screen"),
    StockCardMovementScreen("StockCard Movement Screen"),
    StockCardMovementHistoryScreen("StockCard Movement History Screen"),
    ArchivedDrugsListScreen("Archived Drugs List Screen"),
    SelectPeriodScreen("Select Period Screen"),
    SelectUnpackKitNumberScreen("Select Unpack Kit Number Screen"),
    UnpackKitScreen("Unpack Kit Screen"),
    SelectRegimeProductScreen("Select Regime Products Screen"),
    SelectEmergencyProductsScreen("Select Emergency Products Screen");

    private final String screenName;

    ScreenName(String ScreenName) {
        this.screenName = ScreenName;
    }

    public String getScreenName() {
        return this.screenName;
    }
}
