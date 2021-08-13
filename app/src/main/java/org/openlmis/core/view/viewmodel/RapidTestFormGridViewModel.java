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
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.ProgramDataColumn;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.model.UsageColumnsMap;
import org.openlmis.core.model.UsageInformationLineItem;

@SuppressWarnings("squid:S1874")
@Data
public class RapidTestFormGridViewModel {

  public enum RapidTestGridColumnCode {
    CONSUMPTION,
    POSITIVE,
    UNJUSTIFIED
  }

  public enum ColumnCode {
    HIVDETERMINE,
    HIVUNIGOLD,
    SYPHILLIS,
    MALARIA;

    @Override
    public String toString() {
      return StringUtils.upperCase(name());
    }
  }

  ColumnCode columnCode;
  String consumptionValue = StringUtils.EMPTY;
  String positiveValue = StringUtils.EMPTY;
  String unjustifiedValue = StringUtils.EMPTY;
  UsageColumnsMap positiveColumn;
  UsageColumnsMap consumeColumn;
  UsageColumnsMap unjustifiedColumn;
  Boolean isNeedAllAPEValue = false;
  Boolean isAPE = false;

  private static final String COLUMN_CODE_PREFIX_CONSUME = "CONSUME_";
  private static final String COLUMN_CODE_PREFIX_POSITIVE = "POSITIVE_";
  private static final String COLUMN_CODE_PREFIX_UNJUSTIFIED = "UNJUSTIFIED_";

  RapidTestFormGridViewModel(ColumnCode columnCode) {
    this.columnCode = columnCode;
  }

  public boolean validate() {
    try {
      return isEmpty()
          || (Long.parseLong(consumptionValue) >= Long.parseLong(positiveValue)
          && Long.parseLong(unjustifiedValue) >= 0);
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public boolean validatePositive() {
    try {
      return (StringUtils.isEmpty(consumptionValue)
          && StringUtils.isEmpty(positiveValue)
          && StringUtils.isEmpty(unjustifiedValue))
          || (Long.parseLong(consumptionValue) >= Long.parseLong(positiveValue));
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public boolean validateUnjustified() {
    try {
      return isEmpty()
          || (Long.parseLong(consumptionValue) >= Long.parseLong(positiveValue)
          && Long.parseLong(unjustifiedValue) >= 0);
    } catch (NumberFormatException e) {
      return false;
    }
  }


  public void setValue(UsageColumnsMap column, int value) {
    setConsumptionValue(column, value);
    setPositiveValue(column, value);
    setUnjustifiedValue(column, value);
  }

  public void setValue(RapidTestGridColumnCode column, String value) {
    switch (column) {
      case POSITIVE:
        positiveValue = value;
        break;
      case CONSUMPTION:
        consumptionValue = value;
        break;
      case UNJUSTIFIED:
        unjustifiedValue = value;
        break;
      default:
        // do nothing
    }
  }

  public List<UsageInformationLineItem> convertFormGridViewModelToDataModel(
      MovementReasonManager.MovementReason issueReason) {
    List<UsageInformationLineItem> programDataFormItems = new ArrayList<>();
    setConsumptionFormItem(issueReason, programDataFormItems);
    setPositiveFormItem(issueReason, programDataFormItems);
    setUnjustifiedFormItem(issueReason, programDataFormItems);
    return programDataFormItems;
  }

  public void clear(RapidTestGridColumnCode column) {
    switch (column) {
      case POSITIVE:
        positiveValue = StringUtils.EMPTY;
        break;
      case CONSUMPTION:
        consumptionValue = StringUtils.EMPTY;
        break;
      case UNJUSTIFIED:
        unjustifiedValue = StringUtils.EMPTY;
        break;
      default:
        // do nothing
    }
  }

  public boolean isEmpty() {
    return StringUtils.isEmpty(consumptionValue)
        && StringUtils.isEmpty(positiveValue)
        && StringUtils.isEmpty(unjustifiedValue);
  }

  public boolean isNeedAddGridViewWarning() {
    return isAPE && isNeedAllAPEValue && !isAllNotEmpty();
  }

  private boolean isAllNotEmpty() {
    return StringUtils.isNotEmpty(consumptionValue)
        && StringUtils.isNotEmpty(positiveValue)
        && StringUtils.isNotEmpty(unjustifiedValue);
  }

  public boolean isAddUnjustified() {
    return StringUtils.isEmpty(unjustifiedValue)
        && !(StringUtils.isEmpty(consumptionValue)
        && StringUtils.isEmpty(positiveValue));
  }

  private String generateFullColumnName(String prefix) {
    return prefix + StringUtils.upperCase(getColumnCode().name());
  }

  private void setUnjustifiedValue(UsageColumnsMap column, int value) {
    if (column.getCode().contains(COLUMN_CODE_PREFIX_UNJUSTIFIED)) {
      unjustifiedColumn = column;
      setUnjustifiedValue(String.valueOf(value));
    }
  }

  private void setPositiveValue(UsageColumnsMap column, int value) {
    if (column.getCode().contains(COLUMN_CODE_PREFIX_POSITIVE)) {
      positiveColumn = column;
      setPositiveValue(String.valueOf(value));
    }
  }

  private void setConsumptionValue(UsageColumnsMap column, int value) {
    if (column.getCode().contains(COLUMN_CODE_PREFIX_CONSUME)) {
      consumeColumn = column;
      setConsumptionValue(String.valueOf(value));
    }
  }

  private void setUnjustifiedFormItem(MovementReasonManager.MovementReason issueReason,
      List<UsageInformationLineItem> programDataFormItems) {
    if (!StringUtils.isEmpty(getUnjustifiedValue())) {
      if (unjustifiedColumn == null) {
        unjustifiedColumn = new UsageColumnsMap();
        unjustifiedColumn.setCode(generateFullColumnName(COLUMN_CODE_PREFIX_UNJUSTIFIED));
      }
      UsageInformationLineItem unjustfiedDataFormItem = new UsageInformationLineItem(issueReason.getCode(),
          unjustifiedColumn, Integer.parseInt(getUnjustifiedValue()));
      programDataFormItems.add(unjustfiedDataFormItem);
    }
  }

  private void setPositiveFormItem(MovementReasonManager.MovementReason issueReason,
      List<UsageInformationLineItem> programDataFormItems) {
    if (!StringUtils.isEmpty(getPositiveValue())) {
      if (positiveColumn == null) {
        positiveColumn = new UsageColumnsMap();
        positiveColumn.setCode(generateFullColumnName(COLUMN_CODE_PREFIX_POSITIVE));
      }
      UsageInformationLineItem positiveDataFormItem = new UsageInformationLineItem(issueReason.getCode(),
          positiveColumn, Integer.parseInt(getPositiveValue()));
      programDataFormItems.add(positiveDataFormItem);
    }
  }

  private void setConsumptionFormItem(MovementReasonManager.MovementReason issueReason,
      List<UsageInformationLineItem> programDataFormItems) {
    if (!StringUtils.isEmpty(getConsumptionValue())) {
      if (consumeColumn == null) {
        consumeColumn = new UsageColumnsMap();
        consumeColumn.setCode(generateFullColumnName(COLUMN_CODE_PREFIX_CONSUME));
      }
      UsageInformationLineItem consumeDataFormItem = new UsageInformationLineItem(issueReason.getCode(),
          consumeColumn, Integer.parseInt(getConsumptionValue()));
      programDataFormItems.add(consumeDataFormItem);
    }
  }

}
