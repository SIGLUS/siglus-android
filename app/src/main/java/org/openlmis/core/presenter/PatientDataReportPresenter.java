package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.MalariaProgramStatus;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.repository.MalariaProgramRepository;
import org.openlmis.core.service.PatientDataService;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.malaria.PatientDataReportViewModel;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class PatientDataReportPresenter extends Presenter {

    @Inject
    private PatientDataService patientDataService;

    @Inject
    private MalariaProgramRepository malariaProgramRepository;

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {

    }

    public Observable<List<PatientDataReportViewModel>> getViewModels() {
        return Observable.create(new Observable.OnSubscribe<List<PatientDataReportViewModel>>() {
            @Override
            public void call(Subscriber<? super List<PatientDataReportViewModel>> subscriber) {
                try {
                    List<PatientDataReportViewModel> viewModels = generateViewModelsForAvailablePeriods();
                    subscriber.onNext(viewModels);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    public List<PatientDataReportViewModel> generateViewModelsForAvailablePeriods() throws LMISException {
        List<PatientDataReportViewModel> results = newArrayList();
        List<Period> periods = patientDataService.calculatePeriods();
        List<MalariaProgram> malariaPrograms = malariaProgramRepository.getAll();
        for (Period period : periods) {
            Optional<MalariaProgram> malariaProgramOptional = findMalariaProgramForPeriod(malariaPrograms, period);
            if (malariaProgramOptional.isPresent()) {
                results.add(new PatientDataReportViewModel(period, malariaProgramOptional.get().getReportedDate(), malariaProgramOptional.get().getStatus()));
            } else {
                results.add(new PatientDataReportViewModel(period, null, MalariaProgramStatus.MISSING));
            }
        }
        return results;
    }

    private Optional<MalariaProgram> findMalariaProgramForPeriod(List<MalariaProgram> malariaPrograms, final Period period) {
        return FluentIterable.from(malariaPrograms).firstMatch(new Predicate<MalariaProgram>() {
            @Override
            public boolean apply(MalariaProgram malariaProgram) {
                return malariaProgram.getStartPeriodDate().equals(period.getBegin()) &&
                        malariaProgram.getEndPeriodDate().equals(period.getEnd());
            }
        });
    }
}
