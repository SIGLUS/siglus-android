package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormBasicItem;
import org.openlmis.core.model.repository.ProgramBasicItemsRepository;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;

import java.util.List;

import lombok.Getter;
import roboguice.inject.ContextSingleton;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@ContextSingleton
public class RapidTestReportFormPresenter extends BaseReportPresenter {
    @Inject
    ProgramDataFormRepository programDataFormRepository;

    @Inject
    ProgramBasicItemsRepository programBasicItemsRepository;

    @Inject
    ProgramRepository programRepository;

    @Getter
    protected RapidTestReportViewModel viewModel;

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {

    }

    public Observable<RapidTestReportViewModel> loadViewModel(final long formId, final Period period) {
        return Observable.create(new Observable.OnSubscribe<RapidTestReportViewModel>() {
            @Override
            public void call(Subscriber<? super RapidTestReportViewModel> subscriber) {
                try {
                    if (formId == 0) {
                        generateNewRapidTestForm(period);
                        saveForm();
                        List<ProgramDataFormBasicItem> basicItems = programBasicItemsRepository.createInitProgramForm(viewModel.getRapidTestForm(), period.getEnd().toDate());
                        viewModel.setBasicItems(basicItems);
                        saveForm();
                    } else {
                        convertProgramDataFormToRapidTestReportViewModel(programDataFormRepository.queryById(formId));
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

    private void generateNewRapidTestForm(Period period) {
        viewModel = new RapidTestReportViewModel(period);
        viewModel.setStatus(RapidTestReportViewModel.Status.INCOMPLETE);
    }

    public Observable<RapidTestReportViewModel> onSaveDraftForm() {
        return Observable.create(new Observable.OnSubscribe<RapidTestReportViewModel>() {
            @Override
            public void call(Subscriber<? super RapidTestReportViewModel> subscriber) {
                try {
                    saveForm();
                    subscriber.onNext(viewModel);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<RapidTestReportViewModel> onAuthoriseDraftForm() {
        return Observable.create(new Observable.OnSubscribe<RapidTestReportViewModel>() {
            @Override
            public void call(Subscriber<? super RapidTestReportViewModel> subscriber) {
                try {
                    saveForm();
                    syncService.requestSyncImmediately(false);
                    subscriber.onNext(viewModel);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public void saveForm() {
        try {
            viewModel.convertFormViewModelToDataModel(programRepository.queryByCode(Constants.RAPID_TEST_CODE));
            programDataFormRepository.batchCreateOrUpdate(viewModel.getRapidTestForm());
        } catch (Exception e) {
            new LMISException(e).reportToFabric();
        }
    }

    @Override
    public void deleteDraft() {
        if (viewModel.getRapidTestForm().getId() != 0L) {
            try {
                programDataFormRepository.delete(viewModel.getRapidTestForm());
            } catch (Exception e) {
                new LMISException(e).reportToFabric();
            }
        }
    }

    @Override
    public boolean isDraft() {
        return viewModel.isDraft();
    }

    public boolean isSubmitted() {
        return viewModel.isSubmitted();
    }

    public void addSignature(String sign) {
        viewModel.addSignature(sign);
    }
}
