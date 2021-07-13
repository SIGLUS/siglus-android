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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;

@Data
public class ALReportViewModel {

  public enum ALItemType {
    TOTAL("total"),
    HF("hf"),
    CHW("chw");
    private final String itemType;

    ALItemType(String type) {
      this.itemType = type;
    }

    public String getName() {
      return itemType;
    }
  }

  private ALReportItemViewModel itemTotal;
  private ALReportItemViewModel itemHF;
  private ALReportItemViewModel itemCHW;

  private RnRForm form;
  private List<ALReportItemViewModel> itemViewModelList = new ArrayList<>();
  private Map<String, ALReportItemViewModel> itemViewModelMap = new HashMap<>();

  public ALReportViewModel() {
    setupCategories();
    setItemViewModelMap();
  }

  private void setupCategories() {
    itemHF = new ALReportItemViewModel(ALItemType.HF);
    itemCHW = new ALReportItemViewModel(ALItemType.CHW);
    itemTotal = new ALReportItemViewModel(ALItemType.TOTAL);
    itemViewModelList.add(itemHF);
    itemViewModelList.add(itemCHW);
    itemViewModelList.add(itemTotal);
  }

  private void setItemViewModelMap() {
    for (ALReportItemViewModel viewModel : itemViewModelList) {
      itemViewModelMap.put(viewModel.getItemType().getName(), viewModel);
    }
  }

  public ALReportViewModel(RnRForm form) {
    this.form = form;
    setupCategories();
    setItemViewModelMap();
    setFormItemViewModels(form);
  }

  private void setFormItemViewModels(RnRForm form) {
    List<RegimenItem> regimenItems = form.getRegimenItemListWrapper();
    if (!regimenItems.isEmpty()) {
      for (RegimenItem regimen : regimenItems) {
        itemHF.setColumnValue(regimen, regimen.getHf());
        itemCHW.setColumnValue(regimen, regimen.getChw());
        itemTotal.setColumnValue(regimen, regimen.getAmount());
      }
    }
  }

  public void updateTotal(ALGridViewModel.ALColumnCode columnCode,
      ALGridViewModel.ALGridColumnCode gridColumnCode) {
    clearCheckTip();
    if (gridColumnCode == ALGridViewModel.ALGridColumnCode.TREATMENT) {
      Long hf = itemHF.getAlGridViewModelMap().get(columnCode.getColumnName()).getTreatmentsValue();
      Long chf = itemCHW.getAlGridViewModelMap().get(columnCode.getColumnName())
          .getTreatmentsValue();
      itemTotal.getAlGridViewModelMap().get(columnCode.getColumnName())
          .setTreatmentsValue(calculate(hf, chf));

    } else {
      Long hf = itemHF.getAlGridViewModelMap().get(columnCode.getColumnName())
          .getExistentStockValue();
      Long chf = itemCHW.getAlGridViewModelMap().get(columnCode.getColumnName())
          .getExistentStockValue();
      itemTotal.getAlGridViewModelMap().get(columnCode.getColumnName())
          .setExistentStockValue(calculate(hf, chf));
    }
  }

  public boolean isComplete() {
    itemHF.setShowCheckTip(false);
    itemCHW.setShowCheckTip(false);
    if (!itemHF.isComplete()) {
      itemHF.setShowCheckTip(true);
    } else if (!itemCHW.isComplete()) {
      itemCHW.setShowCheckTip(true);
    }
    return !itemHF.isShowCheckTip()
        && !itemCHW.isShowCheckTip();
  }

  private void clearCheckTip() {
    itemHF.setShowCheckTip(false);
    itemCHW.setShowCheckTip(false);
  }

  private Long calculate(Long hf, Long chf) {
    if (hf == null && chf == null) {
      return null;
    }
    Long total = Long.valueOf(0);
    total = hf == null ? total : total + hf;
    total = chf == null ? total : total + chf;
    return total;
  }
}

