package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;

import lombok.Getter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RapidTestReportFormPresenter extends Presenter {
    @Inject
    ProgramDataFormRepository programDataFormRepository;

    @Inject
    ProgramRepository programRepository;

    @Getter
    protected RapidTestReportViewModel viewModel;

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
    }


    public Observable<RapidTestReportViewModel> loadViewModel(final long formId, final DateTime periodBegin) {
        return Observable.create(new Observable.OnSubscribe<RapidTestReportViewModel>() {
            @Override
            public void call(Subscriber<? super RapidTestReportViewModel> subscriber) {
                try {
                    if (formId == 0) {
                        generateNewRapidTestForm(periodBegin);
                    } else {
                        ProgramDataForm programDataForm = programDataFormRepository.queryById(formId);
                        convertProgramDataFormToRapidTestReportViewModel(programDataForm);
                    }
                    subscriber.onNext(viewModel);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                    e.reportToFabric();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private void convertProgramDataFormToRapidTestReportViewModel(ProgramDataForm programDataForm) {
        viewModel = new RapidTestReportViewModel(programDataForm);
    }

    private void generateNewRapidTestForm(DateTime periodBegin) {
        viewModel = new RapidTestReportViewModel(new Period(periodBegin));
    }

    public Observable<RapidTestReportViewModel> saveDraftForm() {
        return Observable.create(new Observable.OnSubscribe<RapidTestReportViewModel>() {
            @Override
            public void call(Subscriber<? super RapidTestReportViewModel> subscriber) {
                try {
                    viewModel.convertFormViewModelToDataModel(programRepository.queryByCode(Constants.RAPID_TEST_CODE));
                    programDataFormRepository.batchCreateOrUpdate(viewModel.getRapidTestForm());
                    subscriber.onNext(viewModel);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                    new LMISException(e).reportToFabric();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public void deleteDraft() {
        if (viewModel.getRapidTestForm() != null) {
            try {
                programDataFormRepository.delete(viewModel.getRapidTestForm());
            } catch (Exception e) {
                new LMISException(e).reportToFabric();
            }
        }
    }
}
