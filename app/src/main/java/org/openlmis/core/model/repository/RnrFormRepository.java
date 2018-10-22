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
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.helper.RnrFormHelper;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

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
    RegimenItemRepository regimenItemRepository;

    @Inject
    RnrFormSignatureRepository signatureRepository;

    @Inject
    BaseInfoItemRepository baseInfoItemRepository;

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
    private RequisitionPeriodService requisitionPeriodService;
    @Inject
    public StockMovementRepository stockMovementRepository;

    @Inject
    public RnrFormRepository(Context context) {
        genericDao = new GenericDao<>(RnRForm.class, context);
        rnrFormItemGenericDao = new GenericDao<>(RnrFormItem.class, context);
        this.context = context;
    }

    public RnRForm initNormalRnrForm(Date periodEndDate) throws LMISException {
        RnRForm rnrForm = initRnRForm(periodEndDate, RnRForm.Emergency.No);
        return createInitRnrForm(rnrForm);
    }

    public RnRForm initEmergencyRnrForm(Date periodEndDate, List<StockCard> stockCards) throws LMISException {
        RnRForm rnRForm = initRnRForm(periodEndDate, RnRForm.Emergency.Yes);
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
                        createOrUpdateWithItems(form);
                    }
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    public void createOrUpdateWithItems(final RnRForm form) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    genericDao.createOrUpdate(form);
                    createOrUpdateRnrWrappers(form);
                    genericDao.refresh(form);
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

    public List<RnRForm> listInclude(RnRForm.Emergency includeEmergency, String programCode) throws LMISException {
        return list(programCode, includeEmergency.Emergency());
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
        RnRForm rnRForm = dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, RnRForm>() {
            @Override
            public RnRForm operate(Dao<RnRForm, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("program_id", program.getId()).and().ne("status", RnRForm.STATUS.AUTHORIZED).queryForFirst();
            }
        });
        assignCategoryForRnrItems(rnRForm);
        return rnRForm;
    }

    public RnRForm queryRnRForm(final long id) throws LMISException {
        RnRForm rnRForm = dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, RnRForm>() {
            @Override
            public RnRForm operate(Dao<RnRForm, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("id", id).queryForFirst();
            }
        });
        assignCategoryForRnrItems(rnRForm);

        return rnRForm;
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
        List<String> programCodes = programRepository.queryProgramCodesByProgramCodeOrParentCode(form.getProgram().getProgramCode());
        for (StockCard stockCard : stockCards) {
            RnrFormItem rnrFormItem = createRnrFormItemByPeriod(stockCard, form.getPeriodBegin(), form.getPeriodEnd());
            rnrFormItem.setForm(form);
            rnrFormItems.add(rnrFormItem);
            rnrFormItem.setCategory(productProgramRepository.queryByCode(rnrFormItem.getProduct().getCode(), programCodes).getCategory());
        }
        return rnrFormItems;
    }

    public void removeRnrForm(RnRForm form) throws LMISException {
        if (form != null) {
            rnrFormItemRepository.deleteFormItems(form.getRnrFormItemListWrapper());
            regimenItemRepository.deleteRegimenItems(form.getRegimenItemListWrapper());
            baseInfoItemRepository.batchDelete(form.getBaseInfoItemListWrapper());
            signatureRepository.batchDelete(form.getSignaturesWrapper());
            genericDao.delete(form);
        }
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


    public boolean hasOldDate() {
        List<RnRForm> list = null;
        try {
            list = list();
        } catch (LMISException e) {
            e.reportToFabric();
        }
        Date dueDateShouldDataLivedInDB = DateUtil.dateMinusMonth(new Date(), SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData());

        if(hasRequisitionData()){
            for(RnRForm rnrForm: list) {
                if (rnrForm.getPeriodEnd().before(dueDateShouldDataLivedInDB)) {
                    return true;
                }
            }
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

    protected RnrFormItem createRnrFormItemByPeriod(StockCard stockCard, Date startDate, Date endDate) throws LMISException {
        RnrFormItem rnrFormItem = new RnrFormItem();
        List<StockMovementItem> stockMovementItems = stockMovementRepository.queryStockItemsByCreatedDate(stockCard.getId(), startDate, endDate);

        if (stockMovementItems.isEmpty()) {
            rnrFormHelper.initRnrFormItemWithoutMovement(rnrFormItem, lastRnrInventory(stockCard));
        } else {
            rnrFormItem.setInitialAmount(stockMovementItems.get(0).calculatePreviousSOH());
            rnrFormHelper.assignTotalValues(rnrFormItem, stockMovementItems);
        }

        rnrFormItem.setProduct(stockCard.getProduct());
        return rnrFormItem;
    }

    protected List<RegimenItem> generateRegimeItems(RnRForm form) throws LMISException {
        return new ArrayList<>();
    }

    protected List<BaseInfoItem> generateBaseInfoItems(RnRForm form) {
        return new ArrayList<>();
    }

    private RnRForm initRnRForm(Date periodEndDate, RnRForm.Emergency emergency) throws LMISException {
        final Program program = programRepository.queryByCode(programCode);
        if (program == null) {
            throw new LMISException("Program cannot be null !");
        }

        Period period = requisitionPeriodService.generateNextPeriod(programCode, periodEndDate);
        return RnRForm.init(program, period, emergency.Emergency());
    }

    private RnRForm createInitRnrForm(final RnRForm rnrForm) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    create(rnrForm);
                    List<StockCard> stockCards = stockRepository.getStockCardsBeforePeriodEnd(rnrForm);
                    rnrFormItemRepository.batchCreateOrUpdate(generateRnrFormItems(rnrForm, stockCards));
                    regimenItemRepository.batchCreateOrUpdate(generateRegimeItems(rnrForm));
                    baseInfoItemRepository.batchCreateOrUpdate(generateBaseInfoItems(rnrForm));
                    genericDao.refresh(rnrForm);
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }

        assignCategoryForRnrItems(rnrForm);

        return rnrForm;
    }

    private void assignCategoryForRnrItems(RnRForm rnrForm) throws LMISException {
        if (rnrForm == null || rnrForm.getRnrFormItemListWrapper() == null) {
            return;
        }
        List<String> programCodes = programRepository.queryProgramCodesByProgramCodeOrParentCode(programCode);

        for (RnrFormItem item : rnrForm.getRnrFormItemListWrapper()) {
            if (item.getProduct() != null) {
                item.setCategory(productProgramRepository.queryByCode(item.getProduct().getCode(), programCodes).getCategory());
            }
        }
    }

    protected long lastRnrInventory(StockCard stockCard) throws LMISException {
        List<RnRForm> rnRForms = listInclude(RnRForm.Emergency.No, programCode);
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

    private void createOrUpdateRnrWrappers(RnRForm form) throws SQLException, LMISException {
        rnrFormItemRepository.batchCreateOrUpdate(form.getRnrFormItemListWrapper());
        signatureRepository.batchCreateOrUpdate(form.getSignaturesWrapper());
        regimenItemRepository.batchCreateOrUpdate(form.getRegimenItemListWrapper());
        baseInfoItemRepository.batchCreateOrUpdate(form.getBaseInfoItemListWrapper());
    }

    public void deleteOldData() {
        String dueDateShouldDataLivedInDB = DateUtil.formatDate(DateUtil.dateMinusMonth(new Date(), SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData()), DateUtil.DB_DATE_FORMAT);

        String rawSqlDeleteRnrFormItems = "DELETE FROM rnr_form_items "
                + "WHERE form_id IN (SELECT id FROM rnr_forms WHERE periodEnd < '" + dueDateShouldDataLivedInDB + "' );";

        String rawSqlDeleteSignature = "DELETE FROM rnr_form_signature "
                + "WHERE form_id IN (SELECT id FROM rnr_forms WHERE periodEnd < '" + dueDateShouldDataLivedInDB + "' );";

        String rawSqlDeleteRegimeItems = "DELETE FROM regime_items "
                + "WHERE form_id IN (SELECT id FROM rnr_forms WHERE periodEnd < '" + dueDateShouldDataLivedInDB + "' );";

        String rawSqlDeleteBaseInfoItems = "DELETE FROM rnr_baseInfo_items "
                + "WHERE rnRForm_id IN (SELECT id FROM rnr_forms WHERE periodEnd < '" + dueDateShouldDataLivedInDB + "' );";

        String rawSqlDeleteRnrForms = "DELETE FROM rnr_forms "
                + "WHERE periodEnd < '" + dueDateShouldDataLivedInDB + "'; ";

        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteRnrFormItems);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteBaseInfoItems);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteRegimeItems);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteSignature);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(rawSqlDeleteRnrForms);
    }
}
