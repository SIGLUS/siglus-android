package org.openlmis.core.view.viewmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegimeProductViewModel {
    private String shortCode;
    private boolean checked;

    public RegimeProductViewModel(String shortCode) {
        this.shortCode = shortCode;
    }
}
