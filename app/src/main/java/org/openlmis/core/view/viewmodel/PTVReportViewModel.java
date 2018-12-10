package org.openlmis.core.view.viewmodel;
import org.openlmis.core.model.RnRForm;

import java.io.Serializable;

import lombok.Data;

@Data
public class PTVReportViewModel implements Serializable {

    public RnRForm form;

    public PTVReportViewModel(RnRForm form) {
        this.form = form;
    }

    public Boolean isEmpty() {
        return this.form.getBaseInfoItemListWrapper().size() == 0;
    }
}

