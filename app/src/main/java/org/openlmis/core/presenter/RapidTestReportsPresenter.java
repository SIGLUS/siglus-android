package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.service.ProgramDataFormPeriodService;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class RapidTestReportsPresenter extends Presenter {

    @Getter
    private List<RapidTestReportViewModel> viewModelList = new ArrayList<>();

    @Inject
    private ProgramDataFormRepository programDataFormRepository;

    @Inject
    private ProgramDataFormPeriodService periodService;

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
        Period period = periodService.getFirstStandardPeriod();

        List<ProgramDataForm> rapidTestForms = programDataFormRepository.listByProgramCode(Constants.RAPID_TEST_CODE);
        while (period != null) {
            addViewModel(period, rapidTestForms);
            period = periodService.generateNextPeriod(period);
        }
        Collections.sort(viewModelList, new Comparator<RapidTestReportViewModel>() {
            @Override
            public int compare(RapidTestReportViewModel lhs, RapidTestReportViewModel rhs) {
                return rhs.getPeriod().getBegin().toDate().compareTo(lhs.getPeriod().getBegin().toDate());
            }
        });
    }

    private void addViewModel(Period period, List<ProgramDataForm> rapidTestForms) {
        RapidTestReportViewModel rapidTestReportViewModel = new RapidTestReportViewModel(period);
        setExistingProgramDataForm(rapidTestReportViewModel, rapidTestForms);
        viewModelList.add(rapidTestReportViewModel);
    }

    private void setExistingProgramDataForm(final RapidTestReportViewModel viewModel, List<ProgramDataForm> rapidTestForms) {
        Optional<ProgramDataForm> existingProgramDataForm = from(rapidTestForms).firstMatch(new Predicate<ProgramDataForm>() {
            @Override
            public boolean apply(ProgramDataForm programDataForm) {
                return DateUtil.formatDate(programDataForm.getPeriodBegin()).equals(DateUtil.formatDate(viewModel.getPeriod().getBegin().toDate()));
            }
        });
        if (existingProgramDataForm.isPresent()) {
            viewModel.setRapidTestForm(existingProgramDataForm.get());
        }
    }
}
