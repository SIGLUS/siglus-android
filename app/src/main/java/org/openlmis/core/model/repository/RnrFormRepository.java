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
import com.j256.ormlite.misc.TransactionManager;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Callable;

public class RnrFormRepository {

    public static final int DAY_PERIOD_END = 20;
    private static final String TAG = "RnrFormRepository";

    @Inject
    DbUtil dbUtil;

    @Inject
    StockRepository stockRepository;

    @Inject
    RegimenRepository regimenRepository;

    @Inject
    RnrFormItemRepository rnrFormItemRepository;

    GenericDao<RnRForm> genericDao;
    GenericDao<RnrFormItem> rnrFormItemGenericDao;

    private Context context;


    @Inject
    public RnrFormRepository(Context context){
        genericDao = new GenericDao<>(RnRForm.class, context);
        rnrFormItemGenericDao = new GenericDao<>(RnrFormItem.class, context);
        this.context = context;
    }


    public RnRForm initRnrForm(final Program program) throws LMISException {
        if (program == null){
            throw  new LMISException("Program cannot be null !");
        }

        final RnRForm form = new RnRForm();
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {

                    form.setProgram(program);
                    create(form);
                    createRnrFormItems(generateProductItems(form));
                    createRegimenItems(generateRegimeItems(form));
                    createBaseInfoItems(generateBaseInfoItems(form));
                    genericDao.refresh(form);
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }

        return form;
    }

    protected void create(RnRForm rnRForm) throws LMISException{
        genericDao.create(rnRForm);
    }

    public void save(RnRForm form) throws LMISException, SQLException {
        updateWrapperList(form);
        genericDao.update(form);
    }

    public void updateWrapperList(RnRForm form) throws SQLException {
        for (RnrFormItem item : form.getRnrFormItemListWrapper()) {
            form.getRnrFormItemList().update(item);
        }
        for (RegimenItem item : form.getRegimenItemListWrapper()) {
            form.getRegimenItemList().update(item);
        }
        for (BaseInfoItem item : form.getBaseInfoItemListWrapper()) {
            form.getBaseInfoItemList().update(item);
        }
    }

    public List<RnRForm> list() throws LMISException {
        return genericDao.queryForAll();
    }

    public List<RnRForm> listUnSynced() throws LMISException{
        return dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, List<RnRForm>>() {
            @Override
            public List<RnRForm> operate(Dao<RnRForm, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("synced", false).and().eq("status", RnRForm.STATUS.AUTHORIZED).query();
            }
        });
    }

    public RnRForm queryDraft(final Program program) throws LMISException{
        return dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, RnRForm>() {
            @Override
            public RnRForm operate(Dao<RnRForm, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("program_id", program.getId()).and().eq("status", RnRForm.STATUS.DRAFT).queryForFirst();
            }
        });
    }


    public void approve(RnRForm form) throws LMISException{
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        genericDao.update(form);
    }

    private void createRnrFormItems(List<RnrFormItem> form) throws LMISException {
        rnrFormItemRepository.create(form);
    }

    public void createRegimenItems(final List<RegimenItem> regimenItemList) throws LMISException {
        dbUtil.withDao(RegimenItem.class, new DbUtil.Operation<RegimenItem, Void>() {
            @Override
            public Void operate(Dao<RegimenItem, String> dao) throws SQLException {
                for (RegimenItem item : regimenItemList) {
                    dao.create(item);
                }
                return null;
            }
        });
    }

    public void createBaseInfoItems(final List<BaseInfoItem> baseInfoItemList) throws LMISException {
        dbUtil.withDaoAsBatch(BaseInfoItem.class, new DbUtil.Operation<BaseInfoItem, Void>() {
            @Override
            public Void operate(Dao<BaseInfoItem, String> dao) throws SQLException {
                for (BaseInfoItem item : baseInfoItemList) {
                    dao.create(item);
                }
                return null;
            }
        });
    }


    protected List<BaseInfoItem> generateBaseInfoItems(RnRForm form) {
        return null;
    }

    protected List<RnrFormItem> generateProductItems(RnRForm form) throws LMISException {
        List<StockCard> stockCards = stockRepository.list(form.getProgram().getProgramCode());
        List<RnrFormItem> productItems = new ArrayList<>();

        Calendar calendar = GregorianCalendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        Date startDate = new GregorianCalendar(year, month - 1, DAY_PERIOD_END + 1).getTime();
        Date endDate = new GregorianCalendar(year, month, DAY_PERIOD_END).getTime();

        for (StockCard stockCard : stockCards) {
            List<StockMovementItem> stockMovementItems = stockRepository.queryStockItems(stockCard, startDate, endDate);
            RnrFormItem productItem = new RnrFormItem();
            if (stockMovementItems.size() > 0) {

                StockMovementItem firstItem = stockMovementItems.get(0);

                if (firstItem.getMovementType() == StockMovementItem.MovementType.ISSUE
                        || firstItem.getMovementType() == StockMovementItem.MovementType.NEGATIVE_ADJUST){

                    productItem.setInitialAmount(firstItem.getStockOnHand() + firstItem.getMovementQuantity());
                } else {
                    productItem.setInitialAmount(firstItem.getStockOnHand() - firstItem.getMovementQuantity());
                }

                long totalReceived = 0;
                long totalIssued = 0;
                long totalAdjustment = 0;

                for (StockMovementItem item : stockMovementItems) {
                    if (StockMovementItem.MovementType.RECEIVE == item.getMovementType()) {
                        totalReceived += item.getMovementQuantity();
                    } else if (StockMovementItem.MovementType.ISSUE == item.getMovementType()) {
                        totalIssued += item.getMovementQuantity();
                    } else {
                        totalAdjustment += item.getMovementQuantity();
                    }
                }
                productItem.setProduct(stockCard.getProduct());
                productItem.setReceived(totalReceived);
                productItem.setIssued(totalIssued);
                productItem.setAdjustment(totalAdjustment);
                productItem.setForm(form);

                Long inventory = stockMovementItems.get(stockMovementItems.size() - 1).getStockOnHand();
                productItem.setInventory(inventory);
                productItem.setValidate(stockCard.getEarliestExpireDate());

                Long totalRequest = totalIssued * 2 - inventory;
                totalRequest = totalRequest > 0 ? totalRequest : 0;
                productItem.setCalculatedOrderQuantity(totalRequest);

            } else {
                productItem.setProduct(stockCard.getProduct());
                productItem.setReceived(new Long(0L));
                productItem.setIssued(new Long(0L));
                productItem.setAdjustment(new Long(0L));
                productItem.setForm(form);
                productItem.setInventory(stockCard.getStockOnHand());
                productItem.setValidate(stockCard.getEarliestExpireDate());
                productItem.setCalculatedOrderQuantity(new Long(0L));
            }
            productItems.add(productItem);
        }

        return productItems;
    }

    protected List<RegimenItem> generateRegimeItems(RnRForm form) throws LMISException {
        return new ArrayList<>();
    }
}
