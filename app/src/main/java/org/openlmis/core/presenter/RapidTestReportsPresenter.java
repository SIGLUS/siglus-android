package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.service.ProgramDataFormPeriodService;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.openlmis.core.model.ProgramDataForm.STATUS.DRAFT;
import static org.openlmis.core.utils.Constants.RAPID_TEST_CODE;
import static org.openlmis.core.view.viewmodel.RapidTestReportViewModel.Status;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

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
            e.printStackTrace();
        }
        return viewModelList;
    }

    protected void generateViewModelsForAllPeriods() throws LMISException {
        Optional<Period> period = periodService.getFirstStandardPeriod();
        if (period.isPresent()) {
            List<ProgramDataForm> rapidTestForms = programDataFormRepository.listByProgramCode(RAPID_TEST_CODE);
            isHaveFirstPeriod = isAllRapidTestFormInDBCompleted(rapidTestForms) ? false : true;
            while (period.isPresent()) {
                RapidTestReportViewModel rapidTestReportViewModel = getViewModel(period.get(), rapidTestForms);
                viewModelList.add(rapidTestReportViewModel);
                period = generateNextPeriod(rapidTestReportViewModel.getPeriod().getEnd());
            }
            if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
                RapidTestReportViewModel rapidTestReportViewModel = getViewModel(period.get(), rapidTestForms);
                viewModelList.add(rapidTestReportViewModel);
            }

            RapidTestReportViewModel lastViewModel = viewModelList.size() > 0 ? viewModelList.get(viewModelList.size() - 1) : null;
            if (lastViewModel != null && (lastViewModel.status == Status.FIRST_MISSING && lastViewModel.getPeriod().getEnd().isAfterNow())) {

                DateTime dateTime = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
                DateTime endDateTime = new DateTime(lastViewModel.getPeriod().getEnd());

                if (dateTime.getDayOfMonth() >= 18 && dateTime.getMonthOfYear() == endDateTime.getMonthOfYear()) {
                    Period currentPeriod = lastViewModel.getPeriod();
                    Period lastPeriod = new Period(currentPeriod.getBegin(), currentPeriod.getEnd());
                    List<Inventory> physicalInventories = inventoryRepository.queryPeriodInventory(lastPeriod);
                    RapidTestReportViewModel rapidTestReportViewModel;
                    if (physicalInventories == null || physicalInventories.size() == 0) {
                        rapidTestReportViewModel = new RapidTestReportViewModel(lastViewModel.getPeriod(), Status.UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD);
                    } else {
                        rapidTestReportViewModel = new RapidTestReportViewModel(lastPeriod, Status.COMPLETE_INVENTORY);
                    }
                    viewModelList.set(viewModelList.size() - 1, rapidTestReportViewModel);
                } else {
                    RapidTestReportViewModel rapidTest = new RapidTestReportViewModel(lastViewModel.getPeriod(), RapidTestReportViewModel.Status.CANNOT_DO_MONTHLY_INVENTORY);
                    viewModelList.set(viewModelList.size() - 1, rapidTest);
                }
            }
        }

        RapidTestReportViewModel lastViewModel = viewModelList.size() > 0 ? viewModelList.get(viewModelList.size() - 1) : null;
        if (isRapidTestListCompleted(viewModelList)){
            DateTime currentPeriod;
            if (lastViewModel == null) {
                if (period.isPresent()) {
                    currentPeriod = period.get().getBegin();
                } else  {
                    currentPeriod = new DateTime( new DateTime(LMISApp.getInstance().getCurrentTimeMillis()));
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
        viewModelList = removeGreaterThanData(viewModelList);
        Collections.sort(viewModelList, new Comparator<RapidTestReportViewModel>() {
            @Override
            public int compare(RapidTestReportViewModel lhs, RapidTestReportViewModel rhs) {
                return rhs.getPeriod().getBegin().toDate().compareTo(lhs.getPeriod().getBegin().toDate());
            }
        });
    }

    private List<RapidTestReportViewModel> removeGreaterThanData(List<RapidTestReportViewModel> list) {
        if(list.size() > 13) {
               if (list.get(0).status != Status.FIRST_MISSING
                       && list.get(1).status != Status.FIRST_MISSING) {
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
        DateTime  periodEndDate;
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

    private RapidTestReportViewModel generateRnrFormViewModelWithoutRnrForm(Period currentPeriod) throws LMISException {
        if (isCanNotCreateRnr(currentPeriod)) {
            return new RapidTestReportViewModel(currentPeriod, RapidTestReportViewModel.Status.CANNOT_DO_MONTHLY_INVENTORY);
        }

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
            if (stockMovementRepository.queryStockMovementDatesByProgram(RAPID_TEST_CODE).isEmpty()) {
                return new  RapidTestReportViewModel(currentPeriod, RapidTestReportViewModel.Status.CANNOT_DO_MONTHLY_INVENTORY);
            }
        }
        return null;
    }

   private Boolean isAllRapidTestFormInDBCompleted(List<ProgramDataForm> rapidTestForms) {
        for (ProgramDataForm dataForm: rapidTestForms) {
            if (dataForm.getStatus() == DRAFT){
                return  false;
            }
        }
        return true;
   }

    private Boolean isRapidTestListCompleted(List<RapidTestReportViewModel> rapidTestReports) {
        for (RapidTestReportViewModel rapidTest: rapidTestReports) {
            if (rapidTest.getStatus() != Status.COMPLETED || rapidTest.getStatus() != Status.SYNCED){
                return  false;
            }
        }
        return true;
    }

    private RapidTestReportViewModel getViewModel(Period period, List<ProgramDataForm> rapidTestForms) {
        RapidTestReportViewModel rapidTestReportViewModel = new RapidTestReportViewModel(period);
        setExistingProgramDataForm(rapidTestReportViewModel, rapidTestForms);
        if (rapidTestReportViewModel.status ==  RapidTestReportViewModel.Status.MISSING && !isHaveFirstPeriod) {
            isHaveFirstPeriod = true;
            rapidTestReportViewModel.status = RapidTestReportViewModel.Status.FIRST_MISSING;
        }
        return rapidTestReportViewModel;
    }

    private void setExistingProgramDataForm(final RapidTestReportViewModel viewModel, List<ProgramDataForm> rapidTestForms) {
        Optional<ProgramDataForm> existingProgramDataForm = from(rapidTestForms).firstMatch(new Predicate<ProgramDataForm>() {
            @Override
            public boolean apply(ProgramDataForm programDataForm) {
               DateTime programeDateTime = new DateTime(programDataForm.getPeriodBegin());
               DateTime viewModelDateTime = new DateTime(viewModel.getPeriod().getBegin());
                return programeDateTime.getMonthOfYear() == viewModelDateTime.getMonthOfYear() &&
                        programeDateTime.getYear() == viewModelDateTime.getYear();
            }
        });
        if (existingProgramDataForm.isPresent()) {
            ProgramDataForm existingRapidTestForm = existingProgramDataForm.get();
            DateTime beginDate = new DateTime(existingRapidTestForm.getPeriodBegin());
            DateTime endDate= new DateTime(existingRapidTestForm.getPeriodEnd());
            viewModel.setRapidTestForm(existingRapidTestForm);
            Period period = new Period(beginDate, endDate);
            viewModel.setPeriod(period);
        }
    }
}
