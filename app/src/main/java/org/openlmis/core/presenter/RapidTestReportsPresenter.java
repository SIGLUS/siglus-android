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

import static org.openlmis.core.model.Period.BEGIN_DAY;
import static org.openlmis.core.model.Period.END_DAY;
import static org.openlmis.core.model.Period.INVENTORY_BEGIN_DAY;
import static org.openlmis.core.model.ProgramDataForm.Status.DRAFT;
import static org.openlmis.core.utils.Constants.RAPID_TEST_CODE;
import static org.openlmis.core.view.viewmodel.RapidTestReportViewModel.Status;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import android.util.Log;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.service.ProgramDataFormPeriodService;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.base.Predicate;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@SuppressWarnings("PMD")
public class RapidTestReportsPresenter extends Presenter {

  @Getter
  private List<RapidTestReportViewModel> viewModelList = new ArrayList<>();
  private boolean isHaveFirstPeriod;

  @Inject
  private ProgramDataFormRepository programDataFormRepository;

  @Inject
  private ProgramDataFormPeriodService periodService;

  @Inject
  private StockMovementRepository stockMovementRepository;

  @Inject
  ReportTypeFormRepository reportTypeFormRepository;

  @Inject
  InventoryRepository inventoryRepository;

  public RapidTestReportsPresenter() {
  }

  @Override
  public void attachView(BaseView v) throws ViewNotMatchException {

  }

  public Observable<List<RapidTestReportViewModel>> loadViewModels() {
    return Observable.just(generateViewModels())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }

  private List<RapidTestReportViewModel> generateViewModels() {
    viewModelList.clear();
    try {
      generateViewModelsForAllPeriods();
    } catch (LMISException e) {
      Log.w("RapidTestPresenter", e);
    }
    return viewModelList;
  }

  private void generateViewModelsForAllPeriods() throws LMISException {
    ReportTypeForm typeForm = reportTypeFormRepository.queryByCode(Program.RAPID_TEST_CODE);
    Optional<Period> period = periodService.getFirstStandardPeriod();
    DateTime startPeriodTime = new DateTime(typeForm.getStartTime());

    if (period.isPresent() && startPeriodTime.isAfter(period.get().getBegin())) {
      Period reportPeriod = getRapidTestPeriod(startPeriodTime);
      period = Optional.of(reportPeriod);
    }

    if (period.isPresent()) {
      List<ProgramDataForm> rapidTestForms = programDataFormRepository.listByProgramCode(Program.RAPID_TEST_CODE);
      isHaveFirstPeriod = !isAllRapidTestFormInDBCompleted(rapidTestForms);
      while (period.isPresent()) {
        RapidTestReportViewModel rapidTestReportViewModel = getViewModel(period.get(), rapidTestForms);
        viewModelList.add(rapidTestReportViewModel);
        period = generateNextPeriod(rapidTestReportViewModel.getPeriod().getEnd());
      }

      removeInactiveData(viewModelList, typeForm);
      RapidTestReportViewModel lastViewModel =
          viewModelList.size() > 0 ? viewModelList.get(viewModelList.size() - 1) : null;
      if (typeForm.active && lastViewModel != null
          && (lastViewModel.getStatus() == Status.FIRST_MISSING
          && lastViewModel.getPeriod().getEnd().isAfterNow())) {

        DateTime dateTime = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
        DateTime endDateTime = new DateTime(lastViewModel.getPeriod().getEnd());
        addLastRapidTestViewModel(lastViewModel, dateTime, endDateTime);
      }
    }

    RapidTestReportViewModel lastViewModel =
        viewModelList.size() > 0 ? viewModelList.get(viewModelList.size() - 1) : null;
    addCompletedColumn(typeForm, period, lastViewModel);
    viewModelList = removeGreaterThanData(viewModelList);
    Collections.sort(viewModelList, new Comparator<RapidTestReportViewModel>() {
      @Override
      public int compare(RapidTestReportViewModel lhs, RapidTestReportViewModel rhs) {
        return rhs.getPeriod().getBegin().toDate().compareTo(lhs.getPeriod().getBegin().toDate());
      }
    });
    addInactiveDate(viewModelList, typeForm);
  }

  private void addLastRapidTestViewModel(RapidTestReportViewModel lastViewModel, DateTime dateTime,
      DateTime endDateTime) throws LMISException {
    if (dateTime.getDayOfMonth() >= INVENTORY_BEGIN_DAY && dateTime.getMonthOfYear() == endDateTime
        .getMonthOfYear()) {
      Period currentPeriod = lastViewModel.getPeriod();
      Period lastPeriod = new Period(currentPeriod.getBegin(), currentPeriod.getEnd());
      List<Inventory> physicalInventories = inventoryRepository.queryPeriodInventory(lastPeriod);
      RapidTestReportViewModel rapidTestReportViewModel;
      if (physicalInventories == null || physicalInventories.size() == 0) {
        rapidTestReportViewModel = new RapidTestReportViewModel(lastViewModel.getPeriod(),
            Status.UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD);
      } else {
        rapidTestReportViewModel = new RapidTestReportViewModel(lastPeriod,
            Status.COMPLETE_INVENTORY);
      }
      viewModelList.set(viewModelList.size() - 1, rapidTestReportViewModel);
    } else {
      RapidTestReportViewModel rapidTest = new RapidTestReportViewModel(lastViewModel.getPeriod(),
          Status.CANNOT_DO_MONTHLY_INVENTORY);
      viewModelList.set(viewModelList.size() - 1, rapidTest);
    }
  }

  private void addCompletedColumn(ReportTypeForm typeForm, Optional<Period> period,
      RapidTestReportViewModel lastViewModel) throws LMISException {
    if (isRapidTestListCompleted(viewModelList) && typeForm.active) {
      DateTime currentPeriod;
      if (lastViewModel == null) {
        if (period.isPresent()) {
          currentPeriod = period.get().getBegin();
        } else {
          currentPeriod = new DateTime(new DateTime(LMISApp.getInstance().getCurrentTimeMillis()));
        }
      } else {
        currentPeriod = period.get().getEnd();
      }
      Period periodLast = generateNextAvailablePeriod(currentPeriod);
      RapidTestReportViewModel rapidTest = generateRnrFormViewModelWithoutRnrForm(periodLast);
      if (rapidTest != null) {
        viewModelList.add(rapidTest);
      }
    }
  }

  private void addInactiveDate(List<RapidTestReportViewModel> list, ReportTypeForm typeForm) {
    if (!typeForm.isActive()) {
      RapidTestReportViewModel rapidTestReportViewModel = new RapidTestReportViewModel(null,
          Status.INACTIVE);
      list.add(0, rapidTestReportViewModel);
    }
  }

  private void removeInactiveData(List<RapidTestReportViewModel> list, ReportTypeForm typeForm) {
    if (!typeForm.active) {
      List<RapidTestReportViewModel> needBeDeleteList = new ArrayList<>();
      for (int i = list.size() - 1; i >= 0; i--) {
        RapidTestReportViewModel viewModel = list.get(i);
        if (viewModel.getStatus() == Status.MISSING
            || viewModel.getStatus() == Status.FIRST_MISSING) {
          needBeDeleteList.add(viewModel);
        } else {
          break;
        }
      }
      list.removeAll(needBeDeleteList);

    }
  }

  private List<RapidTestReportViewModel> removeGreaterThanData(
      List<RapidTestReportViewModel> list) {
    if (list.size() > 13) {
      if (list.get(0).getStatus() != Status.FIRST_MISSING
          && list.get(1).getStatus() != Status.FIRST_MISSING) {
        list.remove(0);
        return removeGreaterThanData(list);
      }
    }
    return list;
  }

  private Optional<Period> generateNextPeriod(DateTime beginDate) {
    Period period = generateNextAvailablePeriod(beginDate);
    return period.getBegin().isAfterNow() ? Optional.absent() : Optional.of(period);
  }

  private Period generateNextAvailablePeriod(DateTime beginDate) {
    DateTime periodEndDate;
    Calendar date = Calendar.getInstance();
    date.set(beginDate.getYear(), beginDate.getMonthOfYear(), Period.INVENTORY_END_DAY_NEXT);
    periodEndDate = DateUtil.cutTimeStamp(new DateTime(date));
    Period period = new Period(beginDate, periodEndDate);
    return period;
  }

  private boolean isCanNotCreateRnr(Period currentPeriod) {
    DateTime dateTime = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
    return dateTime.isBefore(currentPeriod.getInventoryBegin());
  }

  private RapidTestReportViewModel generateRnrFormViewModelWithoutRnrForm(Period currentPeriod)
      throws LMISException {
    if (isCanNotCreateRnr(currentPeriod)) {
      return new RapidTestReportViewModel(currentPeriod,
          RapidTestReportViewModel.Status.CANNOT_DO_MONTHLY_INVENTORY);
    }

    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
      if (stockMovementRepository.queryStockMovementDatesByProgram(RAPID_TEST_CODE).isEmpty()) {
        return new RapidTestReportViewModel(currentPeriod,
            RapidTestReportViewModel.Status.CANNOT_DO_MONTHLY_INVENTORY);
      }
    }
    return null;
  }

  private Boolean isAllRapidTestFormInDBCompleted(List<ProgramDataForm> rapidTestForms) {
    for (ProgramDataForm dataForm : rapidTestForms) {
      if (dataForm.getStatus() == DRAFT) {
        return false;
      }
    }
    return true;
  }

  private Boolean isRapidTestListCompleted(List<RapidTestReportViewModel> rapidTestReports) {
    for (RapidTestReportViewModel rapidTest : rapidTestReports) {
      if (rapidTest.getStatus() != Status.COMPLETED || rapidTest.getStatus() != Status.SYNCED) {
        return false;
      }
    }
    return true;
  }

  private RapidTestReportViewModel getViewModel(Period period,
      List<ProgramDataForm> rapidTestForms) {
    RapidTestReportViewModel rapidTestReportViewModel = new RapidTestReportViewModel(period);
    setExistingProgramDataForm(rapidTestReportViewModel, rapidTestForms);
    if (rapidTestReportViewModel.getStatus() == RapidTestReportViewModel.Status.MISSING
        && !isHaveFirstPeriod) {
      isHaveFirstPeriod = true;
      rapidTestReportViewModel.setStatus(RapidTestReportViewModel.Status.FIRST_MISSING);
    }
    return rapidTestReportViewModel;
  }

  private void setExistingProgramDataForm(final RapidTestReportViewModel viewModel,
      List<ProgramDataForm> rapidTestForms) {
    Optional<ProgramDataForm> existingProgramDataForm = from(rapidTestForms)
        .firstMatch(new Predicate<ProgramDataForm>() {
          @Override
          public boolean apply(ProgramDataForm programDataForm) {
            DateTime programDateTime = new DateTime(programDataForm.getPeriodBegin());
            DateTime viewModelDateTime = new DateTime(viewModel.getPeriod().getBegin());
            return programDateTime.getMonthOfYear() == viewModelDateTime.getMonthOfYear()
                && programDateTime.getYear() == viewModelDateTime.getYear();
          }
        });
    if (existingProgramDataForm.isPresent()) {
      ProgramDataForm existingRapidTestForm = existingProgramDataForm.get();
      DateTime beginDate = new DateTime(existingRapidTestForm.getPeriodBegin());
      DateTime endDate = new DateTime(existingRapidTestForm.getPeriodEnd());
      viewModel.setRapidTestForm(existingRapidTestForm);
      Period period = new Period(beginDate, endDate);
      viewModel.setPeriod(period);
    }
  }

  private Period getRapidTestPeriod(DateTime dateTime) {
    DateTime periodBegin;
    DateTime periodEnd;
    if (dateTime.dayOfMonth().get() <= BEGIN_DAY) {
      periodBegin = DateUtil.cutTimeStamp(dateTime.withDayOfMonth(BEGIN_DAY));
      periodEnd = DateUtil.cutTimeStamp(dateTime.plusMonths(1).withDayOfMonth(END_DAY));
    } else {
      periodBegin = DateUtil.cutTimeStamp(dateTime.plusMonths(1).withDayOfMonth(BEGIN_DAY));
      periodEnd = DateUtil.cutTimeStamp(dateTime.plusMonths(2).withDayOfMonth(END_DAY));
    }
    return new Period(periodBegin, periodEnd);
  }

}
