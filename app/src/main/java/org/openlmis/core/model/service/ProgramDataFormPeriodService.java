package org.openlmis.core.model.service;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.utils.Constants;
import org.roboguice.shaded.goole.common.base.Optional;

import java.util.List;

public class ProgramDataFormPeriodService {
    @Inject
    private ReportTypeFormRepository reportTypeFormRepository;

    @Inject
    private ProgramDataFormRepository programDataFormRepository;

    @Inject
    private StockMovementRepository stockMovementRepository;

    public Optional<Period> getFirstStandardPeriod() throws LMISException {
        DateTime initializeDateTime;
        Period period = null;
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        ReportTypeForm reportTypeForm = reportTypeFormRepository.queryByCode(Constants.TEST_KIT_PROGRAM_CODE);

        if (reportTypeForm.lastReportEndTime != null) {
            DateTime lastReportEndTime = dateTimeFormatter.parseDateTime(reportTypeForm.lastReportEndTime);
            if (Months.monthsBetween(lastReportEndTime, new DateTime()).getMonths() > 12) {
                initializeDateTime = new DateTime().plusMonths(-12).toDateTime();
                period = new Period(initializeDateTime);
            } else {
                List<ProgramDataForm> forms = programDataFormRepository.listByProgramCode(Constants.TEST_KIT_PROGRAM_CODE);
                if (forms != null && !forms.isEmpty()) {
                    period = new Period(forms.get(0).getPeriodBegin());
                } else {
                    StockMovementItem firstStockMovement = stockMovementRepository.getFirstStockMovement();
                    if (firstStockMovement != null) {
                        period = firstStockMovement.getMovementPeriod();
                    }
                }
            }
        } else {
            StockMovementItem firstStockMovement = stockMovementRepository.getFirstStockMovement();
            if (firstStockMovement != null) {
                period = firstStockMovement.getMovementPeriod();
            }
        }
        if (period != null && period.isOpenToRequisitions()) {
            return Optional.of(period);
        }
        return Optional.absent();
    }
}
