package org.openlmis.core.helpers;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;

import org.joda.time.DateTime;
import org.openlmis.core.model.PatientDataProgramStatus;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;
import org.openlmis.core.view.viewmodel.malaria.MalariaDataReportViewModel;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.Property.newProperty;
import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.joda.time.DateTime.now;
import static org.openlmis.core.helpers.ImplementationReportBuilder.randomImplementationReport;

public class MalariaDataReportBuilder {
    public static final Property<MalariaDataReportViewModel, DateTime> reportedDate = newProperty();
    public static final Property<MalariaDataReportViewModel, DateTime> startPeriodDate = newProperty();
    public static final Property<MalariaDataReportViewModel, DateTime> endPeriodDate = newProperty();
    public static final Property<MalariaDataReportViewModel, ImplementationReportViewModel> usImplementations = newProperty();
    public static final Property<MalariaDataReportViewModel, ImplementationReportViewModel> apeImplementations = newProperty();

    public static final Instantiator<MalariaDataReportViewModel> randomMalariaDataReport = new Instantiator<MalariaDataReportViewModel>() {
        @Override
        public MalariaDataReportViewModel instantiate(PropertyLookup<MalariaDataReportViewModel> lookup) {
            DateTime today = now();
            PatientDataProgramStatus[] statuses = PatientDataProgramStatus.values();
            return new MalariaDataReportViewModel(
                    lookup.valueOf(reportedDate, today),
                    lookup.valueOf(startPeriodDate, today.minusDays(nextInt(10))),
                    lookup.valueOf(endPeriodDate, today.minusDays(nextInt(10))),
                    lookup.valueOf(usImplementations, make(a(randomImplementationReport))),
                    lookup.valueOf(apeImplementations,make(a(randomImplementationReport)))
            );
        }
    };
}
