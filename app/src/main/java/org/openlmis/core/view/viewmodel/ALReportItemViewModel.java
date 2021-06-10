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

import static org.openlmis.core.view.viewmodel.ALGridViewModel.SUFFIX_LENGTH;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.openlmis.core.model.RegimenItem;

@Data
public class ALReportItemViewModel implements Serializable {

  private ALGridViewModel gridOne = new ALGridViewModel(ALGridViewModel.ALColumnCode.OneColumn);
  private ALGridViewModel gridTwo = new ALGridViewModel(ALGridViewModel.ALColumnCode.TwoColumn);
  private ALGridViewModel gridThree = new ALGridViewModel(ALGridViewModel.ALColumnCode.ThreeColumn);
  private ALGridViewModel gridFour = new ALGridViewModel(ALGridViewModel.ALColumnCode.FourColumn);

  private List<ALGridViewModel> alGridViewModelList = Arrays
      .asList(gridOne, gridTwo, gridThree, gridFour);
  private Map<String, ALGridViewModel> alGridViewModelMap = new HashMap<>();
  private ALReportViewModel.ALItemType itemType;
  private boolean showCheckTip = false;

  public ALReportItemViewModel(ALReportViewModel.ALItemType itemType) {
    this.itemType = itemType;
    for (ALGridViewModel viewModel : alGridViewModelList) {
      alGridViewModelMap.put(viewModel.getColumnCode().getColumnName(), viewModel);
    }
  }

  public void setColumnValue(RegimenItem regimen, Long value) {
    String regimenName = regimen.getRegimen().getName();
    String columnName = regimenName.substring(regimenName.length() - SUFFIX_LENGTH);
    alGridViewModelMap.get(columnName).setValue(regimen, value);
  }

  public boolean isComplete() {
    for (ALGridViewModel viewModel : alGridViewModelList) {
      if (viewModel.getTreatmentsValue() == null || viewModel.getExistentStockValue() == null) {
        return false;
      }
    }
    return true;
  }
}
