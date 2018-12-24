package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ALReportViewModel implements Serializable {
    public enum ALItemType {
        Total("total"),
        hf("hf"),
        CHW("chw");
        private String itemType;

        ALItemType(String type) {
            this.itemType = type;
        }

        public String getName() {
            return itemType;
        }
    }

    ALReportItemViewModel itemTotal;
    ALReportItemViewModel itemHF;
    ALReportItemViewModel itemCHW;

    private RnRForm form;
    public List<ALReportItemViewModel> itemViewModelList = new ArrayList<>();
    Map<String, ALReportItemViewModel> itemViewModelMap = new HashMap<>();

    private ProgramDataForm rapidTestForm = new ProgramDataForm();

    public static long DEFAULT_FORM_ID = 0;
    public static String DEFAULT_TOTAl_NULL = "";


    public ALReportViewModel() {
        setupCategories();
        setItemViewModelMap();
    }

    private void setupCategories() {
        itemHF = new ALReportItemViewModel(ALItemType.hf);
        itemCHW = new ALReportItemViewModel(ALItemType.CHW);
        itemTotal = new ALReportItemViewModel(ALItemType.Total);
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
        if (regimenItems.size() > 0) {
            for (RegimenItem regimen : regimenItems) {
                itemHF.setColumnValue(regimen, regimen.getHf());
                itemCHW.setColumnValue(regimen, regimen.getChw());
                itemTotal.setColumnValue(regimen, regimen.getAmount());
            }
        }
    }

    public void updateTotal(ALGridViewModel.ALColumnCode columnCode, ALGridViewModel.ALGridColumnCode gridColumnCode) {
        clearCheckTip();
        if (gridColumnCode == ALGridViewModel.ALGridColumnCode.treatment) {
            Long hf = itemHF.rapidTestFormGridViewModelMap.get(columnCode.getColumnName()).treatmentsValue;
            Long chf = itemCHW.rapidTestFormGridViewModelMap.get(columnCode.getColumnName()).treatmentsValue;
            itemTotal.rapidTestFormGridViewModelMap.get(columnCode.getColumnName()).treatmentsValue = calculate(hf, chf);

        } else {
            Long hf = itemHF.rapidTestFormGridViewModelMap.get(columnCode.getColumnName()).existentStockValue;
            Long chf = itemCHW.rapidTestFormGridViewModelMap.get(columnCode.getColumnName()).existentStockValue;
            itemTotal.rapidTestFormGridViewModelMap.get(columnCode.getColumnName()).existentStockValue = calculate(hf, chf);
        }
    }

    public boolean isComplete() {
        itemHF.showCheckTip = false;
        itemCHW.showCheckTip = false;
        if (!itemHF.isComplete()) {
            itemHF.showCheckTip = true;
        } else if (!itemCHW.isComplete()) {
            itemCHW.showCheckTip = true;
        }
        return !itemHF.showCheckTip
                && !itemCHW.showCheckTip;
    }

    public void clearCheckTip() {
        itemHF.showCheckTip = false;
        itemCHW.showCheckTip = false;
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

