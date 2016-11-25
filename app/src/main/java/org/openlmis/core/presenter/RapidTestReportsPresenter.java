package org.openlmis.core.presenter;

import org.openlmis.core.exceptions.ViewNotMatchException;
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
        viewModelList.addAll(generateReportsFromDB());
        viewModelList.addAll(generateViewModelsForMissingPeriods());
        return viewModelList;
    }

    private List<RapidTestReportViewModel> generateViewModelsForMissingPeriods() {
        List<RapidTestReportViewModel> list = new ArrayList<>();
        list.add(new RapidTestReportViewModel());
        list.add(new RapidTestReportViewModel());
        list.add(new RapidTestReportViewModel());
        return list;
    }

    private List<RapidTestReportViewModel> generateReportsFromDB() {
        List<RapidTestReportViewModel> list = new ArrayList<>();
        return list;
    }
}
