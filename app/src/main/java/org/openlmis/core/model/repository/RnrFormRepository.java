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

import static org.openlmis.core.constant.FieldConstants.EMERGENCY;
import static org.openlmis.core.constant.FieldConstants.ID;
import static org.openlmis.core.constant.FieldConstants.PERIOD_BEGIN;
import static org.openlmis.core.constant.FieldConstants.PERIOD_END;
import static org.openlmis.core.constant.FieldConstants.PROGRAM_CODE;
import static org.openlmis.core.constant.FieldConstants.PROGRAM_ID;
import static org.openlmis.core.constant.FieldConstants.STATUS;
import static org.openlmis.core.constant.FieldConstants.SUBMITTED_TIME;
import static org.openlmis.core.constant.FieldConstants.SYNCED;
import static org.openlmis.core.utils.Constants.MMIA_PROGRAM_CODE;

import android.content.Context;
import android.database.Cursor;
import androidx.annotation.Nullable;
import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.Where;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
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
import org.openlmis.core.model.RnRForm.Status;
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
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@SuppressWarnings({"squid:S3776", "squid:S1172"})
public class RnrFormRepository {

  public static final String WHERE_PERIOD_END = "WHERE form_id IN (SELECT id FROM rnr_forms WHERE periodEnd < '";
  public static final String WHERE_PROGRAM_CODE = "WHERE programCode='";
  public static final String END_STRING = "' AND synced = 1 );";

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
  TestConsumptionLineItemRepository testConsumptionLineItemRepository;

  @Inject
  ProductProgramRepository productProgramRepository;

  @Inject
  ProgramRepository programRepository;

  @Inject
  ProductRepository productRepository;

  @Inject
  ReportTypeFormRepository reportTypeFormRepository;

  @Inject
  RnrFormHelper rnrFormHelper;

  GenericDao<RnRForm> genericDao;
  GenericDao<RnrFormItem> rnrFormItemGenericDao;

  private final Context context;
  protected String programCode;

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
    RnRForm rnrForm = initRnRForm(periodEndDate, RnRForm.Emergency.NO);
    return createInitRnrForm(rnrForm);
  }

  public RnRForm initEmergencyRnrForm(Date periodEndDate, List<StockCard> stockCards) throws LMISException {
    RnRForm rnRForm = initRnRForm(periodEndDate, RnRForm.Emergency.YES);
    rnRForm.setRnrFormItemListWrapper(generateRnrFormItems(rnRForm, stockCards));
    return rnRForm;
  }

  public void create(RnRForm rnRForm) throws LMISException {
    genericDao.create(rnRForm);
  }

  public void createRnRsWithItems(@Nullable final List<RnRForm> forms) throws LMISException {
    if (forms == null) {
      return;
    }
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
      TransactionManager
          .callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
              () -> {
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
      return null == dbUtil.withDao(RnRForm.class, dao ->
          dao.queryBuilder()
              .where().eq(PROGRAM_ID, form.getProgram().getId())
              .and().eq(STATUS, Status.AUTHORIZED)
              .and().eq(PERIOD_BEGIN, form.getPeriodBegin())
              .and().eq(PERIOD_END, form.getPeriodEnd())
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

  public List<RnRForm> listInclude(RnRForm.Emergency includeEmergency, String programCode,
      ReportTypeForm reportTypeForm) throws LMISException {
    return listForm(programCode, includeEmergency.isEmergency(), reportTypeForm);
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
    RnRForm rnRForm = dbUtil
        .withDao(RnRForm.class, dao -> dao.queryBuilder().where().eq(PROGRAM_ID, program.getId())
            .and().between(PERIOD_BEGIN, reportTypeForm.getStartTime(), DateUtil.getCurrentDate())
            .and().ne(STATUS, Status.AUTHORIZED)
            .queryForFirst());
    assignCategoryForRnrItems(rnRForm);
    return rnRForm;
  }

  public RnRForm queryRnRForm(final long id) throws LMISException {
    RnRForm rnRForm = dbUtil
        .withDao(RnRForm.class, dao -> dao.queryBuilder().where().eq(ID, id).queryForFirst());
    assignCategoryForRnrItems(rnRForm);

    return rnRForm;
  }

  protected void deleteDeactivatedAndUnsupportedProductItems(List<RnRForm> rnRForms)
      throws LMISException {
    for (RnRForm rnRForm : rnRForms) {
      List<String> supportedProductCodes = FluentIterable
          .from(productProgramRepository
              .listActiveProductProgramsByProgramCodes(Arrays.asList(rnRForm.getProgram().getProgramCode())))
          .transform(ProductProgram::getProductCode).toList();
      rnrFormItemRepository.deleteFormItems(rnRForm.getDeactivatedAndUnsupportedProductItems(supportedProductCodes));
    }
  }

  public List<RnrFormItem> generateRnrFormItems(final RnRForm form, List<StockCard> stockCards) throws LMISException {
    List<RnrFormItem> rnrFormItems = new ArrayList<>();
    RnRForm rnRForm = getLastSubmitRnr(form.getProgram().getId());
    if (rnRForm != null) {
      rnrFormItemListWrapper = rnRForm.getRnrFormItemListWrapper();
    }
    HashMap<String, String> stringToCategory = getProductCodeToCategory();
    for (StockCard stockCard : stockCards) {
      RnrFormItem rnrFormItem = createRnrFormItemByPeriod(stockCard, form.getPeriodBegin(),
          form.getPeriodEnd());
      rnrFormItem.setForm(form);
      rnrFormItems.add(rnrFormItem);
      rnrFormItem.setCategory(stringToCategory.get(rnrFormItem.getProduct().getCode()));
    }
    return rnrFormItems;
  }

  @NotNull
  private HashMap<String, String> getProductCodeToCategory() throws LMISException {
    List<ProductProgram> productPrograms = productProgramRepository
        .listActiveProductProgramsByProgramCodes(Arrays.asList(programCode));
    HashMap<String, String> codeToCategory = new HashMap<>();
    for (ProductProgram productProgram : productPrograms) {
      codeToCategory.put(productProgram.getProductCode(), productProgram.getCategory());
    }
    return codeToCategory;
  }

  public void removeRnrForm(RnRForm form) throws LMISException {
    if (form != null) {
      rnrFormItemRepository.deleteFormItems(form.getRnrFormItemListWrapper());
      regimenItemRepository.deleteRegimenItems(form.getRegimenItemListWrapper());
      regimenItemThreeLineRepository.deleteRegimeThreeLineItems(form.getRegimenThreeLineListWrapper());
      baseInfoItemRepository.batchDelete(form.getBaseInfoItemListWrapper());
      testConsumptionLineItemRepository.batchDelete(form.getTestConsumptionItemListWrapper());
      signatureRepository.batchDelete(form.getSignaturesWrapper());
      genericDao.delete(form);
    }
  }

  public boolean hasRequisitionData() {
    try {
      if (CollectionUtils.isNotEmpty(list())) {
        return true;
      }
    } catch (LMISException e) {
      new LMISException(e, "RnrFormRepository.hasRequisitionData").reportToFabric();
    }
    return false;
  }

  public boolean hasOldDate() {
    try {
      List<RnRForm> list = list();
      Date dueDateShouldDataLivedInDB = DateUtil.dateMinusMonth(DateUtil.getCurrentDate(),
          SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData());
      for (RnRForm rnrForm : list) {
        if (rnrForm.getPeriodEnd().before(dueDateShouldDataLivedInDB)) {
          return true;
        }
      }
    } catch (LMISException e) {
      new LMISException(e, "RnrFormRepository.hasOldDate").reportToFabric();
    }
    return false;
  }

  protected List<RnRForm> listUnsynced() throws LMISException {
    return dbUtil.withDao(RnRForm.class, dao -> dao.queryBuilder().where().eq(SYNCED, false).and()
        .eq(STATUS, Status.AUTHORIZED).query());
  }

  protected RnRForm getLastSubmitRnr(long programId) throws LMISException {
    return dbUtil.withDao(RnRForm.class, dao -> dao.queryBuilder()
        .orderBy(PERIOD_BEGIN, false)
        .where().eq(PROGRAM_ID, programId)
        .and()
        .isNotNull(SUBMITTED_TIME)
        .queryForFirst());
  }

  protected List<RnRForm> listNotSynchronizedFromStarTime() throws LMISException {
    List<RnRForm> rnRForms = new ArrayList<>();
    for (Constants.Program program : Constants.PROGRAMS) {
      rnRForms.addAll(listNotSynchronizedFromReportStartTime(program.getCode()));
    }
    return rnRForms;
  }

  protected RnrFormItem createRnrFormItemByPeriod(StockCard stockCard, Date startDate, Date endDate)
      throws LMISException {
    RnrFormItem rnrFormItem = new RnrFormItem();
    final List<StockMovementItem> notFullStockItemsByCreatedData = stockMovementRepository
        .queryNotFullFillStockItemsByCreatedData(stockCard.getId(), startDate, endDate);
    if (notFullStockItemsByCreatedData.isEmpty()) {
      rnrFormHelper.initRnrFormItemWithoutMovement(rnrFormItem, lastRnrInventory(stockCard));
    } else {
      rnrFormItem.setInitialAmount(notFullStockItemsByCreatedData.get(0).calculatePreviousSOH());
      rnrFormHelper.assignTotalValues(rnrFormItem, notFullStockItemsByCreatedData);
    }
    rnrFormItem.setProduct(stockCard.getProduct());
    return rnrFormItem;
  }

  protected ArrayList<RnrFormItem> fillAllProducts(RnRForm form, List<RnrFormItem> basicItems)
      throws LMISException {
    List<Long> productIds;
    String programCode = form.getProgram().getProgramCode();
    if (programCode.equals(MMIA_PROGRAM_CODE)) {
      productIds = productProgramRepository.queryActiveProductIdsForMMIA(programCode);
    } else {
      productIds = productProgramRepository.queryActiveProductIdsByProgramWithKits(
          programCode, false);
    }
    List<Product> products = productRepository.queryProductsByProductIds(productIds);
    ArrayList<RnrFormItem> result = new ArrayList<>();

    for (Product product : products) {
      RnrFormItem rnrFormItem = new RnrFormItem();
      RnrFormItem stockFormItem = rnrFormHelper.getStockCardRnr(product, basicItems);
      if (stockFormItem == null) {
        rnrFormItem.setForm(form);
        rnrFormItem.setProduct(product);
        updateDefaultValue(rnrFormItem);
      } else {
        rnrFormItem = stockFormItem;
      }

      updateInitialAmount(rnrFormItem, lastRnrInventory(product));
      result.add(rnrFormItem);

    }
    return result;
  }

  protected void updateInitialAmount(RnrFormItem rnrFormItem, Long lastInventory) {
    rnrFormItem.setInitialAmount(lastInventory != null ? lastInventory : 0);
  }

  protected void updateDefaultValue(RnrFormItem rnrFormItem) {
  }

  protected List<RegimenItem> generateRegimeItems(RnRForm form) throws LMISException {
    return new ArrayList<>();
  }

  protected List<RegimenItemThreeLines> generateRegimeThreeLineItems(RnRForm form)
      throws LMISException {
    return new ArrayList<>();
  }

  protected List<BaseInfoItem> generateBaseInfoItems(RnRForm form, MMIARepository.ReportType type) {
    return new ArrayList<>();
  }

  private RnRForm initRnRForm(Date periodEndDate, RnRForm.Emergency emergency)
      throws LMISException {
    final Program program = programRepository.queryByCode(programCode);
    if (program == null) {
      LMISException e = new LMISException("Program cannot be null !");
      e.reportToFabric();
      throw e;
    }

    Period period = requisitionPeriodService.generateNextPeriod(programCode, periodEndDate);
    return RnRForm.init(program, period, emergency.isEmergency());
  }

  private RnRForm createInitRnrForm(final RnRForm rnrForm) throws LMISException {
    try {
      TransactionManager
          .callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
              () -> {
                create(rnrForm);
                List<StockCard> stockCardWithMovement;
                if (Constants.VIA_PROGRAM_CODE.equals(rnrForm.getProgram().getProgramCode())) {
                  stockCardWithMovement = stockRepository.getStockCardsBelongToProgram(Constants.VIA_PROGRAM_CODE);
                } else {
                  stockCardWithMovement = stockRepository.getStockCardsBeforePeriodEnd(rnrForm);
                }
                rnrFormItemRepository.batchCreateOrUpdate(generateRnrFormItems(rnrForm, stockCardWithMovement));
                regimenItemRepository.batchCreateOrUpdate(generateRegimeItems(rnrForm));
                regimenItemThreeLineRepository.batchCreateOrUpdate(generateRegimeThreeLineItems(rnrForm));
                baseInfoItemRepository
                    .batchCreateOrUpdate(generateBaseInfoItems(rnrForm, MMIARepository.ReportType.NEW));
                genericDao.refresh(rnrForm);
                return null;
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

    if (rnrForm.getProgram() != null
        && "MMIA".equals(rnrForm.getProgram().getProgramCode())
        && rnrForm.isOldMMIALayout()) {
      for (RnrFormItem item : rnrForm.getRnrFormItemListWrapper()) {
        if (item.getProduct() != null) {
          item.setCategory(getCategory(item.getProduct().getCode()));
        }
      }
    } else {
      for (RnrFormItem item : rnrForm.getRnrFormItemListWrapper()) {
        ProductProgram productProgram = null;
        if (item.getProduct() != null) {
          productProgram = productProgramRepository
              .queryByCode(item.getProduct().getCode(), programCode);
        }
        if (productProgram != null) {
          item.setCategory(productProgram.getCategory());
        }
      }
    }
  }

  private String getCategory(String productCode) {
    Map<String, String> productCategoryMap = new HashMap<>();
    productCategoryMap.put("08S30Y", Product.MEDICINE_TYPE_ADULT);
    productCategoryMap.put("08S18Y", Product.MEDICINE_TYPE_ADULT);
    productCategoryMap.put("08S18Z", Product.MEDICINE_TYPE_ADULT);
    productCategoryMap.put("08S39Z", Product.MEDICINE_TYPE_ADULT);
    productCategoryMap.put("08S40", Product.MEDICINE_TYPE_ADULT);
    productCategoryMap.put("08S01", Product.MEDICINE_TYPE_ADULT);
    productCategoryMap.put("08S42", Product.MEDICINE_TYPE_ADULT);
    productCategoryMap.put("08S13", Product.MEDICINE_TYPE_ADULT);
    productCategoryMap.put("08S36", Product.MEDICINE_TYPE_ADULT);
    productCategoryMap.put("08S01ZY", Product.MEDICINE_TYPE_ADULT);
    productCategoryMap.put("08S21", Product.MEDICINE_TYPE_ADULT);
    productCategoryMap.put("08S22", Product.MEDICINE_TYPE_ADULT);
    productCategoryMap.put("08S18WI", Product.MEDICINE_TYPE_ADULT);
    productCategoryMap.put("08S32", Product.MEDICINE_TYPE_ADULT);
    productCategoryMap.put("08S15", Product.MEDICINE_TYPE_ADULT);

    productCategoryMap.put("08S01B", Product.MEDICINE_TYPE_CHILDREN);
    productCategoryMap.put("08S01ZZ", Product.MEDICINE_TYPE_CHILDREN);
    productCategoryMap.put("08S19", Product.MEDICINE_TYPE_CHILDREN);
    productCategoryMap.put("08S20", Product.MEDICINE_TYPE_CHILDREN);
    productCategoryMap.put("08S32Z", Product.MEDICINE_TYPE_CHILDREN);
    productCategoryMap.put("08S34B", Product.MEDICINE_TYPE_CHILDREN);
    productCategoryMap.put("08S39B", Product.MEDICINE_TYPE_CHILDREN);
    productCategoryMap.put("08S39Y", Product.MEDICINE_TYPE_CHILDREN);
    productCategoryMap.put("08S40Z", Product.MEDICINE_TYPE_CHILDREN);
    productCategoryMap.put("08S42B", Product.MEDICINE_TYPE_CHILDREN);
    productCategoryMap.put("08S38Y", Product.MEDICINE_TYPE_CHILDREN);
    productCategoryMap.put("08S30YX", Product.MEDICINE_TYPE_CHILDREN);

    productCategoryMap.put("08S17", Product.MEDICINE_TYPE_SOLUTION);
    productCategoryMap.put("08S23", Product.MEDICINE_TYPE_SOLUTION);

    return productCategoryMap.get(productCode);
  }

  protected long lastRnrInventory(StockCard stockCard) {
    Long inventory = lastRnrInventory(stockCard.getProduct());
    return inventory != null ? inventory : 0;
  }

  protected Long lastRnrInventory(Product product) {
    if (rnrFormItemListWrapper != null) {
      for (RnrFormItem item : rnrFormItemListWrapper) {
        if (item.getProduct() != null && (item.getProduct().getId() == product.getId())) {
          return item.getInventory();
        }
      }
    }
    return null;
  }

  private List<RnRForm> listForm(String programCode, final boolean isWithEmergency,
      ReportTypeForm typeForm) throws LMISException {

    final long programId = programRepository.queryByCode(programCode).getId();

    return dbUtil.withDao(RnRForm.class, dao -> {
      Where<RnRForm, String> where = dao.queryBuilder().orderBy(PERIOD_BEGIN, true).where();
      where.in(PROGRAM_ID, programId).and().between(PERIOD_BEGIN, typeForm.getStartTime(), DateUtil.getCurrentDate());
      if (!isWithEmergency) {
        where.and().eq(EMERGENCY, false);
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
      Where<RnRForm, String> where = dao.queryBuilder()
          .where().eq(PROGRAM_ID, programId)
          .and().eq(SYNCED, false)
          .and().eq(STATUS, Status.AUTHORIZED)
          .and().between(PERIOD_BEGIN, reportTypeForm.getStartTime(), DateUtil.getCurrentDate());
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
    testConsumptionLineItemRepository.batchCreateOrUpdate(form.getTestConsumptionItemListWrapper(), form.getId());
  }

  public void deleteOldData() {
    String dueDateShouldDataLivedInDB = DateUtil.formatDate(DateUtil
            .dateMinusMonth(DateUtil.getCurrentDate(),
                SharedPreferenceMgr.getInstance().getMonthOffsetThatDefinedOldData()),
        DateUtil.DB_DATE_FORMAT);

    String rawSqlDeleteRnrFormItems = "DELETE FROM rnr_form_items "
        + WHERE_PERIOD_END
        + dueDateShouldDataLivedInDB + END_STRING;
    String rawSqlDeleteSignature = "DELETE FROM rnr_form_signature "
        + WHERE_PERIOD_END + dueDateShouldDataLivedInDB + END_STRING;
    String rawSqlDeleteRegimeItems = "DELETE FROM regime_items "
        + WHERE_PERIOD_END + dueDateShouldDataLivedInDB + END_STRING;
    String rawSqlDeleteBaseInfoItems = "DELETE FROM rnr_baseInfo_items "
        + "WHERE rnRForm_id IN (SELECT id FROM rnr_forms WHERE periodEnd < '" + dueDateShouldDataLivedInDB + END_STRING;
    String rawSqlDeleteRnrForms = "DELETE FROM rnr_forms " + "WHERE periodEnd < '" + dueDateShouldDataLivedInDB
        + "' AND synced = 1;";

    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(rawSqlDeleteRnrFormItems);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(rawSqlDeleteBaseInfoItems);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(rawSqlDeleteRegimeItems);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(rawSqlDeleteSignature);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(rawSqlDeleteRnrForms);
  }

  public void deleteRnrFormDirtyData(List<String> productCodeList) {
    String productIds = StringUtils
        .join(productCodeList != null ? productCodeList : new HashSet<>(), ',');
    String getProgramCodeByProductCode =
        "SELECT DISTINCT programCode FROM product_programs WHERE productCode in ('" + productIds
            + "')";
    Cursor getProgramCodeCursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
        .getWritableDatabase().rawQuery(getProgramCodeByProductCode, null);
    List<String> programCodes = new ArrayList<>();
    if (getProgramCodeCursor.moveToFirst()) {
      do {
        programCodes.add(getProgramCodeCursor.getString(
            getProgramCodeCursor.getColumnIndexOrThrow(PROGRAM_CODE)));
      } while (getProgramCodeCursor.moveToNext());
    }
    if (!getProgramCodeCursor.isClosed()) {
      getProgramCodeCursor.close();
    }

    for (String programCode : programCodes) {
      deleteRnrData(programCode);
    }

  }

  private void deleteRnrData(String programCode) {
    String deleteRnrFormItem = "DELETE FROM rnr_form_items "
        + "WHERE form_id=(SELECT id FROM rnr_forms WHERE synced=0 AND program_id=(SELECT id FROM "
        + "programs "
        + WHERE_PROGRAM_CODE + programCode + "'));";
    String deleteRnrFormSignature = "DELETE FROM rnr_form_signature "
        + "WHERE form_id=(SELECT id FROM rnr_forms WHERE synced=0 AND program_id=(SELECT id FROM "
        + "programs WHERE programCode='" + programCode + "'));";
    String deleteRnrBaseInfoItems = "DELETE FROM rnr_baseInfo_items "
        + "WHERE rnRForm_id=(SELECT id FROM rnr_forms WHERE synced=0 AND program_id IN (SELECT id "
        + "FROM programs "
        + WHERE_PROGRAM_CODE + programCode + "'));";
    String deleteRnrForm =
        "DELETE FROM rnr_forms WHERE synced=0 AND program_id = (SELECT id FROM programs "
            + WHERE_PROGRAM_CODE + programCode + "');";
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(deleteRnrFormItem);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(deleteRnrFormSignature);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(deleteRnrBaseInfoItems);
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .execSQL(deleteRnrForm);
  }
}
