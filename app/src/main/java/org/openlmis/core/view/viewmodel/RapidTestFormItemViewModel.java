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

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.TestConsumptionItem;
import org.openlmis.core.model.UsageColumnsMap;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.ColumnCode;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode;

@SuppressWarnings("squid:S1874")
@Data
public class RapidTestFormItemViewModel {

  MovementReasonManager.MovementReason issueReason;

  RapidTestFormGridViewModel gridHIVDetermine = new RapidTestFormGridViewModel(ColumnCode.HIVDETERMINE);
  RapidTestFormGridViewModel gridHIVUnigold = new RapidTestFormGridViewModel(ColumnCode.HIVUNIGOLD);
  RapidTestFormGridViewModel gridSyphillis = new RapidTestFormGridViewModel(ColumnCode.SYPHILLIS);
  RapidTestFormGridViewModel gridMalaria = new RapidTestFormGridViewModel(ColumnCode.MALARIA);

  List<RapidTestFormGridViewModel> rapidTestFormGridViewModelList = Arrays
      .asList(gridHIVDetermine, gridHIVUnigold, gridSyphillis, gridMalaria);

  Map<ColumnCode, RapidTestFormGridViewModel> rapidTestFormGridViewModelMap = new HashMap<>();

  public RapidTestFormItemViewModel(MovementReasonManager.MovementReason issueReason) {
    this.issueReason = issueReason;
    for (RapidTestFormGridViewModel viewModel : rapidTestFormGridViewModelList) {
      rapidTestFormGridViewModelMap.put(viewModel.getColumnCode(), viewModel);
    }
  }

  public void setColumnValue(UsageColumnsMap column, int value) {
    rapidTestFormGridViewModelMap.get(ColumnCode.valueOf(column.getCode().split("_")[1]))
        .setValue(column, value);
  }

  public void updateUnjustifiedColumn() {
    for (RapidTestFormGridViewModel viewModel : rapidTestFormGridViewModelList) {
      if (viewModel.isAddUnjustified()) {
        viewModel.unjustifiedValue = "0";
      }
    }
  }

  public void setAPEItem() {
    for (RapidTestFormGridViewModel viewModel : rapidTestFormGridViewModelList) {
      viewModel.isAPE = true;
    }
  }

  public void updateNoValueGridRowToZero(RapidTestFormGridViewModel viewModel) {
    viewModel.consumptionValue =
        StringUtils.isEmpty(viewModel.consumptionValue) ? "0" : viewModel.consumptionValue;
    viewModel.positiveValue =
        StringUtils.isEmpty(viewModel.positiveValue) ? "0" : viewModel.positiveValue;
    viewModel.unjustifiedValue =
        StringUtils.isEmpty(viewModel.unjustifiedValue) ? "0" : viewModel.unjustifiedValue;
  }

  public List<TestConsumptionItem> convertToDataModel() {
    if (issueReason.getDescription().equals(LMISApp.getInstance().getString(R.string.total))) {
      return newArrayList();
    }

    List<TestConsumptionItem> programDataFormItems = new ArrayList<>();
    for (RapidTestFormGridViewModel gridViewModel : rapidTestFormGridViewModelList) {
      programDataFormItems.addAll(gridViewModel.convertFormGridViewModelToDataModel(issueReason));
    }
    return programDataFormItems;
  }

  public String validate() {
    for (RapidTestFormGridViewModel gridViewModel : rapidTestFormGridViewModelList) {
      if (!validateConsumption(gridViewModel)) {
        return LMISApp.getInstance().getString(R.string.error_rapid_test_consumption);
      } else if (!validatePositive(gridViewModel)) {
        return LMISApp.getInstance().getString(R.string.error_positive_larger_than_consumption);
      } else if (!validateUnjustified(gridViewModel)) {
        return LMISApp.getInstance().getString(R.string.error_rapid_test_unjustified);
      }
    }
    return StringUtils.EMPTY;
  }

  public boolean validateConsumption(RapidTestFormGridViewModel gridViewModel) {
    if (!gridViewModel.validateConsumption()) {
      gridViewModel.setInvalidColumn(RapidTestGridColumnCode.CONSUMPTION);
      return false;
    } else {
      gridViewModel.setInvalidColumn(null);
      return true;
    }
  }

  public boolean validatePositive(RapidTestFormGridViewModel gridViewModel) {
    if (!gridViewModel.validatePositive()) {
      gridViewModel.setInvalidColumn(RapidTestGridColumnCode.POSITIVE);
      return false;
    } else {
      gridViewModel.setInvalidColumn(null);
      return true;
    }
  }

  public boolean validateUnjustified(RapidTestFormGridViewModel gridViewModel) {
    if (!gridViewModel.validateUnjustified()) {
      gridViewModel.setInvalidColumn(RapidTestGridColumnCode.UNJUSTIFIED);
      return false;
    } else {
      gridViewModel.setInvalidColumn(null);
      return true;
    }
  }

  public boolean isEmpty() {
    for (RapidTestFormGridViewModel gridViewModel : rapidTestFormGridViewModelList) {
      if (!gridViewModel.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  public void clearValue(ColumnCode columnCode, RapidTestGridColumnCode gridColumnCode) {
    rapidTestFormGridViewModelMap.get(columnCode).clear(gridColumnCode);
  }
}
