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

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.model.RegimenItem;

@Data
public class ALGridViewModel {

  public enum ALColumnCode {
    ONE_COLUMN("1x6"),
    TWO_COLUMN("2x6"),
    THREE_COLUMN("3x6"),
    FOUR_COLUMN("4x6");
    private final String columnCodeName;

    ALColumnCode(String code) {
      this.columnCodeName = code;
    }

    public String getColumnName() {
      return columnCodeName;
    }
  }

  public enum ALGridColumnCode {
    TREATMENT,
    EXISTENT_STOCK
  }

  public static final String COLUMN_CODE_PREFIX_TREATMENTS = "Consultas AL US/APE Malaria ";
  public static final String COLUMN_CODE_PREFIX_STOCK = "Consultas AL STOCK Malaria ";
  public static final int SUFFIX_LENGTH = "1x6".length();

  private ALColumnCode columnCode;
  private Long treatmentsValue;
  private Long existentStockValue;


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
