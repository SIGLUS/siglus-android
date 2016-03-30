package org.openlmis.core.view.viewmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegimeProductViewModel {
    private String shortCode;
    private String entireName;
    private boolean checked;

    public RegimeProductViewModel(String shortCode, String entireName) {
        this.shortCode = shortCode;
        this.entireName = entireName;
    }
}
