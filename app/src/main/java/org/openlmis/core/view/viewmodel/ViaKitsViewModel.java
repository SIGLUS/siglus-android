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
import org.openlmis.core.model.RnrFormItem;

@Data
public class ViaKitsViewModel {

  private static final String DEFAULT_VALUE = "0";

  private String kitsReceivedHF = DEFAULT_VALUE;

  private String kitsReceivedCHW = DEFAULT_VALUE;

  private String kitsOpenedHF = DEFAULT_VALUE;

  private String kitsOpenedCHW = DEFAULT_VALUE;

  private List<RnrFormItem> kitItems = new ArrayList<>();

  public static final String US_KIT = "26A01";

  public static final String APE_KIT = "26A02";

  public void convertRnrKitItemsToViaKit(List<RnrFormItem> rnrKitItems) {
    kitItems = rnrKitItems;

    for (RnrFormItem rnrKitItem : rnrKitItems) {

      if (US_KIT.equals(rnrKitItem.getProduct().getCode())) {
        if (rnrKitItem.getReceived() > Long.MIN_VALUE) {
          kitsReceivedHF = String.valueOf(rnrKitItem.getReceived());
        }

        if (rnrKitItem.getIssued() > Long.MIN_VALUE) {
          kitsOpenedHF = String.valueOf(rnrKitItem.getIssued());
        }
      } else if (APE_KIT.equals(rnrKitItem.getProduct().getCode())) {
        if (rnrKitItem.getReceived() > Long.MIN_VALUE) {
          kitsReceivedCHW = String.valueOf(rnrKitItem.getReceived());
        }

        if (rnrKitItem.getIssued() > Long.MIN_VALUE) {
          kitsOpenedCHW = String.valueOf(rnrKitItem.getIssued());
        }
      }
    }
  }
}