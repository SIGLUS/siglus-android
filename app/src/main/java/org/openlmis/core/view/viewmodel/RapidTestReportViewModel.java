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

package org.openlmis.core.view.viewmodel;

import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.ColumnCode;
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode;
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.CONSUMPTION;
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.POSITIVE;
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.POSITIVE_HIV;
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.POSITIVE_SYPHILIS;
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.UNJUSTIFIED;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.MMITGridErrorType;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.Signature;
import org.openlmis.core.model.TestConsumptionItem;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@SuppressWarnings("squid:S1874")
@Data
public class RapidTestReportViewModel {

  private Period period;
  private String observation;
  private Status status;
  private Date syncedTime;
  private MovementReasonManager movementReasonManager;
  private RapidTestFormItemViewModel itemTotal;
  private RapidTestFormItemViewModel itemRealTotal;
  private RapidTestFormItemViewModel itemAPEs;
  private List<RapidTestFormItemViewModel> itemViewModelList = new ArrayList<>();
  private Map<String, RapidTestFormItemViewModel> itemViewModelMap = new HashMap<>();
  private List<RnrFormItem> productItems = new ArrayList<>();
  private RnRForm rapidTestForm = new RnRForm();
  private String errorMessage;

  public static final long DEFAULT_FORM_ID = 0;
  private static final String PUB_PHARMACY = "PUB_PHARMACY";
  private static final String APE = "APE";

  public RapidTestReportViewModel(Period period) {
    this.period = period;
    status = RapidTestReportViewModel.Status.MISSING;
    observation = "";
    setupCategories();
    setItemViewModelMap();
  }

  public RapidTestReportViewModel(Period period, Status statusInput) {
    this.period = period;
    status = statusInput;
    observation = "";
    setupCategories();
    setItemViewModelMap();
  }

  private void setupCategories() {
    movementReasonManager = MovementReasonManager.getInstance();
    List<MovementReasonManager.MovementReason> issueReasons = FluentIterable.from(
            movementReasonManager.buildReasonListForMovementType(MovementReasonManager.MovementType.ISSUE))
        .filter(movementReason -> !PUB_PHARMACY.equals(movementReason.getCode())
            && !APE.equals(movementReason.getCode())).toList();

    for (MovementReasonManager.MovementReason movementReason : issueReasons) {
      RapidTestFormItemViewModel item = new RapidTestFormItemViewModel(movementReason);
      itemViewModelList.add(item);
    }

    MovementReasonManager.MovementReason totalCategory = new MovementReasonManager.MovementReason(
        MovementReasonManager.MovementType.ISSUE, "TOTAL", LMISApp.getInstance().getString(R.string.total));
    MovementReasonManager.MovementReason realTotalCategory = new MovementReasonManager.MovementReason(
        MovementReasonManager.MovementType.ISSUE, "realTotalCategory", LMISApp.getInstance().getString(R.string.total));
    MovementReasonManager.MovementReason totalAPES = new MovementReasonManager.MovementReason(
        MovementReasonManager.MovementType.ISSUE, "APES", LMISApp.getInstance().getString(R.string.ape));

    itemTotal = new RapidTestFormItemViewModel(totalCategory);
    itemRealTotal = new RapidTestFormItemViewModel(realTotalCategory);
    itemAPEs = new RapidTestFormItemViewModel(totalAPES);
    itemAPEs.setAPEItem();

    itemViewModelList.add(itemTotal);
    itemViewModelList.add(itemAPEs);
  }

  private void setItemViewModelMap() {
    for (RapidTestFormItemViewModel viewModel : itemViewModelList) {
      itemViewModelMap.put(viewModel.getIssueReason().getCode(), viewModel);
    }
  }

  public RapidTestReportViewModel(RnRForm rnRForm) {
    setRapidTestForm(rnRForm);
    DateTime beginDateTime = new DateTime(rnRForm.getPeriodBegin());
    DateTime endDateTime = new DateTime(rnRForm.getPeriodEnd());
    period = new Period(beginDateTime, endDateTime);
    observation = rnRForm.getComments();

    if (rnRForm.getRnrFormItemListWrapper() != null) {
      productItems.addAll(rnRForm.getRnrFormItemListWrapper());
    }

    setupCategories();
    setItemViewModelMap();
    setFormItemViewModels(rnRForm.getTestConsumptionItemListWrapper());
  }

  private void setFormItemViewModels(List<TestConsumptionItem> testConsumptionLineItemList) {
    for (TestConsumptionItem item : testConsumptionLineItemList) {
      RapidTestFormItemViewModel itemViewModel = itemViewModelMap.get(item.getService());
      if (itemViewModel != null) {
        itemViewModel.setColumnValue(item.getUsageColumnsMap(), item.getValue());
      }
    }
    addCompatibleWithNotSubmitUnjustified();
    addCompatibleWithNotSubmitAPE();
    for (ColumnCode columnCode : ColumnCode.values()) {
      updateTotal(columnCode, CONSUMPTION);
      updateTotal(columnCode, POSITIVE);
      updateTotal(columnCode, POSITIVE_HIV);
      updateTotal(columnCode, POSITIVE_SYPHILIS);
      updateTotal(columnCode, UNJUSTIFIED);
    }
    updateAPEWaring();
  }

  private void addCompatibleWithNotSubmitUnjustified() {
    for (RapidTestFormItemViewModel formItemViewModel : itemViewModelList) {
      formItemViewModel.updateUnjustifiedColumn();
    }
  }

  private void addCompatibleWithNotSubmitAPE() {
    for (ColumnCode columnName : ColumnCode.values()) {
      if (Boolean.TRUE.equals(isNeedAPE(columnName))) {
        RapidTestFormGridViewModel viewModel = itemAPEs.rapidTestFormGridViewModelMap
            .get(columnName);
        if (viewModel != null) {
          itemAPEs.updateNoValueGridRowToZero(viewModel);
        }
      }
    }
  }

  private Boolean isNeedAPE(ColumnCode columnName) {
    for (RapidTestFormItemViewModel viewModel : itemViewModelList) {
      RapidTestFormGridViewModel rapidTestFormGridViewModel =
          viewModel.rapidTestFormGridViewModelMap.get(columnName);

      if (rapidTestFormGridViewModel != null && !rapidTestFormGridViewModel.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  public void setRapidTestForm(RnRForm rapidTestForm) {
    this.rapidTestForm = rapidTestForm;
    switch (rapidTestForm.getStatus()) {
      case DRAFT:
      case SUBMITTED:
        this.status = RapidTestReportViewModel.Status.INCOMPLETE;
        break;
      case AUTHORIZED:
        this.status = RapidTestReportViewModel.Status.COMPLETED;
        break;
      default:
        this.status = RapidTestReportViewModel.Status.MISSING;
    }
    if (rapidTestForm.isSynced()) {
      this.status = RapidTestReportViewModel.Status.SYNCED;
    }
  }

  public Date getSyncedTime() {
    return rapidTestForm.getSubmittedTime();
  }

  public void convertFormViewModelToDataModel(Program program) {
    rapidTestForm.setProgram(program);
    rapidTestForm.setPeriodBegin(period.getBegin().toDate());
    rapidTestForm.setPeriodEnd(period.getEnd().toDate());
    rapidTestForm.setComments(observation);
    rapidTestForm.getTestConsumptionItemListWrapper().clear();
    rapidTestForm.setRnrFormItemListWrapper(productItems);
    convertFormItemViewModelToDataModel();
  }

  private void convertFormItemViewModelToDataModel() {
    for (RapidTestFormItemViewModel itemViewModel : itemViewModelList) {
      rapidTestForm.getTestConsumptionItemListWrapper().addAll(itemViewModel.convertToDataModel());
    }
    for (TestConsumptionItem item : rapidTestForm.getTestConsumptionItemListWrapper()) {
      item.setForm(rapidTestForm);
    }
  }

  public boolean isSynced() {
    return status == RapidTestReportViewModel.Status.SYNCED;
  }

  public boolean isEditable() {
    return status.isEditable() && (isDraft() || isReadyForCompleted());
  }


  public boolean isDraft() {
    return rapidTestForm.getStatus() == null || rapidTestForm.isDraft();
  }

  private boolean isReadyForCompleted() {
    return rapidTestForm.getStatus() == null
        || rapidTestForm.getStatus() == RnRForm.Status.SUBMITTED;
  }

  public boolean validate() {
    clearError();
    for (RapidTestFormItemViewModel itemViewModel : itemViewModelList) {
      for (RapidTestFormGridViewModel gridViewModel : itemViewModel.getRapidTestFormGridViewModelList()) {
        MMITGridErrorType errorType = gridViewModel.validateThreeGrid();
        if (errorType.isError()) {
          errorMessage = errorType.getErrorString();
          return false;
        }
      }
    }
    return true;
  }

  public boolean validateOnlyAPES() {
    return itemRealTotal.isEmpty() && !itemAPEs.isEmpty();
  }

  public void addSignature(String signature) {
    if (rapidTestForm.getSignaturesWrapper().isEmpty()) {
      rapidTestForm.getSignaturesWrapper()
          .add(new RnRFormSignature(rapidTestForm, signature, Signature.TYPE.SUBMITTER));
      rapidTestForm.setStatus(RnRForm.Status.SUBMITTED);
    } else {
      rapidTestForm.getSignaturesWrapper()
          .add(new RnRFormSignature(rapidTestForm, signature, Signature.TYPE.APPROVER));
      rapidTestForm.setStatus(RnRForm.Status.AUTHORIZED);
      rapidTestForm.setSubmittedTime(DateUtil.getCurrentDate());
      status = RapidTestReportViewModel.Status.COMPLETED;
    }
  }

  public boolean isAuthorized() {
    return rapidTestForm.getStatus() == RnRForm.Status.AUTHORIZED;
  }

  public boolean isFormEmpty() {
    for (RapidTestFormItemViewModel itemViewModel : itemViewModelList) {
      if (!itemViewModel.isEmpty()
          && (itemViewModel.issueReason != null
          && itemViewModel.issueReason.getCode() != null
          && !itemViewModel.issueReason.getCode().endsWith("TOTAL"))) {
        return false;
      }
    }
    return true;
  }

  public boolean isSubmitted() {
    return rapidTestForm.getStatus() == RnRForm.Status.SUBMITTED;
  }

  public void updateTotal(ColumnCode columnCode, RapidTestGridColumnCode gridColumnCode) {
    itemTotal.clearValue(columnCode, gridColumnCode);
    itemRealTotal.clearValue(columnCode, gridColumnCode);
    Total total = new Total();
    total.longTotal = 0;
    total.stringTotal = "";
    for (RapidTestFormItemViewModel itemViewModel : itemViewModelList) {
      if (itemViewModel == itemAPEs) {
        continue;
      }
      RapidTestFormGridViewModel gridViewModel = itemViewModel.getRapidTestFormGridViewModelMap().get(columnCode);
      if (gridViewModel != null) {
        calculateTotalLogic(total, gridViewModel, gridColumnCode);
      }
    }
    setTotalRowValue(itemTotal, columnCode, gridColumnCode, String.valueOf(total.longTotal));
    setTotalRowValue(itemRealTotal, columnCode, gridColumnCode, total.stringTotal);
  }

  public void updateAPEWaring() {
    for (RapidTestFormGridViewModel viewModel : itemRealTotal.rapidTestFormGridViewModelList) {
      RapidTestFormGridViewModel apeViewModel = itemAPEs.rapidTestFormGridViewModelMap
          .get(viewModel.getColumnCode());
      if (apeViewModel != null) {
        apeViewModel.isNeedAllAPEValue = !viewModel.isEmpty();
      }
    }
  }

  private void calculateTotalLogic(Total total, @NonNull RapidTestFormGridViewModel gridViewModel,
      RapidTestGridColumnCode gridColumnCode) {
    switch (gridColumnCode) {
      case CONSUMPTION:
        if (!gridViewModel.getConsumptionValue().equals("")) {
          total.longTotal += Long.parseLong(gridViewModel.getConsumptionValue());
          total.stringTotal = String.valueOf(total.longTotal);
        }
        break;
      case POSITIVE:
        if (!gridViewModel.getPositiveValue().equals("")) {
          total.longTotal += Long.parseLong(gridViewModel.getPositiveValue());
          total.stringTotal = String.valueOf(total.longTotal);
        }
        break;
      case POSITIVE_HIV:
        String positiveHivValue = gridViewModel.getPositiveHivValue();
        if (StringUtils.isNotEmpty(positiveHivValue)) {
          total.longTotal += Long.parseLong(positiveHivValue);
          total.stringTotal = String.valueOf(total.longTotal);
        }
        break;
      case POSITIVE_SYPHILIS:
        String positiveSyphilisValue = gridViewModel.getPositiveSyphilisValue();
        if (StringUtils.isNotEmpty(positiveSyphilisValue)) {
          total.longTotal += Long.parseLong(positiveSyphilisValue);
          total.stringTotal = String.valueOf(total.longTotal);
        }
        break;
      case UNJUSTIFIED:
        if (!gridViewModel.getUnjustifiedValue().equals("")) {
          total.longTotal += Long.parseLong(gridViewModel.getUnjustifiedValue());
          total.stringTotal = String.valueOf(total.longTotal);
        }
        break;
      default:
        // do nothing
    }
  }

  private void setTotalRowValue(RapidTestFormItemViewModel totalItem, ColumnCode columnCode,
      RapidTestGridColumnCode gridColumnCode, String total) {
    switch (gridColumnCode) {
      case CONSUMPTION:
        RapidTestFormGridViewModel consumptionFormGridViewModel =
            totalItem.getRapidTestFormGridViewModelMap().get(columnCode);
        if (consumptionFormGridViewModel != null) {
          consumptionFormGridViewModel.setConsumptionValue(total);
        }
        break;
      case POSITIVE:
        RapidTestFormGridViewModel positiveFormGridViewModel =
            totalItem.getRapidTestFormGridViewModelMap().get(columnCode);
        if (positiveFormGridViewModel != null) {
          positiveFormGridViewModel.setPositiveValue(total);
        }
        break;
      case POSITIVE_HIV:
        RapidTestFormGridViewModel positiveHivFormGridViewModel =
            totalItem.getRapidTestFormGridViewModelMap().get(columnCode);
        if (positiveHivFormGridViewModel != null) {
          positiveHivFormGridViewModel.setPositiveHivValue(total);
        }
        break;
      case POSITIVE_SYPHILIS:
        RapidTestFormGridViewModel positiveSyphilisFormGridViewModel =
            totalItem.getRapidTestFormGridViewModelMap().get(columnCode);
        if (positiveSyphilisFormGridViewModel != null) {
          positiveSyphilisFormGridViewModel.setPositiveSyphilisValue(total);
        }
        break;
      case UNJUSTIFIED:
        RapidTestFormGridViewModel unjustifiedFormGridViewModel =
            totalItem.getRapidTestFormGridViewModelMap().get(columnCode);
        if (unjustifiedFormGridViewModel != null) {
          unjustifiedFormGridViewModel.setUnjustifiedValue(total);
        }
        break;
      default:
        // do nothing
    }
  }

  private void clearError() {
    for (RapidTestFormItemViewModel itemViewModel : itemViewModelList) {
      for (RapidTestFormGridViewModel gridViewModel : itemViewModel.rapidTestFormGridViewModelList) {
        gridViewModel.setInvalidColumn(null);
      }
    }
  }

  public enum Status {
    MISSING(true, 0),
    INCOMPLETE(true, 1),
    COMPLETED(false, 2),
    SYNCED(false, 3),
    FIRST_MISSING(false, 4),
    UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD(false, 5),
    CANNOT_DO_MONTHLY_INVENTORY(false, 6),
    COMPLETE_INVENTORY(false, 7),
    INACTIVE(false, 8);


    @Getter
    private final boolean editable;
    @Getter
    private final int viewType;

    Status(boolean editable, int viewType) {
      this.editable = editable;
      this.viewType = viewType;
    }
  }

  private static class Total {

    long longTotal;
    String stringTotal;

    public Total() {
      super();
    }
  }

}
