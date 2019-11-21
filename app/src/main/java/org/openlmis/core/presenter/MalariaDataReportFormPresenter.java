package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ViaReportStatus;
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

import lombok.Setter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MalariaDataReportFormPresenter extends BaseReportPresenter {

    public static final int INVALID_INDEX = -1;
    @Inject
    MalariaProgramToMalariaDataReportViewModelMapper reportViewModelMapper;

    @Inject
    MalariaDataReportViewModelToMalariaProgramMapper malariaProgramMapper;

    @Inject
    PatientDataService patientDataService;

    private Period period;
    private MalariaProgram malariaProgram;
    private List<ImplementationReportViewModel> viewModels;
    @Setter
    private String createdBy;
    @Setter
    private ViaReportStatus status;

    public MalariaDataReportFormPresenter() {
        this.malariaProgram = new MalariaProgram();
        this.viewModels = new ArrayList<>();
    }

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
    }

    public Observable<List<ImplementationReportViewModel>> getImplementationViewModelsForCurrentMalariaProgram(final Period period) {
        this.period = period;
        return Observable.create(new Observable.OnSubscribe<List<ImplementationReportViewModel>>() {
            @Override
            public void call(final Subscriber<? super List<ImplementationReportViewModel>> subscriber) {
                try {
                    malariaProgram = patientDataService.findForPeriod(period.getBegin(), period.getEnd());
                    MalariaDataReportViewModel malariaDataReportViewModel = reportViewModelMapper.Map(malariaProgram);
                    ImplementationReportViewModel usImplementationReportViewModel = malariaDataReportViewModel.getUsImplementationReportViewModel();
                    if (malariaProgram != null) {
                        createdBy = malariaProgram.getCreatedBy();
                        status = malariaProgram.getStatus();
                    } else {
                        createdBy = "";
                        status = ViaReportStatus.MISSING;
                        List<Long> malariaProductsStockHand = patientDataService.getMalariaProductsStockHand();
                        usImplementationReportViewModel.setExistingStock(malariaProductsStockHand);
                    }
                    viewModels.add(usImplementationReportViewModel);
                    viewModels.add(malariaDataReportViewModel.getApeImplementationReportViewModel());
                    viewModels.add(generateTotalViewModel(viewModels.get(0), viewModels.get(1)));
                    subscriber.onNext(viewModels);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    new LMISException(e).reportToFabric();
                    subscriber.onError(e);
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

    public Observable<MalariaProgram> onSaveForm(final ViaReportStatus status, final String sign) {
        return Observable.create(new Observable.OnSubscribe<MalariaProgram>() {
            @Override
            public void call(Subscriber<? super MalariaProgram> subscriber) {
                try {
                    MalariaProgram malariaProgram = getMalariaProgram();
                    malariaProgram.setStatus(status);
                    if (status.equals(ViaReportStatus.SUBMITTED)) {
                        malariaProgram.setVerifiedBy(sign);
                    } else {
                        malariaProgram.setCreatedBy(sign);
                    }
                    patientDataService.save(malariaProgram);
                    subscriber.onNext(malariaProgram);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                    new LMISException(e).reportToFabric();

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
        MalariaProgram malariaProgram = null;
        try {
            malariaProgram = getMalariaProgram();
            malariaProgram.setStatus(status);
            if (status.equals(ViaReportStatus.SUBMITTED)) {
                malariaProgram.setVerifiedBy(signature);
            } else {
                malariaProgram.setCreatedBy(signature);
            }
            patientDataService.save(malariaProgram);
        } catch (Exception e) {
            String str = e.getMessage();
            (new LMISException("MalariaData:" + str)).reportToFabric();
        }
    }

    public boolean isSubmittedForApproval() {
        return createdBy != null
                && (status.equals(ViaReportStatus.DRAFT) || status.equals(ViaReportStatus.MISSING))
                && createdBy.isEmpty();
    }
}