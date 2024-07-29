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

package org.openlmis.core.model.repository;

import android.content.Context;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.openlmis.core.constant.ReportConstants;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.MMIARepository.ReportType;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

public class MMTBRepository extends RnrFormRepository {

  private static final List<KeyEntry> KEY_ENTRIES = Collections.unmodifiableList(Arrays.asList(
      // treatment table
      new KeyEntry(ReportConstants.KEY_TREATMENT_ADULT_SENSITIVE_INTENSIVE,
          ReportConstants.KEY_TREATMENT_ADULT_TABLE, 0),
      new KeyEntry(ReportConstants.KEY_TREATMENT_ADULT_SENSITIVE_MAINTENANCE,
          ReportConstants.KEY_TREATMENT_ADULT_TABLE, 1),
      new KeyEntry(ReportConstants.KEY_TREATMENT_ADULT_MR_INDUCTION, ReportConstants.KEY_TREATMENT_ADULT_TABLE,
          2),
      new KeyEntry(ReportConstants.KEY_TREATMENT_ADULT_MR_INTENSIVE, ReportConstants.KEY_TREATMENT_ADULT_TABLE,
          3),
      new KeyEntry(ReportConstants.KEY_TREATMENT_ADULT_MR_MAINTENANCE,
          ReportConstants.KEY_TREATMENT_ADULT_TABLE, 4),
      new KeyEntry(ReportConstants.KEY_TREATMENT_ADULT_XR_INDUCTION, ReportConstants.KEY_TREATMENT_ADULT_TABLE,
          5),
      new KeyEntry(ReportConstants.KEY_TREATMENT_ADULT_XR_MAINTENANCE,
          ReportConstants.KEY_TREATMENT_ADULT_TABLE, 6),
      new KeyEntry(ReportConstants.KEY_TREATMENT_PEDIATRIC_SENSITIVE_INTENSIVE,
          ReportConstants.KEY_TREATMENT_PEDIATRIC_TABLE, 7),
      new KeyEntry(ReportConstants.KEY_TREATMENT_PEDIATRIC_SENSITIVE_MAINTENANCE,
          ReportConstants.KEY_TREATMENT_PEDIATRIC_TABLE, 8),
      new KeyEntry(ReportConstants.KEY_TREATMENT_PEDIATRIC_MR_INDUCTION,
          ReportConstants.KEY_TREATMENT_PEDIATRIC_TABLE, 9),
      new KeyEntry(ReportConstants.KEY_TREATMENT_PEDIATRIC_MR_INTENSIVE,
          ReportConstants.KEY_TREATMENT_PEDIATRIC_TABLE, 10),
      new KeyEntry(ReportConstants.KEY_TREATMENT_PEDIATRIC_MR_MAINTENANCE,
          ReportConstants.KEY_TREATMENT_PEDIATRIC_TABLE, 11),
      new KeyEntry(ReportConstants.KEY_TREATMENT_PEDIATRIC_XR_INDUCTION,
          ReportConstants.KEY_TREATMENT_PEDIATRIC_TABLE, 12),
      new KeyEntry(ReportConstants.KEY_TREATMENT_PEDIATRIC_XR_MAINTENANCE,
          ReportConstants.KEY_TREATMENT_PEDIATRIC_TABLE, 13),
      // pharmacy product table
      new KeyEntry(ReportConstants.KEY_PHARMACY_PRODUCT_ISONIAZIDA_100,
          ReportConstants.KEY_PHARMACY_PRODUCT_TABLE, 14),
      new KeyEntry(ReportConstants.KEY_PHARMACY_PRODUCT_ISONIAZIDA_300,
          ReportConstants.KEY_PHARMACY_PRODUCT_TABLE, 15),
      new KeyEntry(ReportConstants.KEY_PHARMACY_PRODUCT_LEVOFLOXACINA_100,
          ReportConstants.KEY_PHARMACY_PRODUCT_TABLE, 16),
      new KeyEntry(ReportConstants.KEY_PHARMACY_PRODUCT_LEVOFLOXACINA_250,
          ReportConstants.KEY_PHARMACY_PRODUCT_TABLE, 17),
      new KeyEntry(ReportConstants.KEY_PHARMACY_PRODUCT_RIFAPENTINA_300,
          ReportConstants.KEY_PHARMACY_PRODUCT_TABLE, 18),
      new KeyEntry(ReportConstants.KEY_PHARMACY_PRODUCT_RIFAPENTINA_150,
          ReportConstants.KEY_PHARMACY_PRODUCT_TABLE, 19),
      new KeyEntry(ReportConstants.KEY_PHARMACY_PRODUCT_PIRIDOXINA_25,
          ReportConstants.KEY_PHARMACY_PRODUCT_TABLE, 20),
      new KeyEntry(ReportConstants.KEY_PHARMACY_PRODUCT_PIRIDOXINA_50,
          ReportConstants.KEY_PHARMACY_PRODUCT_TABLE, 21),
      // mmtb new patient table
      new KeyEntry(ReportConstants.KEY_NEW_ADULT_SENSITIVE, ReportConstants.KEY_NEW_PATIENT_TABLE, 22),
      new KeyEntry(ReportConstants.KEY_NEW_ADULT_MR, ReportConstants.KEY_NEW_PATIENT_TABLE, 23),
      new KeyEntry(ReportConstants.KEY_NEW_ADULT_XR, ReportConstants.KEY_NEW_PATIENT_TABLE, 24),
      new KeyEntry(ReportConstants.KEY_NEW_CHILD_SENSITIVE, ReportConstants.KEY_NEW_PATIENT_TABLE, 25),
      new KeyEntry(ReportConstants.KEY_NEW_CHILD_MR, ReportConstants.KEY_NEW_PATIENT_TABLE, 26),
      new KeyEntry(ReportConstants.KEY_NEW_CHILD_XR, ReportConstants.KEY_NEW_PATIENT_TABLE, 27),
      new KeyEntry(ReportConstants.KEY_NEW_PATIENT_TOTAL, ReportConstants.KEY_NEW_PATIENT_TABLE, 28),
      // mmtb follow-up prophylaxis table
      new KeyEntry(ReportConstants.KEY_START_PHASE, ReportConstants.KEY_PROPHYLAXIS_TABLE, 29),
      new KeyEntry(ReportConstants.KEY_CONTINUE_PHASE, ReportConstants.KEY_PROPHYLAXIS_TABLE, 30),
      new KeyEntry(ReportConstants.KEY_FINAL_PHASE, ReportConstants.KEY_PROPHYLAXIS_TABLE, 31),
      new KeyEntry(ReportConstants.KEY_PROPHYLAXIS_TABLE_TOTAL, ReportConstants.KEY_PROPHYLAXIS_TABLE, 32),
      // mmtb type of dispensation of prophylactics table
      new KeyEntry(ReportConstants.KEY_FREQUENCY_MONTHLY, ReportConstants.KEY_TYPE_OF_DISPENSATION_TABLE, 33),
      new KeyEntry(ReportConstants.KEY_FREQUENCY_QUARTERLY, ReportConstants.KEY_TYPE_OF_DISPENSATION_TABLE, 34),
      new KeyEntry(ReportConstants.KEY_FREQUENCY_TOTAL, ReportConstants.KEY_TYPE_OF_DISPENSATION_TABLE, 35)
  ));

  public static int getDisplayOrderByKey(String attrKey) {
    for (KeyEntry keyEntry : KEY_ENTRIES) {
      if (keyEntry.attrKey.equals(attrKey)) {
        return keyEntry.displayOrder;
      }
    }
    return 0;
  }

  @Inject
  public MMTBRepository(Context context) {
    super(context);
    programCode = Constants.MMTB_PROGRAM_CODE;
  }

  @Override
  public List<RnrFormItem> generateRnrFormItems(RnRForm form, List<StockCard> stockCards) throws LMISException {
    return fillAllProducts(form, super.generateRnrFormItems(form, stockCards));
  }

  @Override
  protected RnrFormItem createRnrFormItemByPeriod(
      StockCard stockCard,
      List<StockMovementItem> stockMovementItems,
      Date periodBegin
  ) {
    RnrFormItem rnrFormItem = new RnrFormItem();

    long initialAmount;

    if (stockMovementItems == null || stockMovementItems.isEmpty()) {
      rnrFormItem.setReceived(0);
      initialAmount = getInitialAmountIfPeriodMovementItemsAreEmpty(stockCard, periodBegin);
    } else {
      this.assignMMTBTotalValues(rnrFormItem, stockMovementItems);
      initialAmount = stockMovementItems.get(0).getStockOnHand();
    }
    updateInitialAmount(rnrFormItem, initialAmount);

    rnrFormItem.setProduct(stockCard.getProduct());
    Date earliestLotExpiryDate = stockCard.getEarliestLotExpiryDate();
    if (earliestLotExpiryDate != null) {
      rnrFormItem.setValidate(DateUtil.formatDate(earliestLotExpiryDate, DateUtil.SIMPLE_DATE_FORMAT));
    }
    return rnrFormItem;
  }

  private void assignMMTBTotalValues(RnrFormItem rnrFormItem, List<StockMovementItem> stockMovementItems) {
    long totalReceived = 0;
    for (StockMovementItem item : stockMovementItems) {
      if (MovementReasonManager.MovementType.RECEIVE == item.getMovementType()) {
        totalReceived += item.getMovementQuantity();
      }
    }
    rnrFormItem.setReceived(totalReceived);
  }

  @Override
  protected List<RegimenItemThreeLines> generateRegimeThreeLineItems(RnRForm form) {
    List<String> regimeThreeLines = new ArrayList<>();
    regimeThreeLines.add(ReportConstants.KEY_SERVICE_ADULT);
    regimeThreeLines.add(ReportConstants.KEY_SERVICE_LESS_THAN_25);
    regimeThreeLines.add(ReportConstants.KEY_SERVICE_MORE_THAN_25);

    return FluentIterable.from(regimeThreeLines)
        .transform(type -> {
          RegimenItemThreeLines itemThreeLines = new RegimenItemThreeLines(type);
          itemThreeLines.setForm(form);
          return itemThreeLines;
        }).toList();
  }

  @Override
  protected List<BaseInfoItem> generateBaseInfoItems(RnRForm form, ReportType type) {
    return FluentIterable.from(KEY_ENTRIES)
        .transform(keyEntry -> new BaseInfoItem(keyEntry.attrKey, BaseInfoItem.TYPE.INT, form, keyEntry.tableName,
            keyEntry.displayOrder))
        .toSortedList((o1, o2) -> o1.getDisplayOrder() - o2.getDisplayOrder());
  }

  @Override
  protected void updateInitialAmount(RnrFormItem rnrFormItem, Long initialAmount) {
    rnrFormItem.setIsCustomAmount(initialAmount == null);
    rnrFormItem.setInitialAmount(initialAmount);
  }

  @AllArgsConstructor
  @Data
  private static class KeyEntry {

    private String attrKey;
    private String tableName;
    private int displayOrder;
  }
}
