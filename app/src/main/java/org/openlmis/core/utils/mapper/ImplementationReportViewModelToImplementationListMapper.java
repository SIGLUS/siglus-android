package org.openlmis.core.utils.mapper;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.Treatment;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.utils.MalariaExecutors;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportType;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static org.openlmis.core.utils.MalariaExecutors.APE;
import static org.openlmis.core.utils.MalariaExecutors.US;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x1_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x2_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x3_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x4_CODE;
import static org.openlmis.core.utils.mapper.ImplementationUtils.findImplementationForExecutor;
import static org.openlmis.core.utils.mapper.ImplementationUtils.findTreatmentForProduct;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class ImplementationReportViewModelToImplementationListMapper {

    public static final String CURRENT_TREATMENT_METHOD = "getCurrentTreatment";
    public static final String EXISTING_STOCK_METHOD = "getExistingStock";
    @Inject
    private ProductRepository productRepository;

    private HashMap<String, String> productPartialCodes;

    public ImplementationReportViewModelToImplementationListMapper() {
        productPartialCodes = new HashMap<>();
        productPartialCodes.put("6x1", PRODUCT_6x1_CODE.getValue());
        productPartialCodes.put("6x2", PRODUCT_6x2_CODE.getValue());
        productPartialCodes.put("6x3", PRODUCT_6x3_CODE.getValue());
        productPartialCodes.put("6x4", PRODUCT_6x4_CODE.getValue());
    }

    public Collection<Implementation> map(ImplementationReportViewModel usReportModel, ImplementationReportViewModel apeReportModel) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, LMISException {
        Implementation usImplementation = MapTreatments(usReportModel);
        Implementation apeImplementation = MapTreatments(apeReportModel);
        return newArrayList(usImplementation, apeImplementation);
    }

    public Collection<Implementation> map(ImplementationReportViewModel sourceReport, Collection<Implementation> destinationImplementations) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (sourceReport.getType() == ImplementationReportType.US) {
            Implementation usImplementation = findImplementationForExecutor(destinationImplementations, US.name());
            MapTreatmentsToImplementation(sourceReport, usImplementation);
        } else {
            Implementation apeImplementation = findImplementationForExecutor(destinationImplementations, APE.name());
            MapTreatmentsToImplementation(sourceReport, apeImplementation);
        }
        return destinationImplementations;
    }

    private Implementation MapTreatments(ImplementationReportViewModel sourceReport) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, LMISException {
        List<Treatment> results = new ArrayList<>(4);
        for (String productCode : productPartialCodes.keySet()) {
            Method getCurrentTreatment = ImplementationReportViewModel.class.getMethod(CURRENT_TREATMENT_METHOD + productCode);
            Method getExistingStock = ImplementationReportViewModel.class.getMethod(EXISTING_STOCK_METHOD + productCode);
            results.add(new Treatment(productRepository.getByCode(productPartialCodes.get(productCode)),
                    (Long) getCurrentTreatment.invoke(sourceReport),
                    (Long) getExistingStock.invoke(sourceReport)));
        }
        MalariaExecutors executor = sourceReport.getType() == ImplementationReportType.APE ? MalariaExecutors.APE : MalariaExecutors.US;
        return new Implementation(executor.name(), results);
    }

    private void MapTreatmentsToImplementation(ImplementationReportViewModel sourceReport, Implementation implementation) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (String productCode : productPartialCodes.keySet()) {
            Treatment treatmentForProduct = findTreatmentForProduct(implementation.getTreatments(), productPartialCodes.get(productCode));
            if (treatmentForProduct != null) {
                Method getCurrentTreatment = ImplementationReportViewModel.class.getMethod(CURRENT_TREATMENT_METHOD + productCode);
                Method getExistingStock = ImplementationReportViewModel.class.getMethod(EXISTING_STOCK_METHOD + productCode);
                treatmentForProduct.setAmount((Long) getCurrentTreatment.invoke(sourceReport));
                treatmentForProduct.setStock((Long) getExistingStock.invoke(sourceReport));
            }
        }
    }
}
