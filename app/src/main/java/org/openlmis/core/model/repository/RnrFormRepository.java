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
import android.database.Cursor;

import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.Where;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.ReportTypeForm;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RnrFormRepository {
    private static final String TAG = RnrFormRepository.class.getSimpleName();

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
    RegimenItemThreeLineRepository regimenItemThreeLineRepository;

    @Inject
    RnrFormSignatureRepository signatureRepository;

    @Inject
    BaseInfoItemRepository baseInfoItemRepository;

    @Inject
    ProductProgramRepository productProgramRepository;

    @Inject
    ProgramRepository programRepository;

    @Inject
    ReportTypeFormRepository reportTypeFormRepository;

    @Inject
    RnrFormHelper rnrFormHelper;

    GenericDao<RnRForm> genericDao;
    GenericDao<RnrFormItem> rnrFormItemGenericDao;

    private Context context;
    protected String programCode;

    private List<RnRForm> rnRForms;
    private List<RnrFormItem> rnrFormItemListWrapper;

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
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
                for (RnRForm form : forms) {
                    createOrUpdateWithItems(form);
                }
                return null;
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    public void createOrUpdateWithItems(final RnRForm form) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
                genericDao.createOrUpdate(form);
                createOrUpdateRnrWrappers(form);
                genericDao.refresh(form);
                return null;
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    public boolean isPeriodUnique(final RnRForm form) {
        try {
            return null == dbUtil.withDao(RnRForm.class, dao -> dao.queryBuilder().where().eq("program_id", form.getProgram().getId())
                    .and().eq("status", RnRForm.STATUS.AUTHORIZED)
                    .and().eq("periodBegin", form.getPeriodBegin())
                    .and().eq("periodEnd", form.getPeriodEnd())
                    .queryForFirst());

        } catch (LMISException e) {
            new LMISException(e, "RnrFormRepository.isPeriodUnique").reportToFabric();
        }
        return false;
    }

    public List<RnRForm> list() throws LMISException {
        return genericDao.queryForAll();
    }

    public List<RnRForm> listInclude(RnRForm.Emergency includeEmergency, String programCode) throws LMISException {
        ReportTypeForm reportTypeForm = reportTypeFormRepository.getReportType(programCode);
        return listInclude(includeEmergency, programCode, reportTypeForm);
    }

    public List<RnRForm> listInclude(RnRForm.Emergency includeEmergency, String programCode, ReportTypeForm reportTypeForm) throws LMISException {
        return list(programCode, includeEmergency.Emergency(), reportTypeForm);
    }

    public List<RnRForm> queryAllUnsyncedForms() throws LMISException {
        List<RnRForm> unsyncedRnr = listNotSynchronizedFromStarTime();
        deleteDeactivatedAndUnsupportedProductItems(unsyncedRnr);
        return unsyncedRnr;
    }

    public RnRForm queryUnAuthorized() throws LMISException {
        final Program program = programRepository.queryByCode(programCode);
        ReportTypeForm reportTypeForm = reportTypeFormRepository.getReportType(programCode);
        if (program == null) {
            LMISException e = new LMISException("Program cannot be null !");
            e.reportToFabric();
            throw e;
        }
        RnRForm rnRForm = dbUtil.withDao(RnRForm.class, dao -> dao.queryBuilder().where().eq("program_id", program.getId())
                .and().between("periodBegin", reportTypeForm.getStartTime(), new Date())
                .and().ne("status", RnRForm.STATUS.AUTHORIZED)
                .queryForFirst());
        assignCategoryForRnrItems(rnRForm);
        return rnRForm;
    }

    public RnRForm queryRnRForm(final long id) throws LMISException {
        RnRForm rnRForm = dbUtil.withDao(RnRForm.class, dao -> dao.queryBuilder().where().eq("id", id).queryForFirst());
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
        //为避免超时，在进入循环之前对以下两个变量赋值
        rnRForms = listInclude(RnRForm.Emergency.No, programCode);
        //避免出现越界异常，需要条件判断
        if (rnRForms.size() > 1) {
            rnrFormItemListWrapper = rnRForms.get(rnRForms.size() - 2).getRnrFormItemListWrapper();
        }
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
            regimenItemThreeLineRepository.deleteRegimeThreeLineItems(form.getRegimenThreeLineListWrapper());
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
            new LMISException(e, "RnrFormRepository.hasRequisitionData").reportToFabric();
        }
        return false;
    }


    public boolean hasOldDate() {
        List<RnRForm> list = null;
        try {
            list = list();
        } catch (LMISException e) {
            new LMISException(e, "RnrFormRepository.hasOldDate").reportToFabric();
        }
        Date dueDateShouldDataLivedInDB = DateUtil.dateMinusMonth(new Date(), SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData());

        if (list != null && list.size() > 0) {
            for (RnRForm rnrForm : list) {
                if (rnrForm.getPeriodEnd().before(dueDateShouldDataLivedInDB)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected List<RnRForm> listUnsynced() throws LMISException {
        return dbUtil.withDao(RnRForm.class, dao -> dao.queryBuilder().where().eq("synced", false).and().eq("status", RnRForm.STATUS.AUTHORIZED).query());
    }

    protected List<RnRForm> listNotSynchronizedFromStarTime() throws LMISException {
        List<RnRForm> rnRForms = new ArrayList<>();
        for (Constants.Program program : Constants.PROGRAMES) {
            rnRForms.addAll(listNotSynchronizedFromReportStartTime(program.getCode()));
        }
        return rnRForms;
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

    protected List<RegimenItemThreeLines> generateRegimeThreeLineItems(RnRForm form) throws LMISException {
        return new ArrayList<>();
    }

    protected List<BaseInfoItem> generateBaseInfoItems(RnRForm form, MMIARepository.ReportType type) {
        return new ArrayList<>();
    }

    private RnRForm initRnRForm(Date periodEndDate, RnRForm.Emergency emergency) throws LMISException {
        final Program program = programRepository.queryByCode(programCode);
        if (program == null) {
            LMISException e = new LMISException("Program cannot be null !");
            e.reportToFabric();
            throw e;
        }

        Period period = requisitionPeriodService.generateNextPeriod(programCode, periodEndDate);
        return RnRForm.init(program, period, emergency.Emergency());
    }

    private RnRForm createInitRnrForm(final RnRForm rnrForm) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
                create(rnrForm);
                List<StockCard> stockCards = new ArrayList<>();
                List<StockCard> stockCardWithMovement = stockRepository.getStockCardsBeforePeriodEnd(rnrForm);
                if (Constants.VIA_PROGRAM_CODE.equals(rnrForm.getProgram().getProgramCode())) {
                    List<StockCard> stockCardsBelongToProgram = stockRepository.getStockCardsBelongToProgram(Constants.VIA_PROGRAM_CODE);
                    stockCards.clear();
                    stockCards.addAll(combineStockCard(stockCardWithMovement, stockCardsBelongToProgram));
                } else {
                    stockCards.addAll(stockCardWithMovement);
                }
                rnrFormItemRepository.batchCreateOrUpdate(generateRnrFormItems(rnrForm, stockCards));
                regimenItemRepository.batchCreateOrUpdate(generateRegimeItems(rnrForm));
                regimenItemThreeLineRepository.batchCreateOrUpdate(generateRegimeThreeLineItems(rnrForm));
                baseInfoItemRepository.batchCreateOrUpdate(generateBaseInfoItems(rnrForm, MMIARepository.ReportType.NEW));
                genericDao.refresh(rnrForm);
                return null;
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }

        assignCategoryForRnrItems(rnrForm);

        return rnrForm;
    }


    private List<StockCard> combineStockCard(List<StockCard> stockCardsWithMovement, List<StockCard> belongToProgram) {
        List<StockCard> stockCards = new ArrayList<>();
        for (StockCard stockCard : belongToProgram) {
            if (!stockCardsWithMovement.contains(stockCard)) {
                stockCards.add(stockCard);
            }
        }
        stockCards.addAll(stockCardsWithMovement);
        return stockCards;
    }

    private void assignCategoryForRnrItems(RnRForm rnrForm) throws LMISException {
        if (rnrForm == null || rnrForm.getRnrFormItemListWrapper() == null) {
            return;
        }

        if (rnrForm.getProgram() != null
                && "MMIA".equals(rnrForm.getProgram().getProgramCode())
                && rnrForm.isOldMMIALayout()) {
            for (RnrFormItem item : rnrForm.getRnrFormItemListWrapper()) {
                if (item.getProduct() != null) {
                    item.setCategory(getCategory(item.getProduct().getCode()));
                }
            }
        } else {
            List<String> programCodes = programRepository.queryProgramCodesByProgramCodeOrParentCode(programCode);

            for (RnrFormItem item : rnrForm.getRnrFormItemListWrapper()) {
                ProductProgram productProgram = null;
                if (item.getProduct() != null) {
                    productProgram = productProgramRepository.queryByCode(item.getProduct().getCode(), programCodes);
                }
                if (productProgram != null) {
                    item.setCategory(productProgram.getCategory());
                }
            }
        }
    }

    private String getCategory(String productCode) {
        Map<String, String> productCategoryMap = new HashMap<>();
        productCategoryMap.put("08S01", Product.MEDICINE_TYPE_ADULT);
        productCategoryMap.put("08S01ZY", Product.MEDICINE_TYPE_ADULT);
        productCategoryMap.put("08S13", Product.MEDICINE_TYPE_ADULT);
        productCategoryMap.put("08S15", Product.MEDICINE_TYPE_ADULT);
        productCategoryMap.put("08S18WI", Product.MEDICINE_TYPE_ADULT);
        productCategoryMap.put("08S18Y", Product.MEDICINE_TYPE_ADULT);
        productCategoryMap.put("08S18Z", Product.MEDICINE_TYPE_ADULT);
        productCategoryMap.put("08S21", Product.MEDICINE_TYPE_ADULT);
        productCategoryMap.put("08S22", Product.MEDICINE_TYPE_ADULT);
        productCategoryMap.put("08S32", Product.MEDICINE_TYPE_ADULT);
        productCategoryMap.put("08S39Z", Product.MEDICINE_TYPE_ADULT);
        productCategoryMap.put("08S40", Product.MEDICINE_TYPE_ADULT);
        productCategoryMap.put("08S42", Product.MEDICINE_TYPE_ADULT);
        productCategoryMap.put("08S36", Product.MEDICINE_TYPE_ADULT);

        productCategoryMap.put("08S01B", Product.MEDICINE_TYPE_CHILDREN);
        productCategoryMap.put("08S01ZZ", Product.MEDICINE_TYPE_CHILDREN);
        productCategoryMap.put("08S19", Product.MEDICINE_TYPE_CHILDREN);
        productCategoryMap.put("08S20", Product.MEDICINE_TYPE_CHILDREN);
        productCategoryMap.put("08S30YX", Product.MEDICINE_TYPE_CHILDREN);
        productCategoryMap.put("08S32Z", Product.MEDICINE_TYPE_CHILDREN);
        productCategoryMap.put("08S34B", Product.MEDICINE_TYPE_CHILDREN);
        productCategoryMap.put("08S38Y", Product.MEDICINE_TYPE_CHILDREN);
        productCategoryMap.put("08S39B", Product.MEDICINE_TYPE_CHILDREN);
        productCategoryMap.put("08S39Y", Product.MEDICINE_TYPE_CHILDREN);
        productCategoryMap.put("08S40Z", Product.MEDICINE_TYPE_CHILDREN);
        productCategoryMap.put("08S42B", Product.MEDICINE_TYPE_CHILDREN);

        productCategoryMap.put("08S17", Product.MEDICINE_TYPE_SOLUTION);
        productCategoryMap.put("08S23", Product.MEDICINE_TYPE_SOLUTION);

        return productCategoryMap.get(productCode);
    }

    protected long lastRnrInventory(StockCard stockCard) {
        return lastRnrInventory(stockCard.getProduct());
    }

    protected long lastRnrInventory(Product product) {
        if (rnrFormItemListWrapper != null) {
            for (RnrFormItem item : rnrFormItemListWrapper) {
                if (item.getProduct() != null && (item.getProduct().getId() == product.getId())) {
                    return item.getInventory();
                }
            }
        }
        return 0;
    }

    private List<RnRForm> list(String programCode, final boolean isWithEmergency, ReportTypeForm typeForm) throws LMISException {

        final long programId = programRepository.queryByCode(programCode).getId();

        return dbUtil.withDao(RnRForm.class, dao -> {
            Where<RnRForm, String> where = dao.queryBuilder().orderBy("periodBegin", true).where();
            where.in("program_id", programId).and().between("periodBegin", typeForm.getStartTime(), new Date());

            if (!isWithEmergency) {
                where.and().eq("emergency", false);
            }
            return where.query();
        });
    }

    public List<RnRForm> listNotSynchronizedFromReportStartTime(String programCode) throws LMISException {
        long programId;
        ReportTypeForm reportTypeForm;

        try {
            programId = programRepository.queryByCode(programCode).getId();
            reportTypeForm = reportTypeFormRepository.getReportType(programCode);
        } catch (Exception e) {
            return new ArrayList<>();
        }
        if (reportTypeForm == null) {
            return new ArrayList<>();
        }

        return dbUtil.withDao(RnRForm.class, dao -> {
            Where<RnRForm, String> where = dao.queryBuilder().where().
                    eq("program_id", programId).and().
                    eq("synced", false).and().
                    eq("status", RnRForm.STATUS.AUTHORIZED).and().
                    between("periodBegin", reportTypeForm.getStartTime(), new Date());

            return where.query();
        });
    }

    private List<RnRForm> list(String programCode, final boolean isWithEmergency) throws LMISException {

        final long programId = programRepository.queryByCode(programCode).getId();

        return dbUtil.withDao(RnRForm.class, dao -> {
            Where<RnRForm, String> where = dao.queryBuilder().orderBy("periodBegin", true).where();
            where.in("program_id", programId);

            if (!isWithEmergency) {
                where.and().eq("emergency", false);
            }
            return where.query();
        });
    }

    public void createAndRefresh(RnRForm rnRForm) throws LMISException {
        create(rnRForm);
        genericDao.refresh(rnRForm);
    }

    private void createOrUpdateRnrWrappers(RnRForm form) throws LMISException {
        rnrFormItemRepository.batchCreateOrUpdate(form.getRnrFormItemListWrapper());
        signatureRepository.batchCreateOrUpdate(form.getSignaturesWrapper());
        regimenItemRepository.batchCreateOrUpdate(form.getRegimenItemListWrapper());
        regimenItemThreeLineRepository.batchCreateOrUpdate(form.getRegimenThreeLineListWrapper());
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

    public void deleteRnrFormDirtyData(List<String> productCodeList) {
        Cursor getProgramCodeCursor = null;
        Cursor getParentCodeCursor = null;
        for (String productCode : productCodeList) {
            String getProgramCodeByProductCode = "SELECT programCode FROM product_programs WHERE productCode='" + productCode + "'";
            getProgramCodeCursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(getProgramCodeByProductCode, null);
            while (getProgramCodeCursor.moveToNext()) {
                String getParentCode = "SELECT count(parentCode) AS 'result' FROM programs WHERE programCode='" + getProgramCodeCursor.getString(getProgramCodeCursor.getColumnIndexOrThrow("programCode")) + "'";
                getParentCodeCursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(getParentCode, null);
                getParentCodeCursor.moveToFirst();
                if (getParentCodeCursor.getInt(getParentCodeCursor.getColumnIndexOrThrow("result")) == 0 && !getProgramCodeCursor.getString(getProgramCodeCursor.getColumnIndexOrThrow("programCode")).equals(Constants.RAPID_TEST_OLD_CODE)) {
                    if (getProgramCodeCursor.getString(getProgramCodeCursor.getColumnIndexOrThrow("programCode")).equals(Constants.MMIA_PROGRAM_CODE)) {
                        regimenRepository.deleteRegimeDirtyData(Constants.MMIA_PROGRAM_CODE);
                        deleteRnrData(Constants.MMIA_PROGRAM_CODE);
                    }
                    if (getProgramCodeCursor.getString(getProgramCodeCursor.getColumnIndexOrThrow("programCode")).equals(Constants.VIA_PROGRAM_CODE)) {
                        deleteRnrData(Constants.VIA_PROGRAM_CODE);
                    }
                } else {
                    if (getProgramCodeCursor.getString(getProgramCodeCursor.getColumnIndexOrThrow("programCode")).equals(Constants.PTV_PROGRAM_CODE)) {
                        regimenRepository.deleteRegimeDirtyData(Constants.MMIA_PROGRAM_CODE);
                        deleteRnrData(Constants.PTV_PROGRAM_CODE);
                        deleteRnrData(Constants.MMIA_PROGRAM_CODE);
                    } else if (getProgramCodeCursor.getString(getProgramCodeCursor.getColumnIndexOrThrow("programCode")).equals(Constants.AL_PROGRAM_CODE)) {
                        deleteRnrData(Constants.AL_PROGRAM_CODE);
                        deleteRnrData(Constants.VIA_PROGRAM_CODE);
                    } else if (getProgramCodeCursor.getString(getProgramCodeCursor.getColumnIndexOrThrow("programCode")).equals(Constants.VIA_PROGRAM_CHILD_CODE_TARV)) {
                        regimenRepository.deleteRegimeDirtyData(Constants.MMIA_PROGRAM_CODE);
                        deleteRnrData(Constants.MMIA_PROGRAM_CODE);
                    } else {
                        if (!getProgramCodeCursor.getString(getProgramCodeCursor.getColumnIndexOrThrow("programCode")).equals(Constants.RAPID_TEST_OLD_CODE)) {
                            deleteRnrData(Constants.VIA_PROGRAM_CODE);
                        }
                    }
                }
            }
        }
        if (!getProgramCodeCursor.isClosed()) {
            getProgramCodeCursor.close();
        }
        if (!getParentCodeCursor.isClosed()) {
            getParentCodeCursor.close();
        }
    }

    private void deleteRnrData(String programCode) {
        String deleteRnrFormItem = "DELETE FROM rnr_form_items "
                + "WHERE form_id=(SELECT id FROM rnr_forms WHERE synced=0 AND program_id=(SELECT id FROM programs "
                + "WHERE programCode='" + programCode + "'));";
        String deleteRnrFormSignature = "DELETE FROM rnr_form_signature "
                + "WHERE form_id=(SELECT id FROM rnr_forms WHERE synced=0 AND program_id=(SELECT id FROM "
                + "programs WHERE programCode='" + programCode + "'));";
        String deleteRnrBaseInfoItems = "DELETE FROM rnr_baseInfo_items "
                + "WHERE rnRForm_id=(SELECT id FROM rnr_forms WHERE synced=0 AND program_id IN (SELECT id FROM programs "
                + "WHERE programCode='" + programCode + "'));";
        String deleteRnrForm = "DELETE FROM rnr_forms WHERE synced=0 AND program_id = (SELECT id FROM programs "
                + "WHERE programCode='" + programCode + "');";
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteRnrFormItem);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteRnrFormSignature);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteRnrBaseInfoItems);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteRnrForm);
    }
}
