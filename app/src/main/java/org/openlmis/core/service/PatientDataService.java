package org.openlmis.core.service;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.MalariaProgramRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.roboguice.shaded.goole.common.base.Optional;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x1_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x2_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x3_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x4_CODE;

public class PatientDataService {
    private static final long NO_STOCK_ON_HAND_VALUE = 0;

    @Inject
    MalariaProgramRepository malariaProgramRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    StockRepository stockRepository;

    public List<Period> calculatePeriods() {
        List<Period> periods = new ArrayList<>();
        Optional<MalariaProgram> malariaProgramOptional;
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

    public boolean save(MalariaProgram malariaProgram) {
        try {
            Optional<MalariaProgram> malariaProgramSaved = malariaProgramRepository.save(malariaProgram);
            return malariaProgramSaved.isPresent();
        } catch (LMISException e) {
            e.reportToFabric();
            return false;
        }
    }

    public MalariaProgram findForPeriod(DateTime beginDate, DateTime endDate) throws LMISException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return  malariaProgramRepository.getPatientDataReportByPeriodAndType(beginDate, endDate);
    }

    public List<Product> getMalariaProducts() {
        List<Product> malariaProducts = new ArrayList<>();
        try {
            malariaProducts.add(productRepository.getByCode(PRODUCT_6x1_CODE.getValue()));
            malariaProducts.add(productRepository.getByCode(PRODUCT_6x2_CODE.getValue()));
            malariaProducts.add(productRepository.getByCode(PRODUCT_6x3_CODE.getValue()));
            malariaProducts.add(productRepository.getByCode(PRODUCT_6x4_CODE.getValue()));
        } catch (LMISException e) {
            e.printStackTrace();
        }
        return malariaProducts;
    }
}
