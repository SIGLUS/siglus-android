/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.view.viewmodel;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.Service;
import org.openlmis.core.model.ServiceItem;

@Data
public class PTVReportViewModel {

  private RnRForm form;
  private List<Service> services = new ArrayList<>();

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

