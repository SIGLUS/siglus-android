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
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
    public RnrFormRepository(Context context) {
        genericDao = new GenericDao<>(RnRForm.class, context);
        rnrFormItemGenericDao = new GenericDao<>(RnrFormItem.class, context);
        signatureDao = new GenericDao<>(RnRFormSignature.class, context);
        this.context = context;
    }

    public RnRForm initRnrForm() throws LMISException {
        return initRnrForm(programRepository.queryByCode(programCode));
    }

    private RnRForm initRnrForm(final Program program) throws LMISException {
        if (program == null) {
            throw new LMISException("Program cannot be null !");
        }
        final RnRForm form = RnRForm.init(program, DateUtil.today());
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    create(form);
                    createRnrFormItems(generateRnrFormItems(form));
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
        form.setStatus(RnRForm.STATUS.SUBMITTED);
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
        final long programId = programRepository.queryByCode(programCode).getId();

        return dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, List<RnRForm>>() {
            @Override
            public List<RnRForm> operate(Dao<RnRForm, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("program_id", programId).query();
            }
        });
    }

    public List<RnRForm> listUnSynced() throws LMISException {
        return dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, List<RnRForm>>() {
            @Override
            public List<RnRForm> operate(Dao<RnRForm, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("synced", false).and().eq("status", RnRForm.STATUS.AUTHORIZED).query();
            }
        });
    }

    public List<RnRForm> listMMIA() throws LMISException {
        return list(MMIARepository.MMIA_PROGRAM_CODE);
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

    //TODO add Test
    //TODO  query 时间 23:59:59
    protected List<RnrFormItem> generateRnrFormItems(final RnRForm form) throws LMISException {
        List<StockCard> stockCards = stockRepository.listByProgramId(form.getProgram().getId());
        List<StockCard> stockCardsTemp = new ArrayList<>(stockCards);

        for (StockCard stockCard : stockCards) {
            StockMovementItem stockMovementItem = stockRepository.queryFirstStockMovementItem(stockCard);
            if (stockMovementItem.getMovementDate().after(form.getPeriodEnd())) {
                stockCardsTemp.remove(stockCard);
            }
        }

        List<RnrFormItem> rnrFormItems = new ArrayList<>();

        for (StockCard stockCard : stockCardsTemp) {
            RnrFormItem rnrFormItem = createRnrFormItemByPeriod(stockCard, form.getPeriodBegin(), form.getPeriodEnd());
            rnrFormItem.setForm(form);
            rnrFormItems.add(rnrFormItem);
        }
        return rnrFormItems;
    }

    private RnrFormItem createRnrFormItemByPeriod(StockCard stockCard, Date startDate, Date endDate) throws LMISException {
        RnrFormItem rnrFormItem = new RnrFormItem();

        List<StockMovementItem> stockMovementItems = stockRepository.queryStockItems(stockCard, startDate, endDate);
        if (!stockMovementItems.isEmpty()) {
            adjustInitialAmount(rnrFormItem, stockMovementItems.get(0));
            assignTotalValues(rnrFormItem, stockMovementItems);
        } else {
            rnrFormItem.setReceived(0);
            rnrFormItem.setIssued(0);
            rnrFormItem.setAdjustment(0);
            rnrFormItem.setCalculatedOrderQuantity(0L);

            rnrFormItem.setInitialAmount(stockCard.getStockOnHand());
            rnrFormItem.setInventory(stockCard.getStockOnHand());
        }

        rnrFormItem.setProduct(stockCard.getProduct());
        rnrFormItem.setValidate(stockCard.getEarliestExpireDate());

        return rnrFormItem;
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

        Long totalRequest = totalIssued * 2 - inventory;
        rnrFormItem.setCalculatedOrderQuantity(totalRequest > 0 ? totalRequest : 0);
    }

    private void adjustInitialAmount(RnrFormItem rnrFormItem, StockMovementItem firstItem) {
        if (firstItem.getMovementType() == StockMovementItem.MovementType.ISSUE
                || firstItem.getMovementType() == StockMovementItem.MovementType.NEGATIVE_ADJUST) {
            rnrFormItem.setInitialAmount(firstItem.getStockOnHand() + firstItem.getMovementQuantity());
        } else if (firstItem.getMovementType() == StockMovementItem.MovementType.RECEIVE
                || firstItem.getMovementType() == StockMovementItem.MovementType.POSITIVE_ADJUST) {
            rnrFormItem.setInitialAmount(firstItem.getStockOnHand() - firstItem.getMovementQuantity());
        } else {
            rnrFormItem.setInitialAmount(firstItem.getStockOnHand());
        }
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

    private void deleteBaseInfoItems(final ArrayList<BaseInfoItem> baseInfoItemListWrapper) throws LMISException {
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

    private void deleteRegimenItems(final ArrayList<RegimenItem> regimenItemListWrapper) throws LMISException {
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

    public RnRFormSignature querySignature(final RnRForm form, final RnRFormSignature.TYPE type) throws LMISException {
        if (form == null) {
            throw new LMISException("RnRForm cannot be null !");
        }
        return dbUtil.withDao(RnRFormSignature.class, new DbUtil.Operation<RnRFormSignature, RnRFormSignature>() {
            @Override
            public RnRFormSignature operate(Dao<RnRFormSignature, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("form_id", form.getId()).and().eq("type", type).queryForFirst();
            }
        });
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
