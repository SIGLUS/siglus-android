package org.openlmis.core.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegimeProduct {
    private String shortCode;
    private String entireName;

    public RegimeProduct(String shortCode, String entireName) {
        this.shortCode = shortCode;
        this.entireName = entireName;
    }
}
