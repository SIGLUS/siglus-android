package org.openlmis.core.service;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PatientDataReport;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.PatientDataRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.roboguice.shaded.goole.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

public class PatientDataService {

    public static final String MALARIA_PRODUCT_CODE_6X1 = "08O05";
    public static final String MALARIA_PRODUCT_CODE_6X2 = "08O05Z";
    public static final String MALARIA_PRODUCT_CODE_6X3 = "08O05X";
    public static final String MALARIA_PRODUCT_CODE_6X4 = "08O05Y";
    public static final String NO_STOCK_ON_HAND_VALUE = "0";

    @Inject
    PatientDataRepository patientDataRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    StockRepository stockRepository;

    public List<Period> calculatePeriods() {
        List<Period> periods = new ArrayList<>();
        Optional<PatientDataReport> patientDataReport = null;
        try {
            patientDataReport = patientDataRepository.getFirstMovement();
            Optional<Period> period = calculateFirstAvailablePeriod(patientDataReport);
            while (period.isPresent()) {
                periods.add(period.get());
                period = period.get().generateNextAvailablePeriod();
            }
        } catch (LMISException e) {
            e.printStackTrace();
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

    public List<String> getMalariaProductsStockHand() {
        List<StockCard> malariaProductsStockCard = getMalariaProductsStockCards();
        List<String> stocks = new ArrayList<>();
        for (StockCard stock : malariaProductsStockCard) {
            if (stock != null) {
                stocks.add(String.valueOf(stock.getStockOnHand()));
            } else {
                stocks.add(NO_STOCK_ON_HAND_VALUE);
            }
        }
        return stocks;
    }
}
