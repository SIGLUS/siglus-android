package org.openlmis.core.presenter;

import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Period;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;

import java.util.ArrayList;
import java.util.Date;
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
        generateViewModelsForAllPeriods();
        loadViewModelFromDB();
        return viewModelList;
    }

    private void loadViewModelFromDB() {
        //TODO replace empty view models with data from db
        viewModelList.get(1).setStatus(RapidTestReportViewModel.Status.Draft);
        viewModelList.get(2).setStatus(RapidTestReportViewModel.Status.COMPLETED);
        viewModelList.get(3).setStatus(RapidTestReportViewModel.Status.SYNCED);
        viewModelList.get(4).setStatus(RapidTestReportViewModel.Status.MISSING);
    }

    private void generateViewModelsForAllPeriods() {
        Date currentDate = new Date();
        Date oneMonthBefore = DateUtil.generatePreviousMonthDateBy(currentDate);
        Date twoMonthBefore = DateUtil.generatePreviousMonthDateBy(oneMonthBefore);
        Date threeMonthBefore = DateUtil.generatePreviousMonthDateBy(twoMonthBefore);
        Date fourMonthBefore = DateUtil.generatePreviousMonthDateBy(threeMonthBefore);

        viewModelList.add(new RapidTestReportViewModel(Period.of(currentDate)));
        viewModelList.add(new RapidTestReportViewModel(Period.of(oneMonthBefore)));
        viewModelList.add(new RapidTestReportViewModel(Period.of(twoMonthBefore)));
        viewModelList.add(new RapidTestReportViewModel(Period.of(threeMonthBefore)));
        viewModelList.add(new RapidTestReportViewModel(Period.of(fourMonthBefore)));
    }
}
