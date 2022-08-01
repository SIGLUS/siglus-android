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

import static org.openlmis.core.utils.Constants.ATTR_TABLE_DISPENSED_DM;
import static org.openlmis.core.utils.Constants.ATTR_TABLE_DISPENSED_DS;
import static org.openlmis.core.utils.Constants.ATTR_TABLE_DISPENSED_DS1;
import static org.openlmis.core.utils.Constants.ATTR_TABLE_DISPENSED_DS2;
import static org.openlmis.core.utils.Constants.ATTR_TABLE_DISPENSED_DS3;
import static org.openlmis.core.utils.Constants.ATTR_TABLE_DISPENSED_DS4;
import static org.openlmis.core.utils.Constants.ATTR_TABLE_DISPENSED_DS5;
import static org.openlmis.core.utils.Constants.ATTR_TABLE_DISPENSED_DT;
import static org.openlmis.core.utils.Constants.ATTR_TABLE_DISPENSED_DT1;
import static org.openlmis.core.utils.Constants.ATTR_TABLE_DISPENSED_DT2;

import android.content.Context;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.inject.InjectResource;

public class MMIARepository extends RnrFormRepository {

  @InjectResource(R.string.table_arvt_key)
  public String attrTableTravKey;

  @InjectResource(R.string.table_trav_label_new_key)
  public String attrTableTravNewKey;

  @InjectResource(R.string.table_trav_label_maintenance_key)
  public String attrTableTravMaintenanceKey;

  @InjectResource(R.string.table_trav_label_alteration_key)
  public String attrTableTravAlterationKey;

  @InjectResource(R.string.table_trav_label_transit_key)
  public String attrTableTravTransitKey;

  @InjectResource(R.string.table_trav_label_transfers_key)
  public String attrTableTravTransferKey;

  @InjectResource(R.string.table_patients_key)
  public String attrTablePatientsKey;

  @InjectResource(R.string.table_patients_adults_key)
  public String attrTablePatientsAdultsKey;

  @InjectResource(R.string.table_patients_0to4_key)
  public String attrTablePatients0To4Key;

  @InjectResource(R.string.table_patients_5to9_key)
  public String attrTablePatients5To9Key;

  @InjectResource(R.string.table_patients_10to14_key)
  public String attrTablePatients10To14Key;

  @InjectResource(R.string.table_prophylaxy_key)
  public String attrTableProphylaxisKey;

  @InjectResource(R.string.table_prophylaxis_ppe_key)
  public String attrTableProphylaxisPpeKey;

  @InjectResource(R.string.table_prophylaxis_child_key)
  public String attrTableProphylaxisChildKey;

  @InjectResource(R.string.table_prophylaxis_total)
  public String attrTableProphylaxisTotal;

  @InjectResource(R.string.table_dispensed_key)
  public String attrTableDispensedKey;

  @InjectResource(R.string.key_regime_3lines_1)
  public String attrRegimeTypeFirstLineKey;

  @InjectResource(R.string.key_regime_3lines_2)
  public String attrRegimeTypeSecondLineKey;

  @InjectResource(R.string.key_regime_3lines_3)
  public String attrRegimeTypeThirdLineKey;

  @InjectResource(R.string.label_new_patients)
  public String attrNewPatients;

  @InjectResource(R.string.label_sustaining)
  public String attrSustaining;

  @InjectResource(R.string.label_alteration)
  public String attrAlteration;

  @InjectResource(R.string.label_total_month_dispense)
  public String attrTotalMonthDispense;

  @InjectResource(R.string.label_total_patients)
  public String attrTotalPatients;

  @InjectResource(R.string.label_ptv)
  public String attrPtv;

  @InjectResource(R.string.label_ppe)
  public String attrPpe;

  public enum ReportType {
    NEW,
    OLD,
  }

  @Inject
  public MMIARepository(Context context) {
    super(context);
    programCode = Constants.MMIA_PROGRAM_CODE;
  }

  public Map<String, Integer> getDisplayOrderMap() {
    Map<String, Integer> displayOrderHashMap = new HashMap<>();
    displayOrderHashMap.put(attrTableTravKey, 0);
    displayOrderHashMap.put(attrTableTravNewKey, 1);
    displayOrderHashMap.put(attrTableTravMaintenanceKey, 2);
    displayOrderHashMap.put(attrTableTravAlterationKey, 3);
    displayOrderHashMap.put(attrTableTravTransitKey, 4);
    displayOrderHashMap.put(attrTableTravTransferKey, 5);
    displayOrderHashMap.put(attrTablePatientsKey, 11);
    displayOrderHashMap.put(attrTablePatientsAdultsKey, 12);
    displayOrderHashMap.put(attrTablePatients0To4Key, 13);
    displayOrderHashMap.put(attrTablePatients5To9Key, 14);
    displayOrderHashMap.put(attrTablePatients10To14Key, 15);
    displayOrderHashMap.put(attrTableProphylaxisKey, 16);
    displayOrderHashMap.put(attrTableProphylaxisPpeKey, 17);
    displayOrderHashMap.put(attrTableProphylaxisChildKey, 19);
    displayOrderHashMap.put(attrTableDispensedKey, 21);
    displayOrderHashMap.put(ATTR_TABLE_DISPENSED_DS5, 22);
    displayOrderHashMap.put(ATTR_TABLE_DISPENSED_DS4, 23);
    displayOrderHashMap.put(ATTR_TABLE_DISPENSED_DS3, 24);
    displayOrderHashMap.put(ATTR_TABLE_DISPENSED_DS2, 25);
    displayOrderHashMap.put(ATTR_TABLE_DISPENSED_DS1, 26);
    displayOrderHashMap.put(ATTR_TABLE_DISPENSED_DS, 27);
    displayOrderHashMap.put(ATTR_TABLE_DISPENSED_DT2, 28);
    displayOrderHashMap.put(ATTR_TABLE_DISPENSED_DT1, 29);
    displayOrderHashMap.put(ATTR_TABLE_DISPENSED_DT, 30);
    displayOrderHashMap.put(ATTR_TABLE_DISPENSED_DM, 31);
    return displayOrderHashMap;
  }

  @Override
  protected List<RegimenItem> generateRegimeItems(RnRForm form) throws LMISException {
    List<RegimenItem> regimenItems = new ArrayList<>();
    for (Regimen regimen : regimenRepository.listDefaultRegime()) {
      RegimenItem item = new RegimenItem();
      item.setForm(form);
      item.setRegimen(regimen);
      regimenItems.add(item);
    }
    return regimenItems;
  }

  @Override
  protected List<RegimenItemThreeLines> generateRegimeThreeLineItems(RnRForm form) {
    List<String> regimeThreeLines = new ArrayList<>();
    regimeThreeLines.add(attrRegimeTypeFirstLineKey);
    regimeThreeLines.add(attrRegimeTypeSecondLineKey);
    regimeThreeLines.add(attrRegimeTypeThirdLineKey);

    return FluentIterable.from(regimeThreeLines)
        .transform(type -> {
          RegimenItemThreeLines itemThreeLines = new RegimenItemThreeLines(type);
          itemThreeLines.setForm(form);
          return itemThreeLines;
        }).toList();
  }

  @Override
  protected List<BaseInfoItem> generateBaseInfoItems(final RnRForm form, ReportType type) {

    if (ReportType.NEW != type) {
      ArrayList<String> attrs = new ArrayList<>();
      attrs.add(attrNewPatients);
      attrs.add(attrSustaining);
      attrs.add(attrAlteration);
      attrs.add(attrPtv);
      attrs.add(attrPpe);
      attrs.add(attrTotalMonthDispense);
      attrs.add(attrTotalPatients);

      return FluentIterable.from(attrs)
          .transform(attr -> new BaseInfoItem(attr, BaseInfoItem.TYPE.INT, form, "", 0))
          .toList();
    }

    Map<String, String> mAttrs = new HashMap<>();
    initAttrs(mAttrs);

    initDisplayOrder();

    return FluentIterable.from(mAttrs.keySet())
        .transform(key -> new BaseInfoItem(key, BaseInfoItem.TYPE.INT, form, mAttrs.get(key),
            getDisplayOrder(key)))
        .toSortedList((o1, o2) -> o1.getDisplayOrder() - o2.getDisplayOrder());
  }


  Map<String, Integer> displayOrderMap = new HashMap<>();

  private void initAttrs(Map<String, String> mAttrs) {
    mAttrs.put(attrTableTravNewKey, attrTableTravKey);
    mAttrs.put(attrTableTravMaintenanceKey, attrTableTravKey);
    mAttrs.put(attrTableTravAlterationKey, attrTableTravKey);
    mAttrs.put(attrTableTravTransitKey, attrTableTravKey);
    mAttrs.put(attrTableTravTransferKey, attrTableTravKey);
    mAttrs.put(attrTablePatientsAdultsKey, attrTablePatientsKey);
    mAttrs.put(attrTablePatients0To4Key, attrTablePatientsKey);
    mAttrs.put(attrTablePatients5To9Key, attrTablePatientsKey);
    mAttrs.put(attrTablePatients10To14Key, attrTablePatientsKey);
    mAttrs.put(attrTableProphylaxisPpeKey, attrTableProphylaxisKey);
    mAttrs.put(attrTableProphylaxisChildKey, attrTableProphylaxisKey);
    mAttrs.put(ATTR_TABLE_DISPENSED_DS5, attrTableDispensedKey);
    mAttrs.put(ATTR_TABLE_DISPENSED_DS4, attrTableDispensedKey);
    mAttrs.put(ATTR_TABLE_DISPENSED_DS3, attrTableDispensedKey);
    mAttrs.put(ATTR_TABLE_DISPENSED_DS2, attrTableDispensedKey);
    mAttrs.put(ATTR_TABLE_DISPENSED_DS1, attrTableDispensedKey);
    mAttrs.put(ATTR_TABLE_DISPENSED_DS, attrTableDispensedKey);
    mAttrs.put(ATTR_TABLE_DISPENSED_DT2, attrTableDispensedKey);
    mAttrs.put(ATTR_TABLE_DISPENSED_DT1, attrTableDispensedKey);
    mAttrs.put(ATTR_TABLE_DISPENSED_DT, attrTableDispensedKey);
    mAttrs.put(ATTR_TABLE_DISPENSED_DM, attrTableDispensedKey);
  }

  private void initDisplayOrder() {
    displayOrderMap = getDisplayOrderMap();
  }

  private int getDisplayOrder(String attrName) {
    return displayOrderMap.get(attrName);
  }

  public long getTotalPatients(RnRForm form) {
    for (BaseInfoItem item : form.getBaseInfoItemListWrapper()) {
      if (attrTableProphylaxisTotal.equals(item.getName())) {
        return Long.parseLong(item.getValue());
      }
    }
    return 0L;
  }

  @Override
  public List<RnrFormItem> generateRnrFormItems(RnRForm form, List<StockCard> stockCards)
      throws LMISException {
    List<RnrFormItem> rnrFormItems = super.generateRnrFormItems(form, stockCards);
    return fillAllProducts(form, rnrFormItems);
  }

  @Override
  protected RnrFormItem createRnrFormItemByPeriod(StockCard stockCard,
      List<StockMovementItem> notFullStockItemsByCreatedData) throws LMISException {
    RnrFormItem rnrFormItem = this.createMMIARnrFormItemByPeriod(stockCard, notFullStockItemsByCreatedData);

    rnrFormItem.setProduct(stockCard.getProduct());
    Date earliestLotExpiryDate = stockCard.getEarliestLotExpiryDate();
    if (earliestLotExpiryDate != null) {
      rnrFormItem
          .setValidate(DateUtil.formatDate(earliestLotExpiryDate, DateUtil.SIMPLE_DATE_FORMAT));
    }

    return rnrFormItem;
  }

  protected RnrFormItem createMMIARnrFormItemByPeriod(StockCard stockCard, List<StockMovementItem> stockMovementItems)
      throws LMISException {
    RnrFormItem rnrFormItem = new RnrFormItem();

    if (stockMovementItems.isEmpty()) {
      this.initMMiARnrFormItemWithoutMovement(rnrFormItem, lastRnrInventory(stockCard));
    } else {
      rnrFormItem.setInitialAmount(getMMiAInitialAmount(stockCard, stockMovementItems));
      this.assignMMIATotalValues(rnrFormItem, stockMovementItems);
    }

    rnrFormItem.setProduct(stockCard.getProduct());
    return rnrFormItem;
  }

  protected long getMMiAInitialAmount(StockCard stockCard,
      List<StockMovementItem> stockMovementItems) throws LMISException {
    List<RnRForm> rnRForms = listInclude(RnRForm.Emergency.NO, programCode);
    if (rnRForms.size() == 1) {
      return stockMovementItems.get(0).calculatePreviousSOH();
    }
    Long lastRnrInventory = lastRnrInventory(stockCard.getProduct());
    return lastRnrInventory != null ? lastRnrInventory
        : stockMovementItems.get(0).calculatePreviousSOH();
  }

  private void assignMMIATotalValues(RnrFormItem rnrFormItem,
      List<StockMovementItem> stockMovementItems) {
    long totalReceived = 0;

    for (StockMovementItem item : stockMovementItems) {
      if (MovementReasonManager.MovementType.RECEIVE == item.getMovementType()) {
        totalReceived += item.getMovementQuantity();
      }
    }
    rnrFormItem.setReceived(totalReceived);
  }

  private void initMMiARnrFormItemWithoutMovement(RnrFormItem rnrFormItem, long lastRnrInventory) {
    rnrFormItem.setReceived(0);
    rnrFormItem.setCalculatedOrderQuantity(0L);
    rnrFormItem.setInitialAmount(lastRnrInventory);
  }

}
