package org.openlmis.core.googleAnalytics;

public enum TrackerActions {
    SelectStockCard("Select Stock Card"),
    SelectReason("Select Reason"),
    SelectMovementDate("Select Movement Date"),
    SelectComplete("Select Complete"),
    SelectApprove("Select Approve"),
    SelectMMIA("Select MMIA"),
    SelectVIA("Select VIA"),
    SelectPTV("Select PTV"),
    SelectAL("Select AL"),
    CreateRnR("Create RnR Form"),
    SelectPeriod("Select Period"),
    SubmitRnR("First Time Approve"),
    AuthoriseRnR("Second Time Approve"),
    SelectInventory("Select Inventory"),
    CompleteInventory("Complete Inventory"),
    ApproveInventory("Approve Inventory"),
    NetworkConnected("Network Connected"),
    NetworkDisconnected("Network Disconnected"),
    SwitchPowerOn("Tablet Power On"),
    SwitchPowerOff("Tablet Power Off");

    private final String trackerAction;

    TrackerActions(String trackerAction) {
        this.trackerAction = trackerAction;
    }

    public String getString() {
        return this.trackerAction;
    }
}
