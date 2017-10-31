package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Period;
import org.openlmis.core.service.PatientDataService;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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

    public Observable<List<PatientDataReportViewModel>> getViewModels() {
        return Observable.create(new Observable.OnSubscribe<List<PatientDataReportViewModel>>(){
            @Override
            public void call(Subscriber<? super List<PatientDataReportViewModel>> subscriber) {
                generateViewModelsForAvailablePeriods();
                subscriber.onNext(viewModels);
                subscriber.onCompleted();
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    public void generateViewModelsForAvailablePeriods() {
        viewModels.clear();
        List<Period> periods = patientDataService.calculatePeriods();
        for (Period period: periods) {
            addViewModel(period);
        }
    }

    private void addViewModel(Period period) {
        viewModels.add(new PatientDataReportViewModel(period));
    }
}
