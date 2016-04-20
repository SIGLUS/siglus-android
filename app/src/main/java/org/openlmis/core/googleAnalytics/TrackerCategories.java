package org.openlmis.core.googleAnalytics;

public enum TrackerCategories {
    StockMovement("F.d.S"),
    Inventory("Inventory"),
    MMIA("MMIA"),
    VIA("VIA"),
    NETWORK("Network"),
    SWITCH("Switch Power");

    private final String trackerCategory;

    TrackerCategories(String trackerCategory) {
        this.trackerCategory = trackerCategory;
    }

    public String getString(){
        return this.trackerCategory;
    }
}