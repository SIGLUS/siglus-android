package org.openlmis.core.utils;

public enum MalariaProductCodes {
    PRODUCT_6x1_CODE ("08O05"),
    PRODUCT_6x2_CODE ("08O05Z"),
    PRODUCT_6x3_CODE ("08O05Y"),
    PRODUCT_6x4_CODE ("08O05X");

    private final String name;

    public String getValue() {
        return name;
    }

    MalariaProductCodes(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
