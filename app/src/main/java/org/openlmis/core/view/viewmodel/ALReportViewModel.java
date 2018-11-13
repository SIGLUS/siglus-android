package org.openlmis.core.view.viewmodel;
import org.openlmis.core.model.Period;
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
    public  List<ALReportItemViewModel> itemViewModelList = new ArrayList<>();
    Map<String, ALReportItemViewModel> itemViewModelMap = new HashMap<>();

    private ProgramDataForm rapidTestForm = new ProgramDataForm();

    public static long DEFAULT_FORM_ID = 0;
    public static String DEFAULT_TOTAl_NULL = "";


    public ALReportViewModel(Period period) {
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
        if (regimenItems.size() > 0 ) {
           for (RegimenItem regimen : regimenItems) {
             itemHF.setColumnValue(regimen, regimen.getHf());
             itemCHW.setColumnValue(regimen, regimen.getChw());
             itemTotal.setColumnValue(regimen, regimen.getAmount());
           }
        }
    }

}

