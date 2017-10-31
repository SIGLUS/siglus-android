package org.openlmis.core.helpers;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;

import org.openlmis.core.view.viewmodel.malaria.ImplementationReportType;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;

import static com.natpryce.makeiteasy.Property.newProperty;
import static org.apache.commons.lang.math.RandomUtils.nextLong;

public class ImplementationReportBuilder {
    public static final Property<ImplementationReportViewModel, ImplementationReportType> type = newProperty();
    public static final Property<ImplementationReportViewModel, Long> currentTreatment6x1 = newProperty();
    public static final Property<ImplementationReportViewModel, Long> currentTreatment6x2 = newProperty();
    public static final Property<ImplementationReportViewModel, Long> currentTreatment6x3 = newProperty();
    public static final Property<ImplementationReportViewModel, Long> currentTreatment6x4 = newProperty();
    public static final Property<ImplementationReportViewModel, Long> existingStock6x1 = newProperty();
    public static final Property<ImplementationReportViewModel, Long> existingStock6x2 = newProperty();
    public static final Property<ImplementationReportViewModel, Long> existingStock6x3 = newProperty();
    public static final Property<ImplementationReportViewModel, Long> existingStock6x4 = newProperty();

    public static final Instantiator<ImplementationReportViewModel> randomImplementationReport = new Instantiator<ImplementationReportViewModel>() {
        @Override
        public ImplementationReportViewModel instantiate(PropertyLookup<ImplementationReportViewModel> lookup) {
            return new ImplementationReportViewModel(
                    lookup.valueOf(type, ImplementationReportType.US),
                    lookup.valueOf(currentTreatment6x1, nextLong()),
                    lookup.valueOf(currentTreatment6x2, nextLong()),
                    lookup.valueOf(currentTreatment6x3, nextLong()),
                    lookup.valueOf(currentTreatment6x4, nextLong()),
                    lookup.valueOf(existingStock6x1, nextLong()),
                    lookup.valueOf(existingStock6x2, nextLong()),
                    lookup.valueOf(existingStock6x3, nextLong()),
                    lookup.valueOf(existingStock6x4, nextLong()));
        }
    };
}
