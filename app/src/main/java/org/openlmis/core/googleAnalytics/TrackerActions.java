package org.openlmis.core.googleAnalytics;

public enum TrackerActions {
    SELECT_STOCK_CARD("Select Stock Card"),
    SELECT_REASON("Select Reason"),
    SELECT_MOVEMENT_DATE("Select Movement Date"),
    SELECT_COMPLETE("Select Complete"),
    SELECT_APPROVE("Select Approve"),
    SELECT_MMIA("Select MMIA"),
    SELECT_VIA("Select VIA"),
    SELECT_PTV("Select PTV"),
    SELECT_AL("Select AL"),
    CREATE_RNR("Create RnR Form"),
    SELECT_PERIOD("Select Period"),
    SUBMIT_RNR("First Time Approve"),
    AUTHORISE_RNR("Second Time Approve"),
    SELECT_INVENTORY("Select Inventory"),
    COMPLETE_INVENTORY("Complete Inventory"),
    APPROVE_INVENTORY("Approve Inventory"),
    NETWORK_CONNECTED("Network Connected"),
    NETWORK_DISCONNECTED("Network Disconnected"),
    SWITCH_POWER_ON("Tablet Power On"),
    SWITCH_POWER_OFF("Tablet Power Off");

    private final String trackerAction;

    TrackerActions(String trackerAction) {
        this.trackerAction = trackerAction;
    }

    public String getString() {
        return this.trackerAction;
    }
}
