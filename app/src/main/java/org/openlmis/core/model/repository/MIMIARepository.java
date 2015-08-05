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
        List<StockCard> stockCards = stockRepository.list("ART");
        List<RnrFormItem> productItems = new ArrayList<>();

        Calendar calendar = GregorianCalendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        Date startDate = new GregorianCalendar(calendar.get(Calendar.YEAR), month - 1, 20).getTime();
        Date endDate = new GregorianCalendar(calendar.get(Calendar.YEAR), month, 20).getTime();

        for (StockCard stockCard : stockCards) {
            List<StockItem> stockItems = stockRepository.queryStockItems(stockCard, startDate, endDate);
            if (stockItems.size() > 0) {
                RnrFormItem productItem = new RnrFormItem();

                StockItem firstItem = stockItems.get(0);
                productItem.setInitialAmount(firstItem.getStockOnHand() + firstItem.getAmount());

                long totalReceived = 0;
                long totalIssued = 0;
                long totalAdjustment = 0;

                for (StockItem item : stockItems){
                    if (StockItem.MovementType.RECEIVE == item.getMovementType()){
                        totalReceived += item.getAmount();
                    }else if (StockItem.MovementType.ISSUE == item.getMovementType()){
                        totalIssued += item.getAmount();
                    }else {
                        totalAdjustment += Math.abs(item.getAmount());
                    }
                }

                productItem.setReceived(totalReceived);
                productItem.setIssued(totalIssued);
                productItem.setAdjustment(totalAdjustment);
                productItem.setForm(form);

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
        BaseInfoItem newPatients = new BaseInfoItem("New Patients", BaseInfoItem.TYPE.INT, form);
        BaseInfoItem sustaining = new BaseInfoItem("Sustaining", BaseInfoItem.TYPE.INT, form);
        BaseInfoItem alteration = new BaseInfoItem("Alteration", BaseInfoItem.TYPE.INT, form);
        BaseInfoItem totalMonthDispense = new BaseInfoItem("Total Month Dispense", BaseInfoItem.TYPE.INT, form);
        BaseInfoItem totalPatients = new BaseInfoItem("Total Patients", BaseInfoItem.TYPE.INT, form);
        BaseInfoItem PTV = new BaseInfoItem("PTV", BaseInfoItem.TYPE.INT, form);
        BaseInfoItem PPE = new BaseInfoItem("PPE", BaseInfoItem.TYPE.INT, form);

        List<BaseInfoItem> baseInfoItemList = new ArrayList<>();

        baseInfoItemList.add(newPatients);
        baseInfoItemList.add(sustaining);
        baseInfoItemList.add(alteration);
        baseInfoItemList.add(totalMonthDispense);
        baseInfoItemList.add(totalPatients);
        baseInfoItemList.add(PTV);
        baseInfoItemList.add(PPE);

        return baseInfoItemList;
    }


    public int getTotalPatients(RnRForm form){

       for (BaseInfoItem item : form.getBaseInfoItemList()){
           if ("Total Patients".equals(item.getName())){
               return Integer.parseInt(item.getValue());
           }
       }

        return 0;
    }
}
