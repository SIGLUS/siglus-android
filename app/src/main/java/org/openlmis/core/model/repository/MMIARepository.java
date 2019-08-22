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
import com.j256.ormlite.stmt.query.In;

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
    private String ATTR_TABLE_TRAV;
    @InjectResource(R.string.table_trav_label_new)
    private String ATTR_TABLE_TRAV_NEW;
    @InjectResource(R.string.table_trav_label_maintenance)
    private String ATTR_TABLE_TRAV_MAINTENANCE;
    @InjectResource(R.string.table_trav_label_alteration)
    private String ATTR_TABLE_TRAV_ALTERATION;
    @InjectResource(R.string.table_trav_label_transit)
    private String ATTR_TABLE_TRAV_TRANSIT;
    @InjectResource(R.string.table_trav_label_transfers)
    private String ATTR_TABLE_TRAV_TRANSFER;

    @InjectResource(R.string.table_dispensed)
    private String ATTR_TABLE_DISPENSED;
    @InjectResource(R.string.table_dispensed_label_dt)
    private String ATTR_TABLE_DISPENSED_DT;
    @InjectResource(R.string.table_dispensed_label_dispense)
    private String ATTR_TABLE_DISPENSED_DISPENSE;
    @InjectResource(R.string.table_dispensed_label_therapeutic)
    private String ATTR_TABLE_DISPENSED_THERAPEUTIC;

    @InjectResource(R.string.table_patients)
    private String ATTR_TABLE_PATIENTS;
    @InjectResource(R.string.table_patients_adults)
    private String ATTR_TABLE_PATIENTS_ADULTS;
    @InjectResource(R.string.table_patients_0to4)
    private String ATTR_TABLE_PATIENTS_0TO4;
    @InjectResource(R.string.table_patients_5to9)
    private String ATTR_TABLE_PATIENTS_5TO9;
    @InjectResource(R.string.table_patients_10to14)
    private String ATTR_TABLE_PATIENTS_10TO14;

    @InjectResource(R.string.table_prophylaxis)
    private String ATTR_TABLE_PROPHYLAXIS;
    @InjectResource(R.string.table_prophylaxis_ppe)
    private String ATTR_TABLE_PROPHYLAXIS_PPE;
    @InjectResource(R.string.table_prophylaxis_prep)
    private String ATTR_TABLE_PROPHYLAXIS_PREP;
    @InjectResource(R.string.table_prophylaxis_child)
    private String ATTR_TABLE_PROPHYLAXIS_CHILD;
    @InjectResource(R.string.table_prophylaxis_total)
    private String ATTR_TABLE_PROPHYLAXIS_TOTAL;

    @InjectResource(R.string.mmia_1stline)
    private String ATTR_REGIME_TYPE_FIRST_LINE;
    @InjectResource(R.string.mmia_2ndline)
    private String ATTR_REGIME_TYPE_SECOND_LINE;
    @InjectResource(R.string.mmia_3rdline)
    private String ATTR_REGIME_TYPE_THIRD_LINE;


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

    public enum REPORT_TYPE {
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
        regimeThreeLines.add(ATTR_REGIME_TYPE_FIRST_LINE);
        regimeThreeLines.add(ATTR_REGIME_TYPE_SECOND_LINE);
        regimeThreeLines.add(ATTR_REGIME_TYPE_THIRD_LINE);

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
    protected List<BaseInfoItem> generateBaseInfoItems(final RnRForm form, REPORT_TYPE type) {

        if (REPORT_TYPE.NEW != type) {
            ArrayList<String> attrs = new ArrayList<>();
            attrs.add(ATTR_NEW_PATIENTS);
            attrs.add(ATTR_SUSTAINING);
            attrs.add(ATTR_ALTERATION);
            attrs.add(ATTR_PTV);
            attrs.add(ATTR_PPE);
            attrs.add(ATTR_TOTAL_MONTH_DISPENSE);
            attrs.add(ATTR_TOTAL_PATIENTS);

            return FluentIterable.from(attrs).transform(new Function<String, BaseInfoItem>() {
                @Override
                public BaseInfoItem apply(String attr) {
                    return new BaseInfoItem(attr, BaseInfoItem.TYPE.INT, form, "", 0);
                }
            }).toList();
        }

        Map<String, String> mAttrs = new HashMap<>();
        mAttrs.put(ATTR_TABLE_TRAV_NEW, ATTR_TABLE_TRAV);
        mAttrs.put(ATTR_TABLE_TRAV_MAINTENANCE, ATTR_TABLE_TRAV);
        mAttrs.put(ATTR_TABLE_TRAV_ALTERATION, ATTR_TABLE_TRAV);
        mAttrs.put(ATTR_TABLE_TRAV_TRANSIT, ATTR_TABLE_TRAV);
        mAttrs.put(ATTR_TABLE_TRAV_TRANSFER, ATTR_TABLE_TRAV);
        mAttrs.put(ATTR_TABLE_DISPENSED_DT, ATTR_TABLE_DISPENSED);
        mAttrs.put(ATTR_TABLE_DISPENSED_DISPENSE, ATTR_TABLE_DISPENSED);
        mAttrs.put(ATTR_TABLE_DISPENSED_THERAPEUTIC, ATTR_TABLE_DISPENSED);
        mAttrs.put(ATTR_TABLE_PATIENTS_ADULTS, ATTR_TABLE_PATIENTS);
        mAttrs.put(ATTR_TABLE_PATIENTS_0TO4, ATTR_TABLE_PATIENTS);
        mAttrs.put(ATTR_TABLE_PATIENTS_5TO9, ATTR_TABLE_PATIENTS);
        mAttrs.put(ATTR_TABLE_PATIENTS_10TO14, ATTR_TABLE_PATIENTS);
        mAttrs.put(ATTR_TABLE_PROPHYLAXIS_PPE, ATTR_TABLE_PROPHYLAXIS);
        mAttrs.put(ATTR_TABLE_PROPHYLAXIS_PREP, ATTR_TABLE_PROPHYLAXIS);
        mAttrs.put(ATTR_TABLE_PROPHYLAXIS_CHILD, ATTR_TABLE_PROPHYLAXIS);
        mAttrs.put(ATTR_TABLE_PROPHYLAXIS_TOTAL, ATTR_TABLE_PROPHYLAXIS);

        initDisplayOrder();

        return FluentIterable.from(mAttrs.keySet()).transform(new Function<String, BaseInfoItem>() {
            @Override
            public BaseInfoItem apply(String key) {
                return new BaseInfoItem(key, BaseInfoItem.TYPE.INT, form, mAttrs.get(key), getDisplayOrder(key));
            }
        }).toList();
    }


    Map<String, Integer> displayOrderMap = new HashMap<String, Integer>();

    private void initDisplayOrder() {
        displayOrderMap.put(ATTR_TABLE_TRAV, 0);
        displayOrderMap.put(ATTR_TABLE_TRAV_NEW, 1);
        displayOrderMap.put(ATTR_TABLE_TRAV_MAINTENANCE, 2);
        displayOrderMap.put(ATTR_TABLE_TRAV_ALTERATION, 3);
        displayOrderMap.put(ATTR_TABLE_TRAV_TRANSIT, 4);
        displayOrderMap.put(ATTR_TABLE_TRAV_TRANSFER, 5);
        displayOrderMap.put(ATTR_TABLE_DISPENSED, 6);
        displayOrderMap.put(ATTR_TABLE_DISPENSED_DT, 7);
        displayOrderMap.put(ATTR_TABLE_DISPENSED_DISPENSE, 8);
        displayOrderMap.put(ATTR_TABLE_DISPENSED_THERAPEUTIC, 9);
        displayOrderMap.put(ATTR_TABLE_PATIENTS, 10);
        displayOrderMap.put(ATTR_TABLE_PATIENTS_ADULTS, 11);
        displayOrderMap.put(ATTR_TABLE_PATIENTS_0TO4, 12);
        displayOrderMap.put(ATTR_TABLE_PATIENTS_5TO9, 13);
        displayOrderMap.put(ATTR_TABLE_PATIENTS_10TO14, 14);
        displayOrderMap.put(ATTR_TABLE_PROPHYLAXIS, 15);
        displayOrderMap.put(ATTR_TABLE_PROPHYLAXIS_PPE, 16);
        displayOrderMap.put(ATTR_TABLE_PROPHYLAXIS_PREP, 17);
        displayOrderMap.put(ATTR_TABLE_PROPHYLAXIS_CHILD, 18);
        displayOrderMap.put(ATTR_TABLE_PROPHYLAXIS_TOTAL, 19);
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
