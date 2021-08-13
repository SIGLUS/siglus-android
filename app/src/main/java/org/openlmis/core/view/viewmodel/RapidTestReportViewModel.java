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
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.UNJUSTIFIED;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.model.ProgramDataFormSignature;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.Signature;
import org.openlmis.core.model.UsageInformationLineItem;
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

  public static final long DEFAULT_FORM_ID = 0;

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
        .filter(movementReason -> !movementReason.getCode().equals("PUB_PHARMACY")).toList();

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
    setFormItemViewModels(rnRForm.getUsageInformationLineItemsWrapper());
  }

  private void setFormItemViewModels(List<UsageInformationLineItem> usageInformationLineItemList) {
    for (UsageInformationLineItem item : usageInformationLineItemList) {
      itemViewModelMap.get(item.getService())
          .setColumnValue(item.getUsageColumnsMap(), item.getValue());
    }
    addCompatibleWithNotSubmitUnjustified();
    addCompatibleWithNotSubmitAPE();
    for (ColumnCode columnCode : ColumnCode.values()) {
      updateTotal(columnCode, CONSUMPTION);
      updateTotal(columnCode, POSITIVE);
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
      if (isNeedAPE(columnName)) {
        RapidTestFormGridViewModel viewModel = itemAPEs.rapidTestFormGridViewModelMap
            .get(columnName);
        itemAPEs.updateNoValueGridRowToZero(viewModel);
      }
    }
  }

  private Boolean isNeedAPE(ColumnCode columnName) {
    for (RapidTestFormItemViewModel viewModel : itemViewModelList) {
      if (!viewModel.rapidTestFormGridViewModelMap.get(columnName).isEmpty()) {
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
    rapidTestForm.getUsageInformationLineItemsWrapper().clear();
    rapidTestForm.setRnrFormItemListWrapper(productItems);
    convertFormItemViewModelToDataModel();
  }

  private void convertFormItemViewModelToDataModel() {
    for (RapidTestFormItemViewModel itemViewModel : itemViewModelList) {
      rapidTestForm.getUsageInformationLineItemsWrapper().addAll(itemViewModel.convertToDataModel());
    }
    for (UsageInformationLineItem item : rapidTestForm.getUsageInformationLineItemsWrapper()) {
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
    return rapidTestForm.getStatus() == null || rapidTestForm.isDraft() ;
  }

  private boolean isReadyForCompleted() {
    return rapidTestForm.getStatus() == null
        || rapidTestForm.getStatus() == RnRForm.Status.SUBMITTED;
  }

  public boolean validatePositive() {
    for (RapidTestFormItemViewModel itemViewModel : itemViewModelList) {
      if (!itemViewModel.validatePositive()) {
        return false;
      }
    }
    return true;
  }

  public boolean validateUnjustified() {
    for (RapidTestFormItemViewModel itemViewModel : itemViewModelList) {
      if (!itemViewModel.validateUnjustified()) {
        return false;
      }
    }
    return true;
  }

  public boolean validateAPES() {
    for (RapidTestFormGridViewModel itemViewModel : itemAPEs.rapidTestFormGridViewModelList) {
      if (itemViewModel.isNeedAddGridViewWarning()) {
        return false;
      }
    }
    return true;
  }

  public boolean validateOnlyAPES() {
    return itemRealTotal.isEmpty() && !itemAPEs.isEmpty();
  }

  public void addSignature(String signature) {
    if (rapidTestForm.getSignaturesWrapper().size() == 0) {
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
      calculateTotalLogic(total, gridViewModel, gridColumnCode);
    }
    setTotalRowValue(itemTotal, columnCode, gridColumnCode, String.valueOf(total.longTotal));
    setTotalRowValue(itemRealTotal, columnCode, gridColumnCode, total.stringTotal);
  }

  public void updateAPEWaring() {
    for (RapidTestFormGridViewModel viewModel : itemRealTotal.rapidTestFormGridViewModelList) {
      RapidTestFormGridViewModel apeViewModel = itemAPEs.rapidTestFormGridViewModelMap
          .get(viewModel.getColumnCode());
      apeViewModel.isNeedAllAPEValue = !viewModel.isEmpty();
    }
  }

  private void calculateTotalLogic(Total total, RapidTestFormGridViewModel gridViewModel,
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
        totalItem.getRapidTestFormGridViewModelMap().get(columnCode).setConsumptionValue(total);
        break;
      case POSITIVE:
        totalItem.getRapidTestFormGridViewModelMap().get(columnCode).setPositiveValue(total);
        break;
      case UNJUSTIFIED:
        totalItem.getRapidTestFormGridViewModelMap().get(columnCode).setUnjustifiedValue(total);
        break;
      default:
        // do nothing
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
