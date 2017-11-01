package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.MalariaProgramStatus;
import org.openlmis.core.model.Period;
import org.openlmis.core.service.PatientDataService;
import org.openlmis.core.utils.mapper.MalariaDataReportViewModelToMalariaProgramMapper;
import org.openlmis.core.utils.mapper.MalariaProgramToMalariaDataReportViewModelMapper;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportType;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;
import org.openlmis.core.view.viewmodel.malaria.MalariaDataReportViewModel;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class PatientDataReportFormPresenter extends BaseReportPresenter {

    public static final int INVALID_INDEX = -1;
    @Inject
    MalariaProgramToMalariaDataReportViewModelMapper reportViewModelMapper;

    @Inject
    MalariaDataReportViewModelToMalariaProgramMapper malariaProgramMapper;

    @Inject
    PatientDataService patientDataService;

    private Period period;
    private MalariaProgram malariaProgram = new MalariaProgram();

    private List<ImplementationReportViewModel> viewModels;

    public PatientDataReportFormPresenter() {
        this.viewModels = new ArrayList<>();
    }

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
    }

    public Observable<List<ImplementationReportViewModel>> loadPatientData(final Period period) {
        this.period = period;
        return Observable.create(new Observable.OnSubscribe<List<ImplementationReportViewModel>>() {
            @Override
            public void call(final Subscriber<? super List<ImplementationReportViewModel>> subscriber) {
                try {
                    malariaProgram = patientDataService.findForPeriod(period.getBegin(), period.getEnd());
                    List<Long> malariaProductsStockHand = patientDataService.getMalariaProductsStockHand();
                    MalariaDataReportViewModel malariaDataReportViewModel = reportViewModelMapper.Map(malariaProgram);
                    ImplementationReportViewModel usImplementationReportViewModel = malariaDataReportViewModel.getUsImplementationReportViewModel();
                    usImplementationReportViewModel.setExistingStock(malariaProductsStockHand);
                    viewModels.add(usImplementationReportViewModel);
                    viewModels.add(malariaDataReportViewModel.getApeImplementationReportViewModel());
                    viewModels.add(generateTotalViewModel(viewModels.get(0), viewModels.get(1)));
                    subscriber.onNext(viewModels);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    new LMISException(e).reportToFabric();
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public MalariaProgram getMalariaProgram() throws InvocationTargetException, NoSuchMethodException, LMISException, IllegalAccessException {
        MalariaDataReportViewModel reportViewModel = new MalariaDataReportViewModel(
                DateTime.now(),
                period.getBegin(),
                period.getEnd(),
                viewModels.get(0),
                viewModels.get(1));
        return malariaProgramMapper.map(reportViewModel, malariaProgram);
    }

    public List<ImplementationReportViewModel> regenerateImplementationModels(ImplementationReportViewModel updatedViewModel) {
        int indexOfUpdatedModel = getIndexOfModel(updatedViewModel);
        viewModels.remove(indexOfUpdatedModel);
        viewModels.add(indexOfUpdatedModel, updatedViewModel);
        viewModels.remove(viewModels.size() - 1);
        viewModels.add(generateTotalViewModel(viewModels.get(0), viewModels.get(1)));
        return viewModels;
    }

    private int getIndexOfModel(ImplementationReportViewModel updatedViewModel) {
        for (int i = 0; i < viewModels.size(); i++) {
            if (viewModels.get(i).getType() == updatedViewModel.getType()) {
                return i;
            }
        }
        return INVALID_INDEX;
    }

    private ImplementationReportViewModel generateTotalViewModel(ImplementationReportViewModel usImplementation,
                                                                 ImplementationReportViewModel apeImplementation) {
        return new ImplementationReportViewModel(ImplementationReportType.TOTAL,
                usImplementation.getCurrentTreatment6x1() + apeImplementation.getCurrentTreatment6x1(),
                usImplementation.getCurrentTreatment6x2() + apeImplementation.getCurrentTreatment6x2(),
                usImplementation.getCurrentTreatment6x3() + apeImplementation.getCurrentTreatment6x3(),
                usImplementation.getCurrentTreatment6x4() + apeImplementation.getCurrentTreatment6x4(),
                usImplementation.getExistingStock6x1() + apeImplementation.getExistingStock6x1(),
                usImplementation.getExistingStock6x2() + apeImplementation.getExistingStock6x2(),
                usImplementation.getExistingStock6x3() + apeImplementation.getExistingStock6x3(),
                usImplementation.getExistingStock6x4() + apeImplementation.getExistingStock6x4());

    }

    public Observable<MalariaProgram> onSaveForm(final MalariaProgramStatus status) {
        return Observable.create(new Observable.OnSubscribe<MalariaProgram>() {
            @Override
            public void call(Subscriber<? super MalariaProgram> subscriber) {
                try {
                    MalariaProgram malariaProgram = getMalariaProgram();
                    malariaProgram.setStatus(status);
                    patientDataService.save(malariaProgram);
                    subscriber.onNext(malariaProgram);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public void deleteDraft() {

    }

    @Override
    public boolean isDraft() {
        return false;
    }

    @Override
    protected void addSignature(String signature) {

    }
}