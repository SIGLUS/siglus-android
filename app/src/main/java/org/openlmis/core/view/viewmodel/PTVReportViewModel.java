package org.openlmis.core.view.viewmodel;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.Service;
import org.openlmis.core.model.ServiceItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class PTVReportViewModel implements Serializable {

    public RnRForm form;
    public List<Service> services = new ArrayList<>();

    public PTVReportViewModel(RnRForm form) {
        this.form = form;
        if (form.getRnrFormItemListWrapper().size() > 0) {
            services.clear();
            for (ServiceItem serviceItem : form.getRnrFormItemListWrapper().get(0).getServiceItemList()) {
                services.add(serviceItem.getService());
            }
        }
    }

    public Boolean isEmpty() {
        return this.form.getRnrFormItemListWrapper().size() == 0;
    }
}

