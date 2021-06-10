package org.openlmis.core.view.viewmodel;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.model.RegimenItem;

@Data
public class ALGridViewModel {

  public enum ALColumnCode {
    OneColumn("1x6"),
    TwoColumn("2x6"),
    ThreeColumn("3x6"),
    FourColumn("4x6");
    private final String columnCodeName;

    ALColumnCode(String code) {
      this.columnCodeName = code;
    }

    public String getColumnName() {
      return columnCodeName;
    }
  }

  public enum ALGridColumnCode {
    treatment,
    existentStock
  }

  private ALColumnCode columnCode;
  private Long treatmentsValue;
  private Long existentStockValue;

  public final static String COLUMN_CODE_PREFIX_TREATMENTS = "Consultas AL US/APE Malaria ";
  public final static String COLUMN_CODE_PREFIX_STOCK = "Consultas AL STOCK Malaria ";
  public final static int SUFFIX_LENGTH = "1x6".length();

  ALGridViewModel(ALColumnCode columnCode) {
    this.columnCode = columnCode;
  }

  public void setValue(RegimenItem regimen, Long value) {
    String regimenName = regimen.getRegimen().getName();
    if (regimenName.contains(COLUMN_CODE_PREFIX_TREATMENTS)) {
      setTreatmentsValue(value);
    } else if (regimenName.contains(COLUMN_CODE_PREFIX_STOCK)) {
      setExistentStockValue(value);
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
    return StringUtils.isEmpty(getValue(treatmentsValue))
        || StringUtils.isEmpty(getValue(existentStockValue));
  }

  public String getValue(Long vaule) {
    return vaule == null ? "" : String.valueOf(vaule.longValue());
  }

}
