package org.openlmis.core.model.service;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.roboguice.shaded.goole.common.base.Optional;

public class ProgramDataFormPeriodService {
    @Inject
    private ReportTypeFormRepository reportTypeFormRepository;

    private String programCode = "TEST_KIT";

    public Optional<Period> getFirstStandardPeriod() throws LMISException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        ReportTypeForm reportTypeForm = reportTypeFormRepository.queryByCode(programCode);
        DateTime lastReportEndTime = dateTimeFormatter.parseDateTime(reportTypeForm.lastReportEndTime);
        DateTime initializeDateTime;
        if (Months.monthsBetween(lastReportEndTime, new DateTime()).getMonths() > 12){
            initializeDateTime = new DateTime().plusMonths(-12).toDateTime();
        }else {
            initializeDateTime = lastReportEndTime.plusMonths(1).toDateTime();
        }
        Period period = new Period(initializeDateTime);
        if (period != null && period.isOpenToRequisitions()) {
            return Optional.of(period);
        }
        return Optional.absent();
    }
}
