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

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import roboguice.inject.InjectResource;

public class MMIARepository extends RnrFormRepository {
    @InjectResource(R.string.table_trav)
    public String ATTR_TABLE_TRAV;
    @InjectResource(R.string.table_arvt_key)
    public String ATTR_TABLE_TRAV_KEY;
    @InjectResource(R.string.table_trav_label_new_key)
    public String ATTR_TABLE_TRAV_NEW_KEY;
    @InjectResource(R.string.table_trav_label_maintenance_key)
    public String ATTR_TABLE_TRAV_MAINTENANCE_KEY;
    @InjectResource(R.string.table_trav_label_alteration_key)
    public String ATTR_TABLE_TRAV_ALTERATION_KEY;
    @InjectResource(R.string.table_trav_label_transit_key)
    public String ATTR_TABLE_TRAV_TRANSIT_KEY;
    @InjectResource(R.string.table_trav_label_transfers_key)
    public String ATTR_TABLE_TRAV_TRANSFER_KEY;

    @InjectResource(R.string.table_patients)
    public String ATTR_TABLE_PATIENTS;
    @InjectResource(R.string.table_patients_key)
    public String ATTR_TABLE_PATIENTS_KEY;
    @InjectResource(R.string.table_patients_adults_key)
    public String ATTR_TABLE_PATIENTS_ADULTS_KEY;
    @InjectResource(R.string.table_patients_0to4_key)
    public String ATTR_TABLE_PATIENTS_0TO4_KEY;
    @InjectResource(R.string.table_patients_5to9_key)
    public String ATTR_TABLE_PATIENTS_5TO9_KEY;
    @InjectResource(R.string.table_patients_10to14_key)
    public String ATTR_TABLE_PATIENTS_10TO14_KEY;

    @InjectResource(R.string.table_prophylaxis)
    public String ATTR_TABLE_PROPHYLAXIS;
    @InjectResource(R.string.table_prophylaxy_key)
    public String ATTR_TABLE_PROPHYLAXIS_KEY;
    @InjectResource(R.string.table_prophylaxis_ppe_key)
    public String ATTR_TABLE_PROPHYLAXIS_PPE_KEY;
    @InjectResource(R.string.table_prophylaxis_prep_key)
    public String ATTR_TABLE_PROPHYLAXIS_PREP_KEY;
    @InjectResource(R.string.table_prophylaxis_child_key)
    public String ATTR_TABLE_PROPHYLAXIS_CHILD_KEY;
    @InjectResource(R.string.table_prophylaxis_total)
    public String ATTR_TABLE_PROPHYLAXIS_TOTAL;
    @InjectResource(R.string.table_prophylaxis_total_key)
    public String ATTR_TABLE_PROPHYLAXIS_TOTAL_KEY;

    @InjectResource(R.string.table_dispensed)
    public String ATTR_TABLE_DISPENSED;
    @InjectResource(R.string.table_dispensed_key)
    public String ATTR_TABLE_DISPENSED_KEY;
    public String ATTR_TABLE_DISPENSED_DS5 = "dispensed_ds5";
    public String ATTR_TABLE_DISPENSED_DS4 = "dispensed_ds4";
    public String ATTR_TABLE_DISPENSED_DS3 = "dispensed_ds3";
    public String ATTR_TABLE_DISPENSED_DS2 = "dispensed_ds2";
    public String ATTR_TABLE_DISPENSED_DS1 = "dispensed_ds1";
    public String ATTR_TABLE_DISPENSED_DS = "dispensed_ds";
    public String ATTR_TABLE_DISPENSED_DT2 = "dispensed_dt2";
    public String ATTR_TABLE_DISPENSED_DT1 = "dispensed_dt1";
    public String ATTR_TABLE_DISPENSED_DT = "dispensed_dt";
    public String ATTR_TABLE_DISPENSED_DM = "dispensed_dm";

    @InjectResource(R.string.key_regime_3lines_1)
    public String ATTR_REGIME_TYPE_FIRST_LINE_KEY;
    @InjectResource(R.string.key_regime_3lines_2)
    public String ATTR_REGIME_TYPE_SECOND_LINE_KEY;
    @InjectResource(R.string.key_regime_3lines_3)
    public String ATTR_REGIME_TYPE_THIRD_LINE_KEY;


    @InjectResource(R.string.label_new_patients)
    public String ATTR_NEW_PATIENTS;
    @InjectResource(R.string.label_sustaining)
    public String ATTR_SUSTAINING;
    @InjectResource(R.string.label_alteration)
    public String ATTR_ALTERATION;
    @InjectResource(R.string.label_total_month_dispense)
    public String ATTR_TOTAL_MONTH_DISPENSE;
    @InjectResource(R.string.label_total_patients)
    public String ATTR_TOTAL_PATIENTS;
    @InjectResource(R.string.label_ptv)
    public String ATTR_PTV;
    @InjectResource(R.string.label_ppe)
    public String ATTR_PPE;

    public enum ReportType {
        NEW,
        OLD,
    }

    @Inject
    ProgramRepository programRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    ProductProgramRepository productProgramRepository;

    @Inject
    public MMIARepository(Context context) {
        super(context);
        programCode = Constants.MMIA_PROGRAM_CODE;
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
        regimeThreeLines.add(ATTR_REGIME_TYPE_FIRST_LINE_KEY);
        regimeThreeLines.add(ATTR_REGIME_TYPE_SECOND_LINE_KEY);
        regimeThreeLines.add(ATTR_REGIME_TYPE_THIRD_LINE_KEY);

        return FluentIterable.from(regimeThreeLines).transform(new Function<String, RegimenItemThreeLines>() {
            @Nullable
            @Override
            public RegimenItemThreeLines apply(@Nullable String type) {
                RegimenItemThreeLines itemThreeLines = new RegimenItemThreeLines(type);
                itemThreeLines.setForm(form);
                return itemThreeLines;
            }
        }).toList();
    }

    @Override
    protected List<BaseInfoItem> generateBaseInfoItems(final RnRForm form, ReportType type) {

        if (ReportType.NEW != type) {
            ArrayList<String> attrs = new ArrayList<>();
            attrs.add(ATTR_NEW_PATIENTS);
            attrs.add(ATTR_SUSTAINING);
            attrs.add(ATTR_ALTERATION);
            attrs.add(ATTR_PTV);
            attrs.add(ATTR_PPE);
            attrs.add(ATTR_TOTAL_MONTH_DISPENSE);
            attrs.add(ATTR_TOTAL_PATIENTS);

            return FluentIterable.from(attrs)
                    .transform(attr -> new BaseInfoItem(attr, BaseInfoItem.TYPE.INT, form, "", 0))
                    .toList();
        }

        Map<String, String> mAttrs = new HashMap<>();
        initAttrs(mAttrs);

        initDisplayOrder();

        return FluentIterable.from(mAttrs.keySet())
                .transform(key -> new BaseInfoItem(key, BaseInfoItem.TYPE.INT, form, mAttrs.get(key), getDisplayOrder(key)))
                .toSortedList((o1, o2) -> o1.getDisplayOrder() - o2.getDisplayOrder());
    }


    Map<String, Integer> displayOrderMap = new HashMap<>();

    private void initAttrs(Map<String, String> mAttrs) {
        mAttrs.put(ATTR_TABLE_TRAV_NEW_KEY,ATTR_TABLE_TRAV_KEY);
        mAttrs.put(ATTR_TABLE_TRAV_MAINTENANCE_KEY, ATTR_TABLE_TRAV_KEY);
        mAttrs.put(ATTR_TABLE_TRAV_ALTERATION_KEY, ATTR_TABLE_TRAV_KEY);
        mAttrs.put(ATTR_TABLE_TRAV_TRANSIT_KEY, ATTR_TABLE_TRAV_KEY);
        mAttrs.put(ATTR_TABLE_TRAV_TRANSFER_KEY, ATTR_TABLE_TRAV_KEY);
        mAttrs.put(ATTR_TABLE_PATIENTS_ADULTS_KEY, ATTR_TABLE_PATIENTS_KEY);
        mAttrs.put(ATTR_TABLE_PATIENTS_0TO4_KEY, ATTR_TABLE_PATIENTS_KEY);
        mAttrs.put(ATTR_TABLE_PATIENTS_5TO9_KEY, ATTR_TABLE_PATIENTS_KEY);
        mAttrs.put(ATTR_TABLE_PATIENTS_10TO14_KEY, ATTR_TABLE_PATIENTS_KEY);
        mAttrs.put(ATTR_TABLE_PROPHYLAXIS_PPE_KEY, ATTR_TABLE_PROPHYLAXIS_KEY);
        mAttrs.put(ATTR_TABLE_PROPHYLAXIS_PREP_KEY, ATTR_TABLE_PROPHYLAXIS_KEY);
        mAttrs.put(ATTR_TABLE_PROPHYLAXIS_CHILD_KEY, ATTR_TABLE_PROPHYLAXIS_KEY);
        mAttrs.put(ATTR_TABLE_PROPHYLAXIS_TOTAL_KEY, ATTR_TABLE_PROPHYLAXIS_KEY);
        mAttrs.put(ATTR_TABLE_DISPENSED_DS5, ATTR_TABLE_DISPENSED_KEY);
        mAttrs.put(ATTR_TABLE_DISPENSED_DS4, ATTR_TABLE_DISPENSED_KEY);
        mAttrs.put(ATTR_TABLE_DISPENSED_DS3, ATTR_TABLE_DISPENSED_KEY);
        mAttrs.put(ATTR_TABLE_DISPENSED_DS2, ATTR_TABLE_DISPENSED_KEY);
        mAttrs.put(ATTR_TABLE_DISPENSED_DS1, ATTR_TABLE_DISPENSED_KEY);
        mAttrs.put(ATTR_TABLE_DISPENSED_DS, ATTR_TABLE_DISPENSED_KEY);
        mAttrs.put(ATTR_TABLE_DISPENSED_DT2, ATTR_TABLE_DISPENSED_KEY);
        mAttrs.put(ATTR_TABLE_DISPENSED_DT1, ATTR_TABLE_DISPENSED_KEY);
        mAttrs.put(ATTR_TABLE_DISPENSED_DT, ATTR_TABLE_DISPENSED_KEY);
        mAttrs.put(ATTR_TABLE_DISPENSED_DM, ATTR_TABLE_DISPENSED_KEY);
    }

    private void initDisplayOrder() {
        displayOrderMap.put(ATTR_TABLE_TRAV, 0);
        displayOrderMap.put(ATTR_TABLE_TRAV_NEW_KEY, 1);
        displayOrderMap.put(ATTR_TABLE_TRAV_MAINTENANCE_KEY, 2);
        displayOrderMap.put(ATTR_TABLE_TRAV_ALTERATION_KEY, 3);
        displayOrderMap.put(ATTR_TABLE_TRAV_TRANSIT_KEY, 4);
        displayOrderMap.put(ATTR_TABLE_TRAV_TRANSFER_KEY, 5);
        displayOrderMap.put(ATTR_TABLE_PATIENTS, 11);
        displayOrderMap.put(ATTR_TABLE_PATIENTS_ADULTS_KEY, 12);
        displayOrderMap.put(ATTR_TABLE_PATIENTS_0TO4_KEY, 13);
        displayOrderMap.put(ATTR_TABLE_PATIENTS_5TO9_KEY, 14);
        displayOrderMap.put(ATTR_TABLE_PATIENTS_10TO14_KEY, 15);
        displayOrderMap.put(ATTR_TABLE_PROPHYLAXIS, 16);
        displayOrderMap.put(ATTR_TABLE_PROPHYLAXIS_PPE_KEY, 17);
        displayOrderMap.put(ATTR_TABLE_PROPHYLAXIS_PREP_KEY, 18);
        displayOrderMap.put(ATTR_TABLE_PROPHYLAXIS_CHILD_KEY, 19);
        displayOrderMap.put(ATTR_TABLE_PROPHYLAXIS_TOTAL_KEY, 20);
        displayOrderMap.put(ATTR_TABLE_DISPENSED, 21);
        displayOrderMap.put(ATTR_TABLE_DISPENSED_DS5, 22);
        displayOrderMap.put(ATTR_TABLE_DISPENSED_DS4, 23);
        displayOrderMap.put(ATTR_TABLE_DISPENSED_DS3, 24);
        displayOrderMap.put(ATTR_TABLE_DISPENSED_DS2, 25);
        displayOrderMap.put(ATTR_TABLE_DISPENSED_DS1, 26);
        displayOrderMap.put(ATTR_TABLE_DISPENSED_DS, 27);
        displayOrderMap.put(ATTR_TABLE_DISPENSED_DT2, 28);
        displayOrderMap.put(ATTR_TABLE_DISPENSED_DT1, 29);
        displayOrderMap.put(ATTR_TABLE_DISPENSED_DT, 30);
        displayOrderMap.put(ATTR_TABLE_DISPENSED_DM, 31);
    }

    private int getDisplayOrder(String attrName) {
        return displayOrderMap.get(attrName);
    }

    public long getTotalPatients(RnRForm form) {
        for (BaseInfoItem item : form.getBaseInfoItemListWrapper()) {
            if (ATTR_TABLE_PROPHYLAXIS_TOTAL.equals(item.getName())) {
                return Long.parseLong(item.getValue());
            }
        }
        return 0L;
    }

    @Override
    public List<RnrFormItem> generateRnrFormItems(RnRForm form, List<StockCard> stockCards) throws LMISException {
        List<RnrFormItem> rnrFormItems = super.generateRnrFormItems(form, stockCards);
        return fillAllMMIAProducts(form, rnrFormItems);
    }

    @Override
    protected RnrFormItem createRnrFormItemByPeriod(StockCard stockCard, Date startDate, Date endDate) throws LMISException {
        RnrFormItem rnrFormItem = this.createMMIARnrFormItemByPeriod(stockCard, startDate, endDate);

        rnrFormItem.setProduct(stockCard.getProduct());
        Date earliestLotExpiryDate = stockCard.getEarliestLotExpiryDate();
        if (earliestLotExpiryDate != null) {
            rnrFormItem.setValidate(DateUtil.formatDate(earliestLotExpiryDate, DateUtil.SIMPLE_DATE_FORMAT));
        }

        return rnrFormItem;
    }

    protected RnrFormItem createMMIARnrFormItemByPeriod(StockCard stockCard, Date startDate, Date endDate) throws LMISException {
        RnrFormItem rnrFormItem = new RnrFormItem();
        List<StockMovementItem> stockMovementItems = stockMovementRepository.queryStockItemsByCreatedDate(stockCard.getId(), startDate, endDate);

        if (stockMovementItems.isEmpty()) {
            this.initMMiARnrFormItemWithoutMovement(rnrFormItem, lastRnrInventory(stockCard));
        } else {
            rnrFormItem.setInitialAmount(getMMiAInitialAmount(stockCard, stockMovementItems));
            this.assignMMIATotalValues(rnrFormItem, stockMovementItems);
        }

        rnrFormItem.setProduct(stockCard.getProduct());
        return rnrFormItem;
    }

    protected long getMMiAInitialAmount(StockCard stockCard, List<StockMovementItem> stockMovementItems) throws LMISException {
        List<RnRForm> rnRForms = listInclude(RnRForm.Emergency.No, programCode);
        if (rnRForms.size() == 1) {
            return stockMovementItems.get(0).calculatePreviousSOH();
        }
        return lastRnrInventory(stockCard);
    }

    private void assignMMIATotalValues(RnrFormItem rnrFormItem, List<StockMovementItem> stockMovementItems) {
        long totalReceived = 0;

        for (StockMovementItem item : stockMovementItems) {
            if (MovementReasonManager.MovementType.RECEIVE == item.getMovementType()) {
                totalReceived += item.getMovementQuantity();
            }
        }
        rnrFormItem.setReceived(totalReceived);
    }

    private void initMMiARnrFormItemWithoutMovement(RnrFormItem rnrFormItem, long lastRnrInventory) throws LMISException {
        rnrFormItem.setReceived(0);
        rnrFormItem.setCalculatedOrderQuantity(0L);
        rnrFormItem.setInitialAmount(lastRnrInventory);
    }

    protected ArrayList<RnrFormItem> fillAllMMIAProducts(RnRForm form, List<RnrFormItem> rnrFormItems) throws LMISException {
        List<Product> products;

        List<String> programCodes = programRepository.queryProgramCodesByProgramCodeOrParentCode(Constants.MMIA_PROGRAM_CODE);
        List<Long> productIds = productProgramRepository.queryActiveProductIdsByProgramsWithKits(programCodes, false);
        products = productRepository.queryProductsByProductIds(productIds);

        ArrayList<RnrFormItem> result = new ArrayList<>();

        for (Product product : products) {
            RnrFormItem rnrFormItem = new RnrFormItem();
            rnrFormItem.setForm(form);
            rnrFormItem.setProduct(product);
            RnrFormItem stockFormItem = getStockCardRnr(product, rnrFormItems);
            if (stockFormItem == null) {
                rnrFormItem.setInitialAmount(lastRnrInventory(product));
            } else {
                rnrFormItem = stockFormItem;
            }
            result.add(rnrFormItem);
        }
        return result;
    }

    private RnrFormItem getStockCardRnr(Product product, List<RnrFormItem> rnrStockFormItems) {
        for (RnrFormItem item : rnrStockFormItems) {
            if (item.getProduct().getId() == product.getId()) {
                return item;
            }
        }
        return null;
    }
}
