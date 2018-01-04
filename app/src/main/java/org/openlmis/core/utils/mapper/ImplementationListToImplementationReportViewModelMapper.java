package org.openlmis.core.utils.mapper;

import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.ViaReportStatus;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportType;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import static org.openlmis.core.utils.MalariaExecutors.APE;
import static org.openlmis.core.utils.MalariaExecutors.US;
import static org.openlmis.core.utils.mapper.ImplementationUtils.findImplementationForExecutor;
import static org.openlmis.core.utils.mapper.ImplementationUtils.mapForProduct;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class ImplementationListToImplementationReportViewModelMapper {
    private static final String PRODUCT_6X1 = "6x1";
    private static final String PRODUCT_6X2 = "6x2";
    private static final String PRODUCT_6X3 = "6x3";
    private static final String PRODUCT_6X4 = "6x4";
    private final List<String> products = newArrayList(PRODUCT_6X1, PRODUCT_6X2, PRODUCT_6X3, PRODUCT_6X4);

    public ImplementationReportViewModel mapApeImplementations(Collection<Implementation> implementations, ViaReportStatus status) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Implementation implementation = findImplementationForExecutor(implementations, APE.name());
        ImplementationReportViewModel report = new ImplementationReportViewModel();
        report.setType(ImplementationReportType.APE);
        report.setStatus(status);
        if (implementation != null) {
            for (String product : products) {
                mapForProduct(implementation, product, report);
            }
        }
        return report;
    }

    public ImplementationReportViewModel mapUsImplementations(Collection<Implementation> implementations, ViaReportStatus status) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Implementation implementation = findImplementationForExecutor(implementations, US.name());
        ImplementationReportViewModel report = new ImplementationReportViewModel();
        report.setType(ImplementationReportType.US);
        report.setStatus(status);
        if (implementation != null) {
            for (String product : products) {
                mapForProduct(implementation, product, report);
            }
        }
        return report;
    }
}

