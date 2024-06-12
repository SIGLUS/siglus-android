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

import static org.openlmis.core.persistence.migrations.UpdateUsageColumnsMap.USAGE_COLUMN_SEPARATOR;
import static org.openlmis.core.persistence.migrations.UpdateUsageColumnsMapV2.POSITIVE_HIV;
import static org.openlmis.core.persistence.migrations.UpdateUsageColumnsMapV2.POSITIVE_SYPHILIS;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.enumeration.MMITGridErrorType;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.TestConsumptionItem;
import org.openlmis.core.model.UsageColumnsMap;

@SuppressWarnings("squid:S1874")
@Data
public class RapidTestFormGridViewModel {

  public enum RapidTestGridColumnCode {
    CONSUMPTION,
    POSITIVE,
    UNJUSTIFIED,
    POSITIVE_HIV,
    POSITIVE_SYPHILIS,
  }

  public enum ColumnCode {
    DUOTESTEHIVSIFILIS,
    HEPATITEBTESTES,
    TDRORALDEHIV,
    NEWTEST,
    HIVDETERMINE,
    HIVUNIGOLD,
    SYPHILLIS,
    MALARIA;

    @NonNull
    @Override
    public String toString() {
      return StringUtils.upperCase(name());
    }
  }

  ColumnCode columnCode;
  String consumptionValue = StringUtils.EMPTY;
  String positiveValue = StringUtils.EMPTY;
  String positiveHivValue = StringUtils.EMPTY;
  String positiveSyphilisValue = StringUtils.EMPTY;
  String unjustifiedValue = StringUtils.EMPTY;
  UsageColumnsMap positiveColumn;
  UsageColumnsMap positiveHivColumn;
  UsageColumnsMap positiveSyphilisColumn;
  UsageColumnsMap consumeColumn;
  UsageColumnsMap unjustifiedColumn;
  Boolean isNeedAllAPEValue = false;
  Boolean isAPE = false;
  RapidTestGridColumnCode invalidColumn;

  private static final String COLUMN_CODE_PREFIX_CONSUME = "CONSUME_";
  private static final String COLUMN_CODE_PREFIX_POSITIVE = "POSITIVE_";
  private static final String COLUMN_CODE_PREFIX_UNJUSTIFIED = "UNJUSTIFIED_";
  private static final String COLUMN_CODE_PREFIX_POSITIVE_HIV = POSITIVE_HIV + USAGE_COLUMN_SEPARATOR;
  private static final String COLUMN_CODE_PREFIX_POSITIVE_SYPHILIS = POSITIVE_SYPHILIS + USAGE_COLUMN_SEPARATOR;

  RapidTestFormGridViewModel(ColumnCode columnCode) {
    this.columnCode = columnCode;
  }

  public boolean validate() {
    try {
      return isEmpty()
          || (isConsumptionGreaterThanPositive()
          && Long.parseLong(unjustifiedValue) >= 0);
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private boolean isConsumptionGreaterThanPositive() {
    return Long.parseLong(consumptionValue) >= calculatePositiveValue();
  }

  private long calculatePositiveValue() {
    if (isDuoTest()) {
      return Long.parseLong(positiveHivValue) + Long.parseLong(positiveSyphilisValue);
    }
    return Long.parseLong(positiveValue);
  }

  public boolean isDuoTest() {
    return columnCode == ColumnCode.DUOTESTEHIVSIFILIS;
  }

  public MMITGridErrorType validateThreeGrid() {
    if (!validateConsumption()) {
      this.setInvalidColumn(RapidTestGridColumnCode.CONSUMPTION);
      return MMITGridErrorType.EMPTY_CONSUMPTION;
    } else if (!validatePositiveIsEmpty()) {
      this.setInvalidColumn(RapidTestGridColumnCode.POSITIVE);
      return MMITGridErrorType.EMPTY_POSITIVE;
    } else if (!validateUnjustified()) {
      this.setInvalidColumn(RapidTestGridColumnCode.UNJUSTIFIED);
      return MMITGridErrorType.EMPTY_UNJUSTIFIED;
    } else if (!validatePositiveMoreThanCon()) {
      this.setInvalidColumn(RapidTestGridColumnCode.POSITIVE);
      return MMITGridErrorType.POSITIVE_MORE_THAN_CONSUMPTION;
    } else if (isNeedAddGridViewWarning()) {
      this.setInvalidColumn(RapidTestGridColumnCode.CONSUMPTION);
      return MMITGridErrorType.APE_ALL_EMPTY;
    } else {
      return MMITGridErrorType.NO_ERROR;
    }
  }

  private boolean validateConsumption() {
    try {
      return !((isPositiveNotEmpty() || StringUtils.isNotEmpty(unjustifiedValue))
          && StringUtils.isEmpty(consumptionValue));
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private boolean isPositiveNotEmpty() {
    if (isDuoTest()) {
      return StringUtils.isNotEmpty(positiveHivValue)
          && StringUtils.isNotEmpty(positiveSyphilisValue);
    }
    return StringUtils.isNotEmpty(positiveValue);
  }

  private boolean validatePositiveIsEmpty() {
    try {
      return !(StringUtils.isNotEmpty(consumptionValue) && !isPositiveNotEmpty());
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private boolean validatePositiveMoreThanCon() {
    try {
      return !positiveGreaterThanConsumption();
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private boolean validateUnjustified() {
    try {
      return !(StringUtils.isNotEmpty(consumptionValue)
          && isPositiveNotEmpty()
          && StringUtils.isEmpty(unjustifiedValue));
    } catch (NumberFormatException e) {
      return false;
    }
  }


  public void setValue(UsageColumnsMap column, int value) {
    setConsumptionValue(column, value);
    setPositiveValue(column, value);
    setPositiveHivValue(column, value);
    setPositiveSyphilisValue(column, value);
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
      case POSITIVE_HIV:
        positiveHivValue = value;
        break;
      case POSITIVE_SYPHILIS:
        positiveSyphilisValue = value;
        break;
      default:
        // do nothing
    }
  }

  public List<TestConsumptionItem> convertFormGridViewModelToDataModel(
      MovementReasonManager.MovementReason issueReason) {
    List<TestConsumptionItem> testConsumptionLineItems = new ArrayList<>();
    setConsumptionFormItem(issueReason, testConsumptionLineItems);
    if (isDuoTest()) {
      setPositiveHivFormItem(issueReason, testConsumptionLineItems);
      setPositiveSyphilisFormItem(issueReason, testConsumptionLineItems);
    } else {
      setPositiveFormItem(issueReason, testConsumptionLineItems);
    }
    setUnjustifiedFormItem(issueReason, testConsumptionLineItems);
    return testConsumptionLineItems;
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
      case POSITIVE_HIV:
        positiveHivValue = StringUtils.EMPTY;
        break;
      case POSITIVE_SYPHILIS:
        positiveSyphilisValue = StringUtils.EMPTY;
        break;
      default:
        // do nothing
    }
  }

  public boolean isEmpty() {
    return StringUtils.isEmpty(consumptionValue)
        && !isPositiveNotEmpty()
        && StringUtils.isEmpty(unjustifiedValue);
  }

  public boolean isNeedAddGridViewWarning() {
    return isAPE && isNeedAllAPEValue && !isAllNotEmpty();
  }

  private boolean positiveGreaterThanConsumption() {
    if (StringUtils.isNotEmpty(consumptionValue) && isPositiveNotEmpty()) {
      return Long.parseLong(consumptionValue) < calculatePositiveValue();
    } else {
      return false;
    }
  }

  private boolean isAllNotEmpty() {
    return StringUtils.isNotEmpty(consumptionValue)
        && isPositiveNotEmpty()
        && StringUtils.isNotEmpty(unjustifiedValue);
  }

  public boolean isAddUnjustified() {
    return StringUtils.isEmpty(unjustifiedValue)
        && !(StringUtils.isEmpty(consumptionValue)
        && !isPositiveNotEmpty());
  }

  private String generateFullColumnName(String prefix) {
    return prefix + StringUtils.upperCase(columnCode.name());
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

  private void setPositiveHivValue(UsageColumnsMap column, int value) {
    if (column.getCode().contains(COLUMN_CODE_PREFIX_POSITIVE_HIV)) {
      positiveHivColumn = column;
      positiveHivValue = String.valueOf(value);
    }
  }
  
  private void setPositiveSyphilisValue(UsageColumnsMap column, int value) {
    if (column.getCode().contains(COLUMN_CODE_PREFIX_POSITIVE_SYPHILIS)) {
      positiveSyphilisColumn = column;
      positiveSyphilisValue = String.valueOf(value);
    }
  }

  private void setConsumptionValue(UsageColumnsMap column, int value) {
    if (column.getCode().contains(COLUMN_CODE_PREFIX_CONSUME)) {
      consumeColumn = column;
      setConsumptionValue(String.valueOf(value));
    }
  }

  private void setUnjustifiedFormItem(MovementReasonManager.MovementReason issueReason,
      List<TestConsumptionItem> programDataFormItems) {
    if (!StringUtils.isEmpty(getUnjustifiedValue())) {
      if (unjustifiedColumn == null) {
        unjustifiedColumn = new UsageColumnsMap();
        unjustifiedColumn.setCode(generateFullColumnName(COLUMN_CODE_PREFIX_UNJUSTIFIED));
      }
      TestConsumptionItem unjustfiedDataFormItem = new TestConsumptionItem(issueReason.getCode(),
          unjustifiedColumn, Integer.parseInt(getUnjustifiedValue()));
      programDataFormItems.add(unjustfiedDataFormItem);
    }
  }

  private void setPositiveFormItem(MovementReasonManager.MovementReason issueReason,
      List<TestConsumptionItem> programDataFormItems) {
    if (!StringUtils.isEmpty(getPositiveValue())) {
      if (positiveColumn == null) {
        positiveColumn = new UsageColumnsMap();
        positiveColumn.setCode(generateFullColumnName(COLUMN_CODE_PREFIX_POSITIVE));
      }
      TestConsumptionItem positiveDataFormItem = new TestConsumptionItem(issueReason.getCode(),
          positiveColumn, Integer.parseInt(getPositiveValue()));
      programDataFormItems.add(positiveDataFormItem);
    }
  }

  private void setPositiveHivFormItem(
      MovementReasonManager.MovementReason issueReason,
      List<TestConsumptionItem> programDataFormItems
  ) {
    if (!StringUtils.isEmpty(positiveHivValue)) {
      if (positiveHivColumn == null) {
        positiveHivColumn = new UsageColumnsMap();
        positiveHivColumn.setCode(generateFullColumnName(COLUMN_CODE_PREFIX_POSITIVE_HIV));
      }
      TestConsumptionItem positiveHivDataFormItem = new TestConsumptionItem(
          issueReason.getCode(), positiveHivColumn, Integer.parseInt(positiveHivValue)
      );
      programDataFormItems.add(positiveHivDataFormItem);
    }
  }

  private void setPositiveSyphilisFormItem(
      MovementReasonManager.MovementReason issueReason,
      List<TestConsumptionItem> programDataFormItems
  ) {
    if (!StringUtils.isEmpty(positiveSyphilisValue)) {
      if (positiveSyphilisColumn == null) {
        positiveSyphilisColumn = new UsageColumnsMap();
        positiveSyphilisColumn.setCode(generateFullColumnName(COLUMN_CODE_PREFIX_POSITIVE_SYPHILIS));
      }
      TestConsumptionItem positiveSyphilisDataFormItem = new TestConsumptionItem(
          issueReason.getCode(), positiveSyphilisColumn, Integer.parseInt(positiveSyphilisValue)
      );
      programDataFormItems.add(positiveSyphilisDataFormItem);
    }
  }
  
  private void setConsumptionFormItem(MovementReasonManager.MovementReason issueReason,
      List<TestConsumptionItem> programDataFormItems) {
    if (!StringUtils.isEmpty(consumptionValue)) {
      if (consumeColumn == null) {
        consumeColumn = new UsageColumnsMap();
        consumeColumn.setCode(generateFullColumnName(COLUMN_CODE_PREFIX_CONSUME));
      }
      TestConsumptionItem consumeDataFormItem = new TestConsumptionItem(issueReason.getCode(),
          consumeColumn, Integer.parseInt(consumptionValue));
      programDataFormItems.add(consumeDataFormItem);
    }
  }
}
