package org.openlmis.core.service;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PatientDataReport;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.repository.PatientDataRepository;
import org.openlmis.core.persistence.GenericDao;
import org.roboguice.shaded.goole.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

public class PatientDataService {

    @Inject
    PatientDataRepository patientDataRepository;

    public List<Period> calculatePeriods() throws LMISException {
        Optional<PatientDataReport> patientDataReport = patientDataRepository.getFirstMovement();
        List<Period> periods = new ArrayList<>();
        Optional<Period> period = calculateFirstAvailablePeriod(patientDataReport);
        while (period.isPresent()) {
            periods.add(period.get());
            period = period.get().generateNextAvailablePeriod();
        }
        return periods;
    }

    private Optional<Period> calculateFirstAvailablePeriod(Optional<PatientDataReport> patientDataReport) {
        DateTime today = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
        Optional<Period> period;
        if (patientDataReport.isPresent()) {
            period = Optional.of(new Period(patientDataReport.get().getReportedDate()));
        } else {
            period = Optional.of(new Period(today));
        }
        if (period.get().isOpenToRequisitions()) {
            return period;
        }
        return Optional.absent();
    }
}
