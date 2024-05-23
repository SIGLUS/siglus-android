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

package org.openlmis.core.utils;

import androidx.annotation.NonNull;
import com.viethoa.models.AlphabetItem;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

public final class InventoryUtils {

  private InventoryUtils() {
  }

  @NonNull
  public static List<AlphabetItem> getAlphabetItemsByInventories(
      List<InventoryViewModel> viewModels) {
    List<AlphabetItem> mAlphabetItems = new ArrayList<>();

    if (viewModels != null) {
      List<String> strAlphabets = new ArrayList<>();

      for (int i = 0; i < viewModels.size(); i++) {
        String name = viewModels.get(i).getProductName();

        if (StringUtils.isBlank(name)) {
          continue;
        }

        String word = name.substring(0, 1).toUpperCase();
        if (!strAlphabets.contains(word)) {
          strAlphabets.add(word);
          mAlphabetItems.add(new AlphabetItem(i, word, false));
        }
      }
    }
    return mAlphabetItems;
  }
}