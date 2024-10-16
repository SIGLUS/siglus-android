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

package org.openlmis.core.presenter;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import androidx.annotation.NonNull;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Setter;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RnRFormListPresenter extends Presenter {

  @Inject
  RnrFormRepository repository;

  @Inject
  InventoryRepository inventoryRepository;

  @Inject
  SyncErrorsRepository syncErrorsRepository;

  @Inject
  StockRepository stockRepository;

  @Setter
  String programCode;

  @Inject
  ReportTypeFormRepository reportTypeFormRepository;

  @Inject
  RequisitionPeriodService requisitionPeriodService;

  @Inject
  private StockMovementRepository stockMovementRepository;

  @Override
  public void attachView(BaseView v) {
    // do nothing
  }

  @SuppressWarnings("squid:S1905")
  public Observable<List<RnRFormViewModel>> loadRnRFormList() {
    return Observable.create((Observable.OnSubscribe<List<RnRFormViewModel>>) subscriber -> {
      try {
        subscriber.onNext(buildFormListViewModels());
        subscriber.onCompleted();
      } catch (LMISException e) {
        subscriber.onError(e);
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }

  @NonNull
  protected List<RnRFormViewModel> buildFormListViewModels() throws LMISException {
    List<RnRFormViewModel> rnRFormViewModels = new ArrayList<>();
    ReportTypeForm typeForm = reportTypeFormRepository.queryByCode(programCode);
    List<RnRForm> rnRForms = repository.listInclude(RnRForm.Emergency.YES, programCode, typeForm);

    generateRnrViewModelByRnrFormsInDB(rnRFormViewModels, rnRForms);

    if (typeForm.active) {
      generateViewModelsByCurrentDate(rnRFormViewModels, typeForm);
    }

    populateSyncErrorsOnViewModels(rnRFormViewModels);

    Collections.sort(rnRFormViewModels, (lhs, rhs) -> rhs.getPeriodEndMonth().compareTo(lhs.getPeriodEndMonth()));

    generateInactiveFormListViewModels(rnRFormViewModels, typeForm);

    return rnRFormViewModels;
  }

  private void generateInactiveFormListViewModels(List<RnRFormViewModel> rnRFormViewModels,
      ReportTypeForm typeForm) {
    if (!typeForm.active) {
      rnRFormViewModels.add(0, RnRFormViewModel.buildInactive(programCode));
    }
  }


  private void generateViewModelsByCurrentDate(List<RnRFormViewModel> rnRFormViewModels,
      ReportTypeForm typeForm) throws LMISException {
    if (requisitionPeriodService.hasMissedPeriod(programCode, typeForm)) {
      addPreviousPeriodMissedViewModels(rnRFormViewModels, typeForm);
    } else if (requisitionPeriodService
        .isAllRnrFormInDBCompletedOrNoRnrFormInDB(programCode, typeForm)) {
      Period nextPeriodInSchedule = requisitionPeriodService.generateNextPeriod(programCode, null);
      rnRFormViewModels.add(generateRnrFormViewModelWithoutRnrForm(nextPeriodInSchedule));
    }
  }

  private RnRFormViewModel generateRnrFormViewModelWithoutRnrForm(Period currentPeriod)
      throws LMISException {
    if (stockMovementRepository.queryEarliestStockMovementDateByProgram(programCode) == null) {
      return new RnRFormViewModel(currentPeriod, programCode,
          RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY_NO_MOVEMENTS);
    }

    if (isCanNotCreateRnr(currentPeriod)) {
      return new RnRFormViewModel(currentPeriod, programCode, RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY);
    }

    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)
        && stockMovementRepository.queryMalariaStockMovementDates().isEmpty()) {
      return new RnRFormViewModel(currentPeriod, programCode, RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY);
    }

    List<Inventory> physicalInventories = inventoryRepository.queryPeriodInventory(currentPeriod);
    if (physicalInventories == null || physicalInventories.isEmpty()) {
      return new RnRFormViewModel(currentPeriod, programCode,
          RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD);
    } else {
      return new RnRFormViewModel(currentPeriod, programCode, RnRFormViewModel.TYPE_INVENTORY_DONE);
    }
  }

  private boolean isCanNotCreateRnr(Period currentPeriod) {
    DateTime dateTime = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
    return dateTime.isBefore(currentPeriod.getInventoryBegin());
  }

  private void populateSyncErrorsOnViewModels(final List<RnRFormViewModel> rnrViewModels) {
    for (RnRFormViewModel rnrViewModel : rnrViewModels) {
      rnrViewModel.setSyncServerErrorMessage(getRnrFormSyncError(rnrViewModel.getId()));
    }
  }

  private String getRnrFormSyncError(long rnrId) {
    List<SyncError> syncErrorList = syncErrorsRepository
        .getBySyncTypeAndObjectId(SyncType.RNR_FORM, rnrId);
    if (null == syncErrorList || syncErrorList.isEmpty()) {
      return null;
    }
    return syncErrorList.get(syncErrorList.size() - 1).getErrorMessage();
  }

  protected void generateRnrViewModelByRnrFormsInDB(List<RnRFormViewModel> viewModels,
      List<RnRForm> rnRForms) {
    viewModels.addAll(from(rnRForms).transform(
        form -> form.isEmergency() ? RnRFormViewModel.buildEmergencyViewModel(form)
            : RnRFormViewModel.buildNormalRnrViewModel(form)).toList());
  }

  protected void addPreviousPeriodMissedViewModels(List<RnRFormViewModel> viewModels,
      ReportTypeForm typeForm) throws LMISException {
    int offsetMonth = requisitionPeriodService.getMissedPeriodOffsetMonth(this.programCode, typeForm);
    DateTime periodBeginDate = requisitionPeriodService.getCurrentPeriodBeginDate();

    for (int i = 0; i < offsetMonth; i++) {
      viewModels.add(RnRFormViewModel.buildMissedPeriod(periodBeginDate.toDate(),
          periodBeginDate.plusMonths(1).toDate()));
      periodBeginDate = periodBeginDate.minusMonths(1);
    }
    generateFirstMissedRnrFormViewModel(viewModels, periodBeginDate, typeForm);
  }

  private void generateFirstMissedRnrFormViewModel(List<RnRFormViewModel> viewModels,
      DateTime inventoryBeginDate, ReportTypeForm typeForm) throws LMISException {
    if (requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(programCode, typeForm)) {
      addFirstMissedAndNotPendingRnrForm(viewModels);
    } else {
      viewModels.add(RnRFormViewModel.buildMissedPeriod(inventoryBeginDate.toDate(),
          inventoryBeginDate.plusMonths(1).toDate()));
    }
  }

  private void addFirstMissedAndNotPendingRnrForm(List<RnRFormViewModel> viewModels)
      throws LMISException {
    Period nextPeriodInSchedule = requisitionPeriodService.generateNextPeriod(programCode, null);

    List<Inventory> physicalInventories = inventoryRepository
        .queryPeriodInventory(nextPeriodInSchedule);
    if (physicalInventories.isEmpty()) {
      viewModels.add(RnRFormViewModel
          .buildFirstMissedPeriod(programCode, nextPeriodInSchedule.getBegin().toDate(),
              nextPeriodInSchedule.getEnd().toDate()));
    } else {
      viewModels.add(new RnRFormViewModel(nextPeriodInSchedule, programCode,
          RnRFormViewModel.TYPE_INVENTORY_DONE));
    }
  }

  public void deleteRnRForm(RnRForm form) throws LMISException {
    repository.removeRnrForm(form);
  }

}
