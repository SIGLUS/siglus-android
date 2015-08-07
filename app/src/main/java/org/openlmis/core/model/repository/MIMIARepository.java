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

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockItem;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MIMIARepository extends RnrFormRepository{

    public static final String ATTR_NEW_PATIENTS = "New Patients";
    public static final String ATTR_SUSTAINING = "Sustaining";
    public static final String ATTR_ALTERATION = "Alteration";
    public static final String ATTR_TOTAL_MONTH_DISPENSE = "Total Month Dispense";
    public static final String ATTR_TOTAL_PATIENTS = "Total Patients";
    public static final String ATTR_PTV = "PTV";
    public static final String ATTR_PPE = "PPE";

    public static final int DAY_PERIOD_END = 20;

    @Inject
    public MIMIARepository(Context context){
        super(context);
    }

    public RnRForm initMIMIA() throws LMISException {

        RnRForm form = new RnRForm();
        create(form);
        createRnrFormItems(generateProductItems(form));
        createRegimenItems(generateRegimeItems(form));
        createBaseInfoItems(generateBaseInfoItems(form));

        genericDao.refresh(form);
        return form;
    }

    private List<RnrFormItem> generateProductItems(RnRForm form) throws LMISException {
        //TODO programCode
        List<StockCard> stockCards = stockRepository.list("TB");
        List<RnrFormItem> productItems = new ArrayList<>();

        Calendar calendar = GregorianCalendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        Date startDate = new GregorianCalendar(year, month - 1, DAY_PERIOD_END + 1).getTime();
        Date endDate = new GregorianCalendar(year, month, DAY_PERIOD_END).getTime();

        for (StockCard stockCard : stockCards) {
            List<StockItem> stockItems = stockRepository.queryStockItems(stockCard, startDate, endDate);
            if (stockItems.size() > 0) {
                RnrFormItem productItem = new RnrFormItem();

                StockItem firstItem = stockItems.get(0);
                productItem.setInitialAmount(firstItem.getStockOnHand() - firstItem.getAmount());

                long totalReceived = 0;
                long totalIssued = 0;
                long totalAdjustment = 0;

                for (StockItem item : stockItems){
                    if (StockItem.MovementType.RECEIVE == item.getMovementType()){
                        totalReceived += item.getAmount();
                    }else if (StockItem.MovementType.ISSUE == item.getMovementType()){
                        totalIssued += item.getAmount();
                    }else {
                        totalAdjustment += item.getAmount();
                    }
                }

                productItem.setReceived(totalReceived);
                productItem.setIssued(totalIssued);
                productItem.setAdjustment(totalAdjustment);
                productItem.setForm(form);
                productItem.setInventory(stockItems.get(stockItems.size() - 1).getStockOnHand());
                productItem.setValidate(stockCard.getEarliestExpireDate());

                productItems.add(productItem);
            }
        }

        return productItems;
    }

    private List<RegimenItem> generateRegimeItems(RnRForm form) throws LMISException {
        List<Regimen> regimens = regimenRepository.list();
        List<RegimenItem> regimenItems = new ArrayList<>();
        for (Regimen regimen : regimens) {
            RegimenItem item = new RegimenItem();
            item.setForm(form);
            item.setRegimen(regimen);
            regimenItems.add(item);
        }
        return regimenItems;
    }

    private List<BaseInfoItem> generateBaseInfoItems(RnRForm form){
        BaseInfoItem newPatients = new BaseInfoItem(ATTR_NEW_PATIENTS, BaseInfoItem.TYPE.INT, form);
        BaseInfoItem sustaining = new BaseInfoItem(ATTR_SUSTAINING, BaseInfoItem.TYPE.INT, form);
        BaseInfoItem alteration = new BaseInfoItem(ATTR_ALTERATION, BaseInfoItem.TYPE.INT, form);
        BaseInfoItem totalMonthDispense = new BaseInfoItem(ATTR_TOTAL_MONTH_DISPENSE, BaseInfoItem.TYPE.INT, form);
        BaseInfoItem totalPatients = new BaseInfoItem(ATTR_TOTAL_PATIENTS, BaseInfoItem.TYPE.INT, form);
        BaseInfoItem ptv = new BaseInfoItem(ATTR_PTV, BaseInfoItem.TYPE.INT, form);
        BaseInfoItem ppe = new BaseInfoItem(ATTR_PPE, BaseInfoItem.TYPE.INT, form);

        List<BaseInfoItem> baseInfoItemList = new ArrayList<>();

        baseInfoItemList.add(newPatients);
        baseInfoItemList.add(sustaining);
        baseInfoItemList.add(alteration);
        baseInfoItemList.add(totalMonthDispense);
        baseInfoItemList.add(totalPatients);
        baseInfoItemList.add(ptv);
        baseInfoItemList.add(ppe);

        return baseInfoItemList;
    }


    public long getTotalPatients(RnRForm form) {
        for (BaseInfoItem item : form.getBaseInfoItemList()) {
            if (ATTR_TOTAL_PATIENTS.equals(item.getName())) {
                return Long.parseLong(item.getValue());
            }
        }
        return 0L;
    }
}
