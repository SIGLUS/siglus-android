package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.service.PeriodService;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RapidTestReportsPresenter extends Presenter {

    @Getter
    private List<RapidTestReportViewModel> viewModelList = new ArrayList<>();

    @Inject
    private PeriodService periodService;

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

        if (period == null) { return; }

        while (period != null) {
            viewModelList.add(new RapidTestReportViewModel(period));
            period = periodService.generateNextPeriod(period);
        }
    }
}
