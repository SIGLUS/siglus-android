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
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;

import java.util.ArrayList;
import java.util.List;

public class MMIARepository extends RnrFormRepository {

    public static final String ATTR_NEW_PATIENTS = "New Patients";
    public static final String ATTR_SUSTAINING = "Sustaining";
    public static final String ATTR_ALTERATION = "Alteration";
    public static final String ATTR_TOTAL_MONTH_DISPENSE = "Total Month Dispense";
    public static final String ATTR_TOTAL_PATIENTS = "Total Patients";
    public static final String ATTR_PTV = "PTV";
    public static final String ATTR_PPE = "PPE";

    public static final String MMIA_PROGRAM_CODE = "MMIA";

    @Inject
    ProgramRepository programRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    public MMIARepository(Context context) {
        super(context);
    }

    public RnRForm initMIMIA() throws LMISException {
        return initRnrForm(programRepository.queryByCode(MMIA_PROGRAM_CODE));
    }

    public RnRForm getDraftMMIAForm() throws LMISException {
        return queryDraft(programRepository.queryByCode(MMIA_PROGRAM_CODE));
    }

    @Override
    protected List<RegimenItem> generateRegimeItems(RnRForm form) throws LMISException {
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

    @Override
    protected List<BaseInfoItem> generateBaseInfoItems(RnRForm form) {
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
        baseInfoItemList.add(ptv);
        baseInfoItemList.add(ppe);
        baseInfoItemList.add(totalMonthDispense);
        baseInfoItemList.add(totalPatients);

        return baseInfoItemList;
    }


    public long getTotalPatients(RnRForm form) {
        for (BaseInfoItem item : form.getBaseInfoItemListWrapper()) {
            if (ATTR_TOTAL_PATIENTS.equals(item.getName())) {
                return Long.parseLong(item.getValue());
            }
        }
        return 0L;
    }

    @Override
    protected List<RnrFormItem> generateRnrFormItems(RnRForm form) throws LMISException {
        List<RnrFormItem> rnrFormItems = super.generateRnrFormItems(form);

        List<Product> products = productRepository.queryProducts(programRepository.queryByCode(MMIA_PROGRAM_CODE).getId());
        ArrayList<RnrFormItem> result = new ArrayList<>();

        for (Product product : products) {
            RnrFormItem rnrFormItem = new RnrFormItem();
            rnrFormItem.setForm(form);
            rnrFormItem.setProduct(product);
            for (RnrFormItem item : rnrFormItems) {
                if (item.getProduct().getId() == product.getId()) {
                    rnrFormItem = item;
                }
            }
            result.add(rnrFormItem);
        }
        return result;
    }
}
