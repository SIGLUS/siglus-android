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

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.service.PeriodService;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class RnrFormRepository {

    @Inject
    DbUtil dbUtil;

    @Inject
    StockRepository stockRepository;

    @Inject
    RegimenRepository regimenRepository;

    @Inject
    RnrFormItemRepository rnrFormItemRepository;

    @Inject
    ProgramRepository programRepository;

    GenericDao<RnRForm> genericDao;
    GenericDao<RnrFormItem> rnrFormItemGenericDao;

    private Context context;
    private GenericDao<RnRFormSignature> signatureDao;
    protected String programCode;

    @Inject
    private PeriodService periodService;

    @Inject
    public RnrFormRepository(Context context) {
        genericDao = new GenericDao<>(RnRForm.class, context);
        rnrFormItemGenericDao = new GenericDao<>(RnrFormItem.class, context);
        signatureDao = new GenericDao<>(RnRFormSignature.class, context);
        this.context = context;
    }

    public RnRForm initRnrForm(Date periodEndDate) throws LMISException {
        final Program program = programRepository.queryByCode(programCode);
        if (program == null) {
            throw new LMISException("Program cannot be null !");
        }

        RnRForm form;
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_requisition_period_logic_change)) {
            Period period = periodService.generateNextPeriod(programCode, periodEndDate);
            form = RnRForm.init(program, period);
        } else {
            form = RnRForm.init(program, DateUtil.today());
        }

        final RnRForm finalRnrForm = form;
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    create(finalRnrForm);
                    createRnrFormItems(generateRnrFormItems(finalRnrForm));
                    createRegimenItems(generateRegimeItems(finalRnrForm));
                    createBaseInfoItems(generateBaseInfoItems(finalRnrForm));
                    genericDao.refresh(finalRnrForm);
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
        return form;
    }

    public void createFormAndItems(final List<RnRForm> forms) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    for (RnRForm form : forms) {
                        create(form);
                        createRnrFormItems(form.getRnrFormItemListWrapper());
                        createRegimenItems(form.getRegimenItemListWrapper());
                        createBaseInfoItems(form.getBaseInfoItemListWrapper());
                    }
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    public void create(RnRForm rnRForm) throws LMISException {
        genericDao.create(rnRForm);
    }

    public void save(final RnRForm form) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    updateWrapperList(form);
                    genericDao.update(form);
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    public void submit(RnRForm form) throws LMISException {
        if (form.isMissed()) {
            form.setStatus(RnRForm.STATUS.SUBMITTED_MISSED);
        } else {
            form.setStatus(RnRForm.STATUS.SUBMITTED);
        }
        save(form);
    }

    public void authorise(RnRForm form) throws LMISException {
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        form.setSubmittedTime(DateUtil.today());
        save(form);
    }

    public boolean isPeriodUnique(final RnRForm form) {
        try {
            RnRForm rnRForm = dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, RnRForm>() {
                @Override
                public RnRForm operate(Dao<RnRForm, String> dao) throws SQLException {
                    return dao.queryBuilder().where().eq("program_id", form.getProgram().getId()).and().eq("status", RnRForm.STATUS.AUTHORIZED).and().eq("periodBegin", form.getPeriodBegin()).and().eq("periodEnd", form.getPeriodEnd()).queryForFirst();
                }
            });

            return rnRForm == null;
        } catch (LMISException e) {
            e.reportToFabric();
        }
        return false;
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

    public List<RnRForm> list(String programCode) throws LMISException {
        final List<Long> programIds = programRepository.queryProgramIdsByProgramCodeOrParentCode(programCode);

        return dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, List<RnRForm>>() {
            @Override
            public List<RnRForm> operate(Dao<RnRForm, String> dao) throws SQLException {
                return dao.queryBuilder().orderBy("periodBegin", true).where().in("program_id", programIds).query();
            }
        });
    }

    public List<RnRForm> deleteDeactivatedProductItemsFromUnsyncedForms() throws LMISException {
        List<RnRForm> unsyncedRnr = listUnsynced();
        deleteDeactivatedProductItemsFromForms(unsyncedRnr);
        return unsyncedRnr;
    }

    public List<RnRForm> listMMIA() throws LMISException {
        return list(Constants.MMIA_PROGRAM_CODE);
    }


    public RnRForm queryUnAuthorized() throws LMISException {
        final Program program = programRepository.queryByCode(programCode);
        if (program == null) {
            throw new LMISException("Program cannot be null !");
        }
        return dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, RnRForm>() {
            @Override
            public RnRForm operate(Dao<RnRForm, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("program_id", program.getId()).and().ne("status", RnRForm.STATUS.AUTHORIZED).queryForFirst();
            }
        });
    }

    public RnRForm queryRnRForm(final long id) throws LMISException {
        return dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, RnRForm>() {
            @Override
            public RnRForm operate(Dao<RnRForm, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("id", id).queryForFirst();
            }
        });
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

    public void createRegimenItem(final RegimenItem regimenItem) throws LMISException {
        dbUtil.withDao(RegimenItem.class, new DbUtil.Operation<RegimenItem, Void>() {
            @Override
            public Void operate(Dao<RegimenItem, String> dao) throws SQLException {
                dao.create(regimenItem);
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

    private List<RnRForm> listUnsynced() throws LMISException {
        return dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, List<RnRForm>>() {
            @Override
            public List<RnRForm> operate(Dao<RnRForm, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("synced", false).and().eq("status", RnRForm.STATUS.AUTHORIZED).query();
            }
        });
    }

    private void deleteDeactivatedProductItemsFromForms(List<RnRForm> rnRForms) throws LMISException {
        for (RnRForm rnRForm : rnRForms) {
            deleteRnrFormItems(rnRForm.getDeactivatedProductItems());
        }
    }

    protected List<BaseInfoItem> generateBaseInfoItems(RnRForm form) {
        return new ArrayList<>();
    }

    protected List<RnrFormItem> generateRnrFormItems(final RnRForm form) throws LMISException {
        List<StockCard> stockCards = getStockCardsBeforePeriodEnd(form);

        List<RnrFormItem> rnrFormItems = new ArrayList<>();

        for (StockCard stockCard : stockCards) {
            RnrFormItem rnrFormItem = createRnrFormItemByPeriod(stockCard, form.getPeriodBegin(), form.getPeriodEnd());
            rnrFormItem.setForm(form);
            rnrFormItems.add(rnrFormItem);
        }
        return rnrFormItems;
    }

    protected List<StockCard> getStockCardsBeforePeriodEnd(RnRForm form) throws LMISException {
        List<StockCard> stockCards;
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_auto_fill_kit_rnr)) {
            stockCards = stockRepository.listActiveStockCardsWithKit(form.getProgram().getProgramCode());
        } else {
            stockCards = stockRepository.listActiveStockCardsWithOutKit(form.getProgram().getProgramCode());
        }

        for (Iterator iterator = stockCards.iterator(); iterator.hasNext(); ) {
            StockCard stockCard = (StockCard) iterator.next();
            StockMovementItem stockMovementItem = stockRepository.queryFirstStockMovementItem(stockCard);
            if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_requisition_period_logic_change)) {
                if (stockMovementItem != null && (stockMovementItem.getMovementDate().after(form.getPeriodEnd()) || stockMovementItem.getCreatedTime().after(form.getPeriodEnd()))) {
                    iterator.remove();
                }
            } else {
                if (stockMovementItem != null && (stockMovementItem.getMovementDate().after(form.getPeriodEnd()))) {
                    iterator.remove();
                }
            }
        }
        return stockCards;
    }

    protected RnrFormItem createRnrFormItemByPeriod(StockCard stockCard, Date startDate, Date endDate) throws LMISException {
        RnrFormItem rnrFormItem = new RnrFormItem();
        List<StockMovementItem> stockMovementItems;

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_requisition_period_logic_change)) {
            stockMovementItems = stockRepository.queryStockItemsByPeriodDates(stockCard, startDate, endDate);
        } else {
            stockMovementItems = stockRepository.queryStockItems(stockCard, startDate, endDate);
        }

        if (stockMovementItems.isEmpty()) {
            initRnrFormItemWithoutMovement(stockCard, rnrFormItem, startDate);
        } else {
            rnrFormItem.setInitialAmount(stockMovementItems.get(0).calculatePreviousSOH());
            assignTotalValues(rnrFormItem, stockMovementItems);
        }

        rnrFormItem.setProduct(stockCard.getProduct());
        rnrFormItem.setValidate(stockCard.getEarliestExpireDate());

        return rnrFormItem;
    }

    private void initRnrFormItemWithoutMovement(StockCard stockCard, RnrFormItem rnrFormItem, Date startDate) throws LMISException {
        rnrFormItem.setReceived(0);
        rnrFormItem.setIssued(0);
        rnrFormItem.setAdjustment(0);
        rnrFormItem.setCalculatedOrderQuantity(0L);

        long lastRnrInventory = lastRnrInventory(stockCard);
        rnrFormItem.setInitialAmount(lastRnrInventory);
        rnrFormItem.setInventory(lastRnrInventory);
    }

    private long lastRnrInventory(StockCard stockCard) throws LMISException {
        List<RnRForm> rnRForms = list(programCode);
        if (rnRForms.isEmpty()) {
            return 0;
        }
        List<RnrFormItem> rnrFormItemListWrapper = rnRForms.get(rnRForms.size() - 1).getRnrFormItemListWrapper();
        for (RnrFormItem item : rnrFormItemListWrapper) {
            if (item.getProduct().getId() == stockCard.getProduct().getId()) {
                return item.getInventory();
            }
        }
        return 0;
    }

    private void assignTotalValues(RnrFormItem rnrFormItem, List<StockMovementItem> stockMovementItems) {
        long totalReceived = 0;
        long totalIssued = 0;
        long totalAdjustment = 0;

        for (StockMovementItem item : stockMovementItems) {
            if (StockMovementItem.MovementType.RECEIVE == item.getMovementType()) {
                totalReceived += item.getMovementQuantity();
            } else if (StockMovementItem.MovementType.ISSUE == item.getMovementType()) {
                totalIssued += item.getMovementQuantity();
            } else if (StockMovementItem.MovementType.NEGATIVE_ADJUST == item.getMovementType()) {
                totalAdjustment -= item.getMovementQuantity();
            } else if (StockMovementItem.MovementType.POSITIVE_ADJUST == item.getMovementType()) {
                totalAdjustment += item.getMovementQuantity();
            }
        }
        rnrFormItem.setReceived(totalReceived);
        rnrFormItem.setIssued(totalIssued);
        rnrFormItem.setAdjustment(totalAdjustment);

        Long inventory = stockMovementItems.get(stockMovementItems.size() - 1).getStockOnHand();
        rnrFormItem.setInventory(inventory);

        rnrFormItem.setCalculatedOrderQuantity(calculatedOrderQuantity(totalIssued, inventory));
    }

    private long calculatedOrderQuantity(long totalIssued, Long inventory) {
        Long totalRequest = totalIssued * 2 - inventory;
        return totalRequest > 0 ? totalRequest : 0;
    }

    protected List<RegimenItem> generateRegimeItems(RnRForm form) throws LMISException {
        return new ArrayList<>();
    }

    public void removeRnrForm(RnRForm form) throws LMISException {
        if (form != null) {
            deleteRnrFormItems(form.getRnrFormItemListWrapper());
            deleteRegimenItems(form.getRegimenItemListWrapper());
            deleteBaseInfoItems(form.getBaseInfoItemListWrapper());
            genericDao.delete(form);
        }
    }

    private void deleteBaseInfoItems(final List<BaseInfoItem> baseInfoItemListWrapper) throws LMISException {
        dbUtil.withDaoAsBatch(BaseInfoItem.class, new DbUtil.Operation<BaseInfoItem, Void>() {
            @Override
            public Void operate(Dao<BaseInfoItem, String> dao) throws SQLException {
                for (BaseInfoItem item : baseInfoItemListWrapper) {
                    dao.delete(item);
                }
                return null;
            }
        });
    }

    private void deleteRegimenItems(final List<RegimenItem> regimenItemListWrapper) throws LMISException {
        dbUtil.withDao(RegimenItem.class, new DbUtil.Operation<RegimenItem, Void>() {
            @Override
            public Void operate(Dao<RegimenItem, String> dao) throws SQLException {
                for (RegimenItem item : regimenItemListWrapper) {
                    dao.delete(item);
                }
                return null;
            }
        });
    }

    private void deleteRnrFormItems(final List<RnrFormItem> rnrFormItemListWrapper) throws LMISException {
        rnrFormItemRepository.delete(rnrFormItemListWrapper);
    }

    public void setSignature(RnRForm form, String signature, RnRFormSignature.TYPE type) throws LMISException {
        signatureDao.create(new RnRFormSignature(form, signature, type));
    }

    public List<RnRFormSignature> querySignaturesByRnrForm(final RnRForm form) throws LMISException {
        if (form == null) {
            throw new LMISException("RnRForm cannot be null !");
        }
        return dbUtil.withDao(RnRFormSignature.class, new DbUtil.Operation<RnRFormSignature, List<RnRFormSignature>>() {
            @Override
            public List<RnRFormSignature> operate(Dao<RnRFormSignature, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("form_id", form.getId()).query();
            }
        });
    }

    public boolean hasRequisitionData() {
        try {
            List<RnRForm> list = list();
            if (list != null && list.size() > 0) {
                return true;
            }
        } catch (LMISException e) {
            e.reportToFabric();
        }
        return false;
    }
}
