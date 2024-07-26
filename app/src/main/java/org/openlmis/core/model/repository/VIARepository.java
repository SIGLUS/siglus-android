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

import static org.openlmis.core.manager.MovementReasonManager.MovementType.INITIAL_INVENTORY;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.PHYSICAL_INVENTORY;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

public class VIARepository extends RnrFormRepository {

  public static final String ATTR_CONSULTATION = "consultation";

  @Inject
  public VIARepository(Context context) {
    super(context);
    programCode = Constants.VIA_PROGRAM_CODE;
  }

  @Override
  protected List<BaseInfoItem> generateBaseInfoItems(RnRForm form, MMIARepository.ReportType type) {
    BaseInfoItem newPatients = new BaseInfoItem(ATTR_CONSULTATION, BaseInfoItem.TYPE.STRING, form,
        "", 0);
    List<BaseInfoItem> baseInfoItemList = new ArrayList<>();
    baseInfoItemList.add(newPatients);
    return baseInfoItemList;
  }

  protected List<RnrFormItem> generateRnrFormItems(
      RnRForm form, List<StockCard> stockCards, Period period
  ) throws LMISException {
    List<RnrFormItem> rnrFormItems = new ArrayList<>();

    HashMap<String, String> stringToCategory = getProductCodeToCategory();
    Set<String> stockCardIds = from(stockCards)
        .transform(
            stockCard -> stockCard == null ? null : String.valueOf(stockCard.getId())
        ).toSet();

    Map<String, List<StockMovementItem>> idToStockMovements =
        stockMovementRepository.queryStockMovement(
            stockCardIds, form.getPeriodBegin(), form.getPeriodEnd()
        );

    for (StockCard stockCard : stockCards) {
      RnrFormItem rnrFormItem = createRnrFormItemByPeriod(stockCard,
          idToStockMovements.get(String.valueOf(stockCard.getId())), period);
      rnrFormItem.setForm(form);
      rnrFormItems.add(rnrFormItem);
      rnrFormItem.setCategory(stringToCategory.get(rnrFormItem.getProduct().getCode()));
    }

    return rnrFormItems;
  }

  private RnrFormItem createRnrFormItemByPeriod(
      StockCard stockCard, List<StockMovementItem> stockMovementItems, Period period
  ) {
    int size = stockMovementItems.size();
    int inventoryStartIndex = 0;
    int inventoryEndIndex = size;

    String beginDateString = formatDateToStringWithDBFormat(period.getBegin().toDate());
    String endDateString = formatDateToStringWithDBFormat(period.getEnd().toDate());

    for (int index = 0; index < size; index++) {
      StockMovementItem stockMovementItem = stockMovementItems.get(index);
      if (stockMovementItem != null && isInventoryType(stockMovementItem.getMovementType())) {
        String movementDateString = formatDateToStringWithDBFormat(
            stockMovementItem.getMovementDate());
        if (beginDateString.equals(movementDateString)) {
          inventoryStartIndex = index;
        } else if (endDateString.equals(movementDateString)) {
          inventoryEndIndex = index;
        }
      }
    }
    // the range between inventory movement data is valid data for this period
    List<StockMovementItem> filteredStockMovementItems = stockMovementItems.subList(
        inventoryStartIndex, inventoryEndIndex);

    return createRnrFormItemByPeriod(stockCard, filteredStockMovementItems);
  }

  private @NonNull String formatDateToStringWithDBFormat(Date date) {
    return DateUtil.formatDate(date, DateUtil.DB_DATE_FORMAT);
  }

  private boolean isInventoryType(MovementType movementType) {
    return PHYSICAL_INVENTORY == movementType || INITIAL_INVENTORY == movementType;
  }
}
