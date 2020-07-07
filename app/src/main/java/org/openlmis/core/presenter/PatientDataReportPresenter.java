package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.enums.VIAReportType;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.ViaReportStatus;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.repository.MalariaProgramRepository;
import org.openlmis.core.model.repository.PTVProgramRepository;
import org.openlmis.core.service.PatientDataService;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class PatientDataReportPresenter extends Presenter {

    @Inject
    private PatientDataService patientDataService;

    @Inject
    private MalariaProgramRepository malariaProgramRepository;

    @Inject
    private PTVProgramRepository ptvProgramRepository;

    @Override
    public void attachView(BaseView v) {

    }

    public Observable<List<PatientDataReportViewModel>> getViewModels(final VIAReportType reportType) {
        return Observable.create((Observable.OnSubscribe<List<PatientDataReportViewModel>>) subscriber -> {
            try {
                List<PatientDataReportViewModel> viewModels = generateViewModelsForAvailablePeriods(reportType);
                subscriber.onNext(viewModels);
                subscriber.onCompleted();
            } catch (LMISException e) {
                subscriber.onError(e);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    public List<PatientDataReportViewModel> generateViewModelsForAvailablePeriods(VIAReportType reportType) throws LMISException {
        List<PatientDataReportViewModel> results = newArrayList();
        List<Period> periods = patientDataService.calculatePeriods(reportType);
        if (reportType.equals(VIAReportType.MALARIA)) {
            buildMalariaViewModels(results, periods);
        } else {
            buildPTVViewModels(results, periods);
        }
        return results;
    }

    private void buildPTVViewModels(List<PatientDataReportViewModel> results, List<Period> periods) throws LMISException {
        List<PTVProgram> ptvPrograms = ptvProgramRepository.getAll();
        for (Period period : periods) {
            Optional<PTVProgram> ptvProgramOptional = findPTVProgramForPeriod(ptvPrograms, period);
            if (ptvProgramOptional.isPresent()) {
                results.add(new PatientDataReportViewModel(period, new DateTime(ptvProgramOptional.get().getCreatedAt()), ptvProgramOptional.get().getStatus()));
            } else {
                results.add(new PatientDataReportViewModel(period, null, ViaReportStatus.MISSING));
            }
        }
    }

    private void buildMalariaViewModels(List<PatientDataReportViewModel> results, List<Period> periods) throws LMISException {
        List<MalariaProgram> malariaPrograms = malariaProgramRepository.getAll();
        for (Period period : periods) {
            Optional<MalariaProgram> malariaProgramOptional = findMalariaProgramForPeriod(malariaPrograms, period);
            if (malariaProgramOptional.isPresent()) {
                results.add(new PatientDataReportViewModel(period, new DateTime(malariaProgramOptional.get().getCreatedAt()), malariaProgramOptional.get().getStatus()));
            } else {
                results.add(new PatientDataReportViewModel(period, null, ViaReportStatus.MISSING));
            }
        }
    }

    private Optional<MalariaProgram> findMalariaProgramForPeriod(List<MalariaProgram> malariaPrograms, final Period period) {
        return FluentIterable.from(malariaPrograms).firstMatch(malariaProgram -> malariaProgram.getStartPeriodDate().equals(period.getBegin())
                && malariaProgram.getEndPeriodDate().equals(period.getEnd()));
    }

    private Optional<PTVProgram> findPTVProgramForPeriod(List<PTVProgram> ptvPrograms, final Period period) {
        return FluentIterable.from(ptvPrograms).firstMatch(ptvProgram -> ptvProgram.getStartPeriod().equals(period.getBegin().toDate())
                && ptvProgram.getEndPeriod().equals(period.getEnd().toDate()));
    }
}
