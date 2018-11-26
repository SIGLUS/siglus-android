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
import com.j256.ormlite.misc.TransactionManager;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormBasicItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.helper.FormHelper;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import static org.openlmis.core.utils.Constants.RAPID_TEST_CODE;
import static org.openlmis.core.utils.Constants.TEST_KIT_PROGRAM_CODE;

public class ProgramBasicItemsRepository {

    private Context context;

    @Inject
    ProgramRepository programRepository;

    @Inject
    StockRepository stockRepository;

    @Inject
    StockMovementRepository stockMovementRepository;

    @Inject
    ProductProgramRepository productProgramRepository;

    @Inject
    private ProgramDataFormRepository programDataFormRepository;

    @Inject
    ProductRepository productRepository;


    @Inject
    FormHelper formHelper;


    @Inject
    public ProgramBasicItemsRepository(Context context) {
        this.context = context;
    }

    private List<ProgramDataFormBasicItem> createInitProgramForm(ProgramDataForm form, Date periodEnd) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    form.setFormBasicItems(generateDataFormBasicItems(form, TEST_KIT_PROGRAM_CODE, periodEnd));
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }

        return null;
    }

    private List<ProgramDataFormBasicItem> generateDataFormBasicItems(ProgramDataForm form, String programCode, Date periodEnd) throws LMISException {
        List<ProgramDataFormBasicItem> basicItems = generateBasicItem(form, programCode, periodEnd);
        return fillAllRapidTestProducts(form, programCode, basicItems);
    }

    private List<ProgramDataFormBasicItem> generateBasicItem(final ProgramDataForm form, String programCode, Date periodEnd) throws LMISException {
        List<StockCard> stockCards = stockRepository.getStockCardsBeforePeriodEnd(programCode, periodEnd);
        List<ProgramDataFormBasicItem> items = new ArrayList<>();
        for (StockCard stockCard : stockCards) {
            ProgramDataFormBasicItem item = createProgramDataByPeriod(stockCard, form);
            items.add(item);
        }
        return items;
    }

    private ArrayList<ProgramDataFormBasicItem> fillAllRapidTestProducts(ProgramDataForm form, String programCode, List<ProgramDataFormBasicItem> basicItems) throws LMISException {
        List<Product> products = getProgramProducts(programCode);
        List<ProgramDataForm> rapidTestForms = programDataFormRepository.listByProgramCode(RAPID_TEST_CODE);
        ArrayList<ProgramDataFormBasicItem> result = new ArrayList<>();

        for (Product product : products) {
            ProgramDataFormBasicItem rapidItem = new ProgramDataFormBasicItem();
            ProgramDataFormBasicItem stockFormItem = getStockCardRapidData(product, basicItems);
            if (stockFormItem == null) {
                rapidItem.setForm(form);
                rapidItem.setProduct(product);
            } else {
                rapidItem = stockFormItem;
            }
            rapidItem.setInitialAmount(lastProgramInventory(product, rapidTestForms));
            result.add(rapidItem);
        }
        return result;
    }

    protected ProgramDataFormBasicItem createProgramDataByPeriod(StockCard stockCard, final ProgramDataForm form) throws LMISException {
        ProgramDataFormBasicItem item = new ProgramDataFormBasicItem();
        List<StockMovementItem> stockMovementItems = stockMovementRepository.queryStockItemsByCreatedDate(stockCard.getId(), form.getPeriodBegin(), form.getPeriodEnd());
        FormHelper.StockMovementModifiedItem modifiedItem = formHelper.assignTotalValues(stockMovementItems);
        item.setReceived(modifiedItem.getTotalReceived());
        item.setIssued(modifiedItem.getTotalIssued());
        item.setAdjustment(modifiedItem.getTotalAdjustment());
        item.setProduct(stockCard.getProduct());
        Date earliestLotExpiryDate = stockCard.getEarliestLotExpiryDate();
        if (earliestLotExpiryDate != null) {
            item.setValidate(DateUtil.formatDate(earliestLotExpiryDate, DateUtil.SIMPLE_DATE_FORMAT));
        }
        item.setForm(form);
        return item;
    }


    private ProgramDataFormBasicItem getStockCardRapidData(Product product, List<ProgramDataFormBasicItem> programDataFormBasicItems) {
        for (ProgramDataFormBasicItem item : programDataFormBasicItems) {
            if (item.getProduct().getId() == product.getId()) {
                return item;
            }
        }
        return null;
    }

    private Long lastProgramInventory(Product product, List<ProgramDataForm> rapidTestForms) throws LMISException {
        if (rapidTestForms.isEmpty() || rapidTestForms.size() == 1) {
            return null;
        }
        List<ProgramDataFormBasicItem> programBasicItems = rapidTestForms.get(rapidTestForms.size() - 2).getFormBasicItems();
        for (ProgramDataFormBasicItem item : programBasicItems) {
            if (item.getProduct().getId() == product.getId()) {
                return item.getInventory();
            }
        }
        return null;
    }

    private List<Product> getProgramProducts(String programCode) throws LMISException {
        List<String> programCodes = programRepository.queryProgramCodesByProgramCodeOrParentCode(programCode);
        List<Long> productIds = productProgramRepository.queryActiveProductIdsByProgramsWithKits(programCodes, false);
        List<Product> products = productRepository.queryProductsByProductIds(productIds);
        return products;
    }
}
