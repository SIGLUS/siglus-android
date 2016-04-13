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
import com.j256.ormlite.stmt.Where;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.helper.RnrFormHelper;
import org.openlmis.core.model.service.PeriodService;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

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
    ProductProgramRepository productProgramRepository;

    @Inject
    ProgramRepository programRepository;

    @Inject
    RnrFormHelper rnrFormHelper;

    GenericDao<RnRForm> genericDao;
    GenericDao<RnrFormItem> rnrFormItemGenericDao;

    private Context context;
    protected String programCode;

    @Inject
    private PeriodService periodService;

    @Inject
    public RnrFormRepository(Context context) {
        genericDao = new GenericDao<>(RnRForm.class, context);
        rnrFormItemGenericDao = new GenericDao<>(RnrFormItem.class, context);
        this.context = context;
    }

    public RnRForm initNormalRnrForm(Date periodEndDate) throws LMISException {
        RnRForm rnrForm = initRnRForm(periodEndDate, RnRForm.IsEmergency.No);
        return createInitRnrForm(rnrForm);
    }

    public RnRForm initEmergencyRnrForm(Date periodEndDate, List<StockCard> stockCards) throws LMISException {
        RnRForm rnRForm = initRnRForm(periodEndDate, RnRForm.IsEmergency.Yes);
        rnRForm.setRnrFormItemListWrapper(generateRnrFormItems(rnRForm, stockCards));
        return rnRForm;
    }

    public void create(RnRForm rnRForm) throws LMISException {
        genericDao.create(rnRForm);
    }

    public void createRnRsWithItems(final List<RnRForm> forms) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    for (RnRForm form : forms) {
                        create(form);
                        rnrFormItemRepository.create(form.getRnrFormItemListWrapper());
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

    public void update(final RnRForm form) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    rnrFormHelper.updateWrapperList(form);
                    genericDao.update(form);
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    public boolean isPeriodUnique(final RnRForm form) {
        try {
            return null == dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, RnRForm>() {
                @Override
                public RnRForm operate(Dao<RnRForm, String> dao) throws SQLException {
                    return dao.queryBuilder().where().eq("program_id", form.getProgram().getId()).and().eq("status", RnRForm.STATUS.AUTHORIZED).and().eq("periodBegin", form.getPeriodBegin()).and().eq("periodEnd", form.getPeriodEnd()).queryForFirst();
                }
            });

        } catch (LMISException e) {
            e.reportToFabric();
        }
        return false;
    }

    public List<RnRForm> list() throws LMISException {
        return genericDao.queryForAll();
    }

    public List<RnRForm> listWithEmergency(String programCode,  boolean isWithEmergency) throws LMISException {
        return list(programCode, isWithEmergency);
    }

    public List<RnRForm> queryAllUnsyncedForms() throws LMISException {
        List<RnRForm> unsyncedRnr = listUnsynced();
        deleteDeactivatedAndUnsupportedProductItems(unsyncedRnr);
        return unsyncedRnr;
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

    protected void deleteDeactivatedAndUnsupportedProductItems(List<RnRForm> rnRForms) throws LMISException {
        for (RnRForm rnRForm : rnRForms) {

            String programCode = rnRForm.getProgram().getProgramCode();
            List<String> programCodes = programRepository.queryProgramCodesByProgramCodeOrParentCode(programCode);
            List<String> supportedProductCodes = FluentIterable.from(productProgramRepository.listActiveProductProgramsByProgramCodes(programCodes)).transform(new Function<ProductProgram, String>() {
                @Override
                public String apply(ProductProgram productProgram) {
                    return productProgram.getProductCode();
                }
            }).toList();

            rnrFormItemRepository.deleteFormItems(rnRForm.getDeactivatedAndUnsupportedProductItems(supportedProductCodes));
        }
    }

    public List<RnrFormItem> generateRnrFormItems(final RnRForm form, List<StockCard> stockCards) throws LMISException {
        List<RnrFormItem> rnrFormItems = new ArrayList<>();
        for (StockCard stockCard : stockCards) {
            RnrFormItem rnrFormItem = createRnrFormItemByPeriod(stockCard, form.getPeriodBegin(), form.getPeriodEnd());
            rnrFormItem.setForm(form);
            rnrFormItems.add(rnrFormItem);
        }
        return rnrFormItems;
    }

    public void removeRnrForm(RnRForm form) throws LMISException {
        if (form != null) {
            rnrFormItemRepository.deleteFormItems(form.getRnrFormItemListWrapper());
            deleteRegimenItems(form.getRegimenItemListWrapper());
            deleteBaseInfoItems(form.getBaseInfoItemListWrapper());
            genericDao.delete(form);
        }
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

    protected List<RnRForm> listUnsynced() throws LMISException {
        return dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, List<RnRForm>>() {
            @Override
            public List<RnRForm> operate(Dao<RnRForm, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("synced", false).and().eq("status", RnRForm.STATUS.AUTHORIZED).query();
            }
        });
    }

    protected List<StockCard> getStockCardsBeforePeriodEnd(RnRForm form) throws LMISException {
        List<StockCard> stockCards;
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_auto_fill_kit_rnr)) {
            stockCards = stockRepository.listActiveStockCards(form.getProgram().getProgramCode(), ProductRepository.IsWithKit.Yes);
        } else {
            stockCards = stockRepository.listActiveStockCards(form.getProgram().getProgramCode(), ProductRepository.IsWithKit.No);
        }

        for (Iterator iterator = stockCards.iterator(); iterator.hasNext(); ) {
            StockCard stockCard = (StockCard) iterator.next();
            StockMovementItem stockMovementItem = stockRepository.queryFirstStockMovementItem(stockCard);
            if (stockMovementItem != null && (stockMovementItem.getMovementDate().after(form.getPeriodEnd())
                    || stockMovementItem.getCreatedTime().after(form.getPeriodEnd()))) {
                iterator.remove();
            }
        }
        return stockCards;
    }

    protected RnrFormItem createRnrFormItemByPeriod(StockCard stockCard, Date startDate, Date endDate) throws LMISException {
        RnrFormItem rnrFormItem = new RnrFormItem();
        List<StockMovementItem> stockMovementItems = stockRepository.queryStockItemsByPeriodDates(stockCard, startDate, endDate);

        if (stockMovementItems.isEmpty()) {
            rnrFormHelper.initRnrFormItemWithoutMovement(rnrFormItem, lastRnrInventory(stockCard));
        } else {
            rnrFormItem.setInitialAmount(stockMovementItems.get(0).calculatePreviousSOH());
            rnrFormHelper.assignTotalValues(rnrFormItem, stockMovementItems);
        }

        rnrFormItem.setProduct(stockCard.getProduct());
        rnrFormItem.setValidate(stockCard.getEarliestExpireDate());

        return rnrFormItem;
    }

    protected List<RegimenItem> generateRegimeItems(RnRForm form) throws LMISException {
        return new ArrayList<>();
    }

    protected List<BaseInfoItem> generateBaseInfoItems(RnRForm form) {
        return new ArrayList<>();
    }

    private RnRForm initRnRForm(Date periodEndDate, RnRForm.IsEmergency isEmergency) throws LMISException {
        final Program program = programRepository.queryByCode(programCode);
        if (program == null) {
            throw new LMISException("Program cannot be null !");
        }

        Period period = periodService.generateNextPeriod(programCode, periodEndDate);
        return RnRForm.init(program, period, isEmergency.IsEmergency());
    }

    private RnRForm createInitRnrForm(final RnRForm rnrForm) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    create(rnrForm);
                    rnrFormItemRepository.create(generateRnrFormItems(rnrForm, getStockCardsBeforePeriodEnd(rnrForm)));
                    createRegimenItems(generateRegimeItems(rnrForm));
                    createBaseInfoItems(generateBaseInfoItems(rnrForm));
                    genericDao.refresh(rnrForm);
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
        return rnrForm;
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

    private long lastRnrInventory(StockCard stockCard) throws LMISException {
        List<RnRForm> rnRForms = listWithEmergency(programCode, false);
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

    private List<RnRForm> list(String programCode, final boolean isWithEmergency) throws LMISException {
        final List<Long> programIds = programRepository.queryProgramIdsByProgramCodeOrParentCode(programCode);

        return dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, List<RnRForm>>() {
            @Override
            public List<RnRForm> operate(Dao<RnRForm, String> dao) throws SQLException {
                Where<RnRForm, String> where = dao.queryBuilder().orderBy("periodBegin", true).where();
                where.in("program_id", programIds);

                if (!isWithEmergency) {
                    where.and().eq("emergency", false);
                }
                return where.query();
            }
        });
    }

    public void createAndRefresh(RnRForm rnRForm) throws LMISException {
        create(rnRForm);
        genericDao.refresh(rnRForm);
    }
}
