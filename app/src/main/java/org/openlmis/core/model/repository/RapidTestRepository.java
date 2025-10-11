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
import java.util.Date;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.helper.FormHelper;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

public class RapidTestRepository extends RnrFormRepository {

  @Inject
  FormHelper formHelper;

  @Inject
  public RapidTestRepository(Context context) {
    super(context);
    programCode = Constants.RAPID_TEST_PROGRAM_CODE;
  }

  @Override
  public List<RnrFormItem> generateRnrFormItems(RnRForm form, List<StockCard> stockCards)
      throws LMISException {
    return fillAllProducts(form, super.generateRnrFormItems(form, stockCards));
  }

  @Override
  protected RnrFormItem createRnrFormItemByPeriod(
      StockCard stockCard,
      List<StockMovementItem> stockMovementItems,
      Date periodBegin
  ) {
    RnrFormItem rnrFormItem = new RnrFormItem();

    if (stockMovementItems.isEmpty()) {
      this.initMmitRnrFormItemWithoutMovement(rnrFormItem, lastRnrInventory(stockCard));
    } else {
      FormHelper.StockMovementModifiedItem modifiedItem = formHelper
              .assignTotalValues(stockMovementItems);
      rnrFormItem.setReceived(modifiedItem.getTotalReceived());
      rnrFormItem.setProduct(stockCard.getProduct());
      rnrFormItem.setInitialAmount(getMmitInitialAmount(stockCard, stockMovementItems));
    }

    Date earliestLotExpiryDate = stockCard.getEarliestLotExpiryDate();
    if (earliestLotExpiryDate != null) {
      rnrFormItem.setValidate(DateUtil.formatDate(earliestLotExpiryDate, DateUtil.SIMPLE_DATE_FORMAT));
    }
    return rnrFormItem;
  }

  protected long getMmitInitialAmount(StockCard stockCard,
                                      List<StockMovementItem> stockMovementItems) {
    List<RnRForm> rnRForms = listInclude(RnRForm.Emergency.NO, programCode);
    if (rnRForms.size() == 1) {
      return stockMovementItems.get(0).calculatePreviousSOH();
    }
    Long lastRnrInventory = lastRnrInventory(stockCard.getProduct());
    return lastRnrInventory != null ? lastRnrInventory
            : stockMovementItems.get(0).calculatePreviousSOH();
  }

  @Override
  protected void updateInitialAmount(RnrFormItem rnrFormItem, Long initialAmount) {
    rnrFormItem.setIsCustomAmount(initialAmount == null);
    rnrFormItem.setInitialAmount(initialAmount);
  }

  private void initMmitRnrFormItemWithoutMovement(RnrFormItem rnrFormItem, long lastRnrInventory) {
    rnrFormItem.setReceived(0);
    rnrFormItem.setCalculatedOrderQuantity(0L);
    rnrFormItem.setInitialAmount(lastRnrInventory);
  }
}
