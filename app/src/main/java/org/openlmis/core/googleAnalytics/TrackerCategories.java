package org.openlmis.core.googleAnalytics;

public enum TrackerCategories {
    StockMovement("F.d.S"),
    MMIA("MMIA"),
    VIA("VIA");

    private final String trackerCategory;

    TrackerCategories(String trackerCategory) {
        this.trackerCategory = trackerCategory;
    }

    public String getString() {
        return this.trackerCategory;
    }
}