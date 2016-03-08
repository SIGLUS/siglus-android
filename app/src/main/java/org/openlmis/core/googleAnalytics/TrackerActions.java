package org.openlmis.core.googleAnalytics;

public enum TrackerActions {
    SelectStockCard("Select Stock Card"),
    SelectReason("Select Reason"),
    SelectMovementDate("Select Movement Date"),
    SelectComplete("Select Complete"),
    SelectApprove("Select Approve");

    private final String trackerAction;

    TrackerActions(String trackerAction) {
        this.trackerAction = trackerAction;
    }

    public String getString() {
        return this.trackerAction;
    }
}
