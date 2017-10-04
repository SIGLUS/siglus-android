package org.openlmis.core.service;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.PatientDataReport;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.MalariaProgramRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.MalariaProgramMapper;
import org.openlmis.core.utils.PatientDataReportMapper;
import org.openlmis.core.view.viewmodel.PatientDataReportViewModel;
import org.roboguice.shaded.goole.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

public class PatientDataService {

    public static final String MALARIA_PRODUCT_CODE_6X1 = "08O05";
    public static final String MALARIA_PRODUCT_CODE_6X2 = "08O05Z";
    public static final String MALARIA_PRODUCT_CODE_6X3 = "08O05X";
    public static final String MALARIA_PRODUCT_CODE_6X4 = "08O05Y";
    private static final long NO_STOCK_ON_HAND_VALUE = 0;

    @Inject
    MalariaProgramRepository malariaProgramRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    StockRepository stockRepository;

    public List<Period> calculatePeriods() {
        List<Period> periods = new ArrayList<>();
        Optional<MalariaProgram> malariaProgramOptional = null;
        try {
            malariaProgramOptional = malariaProgramRepository.getFirstMovement();
            Optional<Period> period = calculateFirstAvailablePeriod(malariaProgramOptional);
            while (period.isPresent()) {
                periods.add(period.get());
                period = period.get().generateNextAvailablePeriod();
            }
        } catch (LMISException e) {
            e.printStackTrace();
        }
        return periods;
    }

    private Optional<Period> calculateFirstAvailablePeriod(Optional<MalariaProgram> malariaProgram) {
        DateTime today = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
        Optional<Period> period;
        if (malariaProgram.isPresent()) {
            period = Optional.of(new Period(malariaProgram.get().getReportedDate()));
        } else {
            period = Optional.of(new Period(today));
        }
        if (period.get().isOpenToRequisitions()) {
            return period;
        }
        return Optional.absent();
    }

    public List<Product> getMalariaProducts() {
        List<Product> malariaProducts = new ArrayList<>();
        try {
            malariaProducts.add(productRepository.getByCode(MALARIA_PRODUCT_CODE_6X1));
            malariaProducts.add(productRepository.getByCode(MALARIA_PRODUCT_CODE_6X2));
            malariaProducts.add(productRepository.getByCode(MALARIA_PRODUCT_CODE_6X3));
            malariaProducts.add(productRepository.getByCode(MALARIA_PRODUCT_CODE_6X4));
        } catch (LMISException e) {
            e.printStackTrace();
        }
        return malariaProducts;
    }

    public List<StockCard> getMalariaProductsStockCards() {
        List<StockCard> malariaProductsStockCards = new ArrayList<>();
        List<Product> malariaProducts = getMalariaProducts();
        try {
            for (Product malariaProduct : malariaProducts) {
                StockCard stockCard = stockRepository.queryStockCardByProductId(malariaProduct.getId());
                malariaProductsStockCards.add(stockCard);
            }
        } catch (LMISException e) {
            e.printStackTrace();
        }
        return malariaProductsStockCards;
    }

    public List<Long> getMalariaProductsStockHand() {
        List<StockCard> malariaProductsStockCard = getMalariaProductsStockCards();
        List<Long> stocks = new ArrayList<>();
        for (StockCard stock : malariaProductsStockCard) {
            if (stock != null) {
                stocks.add(stock.getStockOnHand());
            } else {
                stocks.add(NO_STOCK_ON_HAND_VALUE);
            }
        }
        return stocks;
    }

    public Boolean savePatientDataMovementsPerPeriod(List<PatientDataReportViewModel> patientDataReportViewModels) {
        boolean isSuccessful;
        List<PatientDataReport> patientDataReports = new ArrayList<>();
        for (PatientDataReportViewModel model : patientDataReportViewModels) {
            if (isViewModelFully(model)) {
                return Boolean.FALSE;
            } else {
                PatientDataReport patientDataReport = setPatientDataReportInformation(model);
                patientDataReports.add(patientDataReport);
            }
        }

        PatientDataReportMapper patientDataReportMapper = new PatientDataReportMapper();
        MalariaProgram malariaProgram = patientDataReportMapper.mapToMalariaProgramFromAListOfPatientDataReport(patientDataReports);
        try {
            Optional<MalariaProgram> malariaProgramSaved = malariaProgramRepository.saveMovement(malariaProgram);
            if (malariaProgramSaved.isPresent()) {
                isSuccessful = Boolean.TRUE;
            } else {
                isSuccessful = Boolean.FALSE;
            }
        } catch (LMISException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }

        return isSuccessful;
    }

    private PatientDataReport setPatientDataReportInformation(PatientDataReportViewModel model) {
        PatientDataReport patientDataReport = new PatientDataReport();
        patientDataReport.setType(model.getType());
        patientDataReport.setStartDatePeriod(model.getPeriod().getBegin());
        patientDataReport.setEndDatePeriod(model.getPeriod().getEnd());
        patientDataReport.setCurrentTreatments(model.getCurrentTreatments());
        patientDataReport.setExistingStocks(model.getExistingStock());
        return patientDataReport;
    }


    private boolean isViewModelFully(PatientDataReportViewModel model) {
        return model.getCurrentTreatments().contains(null) || model.getExistingStock().contains(null);
    }

    public PatientDataReport getExistingByModelPerPeriod(DateTime beginDate, DateTime endDate, String type) throws LMISException {
        MalariaProgram malariaProgram = malariaProgramRepository.getPatientDataReportByPeriodAndType(beginDate, endDate);
        if (malariaProgram != null) {
            MalariaProgramMapper malariaProgramMapper = new MalariaProgramMapper();
            List<PatientDataReport> patientDataReports = malariaProgramMapper.mapMalariaProgramToPatientDataReport(malariaProgram);
            for (PatientDataReport patientDataReport : patientDataReports) {
                if (patientDataReport.getType().equals(type)) {
                    return patientDataReport;
                }
            }
        }
        return null;
    }
}
