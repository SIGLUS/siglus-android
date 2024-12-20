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
import static org.openlmis.core.utils.Constants.AL_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.MMIA_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.MMTB_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.RAPID_TEST_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.VIA_PROGRAM_CODE;
import static org.openlmis.core.utils.DateUtil.formatDateTimeToDay;
import static org.openlmis.core.utils.DateUtil.getFirstDayForCurrentMonthByDate;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
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
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.helper.RnrFormHelper;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.network.model.RnrFormStatusEntry;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

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
  SyncErrorsRepository syncErrorsRepository;

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
      return null == dbUtil.withDao(RnRForm.class, dao -> {
        QueryBuilder<RnRForm, String> queryBuilder = dao.queryBuilder();
        Where<RnRForm, String> where = queryBuilder.where();

        Where<RnRForm, String> programCondition = where.eq(PROGRAM_ID, form.getProgram().getId());
        Where<RnRForm, String> periodBeginCondition = where.eq(PERIOD_BEGIN, form.getPeriodBegin());
        Where<RnRForm, String> periodEndCondition = where.eq(PERIOD_END, form.getPeriodEnd());
        Where<RnRForm, String> statusCondition = where.or(
            where.eq(STATUS, Status.AUTHORIZED),
            where.eq(STATUS, Status.IN_APPROVAL),
            where.eq(STATUS, Status.APPROVED),
            where.eq(STATUS, Status.REJECTED)
        );
        where.and(programCondition, periodBeginCondition, periodEndCondition, statusCondition);
        return queryBuilder.queryForFirst();
      });
    } catch (LMISException e) {
      new LMISException(e, "RnrFormRepository.isPeriodUnique").reportToFabric();
    }
    return false;
  }

  public List<RnRForm> list() throws LMISException {
    return genericDao.queryForAll();
  }

  public List<RnRForm> listInclude(RnRForm.Emergency includeEmergency, String programCode) {
    ReportTypeForm reportTypeForm = reportTypeFormRepository.getReportType(programCode);
    return listInclude(includeEmergency, programCode, reportTypeForm);
  }

  public List<RnRForm> listInclude(RnRForm.Emergency includeEmergency, String programCode,
      ReportTypeForm reportTypeForm) {
    try {
      return listForm(programCode, includeEmergency.isEmergency(), reportTypeForm);
    } catch (LMISException e) {
      new LMISException(e, "Fail to listForm in listInclude").reportToFabric();
      Log.e("RnrFormRepo", "listInclude: ", e);
      return Collections.emptyList();
    }
  }

  public List<RnRForm> queryAllUnsyncedForms() throws LMISException {
    List<RnRForm> unsyncedRnr = listNotSynchronizedFromStarTime();
    deleteDeactivatedAndUnsupportedProductItems(unsyncedRnr);
    return unsyncedRnr;
  }

  public RnRForm queryLastDraftOrSubmittedForm() throws LMISException {
    final Program program = programRepository.queryByCode(programCode);
    ReportTypeForm reportTypeForm = reportTypeFormRepository.getReportType(programCode);
    if (program == null) {
      LMISException e = new LMISException("Program cannot be null !");
      e.reportToFabric();
      throw e;
    }
    RnRForm rnRForm = dbUtil
        .withDao(RnRForm.class, dao -> {
          QueryBuilder<RnRForm, String> queryBuilder = dao.queryBuilder();
          Where<RnRForm, String> where = queryBuilder.where();

          Where<RnRForm, String> programCondition = where.eq(PROGRAM_ID, program.getId());
          Where<RnRForm, String> periodCondition =
              where.between(PERIOD_BEGIN, reportTypeForm.getStartTime(), DateUtil.getCurrentDate());
          Where<RnRForm, String> statusCondition = where.or(
              where.eq(STATUS, Status.DRAFT),
              where.eq(STATUS, Status.DRAFT_MISSED),
              where.eq(STATUS, Status.SUBMITTED),
              where.eq(STATUS, Status.SUBMITTED_MISSED)
          );
          where.and(programCondition, periodCondition, statusCondition);

          return queryBuilder.queryForFirst();
        });
    assignCategoryForRnrItems(rnRForm);
    return rnRForm;
  }

  public RnRForm queryRnRForm(final long id) throws LMISException {
    RnRForm rnRForm = dbUtil
        .withDao(RnRForm.class, dao -> dao.queryBuilder().where().eq(ID, id).queryForFirst());
    if (programCode != null) {
      assignCategoryForRnrItems(rnRForm);
    }

    return rnRForm;
  }

  protected void deleteDeactivatedAndUnsupportedProductItems(List<RnRForm> rnRForms)
      throws LMISException {
    for (RnRForm rnRForm : rnRForms) {
      List<String> supportedProductCodes = from(productProgramRepository
              .listActiveProductProgramsByProgramCodes(Arrays.asList(rnRForm.getProgram().getProgramCode())))
          .transform(ProductProgram::getProductCode).toList();
      rnrFormItemRepository.deleteFormItems(rnRForm.getDeactivatedAndUnsupportedProductItems(supportedProductCodes));
    }
  }

  protected List<RnrFormItem> generateRnrFormItems(
      final RnRForm form, List<StockCard> stockCards
  ) throws LMISException {
    List<RnrFormItem> rnrFormItems = new ArrayList<>();
    RnRForm rnRForm = getLastSubmitRnr(form.getProgram().getId());
    if (rnRForm != null) {
      rnrFormItemListWrapper = rnRForm.getRnrFormItemListWrapper();
    }
    HashMap<String, String> stringToCategory = getProductCodeToCategory();
    Set<String> stockCardIds = from(stockCards)
        .transform(stockCard -> String.valueOf(stockCard.getId())).toSet();

    Date periodBegin = form.getPeriodBegin();
    Date periodEnd = form.getPeriodEnd();
    Map<String, List<StockMovementItem>> idToStockMovements = stockMovementRepository
        .queryStockMovement(stockCardIds, periodBegin, periodEnd);

    for (StockCard stockCard : stockCards) {
      // period movement items
      List<StockMovementItem> filteredStockMovementItems = filterMovementItemsBaseOnInventory(
          idToStockMovements.get(String.valueOf(stockCard.getId())),
          periodBegin, periodEnd
      );

      RnrFormItem rnrFormItem = createRnrFormItemByPeriod(stockCard, filteredStockMovementItems, periodBegin);
      rnrFormItem.setForm(form);
      rnrFormItems.add(rnrFormItem);
      rnrFormItem.setCategory(stringToCategory.get(rnrFormItem.getProduct().getCode()));
    }
    return rnrFormItems;
  }

  public Long getPreviousPeriodLastMovementItemSOH(
      StockCard stockCard,
      Date periodBegin
  ) {
    if (stockCard != null) {
      List<StockMovementItem> previousStockMovementItems =
          stockMovementRepository.queryStockMovementsByStockCardIdAndPeriod(
              String.valueOf(stockCard.getId()), null, periodBegin
          );

      if (previousStockMovementItems != null && !previousStockMovementItems.isEmpty()) {
        return previousStockMovementItems.get(previousStockMovementItems.size() - 1).getStockOnHand();
      }
    }

    return null;
  }

  /**
   * The movement items should be in the range of
   * 1. [period begin date's last INVENTORY item, period end date's last INVENTORY item]
   * 2. [period begin date's last INVENTORY item, last one item]
   * 3. [oldest one item, period end date's last INVENTORY item]
   * 4. [oldest one item, last one item]
   *
   * @param stockMovementItems movementItems
   * @param periodBegin        periodBegin
   * @param periodEnd          periodEnd
   * @return StockMovementItems
   */
  public List<StockMovementItem> filterMovementItemsBaseOnInventory(
      List<StockMovementItem> stockMovementItems,
      Date periodBegin,
      Date periodEnd
  ) {
    List<StockMovementItem> filteredStockMovementItems = stockMovementItems;
    // the range between inventory movement data is valid data for this period
    if (stockMovementItems != null && !stockMovementItems.isEmpty()) {
      int size = stockMovementItems.size();
      int inventoryStartIndex = 0;
      int inventoryEndIndex = size;

      boolean isCaughtInventoryStartIndex = false;

      String beginDateString = formatDateTimeToDay(periodBegin);
      String endDateString = formatDateTimeToDay(periodEnd);

      for (int index = 0; index < size; index++) {
        StockMovementItem stockMovementItem = stockMovementItems.get(index);
        if (stockMovementItem != null) {
          Date movementDate = stockMovementItem.getMovementDate();
          if (movementDate == null) {
            continue;
          }

          String movementDateString = formatDateTimeToDay(movementDate);
          if (beginDateString.equals(movementDateString) && !isCaughtInventoryStartIndex) {
            inventoryStartIndex = index;
            if (isInventoryType(stockMovementItem.getMovementType())) {
              isCaughtInventoryStartIndex = true;
            }
          } else if (endDateString.equals(movementDateString) && isInventoryType(stockMovementItem.getMovementType())) {
            inventoryEndIndex = index + 1;
          }
        }
      }

      filteredStockMovementItems =
          stockMovementItems.subList(inventoryStartIndex, inventoryEndIndex);
    }

    return filteredStockMovementItems;
  }

  private boolean isInventoryType(MovementType movementType) {
    return movementType.isInventoryType();
  }

  @NonNull
  protected HashMap<String, String> getProductCodeToCategory() throws LMISException {
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

  protected RnrFormItem createRnrFormItemByPeriod(
      StockCard stockCard,
      List<StockMovementItem> notFullStockItemsByCreatedData,
      Date periodBegin
  ) {
    RnrFormItem rnrFormItem = createRnrFormBaseItemByPeriod(
        stockCard, notFullStockItemsByCreatedData, periodBegin
    );

    if (isStockMovementItemsEmpty(notFullStockItemsByCreatedData)) {
      rnrFormHelper.initRnrFormItemWithoutMovement(rnrFormItem, rnrFormItem.getInitialAmount());
    } else {
      rnrFormHelper.assignTotalValues(rnrFormItem, notFullStockItemsByCreatedData);
    }

    return rnrFormItem;
  }

  protected RnrFormItem createRnrFormBaseItemByPeriod(
      StockCard stockCard,
      List<StockMovementItem> notFullStockItemsByCreatedData,
      Date periodBegin
  ) {
    RnrFormItem rnrFormItem = new RnrFormItem();
    // initialAmount
    Long initialAmount;
    if (isStockMovementItemsEmpty(notFullStockItemsByCreatedData)
        || firstItemsTypeIsNotInventory(notFullStockItemsByCreatedData)
    ) {
      initialAmount = getPreviousPeriodLastMovementItemSOH(stockCard, periodBegin);
    } else {
      initialAmount = notFullStockItemsByCreatedData.get(0).getStockOnHand();
    }
    updateInitialAmount(rnrFormItem, initialAmount);
    // product
    rnrFormItem.setProduct(stockCard.getProduct());

    return rnrFormItem;
  }

  private boolean firstItemsTypeIsNotInventory(List<StockMovementItem> notFullStockItemsByCreatedData) {
    return notFullStockItemsByCreatedData != null && !notFullStockItemsByCreatedData.isEmpty()
        && !isInventoryType(notFullStockItemsByCreatedData.get(0).getMovementType());
  }

  private boolean isStockMovementItemsEmpty(List<StockMovementItem> notFullStockItemsByCreatedData) {
    return notFullStockItemsByCreatedData == null || notFullStockItemsByCreatedData.isEmpty();
  }

  protected ArrayList<RnrFormItem> fillAllProducts(RnRForm form, List<RnrFormItem> basicItems)
      throws LMISException {
    List<String> productCodes;
    String formProgramCode = form.getProgram().getProgramCode();
    productCodes = productProgramRepository.queryActiveProductCodesForReports(formProgramCode);
    List<Product> products = productRepository.queryActiveProductsByCodesWithKits(productCodes, false);
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

      if (rnrFormItem.getInitialAmount() == null) {
        updateInitialAmount(
            rnrFormItem, getPreviousPeriodLastMovementItemSOH(
                stockRepository.queryStockCardByProductId(product.getId()),
                form.getPeriodBegin()
            ));
      }

      result.add(rnrFormItem);
    }
    return result;
  }

  protected void updateInitialAmount(RnrFormItem rnrFormItem, Long initialAmount) {
    if (initialAmount == null) {
      rnrFormItem.setInitialAmount(0L);
    } else {
      rnrFormItem.setInitialAmount(initialAmount);
    }
  }

  protected void updateDefaultValue(RnrFormItem rnrFormItem) {
    // do nothing
  }

  protected List<RegimenItem> generateRegimeItems(RnRForm form) {
    return new ArrayList<>();
  }

  protected List<RegimenItemThreeLines> generateRegimeThreeLineItems(RnRForm form) {
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
                List<StockCard> stockCards;
                if (Constants.VIA_PROGRAM_CODE.equals(rnrForm.getProgram().getProgramCode())) {
                  stockCards = stockRepository.getStockCardsBelongToProgram(Constants.VIA_PROGRAM_CODE);
                } else {
                  stockCards = stockRepository.getStockCardsBeforePeriodEnd(rnrForm);
                }

                rnrFormItemRepository.batchCreateOrUpdate(generateRnrFormItems(rnrForm, stockCards));
                saveInitialRegimenItems(rnrForm);
                saveInitialRegimenThreeLines(rnrForm);
                saveInitialBaseInfo(rnrForm);
                genericDao.refresh(rnrForm);
                return null;
              });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
    assignCategoryForRnrItems(rnrForm);
    return rnrForm;
  }

  private void saveInitialRegimenItems(RnRForm rnrForm) throws LMISException {
    List<RegimenItem> regimenItems = generateRegimeItems(rnrForm);
    if (!CollectionUtils.isEmpty(regimenItems)) {
      regimenItemRepository.batchCreateOrUpdate(regimenItems);
    }
  }

  private void saveInitialRegimenThreeLines(RnRForm rnrForm) throws LMISException {
    List<RegimenItemThreeLines> regimenItemThreeLines = generateRegimeThreeLineItems(rnrForm);
    if (!CollectionUtils.isEmpty(regimenItemThreeLines)) {
      regimenItemThreeLineRepository.batchCreateOrUpdate(regimenItemThreeLines);
    }
  }

  private void saveInitialBaseInfo(RnRForm rnrForm) throws LMISException {
    List<BaseInfoItem> baseInfoItems = generateBaseInfoItems(rnrForm, MMIARepository.ReportType.NEW);
    if (!CollectionUtils.isEmpty(baseInfoItems)) {
      baseInfoItemRepository.batchCreateOrUpdate(baseInfoItems);
    }
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
          .orderBy(PERIOD_BEGIN, true)
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
    signatureRepository.batchCreateOrUpdate(form.getSignaturesWrapper());
    if (form.getProgram() == null) {
      return;
    }
    saveRnRItems(form);
    saveRegimens(form);
    saveRegimenThreeLine(form);
    saveBaseInfo(form);
    saveTestConsumption(form);
  }

  private void saveTestConsumption(RnRForm form) throws LMISException {
    if (RAPID_TEST_PROGRAM_CODE.equals(form.getProgram().getProgramCode())) {
      testConsumptionLineItemRepository.batchCreateOrUpdate(form.getTestConsumptionItemListWrapper(), form.getId());
    }
  }

  private void saveRegimenThreeLine(RnRForm form) throws LMISException {
    if (MMIA_PROGRAM_CODE.equals(form.getProgram().getProgramCode())
        || MMTB_PROGRAM_CODE.equals(form.getProgram().getProgramCode())) {
      baseInfoItemRepository.batchCreateOrUpdate(form.getBaseInfoItemListWrapper());
      regimenItemThreeLineRepository.batchCreateOrUpdate(form.getRegimenThreeLineListWrapper());
    }
  }

  private void saveBaseInfo(RnRForm form) throws LMISException {
    if (VIA_PROGRAM_CODE.equals(form.getProgram().getProgramCode())
        || MMIA_PROGRAM_CODE.equals(form.getProgram().getProgramCode())
        || MMTB_PROGRAM_CODE.equals(form.getProgram().getProgramCode())) {
      baseInfoItemRepository.batchCreateOrUpdate(form.getBaseInfoItemListWrapper());
      regimenItemThreeLineRepository.batchCreateOrUpdate(form.getRegimenThreeLineListWrapper());
    }
  }

  private void saveRegimens(RnRForm form) throws LMISException {
    if (MMIA_PROGRAM_CODE.equals(form.getProgram().getProgramCode())
        || MMTB_PROGRAM_CODE.equals(form.getProgram().getProgramCode())
        || AL_PROGRAM_CODE.equals(form.getProgram().getProgramCode())) {
      regimenItemRepository.batchCreateOrUpdate(form.getRegimenItemListWrapper());
    }
  }

  private void saveRnRItems(RnRForm form) throws LMISException {
    if (!AL_PROGRAM_CODE.equals(form.getProgram().getProgramCode())) {
      rnrFormItemRepository.batchCreateOrUpdate(form.getRnrFormItemListWrapper());
    }
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

    for (String formProgramCode : programCodes) {
      deleteRnrData(formProgramCode);
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

  public RnRForm queryOldestSyncedRnRFormGroupByProgram() throws LMISException {
    try (Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
        .getWritableDatabase().rawQuery(
            "select * from rnr_forms form "
                + "where ( "
                + "select count(*) from rnr_forms form2 "
                + "where form.program_id = form2.program_id "
                + "and form.periodBegin < form2.periodBegin "
                + "and synced = 1 "
                + ") = 0 "
                + "and synced = 1 "
                + "order by periodBegin",
            null
        )) {

      boolean moveToFirst = cursor.moveToFirst();
      if (moveToFirst) {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setPeriodBegin(DateUtil.parseString(
                cursor.getString(cursor.getColumnIndexOrThrow(PERIOD_BEGIN)), DateUtil.DB_DATE_FORMAT
            )
        );
        return rnRForm;
      } else {
        return null;
      }
    } catch (IllegalArgumentException e) {
      throw new LMISException(e);
    }
  }

  public void saveAndDeleteDuplicatedPeriodRequisitions(List<RnRForm> forms) throws LMISException {
    try {
      TransactionManager.callInTransaction(
          LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
            for (RnRForm form : forms) {
              RnRForm existingForm = queryNonEmergencyFormByPeriodAndProgramCode(
                  form.getProgram(), form.getPeriodBegin()
              );
              if (existingForm != null) {
                removeRnrForm(existingForm);
                syncErrorsRepository.deleteBySyncTypeAndObjectId(SyncType.RNR_FORM, existingForm.getId());
              }
              createOrUpdateWithItems(form);
            }
            return null;
          });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  @Nullable
  private RnRForm queryNonEmergencyFormByPeriodAndProgramCode(
      Program program, Date periodBegin
  ) throws LMISException {
    Program dbProgram = programRepository.queryByCode(program.getProgramCode());
    if (dbProgram == null) {
      return null;
    }

    Date firstDayOfCurrentPeriod = getFirstDayForCurrentMonthByDate(periodBegin);

    return dbUtil.withDao(
        RnRForm.class,
        dao -> dao.queryBuilder()
            .orderBy(PERIOD_BEGIN, true)
            .where()
            .eq(PROGRAM_ID, dbProgram.getId())
            .and().ge(PERIOD_BEGIN, firstDayOfCurrentPeriod)
            .and().eq(EMERGENCY, false)
            .queryForFirst()
    );
  }

  public List<RnRForm> listAllInApprovalForms() throws LMISException {
    return dbUtil.withDao(
        RnRForm.class,
        dao -> dao.queryBuilder()
            .where()
            .eq(STATUS, Status.IN_APPROVAL)
            .and().eq(EMERGENCY, false)
            .query()
    );
  }

  public List<RnRForm> queryAllRejectedRequisitions() {
    try {
      List<RnRForm> rejectedRnrList = dbUtil.withDao(RnRForm.class,
          dao -> dao.queryBuilder()
              .where()
              .eq(STATUS, Status.REJECTED)
              .query()
          );
      return rejectedRnrList;
    } catch (LMISException e) {
      new LMISException(e, "RnrFormRepository.hasRejectedRequisition").reportToFabric();
    }
    return new ArrayList<>();
  }

  public void updateFormsStatusAndDeleteRejectedFormsSignatures(
      List<RnrFormStatusEntry> requisitionsStatusResponse
  ) throws LMISException {
    try {
      TransactionManager.callInTransaction(
          LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
            for (RnrFormStatusEntry rnrFormStatusEntry : requisitionsStatusResponse) {
              RnRForm form = queryRnRForm(rnrFormStatusEntry.getId());
              if (form != null && form.getStatus() != rnrFormStatusEntry.getStatus()) {
                // update status
                form.setStatus(rnrFormStatusEntry.getStatus());
                genericDao.update(form);
                // delete signatures if status is rejected
                if (rnrFormStatusEntry.getStatus() == Status.REJECTED) {
                  List<RnRFormSignature> signatures = form.getSignaturesWrapper();
                  if (signatures != null && !signatures.isEmpty()) {
                    signatureRepository.batchDelete(signatures);
                  }
                }
              }
            }
            return null;
          });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }
}
