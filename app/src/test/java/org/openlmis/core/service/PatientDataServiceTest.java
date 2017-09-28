package org.openlmis.core.service;

import android.support.annotation.NonNull;

import com.google.inject.AbstractModule;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PatientDataReport;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.PatientDataRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.PatientDataReportViewModel;
import org.roboguice.shaded.goole.common.base.Optional;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class PatientDataServiceTest {
    private static final String US = "US";
    private static final String APE = "APE";

    public static final String MALARIA_PRODUCT_CODE_6X1 = "08O05";
    public static final String MALARIA_PRODUCT_CODE_6X2 = "08O05Z";
    public static final String MALARIA_PRODUCT_CODE_6X3 = "08O05X";
    public static final String MALARIA_PRODUCT_CODE_6X4 = "08O05Y";

    public static final int CURRENT_MONTH = 1;
    public static final int FIRST_PERIOD_POSITION = 0;
    public static final int REQUISITION_PERIOD_STARTING_DAY = 18;

    public static final int TOTAL_MALARIA_PRODUCTS = 4;
    public static final Long STOCK_ON_HAND_VALUE = Long.valueOf(123);

    public static final int MISSING_TYPE = 1;
    public static final int DRAFT_TYPE = 2;

    public static final int VIEW_MODEL_US_POSITION = 0;
    public static final int VIEW_MODEL_APE_POSITION = 1;
    public static final int COMPLETE_TYPE = 3;
    public static final int SYNCED_TYPE = 4;


    private PatientDataRepository patientDataRepository;
    private ProductRepository productRepository;
    private StockRepository stockRepository;

    private PatientDataService patientDataService;

    private int monthsAfterInitialReportedDate;

    private Product product6x1;
    private Product product6x2;
    private Product product6x3;
    private Product product6x4;

    private StockCard stockCard6x1 = new StockCard();
    private StockCard stockCard6x2 = new StockCard();
    private StockCard stockCard6x3 = new StockCard();
    private StockCard stockCard6x4 = new StockCard();

    @Before
    public void setup() throws LMISException {
        patientDataRepository = mock(PatientDataRepository.class);
        productRepository = mock(ProductRepository.class);
        stockRepository = mock(StockRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        patientDataService = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PatientDataService.class);
        product6x1 = new Product();
        product6x2 = new Product();
        product6x3 = new Product();
        product6x4 = new Product();
        product6x1.setId(1);
        product6x2.setId(2);
        product6x3.setId(3);
        product6x4.setId(4);
        product6x1.setCode(MALARIA_PRODUCT_CODE_6X1);
        product6x2.setCode(MALARIA_PRODUCT_CODE_6X2);
        product6x3.setCode(MALARIA_PRODUCT_CODE_6X3);
        product6x4.setCode(MALARIA_PRODUCT_CODE_6X4);
        when(productRepository.getByCode(MALARIA_PRODUCT_CODE_6X1)).thenReturn(product6x1);
        when(productRepository.getByCode(MALARIA_PRODUCT_CODE_6X2)).thenReturn(product6x2);
        when(productRepository.getByCode(MALARIA_PRODUCT_CODE_6X3)).thenReturn(product6x3);
        when(productRepository.getByCode(MALARIA_PRODUCT_CODE_6X4)).thenReturn(product6x4);
        stockCard6x1 = new StockCard();
        stockCard6x2 = new StockCard();
        stockCard6x3 = new StockCard();
        stockCard6x4 = new StockCard();
        stockCard6x1.setProduct(product6x1);
        stockCard6x2.setProduct(product6x2);
        stockCard6x3.setProduct(product6x3);
        stockCard6x4.setProduct(product6x4);
        stockCard6x1.setStockOnHand(STOCK_ON_HAND_VALUE);
        stockCard6x2.setStockOnHand(STOCK_ON_HAND_VALUE);
        stockCard6x3.setStockOnHand(STOCK_ON_HAND_VALUE);
        stockCard6x4.setStockOnHand(STOCK_ON_HAND_VALUE);
        when(stockRepository.queryStockCardByProductId(product6x1.getId())).thenReturn(stockCard6x1);
        when(stockRepository.queryStockCardByProductId(product6x2.getId())).thenReturn(stockCard6x2);
        when(stockRepository.queryStockCardByProductId(product6x3.getId())).thenReturn(stockCard6x3);
        when(stockRepository.queryStockCardByProductId(product6x4.getId())).thenReturn(stockCard6x4);
    }

    @Test
    public void shouldNotReturnPeriodsWhenThereAreNotPatientDataReportedAndCurrentPeriodIsNotOpenToRequisitions() throws LMISException {
        LMISTestApp.getInstance().setCurrentTimeMillis(DateTime.parse("2017-09-13").getMillis());
        Optional<PatientDataReport> patientDataReport = Optional.absent();
        when(patientDataRepository.getFirstMovement()).thenReturn(patientDataReport);
        List<Period> periods = patientDataService.calculatePeriods();
        assertThat(periods.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnCurrentPeriodWhenThereAreNotPatientDataReportedAndCurrentPeriodIsOpenToRequisitions() throws LMISException {
        DateTime today = calculateDateWithinRequisitionPeriod();
        LMISTestApp.getInstance().setCurrentTimeMillis(today.getMillis());
        Optional<PatientDataReport> patientDataReport = Optional.absent();
        when(patientDataRepository.getFirstMovement()).thenReturn(patientDataReport);
        Period expectedPeriod = new Period(new DateTime(LMISApp.getInstance().getCurrentTimeMillis()));
        List<Period> periods = patientDataService.calculatePeriods();
        assertThat(EqualsBuilder.reflectionEquals(expectedPeriod, periods.get(FIRST_PERIOD_POSITION)), is(true));
    }

    @Test
    public void shouldReturnPeriodsStartingFromFirstPatientDataReportedIncludingCurrent() throws LMISException {
        DateTime firstReportedDate = calculateDateWithinRequisitionPeriod();
        DateTime today = calculateValidDateForRequisitionPeriodWithinTwelveMonths(firstReportedDate);
        LMISTestApp.getInstance().setCurrentTimeMillis(today.getMillis());
        PatientDataReport patientDataReport = new PatientDataReport();
        patientDataReport.setReportedDate(firstReportedDate);
        when(patientDataRepository.getFirstMovement()).thenReturn(Optional.of(patientDataReport));
        List<Period> periods = patientDataService.calculatePeriods();
        assertThat(periods.size(), is(monthsAfterInitialReportedDate + CURRENT_MONTH));
    }

    @Test
    public void shouldReturnPeriodsStartingFromFirstPatientDataReportedExcludingCurrentWhenCurrentDateIsNotOpenToRequisitions() throws LMISException {
        DateTime firstReportedDate = calculateDateWithinRequisitionPeriod();
        DateTime today = calculateInvalidDateForRequisitionPeriodWithinTwelveMonths(firstReportedDate);
        LMISTestApp.getInstance().setCurrentTimeMillis(today.getMillis());
        PatientDataReport patientDataReport = new PatientDataReport();
        patientDataReport.setReportedDate(firstReportedDate);
        when(patientDataRepository.getFirstMovement()).thenReturn(Optional.of(patientDataReport));
        List<Period> periods = patientDataService.calculatePeriods();
        assertThat(periods.size(), is(monthsAfterInitialReportedDate));
    }

    private DateTime calculateDateWithinRequisitionPeriod() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int daysToAddWithinCurrentPeriod = nextInt(0, 3);
        calendar.set(Calendar.DAY_OF_MONTH, REQUISITION_PERIOD_STARTING_DAY);
        DateTime actualDate = new DateTime(calendar.getTime().getTime()).plusDays(daysToAddWithinCurrentPeriod);
        return actualDate;
    }

    private DateTime calculateValidDateForRequisitionPeriodWithinTwelveMonths(DateTime startingValidDate) {
        monthsAfterInitialReportedDate = nextInt(1, 13);
        return startingValidDate.plusMonths(monthsAfterInitialReportedDate);
    }

    private DateTime calculateInvalidDateForRequisitionPeriodWithinTwelveMonths(DateTime startingValidDate) {
        int daysAfterInitialReportedDate = nextInt(3, 10);
        DateTime validActualDate = calculateValidDateForRequisitionPeriodWithinTwelveMonths(startingValidDate);
        return validActualDate.minusDays(daysAfterInitialReportedDate);
    }

    @Test
    public void shouldReturnMalariaProducts() throws LMISException {
        Product[] products = {product6x1, product6x2, product6x3, product6x4};
        List<Product> malariaProductsExpected = Arrays.asList(products);
        List<Product> malariaProducts = patientDataService.getMalariaProducts();
        assertThat(malariaProducts.size(), is(TOTAL_MALARIA_PRODUCTS));
        for (int index = 0; index < malariaProducts.size(); index++) {
            Product productExpected = malariaProductsExpected.get(index);
            Product productActual = malariaProducts.get(index);
            assertThat(EqualsBuilder.reflectionEquals(productExpected, productActual), is(true));
        }
    }

    @Test
    public void shouldReturnExistingStockCardOfMalariaProducts() throws LMISException {
        Product[] malariaProducts = {product6x1, product6x2, product6x3, product6x4};
        List<Product> malariaProductsExpected = Arrays.asList(malariaProducts);
        List<StockCard> stocks = patientDataService.getMalariaProductsStockCards();
        assertThat(stocks.size(), is(TOTAL_MALARIA_PRODUCTS));
        for (int index = 0; index < stocks.size(); index++) {
            StockCard stock = stocks.get(index);
            Product productExpected = malariaProductsExpected.get(index);
            assertThat(EqualsBuilder.reflectionEquals(productExpected, stock.getProduct()), is(true));
            assertThat(stock.getStockOnHand(), is(STOCK_ON_HAND_VALUE));
        }
    }

    @Test
    public void shouldReturnStockCardValuesForAllMalariaProducts() throws Exception {
        List<Long> stockValues = patientDataService.getMalariaProductsStockHand();
        for (Long stockValue : stockValues) {
            assertThat(stockValue, is(STOCK_ON_HAND_VALUE));
        }
    }

    @Test
    public void shouldSavePatientDataReportSuccessfullyWhenPeriodIsOpenToRequisitionsStatusIsMissingAndPatientDataIsNotEmpty() throws LMISException {
        DateTime requisitionOpenedDate = calculateDateWithinRequisitionPeriod();
        Period currentPeriod = new Period(requisitionOpenedDate);
        List<PatientDataReportViewModel> patientDataReportViewModels = generatePatientDataViewModels(currentPeriod);
        PatientDataReportViewModel dataReportViewModelUs = patientDataReportViewModels.get(VIEW_MODEL_US_POSITION);
        PatientDataReportViewModel dataReportViewModelApe = patientDataReportViewModels.get(VIEW_MODEL_APE_POSITION);
        PatientDataReport patientDataReportUs = preparePatientDataReport(dataReportViewModelUs);
        PatientDataReport patientDataReportApe = preparePatientDataReport(dataReportViewModelApe);
        PatientDataReport patientDataReportUsExpected = preparePatientDataReport(dataReportViewModelUs);
        patientDataReportUsExpected.setStatusDraft(Boolean.TRUE);
        PatientDataReport patientDataReportApeExpected = preparePatientDataReport(dataReportViewModelApe);
        patientDataReportApeExpected.setStatusDraft(Boolean.TRUE);
        setStatusAsMissing(patientDataReportUs);
        setStatusAsMissing(patientDataReportApe);
        when(patientDataRepository.saveMovement(patientDataReportUs)).thenReturn(Optional.of(patientDataReportUsExpected));
        when(patientDataRepository.saveMovement(patientDataReportApe)).thenReturn(Optional.of(patientDataReportApeExpected));
        boolean isSuccessful = patientDataService.savePatientDataMovementsPerPeriod(patientDataReportViewModels);
        assertThat(isSuccessful, is(Boolean.TRUE));
    }

    @Test
    public void shouldNotSavePatientDataReportWhenPeriodIsOpenToRequisitionsStatusIsDraftCompleteOrSynced() throws LMISException {
        DateTime requisitionOpenedDate = calculateDateWithinRequisitionPeriod();
        Period currentPeriod = new Period(requisitionOpenedDate);
        List<PatientDataReportViewModel> patientDataReportViewModels = generatePatientDataViewModels(currentPeriod);
        PatientDataReportViewModel dataReportViewModelUs = patientDataReportViewModels.get(VIEW_MODEL_US_POSITION);
        PatientDataReportViewModel dataReportViewModelApe = patientDataReportViewModels.get(VIEW_MODEL_APE_POSITION);
        PatientDataReport patientDataReportUs = preparePatientDataReport(dataReportViewModelUs);
        PatientDataReport patientDataReportApe = preparePatientDataReport(dataReportViewModelApe);
        setStatusDraftCompleteOrSynced(patientDataReportUs);
        setStatusDraftCompleteOrSynced(patientDataReportApe);
        when(patientDataRepository.saveMovement(patientDataReportUs)).thenReturn(Optional.<PatientDataReport>absent());
        when(patientDataRepository.saveMovement(patientDataReportApe)).thenReturn(Optional.<PatientDataReport>absent());
        boolean isSuccessful = patientDataService.savePatientDataMovementsPerPeriod(patientDataReportViewModels);
        assertThat(isSuccessful, is(Boolean.FALSE));
    }

    @Test
    public void shouldNotSavePatientDataReportWhenPeriodIsOpenToRequisitionsStatusAndPatientDataIsEmpty() throws LMISException {
        List<Long> existingStocks = Arrays.asList(new Long[]{0L, null, 0L, 0L});
        List<Long> currentTreatments = Arrays.asList(new Long[]{0L, 0L, null, 0L});
        List<PatientDataReportViewModel> patientDataReportViewModels = prepareDataInOrderToSave(existingStocks, currentTreatments);
        boolean isSuccessful = patientDataService.savePatientDataMovementsPerPeriod(patientDataReportViewModels);
        assertThat(isSuccessful, is(Boolean.FALSE));
    }

    @NonNull
    private List<PatientDataReportViewModel> prepareDataInOrderToSave(List<Long> existingStocks, List<Long> currentTreatments) {
        DateTime requisitionOpenedDate = calculateDateWithinRequisitionPeriod();
        Period currentPeriod = new Period(requisitionOpenedDate);
        PatientDataReportViewModel dataReportViewModelUs = new PatientDataReportViewModel(currentPeriod);
        PatientDataReportViewModel dataReportViewModelApe = new PatientDataReportViewModel(currentPeriod);
        dataReportViewModelApe.setType("APE");
        dataReportViewModelUs.setType("US");
        dataReportViewModelUs.setCurrentTreatments(currentTreatments);
        dataReportViewModelUs.setExistingStock(existingStocks);
        dataReportViewModelApe.setCurrentTreatments(currentTreatments);
        dataReportViewModelApe.setExistingStock(existingStocks);
        PatientDataReportViewModel[] patientDataReports = {dataReportViewModelUs, dataReportViewModelApe};
        return Arrays.asList(patientDataReports);
    }

    @Test
    public void shouldReturnEmptyIfNoPreviousPatientDataWasSavedBefore() throws LMISException {
        List<Long> existingStocks = Arrays.asList(new Long[]{0L, 0L, 0L, 0L});
        List<Long> currentTreatments = Arrays.asList(new Long[]{0L, 0L, 0L, 0L});
        List<PatientDataReportViewModel> patientDataReportViewModels = prepareDataInOrderToSave(existingStocks, currentTreatments);
        PatientDataReport patientDataReportUS = new PatientDataReport();
        patientDataReportUS.setId(Long.MAX_VALUE);
        patientDataReportUS.setType(US);
        patientDataReportUS.setCurrentTreatments(currentTreatments);
        patientDataReportUS.setExistingStocks(existingStocks);
        when(patientDataRepository.saveMovement(patientDataReportUS)).thenReturn(Optional.of(patientDataReportUS));
        patientDataService.savePatientDataMovementsPerPeriod(patientDataReportViewModels);
        PatientDataReport dataPatientDataReportUS = patientDataService.getExistingByModelPerPeriod(patientDataReportViewModels.get(0).getPeriod().getBegin(), patientDataReportViewModels.get(0).getPeriod().getEnd(), "US");
        assertThat(dataPatientDataReportUS, nullValue());
    }

    private List<Long> generatePatientDataValues() {
        List<Long> patientDataValues = new ArrayList<>();
        for (int index = 0; index < TOTAL_MALARIA_PRODUCTS; index++) {
            long value = nextInt(0, 1000);
            patientDataValues.add(value);
        }
        return patientDataValues;
    }

    private List<PatientDataReportViewModel> generatePatientDataViewModels(Period currentPeriod) {
        List<Long> existingStock = generatePatientDataValues();
        List<Long> currentTreatment = generatePatientDataValues();
        PatientDataReportViewModel dataReportViewModelUs = new PatientDataReportViewModel(currentPeriod);
        PatientDataReportViewModel dataReportViewModelApe = new PatientDataReportViewModel(currentPeriod);
        PatientDataReportViewModel[] models = new PatientDataReportViewModel[]{dataReportViewModelUs, dataReportViewModelApe};
        preparePatientDataViewModel(existingStock, currentTreatment, dataReportViewModelUs, US);
        preparePatientDataViewModel(existingStock, currentTreatment, dataReportViewModelApe, APE);
        return Arrays.asList(models);
    }

    private void preparePatientDataViewModel(List<Long> existingStock, List<Long> currentTreatment, PatientDataReportViewModel dataReportViewModel, String type) {
        dataReportViewModel.setType(type);
        dataReportViewModel.setExistingStock(existingStock);
        dataReportViewModel.setCurrentTreatments(currentTreatment);
    }

    private PatientDataReport preparePatientDataReport(PatientDataReportViewModel dataReportViewModel) {
        PatientDataReport patientDataReport = new PatientDataReport();
        patientDataReport.setStartDatePeriod(dataReportViewModel.getPeriod().getBegin());
        patientDataReport.setEndDatePeriod(dataReportViewModel.getPeriod().getEnd());
        patientDataReport.setType(dataReportViewModel.getType());
        patientDataReport.setExistingStocks(dataReportViewModel.getExistingStock());
        patientDataReport.setCurrentTreatments(dataReportViewModel.getCurrentTreatments());
        patientDataReport.setStatusMissing(Boolean.FALSE);
        patientDataReport.setStatusDraft(Boolean.FALSE);
        patientDataReport.setStatusComplete(Boolean.FALSE);
        patientDataReport.setStatusSynced(Boolean.FALSE);
        return patientDataReport;
    }

    private void setStatusAsMissing(PatientDataReport patientDataReport) {
        patientDataReport.setStatusMissing(Boolean.TRUE);
    }

    private void setStatusDraftCompleteOrSynced(PatientDataReport patientDataReport) {
        int typeStatus = nextInt(2, 5);
        if (typeStatus == DRAFT_TYPE) {
            patientDataReport.setStatusDraft(Boolean.TRUE);
        }
        if (typeStatus == COMPLETE_TYPE) {
            patientDataReport.setStatusMissing(Boolean.TRUE);
        }
        if (typeStatus == SYNCED_TYPE) {
            patientDataReport.setStatusDraft(Boolean.TRUE);
        }
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(PatientDataRepository.class).toInstance(patientDataRepository);
            bind(ProductRepository.class).toInstance(productRepository);
            bind(StockRepository.class).toInstance(stockRepository);
        }
    }
}
