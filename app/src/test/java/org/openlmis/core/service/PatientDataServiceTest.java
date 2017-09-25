package org.openlmis.core.service;

import com.google.inject.AbstractModule;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.tools.ant.taskdefs.Length;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class PatientDataServiceTest {

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
    public void shouldReturnTrueWhenSavePatientDataWasSavedSuccessfully() throws LMISException {
        Period currentPeriod = new Period(DateTime.parse("2017-09-18"));
        PatientDataReport patientDataReport = new PatientDataReport();
        PatientDataReport patientDataReportExpected = patientDataReport;
        patientDataReportExpected.setStatusDraft(Boolean.TRUE);
        Long[] existingStock = new Long[]{0L, 0L, 0L, 0L};
        Long[] currentTreatment = new Long[]{0L, 0L, 0L, 0L};
        when(patientDataRepository.saveMovement(patientDataReport)).thenReturn(Optional.of(patientDataReportExpected));
        PatientDataReportViewModel dataReportViewModelUs = new PatientDataReportViewModel(currentPeriod);
        dataReportViewModelUs.setExistingStock(Arrays.asList(existingStock));
        dataReportViewModelUs.setCurrentTreatments(Arrays.asList(currentTreatment));
        PatientDataReportViewModel[] patientDataReports = {dataReportViewModelUs};
        List<PatientDataReportViewModel> patientDataReportViewModels = Arrays.asList(patientDataReports);
        boolean isSuccessful = patientDataService.savePatientDataMovementsPerPeriod(patientDataReportViewModels);
        assertThat(isSuccessful, is(Boolean.TRUE));
    }

    @Test
    public void shouldReturnFalseWhenCurrentTreatmentsAndExistingStockValuesAreEmpty() {
        Period currentPeriod = new Period(DateTime.parse("2017-09-18"));
        PatientDataReportViewModel dataReportViewModelUs = new PatientDataReportViewModel(currentPeriod);
        PatientDataReportViewModel dataReportViewModelApe = new PatientDataReportViewModel(currentPeriod);
        Long[] existingStock = new Long[]{0L, null, null, null};
        Long[] currentTreatment = new Long[]{0L, 0L, 0L, 0L};
        dataReportViewModelUs.setExistingStock(Arrays.asList(existingStock));
        dataReportViewModelApe.setExistingStock(Arrays.asList(existingStock));
        dataReportViewModelUs.setCurrentTreatments(Arrays.asList(currentTreatment));
        dataReportViewModelApe.setCurrentTreatments(Arrays.asList(currentTreatment));
        PatientDataReportViewModel[] patientDataReports = {dataReportViewModelUs, dataReportViewModelApe};
        List<PatientDataReportViewModel> patientDataReportViewModels = Arrays.asList(patientDataReports);
        boolean isSuccessful = patientDataService.savePatientDataMovementsPerPeriod(patientDataReportViewModels);
        assertThat(isSuccessful, is (Boolean.FALSE));
    }

    @Test
    public void shouldReturnTrueWhenCurrentTreatmentsAndExistingStockValuesIsNotEmptyAndStatusIsNotComplete() throws LMISException {
        Period currentPeriod = new Period(DateTime.parse("2017-09-18"));
        PatientDataReportViewModel dataReportViewModelUs = new PatientDataReportViewModel(currentPeriod);
        Long[] existingStock = new Long[]{Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE};
        Long[] currentTreatment = new Long[]{Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE};
        dataReportViewModelUs.setExistingStock(Arrays.asList(existingStock));
        dataReportViewModelUs.setCurrentTreatments(Arrays.asList(currentTreatment));
        PatientDataReport patientDataReportUs = new PatientDataReport();
        patientDataReportUs.setExistingStock6x1(Long.MAX_VALUE);
        patientDataReportUs.setExistingStock6x2(Long.MAX_VALUE);
        patientDataReportUs.setExistingStock6x3(Long.MAX_VALUE);
        patientDataReportUs.setExistingStock6x4(Long.MAX_VALUE);
        patientDataReportUs.setCurrentTreatment6x1(Long.MAX_VALUE);
        patientDataReportUs.setCurrentTreatment6x2(Long.MAX_VALUE);
        patientDataReportUs.setCurrentTreatment6x3(Long.MAX_VALUE);
        patientDataReportUs.setCurrentTreatment6x4(Long.MAX_VALUE);
        patientDataReportUs.setStatusDraft(Boolean.FALSE);
        PatientDataReport patientDataReportUsExpected = new PatientDataReport();
        patientDataReportUsExpected.setStatusDraft(Boolean.TRUE);
        PatientDataReportViewModel[] patientDataReports = {dataReportViewModelUs};
        when(patientDataRepository.saveMovement(patientDataReportUs)).thenReturn(Optional.of(patientDataReportUsExpected));
        List<PatientDataReportViewModel> patientDataReportViewModels = Arrays.asList(patientDataReports);
        boolean isSuccessful = patientDataService.savePatientDataMovementsPerPeriod(patientDataReportViewModels);
        assertThat(isSuccessful, is(Boolean.TRUE));
    }

    @Test
    public void shouldReturnFalseWhenSaveAPatientDataReportModelHasStatusCompleteOrSynced() throws LMISException {
        int typeStatus = nextInt(1, 3);
        Period currentPeriod = new Period(DateTime.parse("2017-09-18"));
        PatientDataReportViewModel dataReportViewModel = new PatientDataReportViewModel(currentPeriod);
        Long[] existingStock = new Long[]{Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE};
        Long[] currentTreatment = new Long[]{Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE};
        dataReportViewModel.setExistingStock(Arrays.asList(existingStock));
        dataReportViewModel.setCurrentTreatments(Arrays.asList(currentTreatment));
        PatientDataReport patientDataReport = new PatientDataReport();
        if (typeStatus == MISSING_TYPE) {
            patientDataReport.setStatusMissing(Boolean.TRUE);
        }
        if (typeStatus == DRAFT_TYPE) {
            patientDataReport.setStatusDraft(Boolean.TRUE);
        }
        when(patientDataRepository.saveMovement(patientDataReport)).thenReturn(Optional.<PatientDataReport>absent());
        PatientDataReportViewModel[] patientDataReports = {dataReportViewModel};
        List<PatientDataReportViewModel> patientDataReportViewModels = Arrays.asList(patientDataReports);
        boolean isSuccessful = patientDataService.savePatientDataMovementsPerPeriod(patientDataReportViewModels);
        assertThat(isSuccessful, is(Boolean.FALSE));
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
