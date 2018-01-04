package org.openlmis.core.utils;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.openlmis.core.model.HealthFacilityService;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.ViaReportStatus;
import org.openlmis.core.model.PatientDispensation;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ServiceDispensation;
import org.openlmis.core.view.viewmodel.ptv.PTVViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public final class PTVUtil {


    public static final String SERVICE_NAME = "Maternity";

    private PTVUtil() {
    }

    public static long[] getRandomQuantitiesForPTVViewModels() {
        long[] quantities = new long[5];
        for (int i = 0; i < quantities.length; i++) {
            quantities[i] = 1 + new Random().nextInt(9);
        }
        return quantities;
    }

    public static PTVProgram createDummyPTVProgram(Period period) {
        DateTime today = DateTime.now();
        Date startPeriod = period.getBegin().toDate();
        Date endPeriod = period.getEnd().toDate();
        PTVProgram ptvProgramExpected = new PTVProgram();
        ptvProgramExpected.setStartPeriod(startPeriod);
        ptvProgramExpected.setEndPeriod(endPeriod);
        ptvProgramExpected.setCreatedBy("");
        ptvProgramExpected.setVerifiedBy("MZ");
        ptvProgramExpected.setStatus(ViaReportStatus.MISSING);
        ptvProgramExpected.setCreatedAt(today.toDate());
        ptvProgramExpected.setUpdatedAt(today.toDate());
        return ptvProgramExpected;
    }

    @NonNull
    public static ArrayList<HealthFacilityService> createDummyHealthFacilityServices() {
        ArrayList<HealthFacilityService> expectedHealthFacilityServices = new ArrayList<>();
        expectedHealthFacilityServices.add(getHealthFacilityService(1, "CPN"));
        expectedHealthFacilityServices.add(getHealthFacilityService(2, "Maternity"));
        expectedHealthFacilityServices.add(getHealthFacilityService(3, "CCR"));
        expectedHealthFacilityServices.add(getHealthFacilityService(4, "Pharmacy"));
        expectedHealthFacilityServices.add(getHealthFacilityService(5, "UATS"));
        expectedHealthFacilityServices.add(getHealthFacilityService(6, "Banco de socorro"));
        expectedHealthFacilityServices.add(getHealthFacilityService(7, "Lab"));
        expectedHealthFacilityServices.add(getHealthFacilityService(8, "Estomatologia"));
        return expectedHealthFacilityServices;
    }

    @NonNull
    private static HealthFacilityService getHealthFacilityService(int id, String name) {
        HealthFacilityService healthFacilityService = new HealthFacilityService();
        healthFacilityService.setId(id);
        healthFacilityService.setName(name);
        return healthFacilityService;
    }

    public static List<ServiceDispensation> createDummyServiceDispensations(PTVProgramStockInformation ptvProgramStockInformation) {
        List<ServiceDispensation> serviceDispensations = new ArrayList<>();
        for (HealthFacilityService facilityService : createDummyHealthFacilityServices()) {
            ServiceDispensation serviceDispensation = new ServiceDispensation();
            serviceDispensation.setHealthFacilityService(facilityService);
            serviceDispensation.setPtvProgramStockInformation(ptvProgramStockInformation);
            serviceDispensations.add(serviceDispensation);
        }
        return serviceDispensations;
    }

    public static List<PatientDispensation> createDummyPatientDispensations() {
        List<PatientDispensation> patientDispensations = new ArrayList<>();
        PatientDispensation patientDispensationChild = new PatientDispensation();
        patientDispensationChild.setType(PatientDispensation.Type.CHILD);
        patientDispensationChild.setTotal(new Random().nextLong());
        PatientDispensation patientDispensationWoman = new PatientDispensation();
        patientDispensationWoman.setType(PatientDispensation.Type.WOMAN);
        patientDispensationWoman.setTotal(new Random().nextLong());
        patientDispensations.add(patientDispensationChild);
        patientDispensations.add(patientDispensationWoman);
        return patientDispensations;
    }

    public static List<PTVProgramStockInformation> createDummyPTVProgramStocksInformation() {
        List<PTVProgramStockInformation> ptvProgramStocksInformation = new ArrayList<>();
        List<Product> products = getProductsWithPTVProductCodes();
        for (int i = 0; i < products.size(); i++) {
            PTVProgramStockInformation ptvProgramStockInformation = new PTVProgramStockInformation();
            ptvProgramStockInformation.setEntries(new Random().nextLong());
            ptvProgramStockInformation.setInitialStock(new Random().nextLong());
            ptvProgramStockInformation.setLossesAndAdjustments(new Random().nextLong());
            ptvProgramStockInformation.setRequisition(new Random().nextLong());
            ptvProgramStockInformation.setServiceDispensations(createDummyServiceDispensations(ptvProgramStockInformation));
            ptvProgramStocksInformation.add(ptvProgramStockInformation);
            ptvProgramStockInformation.setProduct(products.get(i));
        }
        return ptvProgramStocksInformation;
    }

    public static List<String> ptvProductCodes = newArrayList(Constants.PTV_PRODUCT_FIRST_CODE, Constants.PTV_PRODUCT_SECOND_CODE,
            Constants.PTV_PRODUCT_THIRD_CODE, Constants.PTV_PRODUCT_FOURTH_CODE, Constants.PTV_PRODUCT_FIFTH_CODE);

    public static List<Product> getProductsWithPTVProductCodes() {
        List<Product> expectedProducts = new ArrayList<>();
        for (String code : ptvProductCodes) {
            Product product = Product.dummyProduct();
            product.setId(ptvProductCodes.indexOf(code));
            product.setCode(code);
            expectedProducts.add(product);
        }
        return expectedProducts;
    }

    @NonNull
    public static List<PTVViewModel> getPtvViewModels(long[] quantities) {
        List<PTVViewModel> ptvViewModels = new ArrayList<>();
        ptvViewModels.add(createPTVViewModel(SERVICE_NAME, quantities));
        ptvViewModels.add(createPTVViewModel(Constants.REQUISITIONS, quantities));
        ptvViewModels.add(createPTVViewModel(Constants.LOSSES_AND_ADJUSTMENTS, quantities));
        return ptvViewModels;
    }

    private static PTVViewModel createPTVViewModel(String name, long[] quantities) {
        PTVViewModel ptvViewModel = new PTVViewModel(name);
        ptvViewModel.setQuantity(1, quantities[0]);
        ptvViewModel.setQuantity(2, quantities[1]);
        ptvViewModel.setQuantity(3, quantities[2]);
        ptvViewModel.setQuantity(4, quantities[3]);
        ptvViewModel.setQuantity(5, quantities[4]);
        return ptvViewModel;
    }

}
