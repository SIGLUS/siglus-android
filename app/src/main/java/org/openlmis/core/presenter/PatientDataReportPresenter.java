package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Period;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.service.PatientDataService;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.holder.PatientDataReportViewHolder;
import org.openlmis.core.view.viewmodel.PatientDataReportViewModel;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class PatientDataReportPresenter extends Presenter {

    private List<PatientDataReportViewModel> viewModels;

    @Inject
    PatientDataService patientDataService;

    public PatientDataReportPresenter() {
        this.viewModels = new ArrayList<>();
    }

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {

    }

    public List<PatientDataReportViewModel> getViewModels() {
        generateViewModelsForAvailablePeriods();
        return viewModels;
    }

    public void generateViewModelsForAvailablePeriods() {
        List<Period> periods = patientDataService.calculatePeriods();
        for (Period period: periods) {
            addViewModel(period);
        }
    }

    private void addViewModel(Period period) {
        viewModels.add(new PatientDataReportViewModel(period));
    }
}
