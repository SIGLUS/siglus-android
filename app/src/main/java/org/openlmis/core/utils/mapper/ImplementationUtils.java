package org.openlmis.core.utils.mapper;

import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.Treatment;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;

import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x1_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x2_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x3_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x4_CODE;

public class ImplementationUtils {

    public static final String CURRENT_TREATMENT_SETTER = "setCurrentTreatment";
    public static final String EXISTING_STOCK_SETTER = "setExistingStock";
    private static HashMap<String, String> productMapped;

    private static HashMap<String, String> producMap() {
        if (productMapped == null) {
            productMapped = new HashMap<>(4);
            productMapped.put("6x1", PRODUCT_6x1_CODE.getValue());
            productMapped.put("6x2", PRODUCT_6x2_CODE.getValue());
            productMapped.put("6x3", PRODUCT_6x3_CODE.getValue());
            productMapped.put("6x4", PRODUCT_6x4_CODE.getValue());
        }
        return productMapped;
    }

    public static void mapForProduct(Implementation implementation, String productName,
                                     ImplementationReportViewModel report) throws
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Treatment treatment = findTreatmentForProduct(implementation.getTreatments(), producMap().get(productName));
        if (treatment == null) {
            return;
        }
        Method setCurrentTreatment = report.getClass().getMethod(CURRENT_TREATMENT_SETTER + productName, long.class);
        Method setExistingStock = report.getClass().getMethod(EXISTING_STOCK_SETTER + productName, long.class);
        setCurrentTreatment.invoke(report, treatment.getAmount());
        setExistingStock.invoke(report, treatment.getStock());
    }


    public static Treatment findTreatmentForProduct(Collection<Treatment> treatments, String productName) {
        for (Treatment treatment : treatments) {
            if (treatment.getProduct().getCode().equals(productName)) {
                return treatment;
            }
        }
        return null;
    }

    public static Implementation findImplementationForExecutor(Collection<Implementation> implementations, String executor) {
        for (Implementation implementation : implementations) {
            if (implementation.isExecutor(executor)) {
                return implementation;
            }
        }
        return null;
    }
}
