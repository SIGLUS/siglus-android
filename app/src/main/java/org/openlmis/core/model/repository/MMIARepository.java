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
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.utils.Constants;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectResource;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class MMIARepository extends RnrFormRepository {

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
    protected List<BaseInfoItem> generateBaseInfoItems(final RnRForm form) {
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
                return new BaseInfoItem(attr, BaseInfoItem.TYPE.INT, form);
            }
        }).toList();
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
    public List<RnrFormItem> generateRnrFormItems(RnRForm form, List<StockCard> stockCards) throws LMISException {
        List<RnrFormItem> rnrFormItems = super.generateRnrFormItems(form, stockCards);
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_sync_mmia_list_from_web)) {
            return rnrFormItems;
        } else {
            return fillAllMMIAProducts(form, rnrFormItems);
        }
    }

    protected ArrayList<RnrFormItem> fillAllMMIAProducts(RnRForm form, List<RnrFormItem> rnrFormItems) throws LMISException {
        List<Product> products;
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_rnr_multiple_programs)){

            if(LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_deactivate_program_product)) {
                List<String> programCodes = programRepository.queryProgramCodesByProgramCodeOrParentCode(Constants.MMIA_PROGRAM_CODE);
                List<Long> productIds = productProgramRepository.queryActiveProductIdsByProgramsWithKits(programCodes, false);
                products = productRepository.queryProductsByProductIds(productIds);
            } else {
                List<Long> programIds = programRepository.queryProgramIdsByProgramCodeOrParentCode(Constants.MMIA_PROGRAM_CODE);
                products = productRepository.queryProductsByProgramIds(programIds);
            }
        }else {
            if(LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_deactivate_program_product)) {
                List<String> programCodes = newArrayList(Constants.MMIA_PROGRAM_CODE);
                List<Long> productIds = productProgramRepository.queryActiveProductIdsByProgramsWithKits(programCodes, false);
                products = productRepository.queryProductsByProductIds(productIds);
            } else {
                products = productRepository.queryProductsByProgramId(programRepository.queryByCode(Constants.MMIA_PROGRAM_CODE).getId());
            }
        }
        ArrayList<RnrFormItem> result = new ArrayList<>();

        for (Product product : products) {
            RnrFormItem rnrFormItem = new RnrFormItem();
            rnrFormItem.setForm(form);
            rnrFormItem.setProduct(product);
            for (RnrFormItem item : rnrFormItems) {
                if (item.getProduct().getId() == product.getId()) {
                    rnrFormItem = item;
                    break;
                }
            }
            result.add(rnrFormItem);
        }
        return result;
    }

    public void deleteRegimeItem(final RegimenItem item) throws LMISException {
        dbUtil.withDao(RegimenItem.class, new DbUtil.Operation<RegimenItem, Void>() {
            @Override
            public Void operate(Dao<RegimenItem, String> dao) throws SQLException {
                dao.delete(item);
                return null;
            }
        });
    }

    public List<RegimenItem> queryRegimeItem() throws LMISException {
        return dbUtil.withDao(RegimenItem.class, new DbUtil.Operation<RegimenItem, List<RegimenItem>>() {
            @Override
            public List<RegimenItem> operate(Dao<RegimenItem, String> dao) throws SQLException {
                return dao.queryForAll();
            }
        });
    }
}
