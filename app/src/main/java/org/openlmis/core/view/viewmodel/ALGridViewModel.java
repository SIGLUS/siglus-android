package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.ProgramDataColumn;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ALGridViewModel {
    public enum ALColumnCode {
        OneColumn("1*6"),
        TwoColumn("2*6"),
        ThreeColumn("3*6"),
        FourColumn("4*6");
        private String columnCodeName;

        ALColumnCode(String code) {
            this.columnCodeName = code;
        }

        public String getColumnName() {
            return columnCodeName;
        }
    }

    ALColumnCode columnCode;
    String treatmentsValue = "";
    String existentStockValue = "";


    final static String COLUMN_CODE_PREFIX_TREATMENTS = "Consultas AL US/APE Malaria ";
    final static String COLUMN_CODE_PREFIX_STOCK = "Consultas AL STOCK Malaria ";


    ALGridViewModel(ALColumnCode columnCode) {
        this.columnCode = columnCode;
    }

    public void setValue(RegimenItem regimen, Long value) {
        String regimenName = regimen.getRegimen().getName();
        if (regimenName.contains(COLUMN_CODE_PREFIX_TREATMENTS)) {
            setTreatmentsValue(String.valueOf(value));
        } else if (regimenName.contains(COLUMN_CODE_PREFIX_STOCK)) {
            setExistentStockValue(String.valueOf(value));
        }
    }

    public boolean validate() {
        try {
            return isEmpty();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(treatmentsValue)
                && StringUtils.isEmpty(existentStockValue);
    }

}
