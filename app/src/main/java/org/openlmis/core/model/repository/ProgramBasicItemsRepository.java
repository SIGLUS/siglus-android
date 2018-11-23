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
    FormHelper formHelper;


    @Inject
    public ProgramBasicItemsRepository(Context context) {
        this.context = context;
    }

    private List<ProgramDataFormBasicItem> createInitProgramForm(Date periodEnd, ProgramDataForm form) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    List<String> programCodes = programRepository.queryProgramCodesByProgramCodeOrParentCode(TEST_KIT_PROGRAM_CODE);
                    List<StockCard> stockCards = stockRepository.getStockCardsBeforePeriodEnd(TEST_KIT_PROGRAM_CODE, periodEnd);

                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }

        return null;
    }

    public List<ProgramDataFormBasicItem> generateDataFormBasicItem(final ProgramDataForm form, List<StockCard> stockCards) throws LMISException {
        List<ProgramDataFormBasicItem> items = new ArrayList<>();
        for (StockCard stockCard : stockCards) {
            ProgramDataFormBasicItem item = createProgramDataByPeriod(stockCard, form);
            items.add(item);
        }
        return items;
    }

    protected ProgramDataFormBasicItem createProgramDataByPeriod(StockCard stockCard, final ProgramDataForm form) throws LMISException {
        ProgramDataFormBasicItem item = new ProgramDataFormBasicItem();
        List<StockMovementItem> stockMovementItems = stockMovementRepository.queryStockItemsByCreatedDate(stockCard.getId(), form.getPeriodBegin(), form.getPeriodEnd());
        FormHelper.StockMovementModifiedItem modifiedItem =formHelper.assignTotalValues(stockMovementItems);
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
}
