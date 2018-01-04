package org.openlmis.core.presenter;

import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.openlmis.core.builders.PTVProgramBuilder;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PatientDataProgramStatus;
import org.openlmis.core.model.PatientDispensation;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.repository.HealthFacilityServiceRepository;
import org.openlmis.core.model.repository.PTVProgramRepository;
import org.openlmis.core.utils.mapper.PTVProgramToPTVViewModelMapper;
import org.openlmis.core.utils.mapper.PTVViewModelToPTVProgramMapper;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.ptv.PTVViewModel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PtvProgramPresenter extends Presenter {

    @Inject
    private PTVProgramBuilder ptvProgramBuilder;

    @Inject
    private PTVProgramRepository ptvProgramRepository;

    @Inject
    private HealthFacilityServiceRepository healthFacilityServiceRepository;

    @Inject
    private PTVProgramToPTVViewModelMapper ptvProgramToPtvViewModelMapper;

    @Inject
    private PTVViewModelToPTVProgramMapper ptvViewModelToPTVProgramMapper;

    @Setter
    private Period period;

    private PTVProgram ptvProgram;

    @Getter
    @Setter
    private List<PTVViewModel> viewModels;

    public PtvProgramPresenter() {
    }

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {

    }

    public Observable<PTVProgram> buildInitialPtvProgram() {
        return Observable.create(new Observable.OnSubscribe<PTVProgram>() {
            @Override
            public void call(Subscriber<? super PTVProgram> subscriber) {
                try {
                    if (period == null) {
                        throw new LMISException("Period cannot be null");
                    }
                    createPTVProgram();
                    buildPlaceholderRows();
                    subscriber.onNext(ptvProgram);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private void createPTVProgram() throws LMISException {
        ptvProgram = ptvProgramRepository.getByPeriod(period);
        if (ptvProgram == null) {
            ptvProgram = ptvProgramBuilder.buildInitialPTVProgram(period);
        } else {
            ptvProgram = ptvProgramBuilder.buildExistentPTVProgram(ptvProgram);
        }
    }

    public Observable<PTVProgram> savePTVProgram(final boolean isCompleted) {
        return Observable.create(new Observable.OnSubscribe<PTVProgram>() {
            @Override
            public void call(Subscriber<? super PTVProgram> subscriber) {
                try {
                    if (ptvProgram == null) {
                        throw new LMISException("PTV Program cannot be null");
                    }
                    setPTVProgramStatus(isCompleted);
                    PTVProgram ptvProgramSaved = ptvProgramRepository.save(ptvProgram);
                    subscriber.onNext(ptvProgramSaved);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                } catch (SQLException e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private void setPTVProgramStatus(boolean isCompleted) {
        if (isCompleted) {
            ptvProgram.setStatus(PatientDataProgramStatus.SUBMITTED);
        } else {
            ptvProgram.setStatus(PatientDataProgramStatus.DRAFT);
        }
    }

    public PTVProgram updatePTVProgram(String totalWoman, String totalChild) {
        ptvProgram = ptvViewModelToPTVProgramMapper.convertToPTVProgram(viewModels, ptvProgram);
        List<PatientDispensation> patientDispensations = updatePatientDispensations(totalWoman, totalChild);
        ptvProgram.setPatientDispensations(patientDispensations);
        return ptvProgram;
    }

    @NonNull
    private List<PatientDispensation> updatePatientDispensations(String totalWoman, String totalChild) {
        List<PatientDispensation> patientDispensations = new ArrayList<>(ptvProgram.getPatientDispensations());
        for (PatientDispensation patientDispensation : patientDispensations) {
            if (patientDispensation.getType().equals(PatientDispensation.Type.CHILD)) {
                if (!totalChild.isEmpty()) {
                    patientDispensation.setTotal(Long.valueOf(totalChild));
                }
            }
            if (patientDispensation.getType().equals(PatientDispensation.Type.WOMAN)) {
                if (!totalWoman.isEmpty()) {
                    patientDispensation.setTotal(Long.valueOf(totalWoman));
                }
            }
        }
        return patientDispensations;
    }

    public void buildPlaceholderRows() throws LMISException {
        ptvProgramToPtvViewModelMapper.setPtvProgram(ptvProgram);
        viewModels = ptvProgramToPtvViewModelMapper.buildPlaceholderRows();
    }

    public boolean isNotSubmittedForApproval() {
        return ptvProgram.getCreatedBy() != null && (ptvProgram.getStatus().equals(PatientDataProgramStatus.DRAFT)
                || ptvProgram.getStatus().equals(PatientDataProgramStatus.MISSING)) && ptvProgram.getCreatedBy().isEmpty();
    }

    public Observable<PTVProgram> signReport(String signature, boolean isComplete) {
        ptvProgram.setCreatedBy(signature);
        return savePTVProgram(isComplete);
    }
}
