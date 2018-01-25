package org.openlmis.core.service;

import com.google.inject.AbstractModule;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.enums.VIAReportType;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.helpers.ProductBuilder;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.MalariaProgramRepository;
import org.openlmis.core.model.repository.PTVProgramRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.roboguice.shaded.goole.common.base.Optional;
import org.robolectric.RuntimeEnvironment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.core.helpers.MalariaProgramBuilder.randomMalariaProgram;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x1_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x2_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x3_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x4_CODE;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class PatientDataServiceTest {
    public static final int CURRENT_MONTH = 1;
    public static final int FIRST_PERIOD_POSITION = 0;
    public static final int REQUISITION_PERIOD_STARTING_DAY = 18;
    public long STOCK_ON_HAND_VALUE = nextLong(1, 100);

    private MalariaProgramRepository malariaProgramRepository;
    private ProductRepository productRepository;
    private StockRepository stockRepository;
    private PTVProgramRepository ptvProgramRepository;

    private PatientDataService patientDataService;

    private int monthsAfterInitialReportedDate;

    private Product product6x1 = make(a(ProductBuilder.product6x1));
    private Product product6x2 = make(a(ProductBuilder.product6x2));
    private Product product6x3 = make(a(ProductBuilder.product6x3));
    private Product product6x4 = make(a(ProductBuilder.product6x4));
    private StockCard stockCard6x1;
    private StockCard stockCard6x2;
    private StockCard stockCard6x3;
    private StockCard stockCard6x4;
    private MalariaProgram malariaProgram = make(a(randomMalariaProgram));

    @Before
    public void setUp() throws LMISException {

        malariaProgramRepository = mock(MalariaProgramRepository.class);
        productRepository = mock(ProductRepository.class);
        stockRepository = mock(StockRepository.class);
        ptvProgramRepository = mock(PTVProgramRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new PatientDataServiceTest.MyTestModule());
        patientDataService = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PatientDataService.class);

        when(productRepository.getByCode(PRODUCT_6x1_CODE.getValue())).thenReturn(product6x1);
        when(productRepository.getByCode(PRODUCT_6x2_CODE.getValue())).thenReturn(product6x2);
        when(productRepository.getByCode(PRODUCT_6x3_CODE.getValue())).thenReturn(product6x3);
        when(productRepository.getByCode(PRODUCT_6x4_CODE.getValue())).thenReturn(product6x4);
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
        when(malariaProgramRepository.getFirstMovement()).thenReturn(null);
        List<Period> periods = patientDataService.calculatePeriods(VIAReportType.MALARIA);
        assertThat(periods.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnCurrentPeriodWhenThereAreNotPatientDataReportedAndCurrentPeriodIsOpenToRequisitions() throws LMISException {
        DateTime today = calculateDateWithinRequisitionPeriod();
        LMISTestApp.getInstance().setCurrentTimeMillis(today.getMillis());
        when(malariaProgramRepository.getFirstMovement()).thenReturn(null);
        Period expectedPeriod = new Period(new DateTime(LMISTestApp.getInstance().getCurrentTimeMillis()));
        List<Period> periods = patientDataService.calculatePeriods(VIAReportType.MALARIA);
        assertThat(EqualsBuilder.reflectionEquals(expectedPeriod, periods.get(FIRST_PERIOD_POSITION)), is(true));
    }

    @Test
    public void shouldReturnPeriodsStartingFromFirstPatientDataReportedIncludingCurrent() throws LMISException {
        DateTime firstReportedDate = calculateDateWithinRequisitionPeriod();
        DateTime today = calculateValidDateForRequisitionPeriodWithinTwelveMonths(firstReportedDate);
        LMISTestApp.getInstance().setCurrentTimeMillis(today.getMillis());
        malariaProgram.setReportedDate(firstReportedDate);
        malariaProgram.setCreatedAt(firstReportedDate.toDate());
        when(malariaProgramRepository.getFirstMovement()).thenReturn(malariaProgram);
        List<Period> periods = patientDataService.calculatePeriods(VIAReportType.MALARIA);
        assertThat(periods.size(), is(monthsAfterInitialReportedDate + CURRENT_MONTH));
    }

    @Test
    public void shouldReturnPeriodsStartingFromFirstPatientDataReportedExcludingCurrentWhenCurrentDateIsNotOpenToRequisitions() throws LMISException {
        DateTime firstReportedDate = calculateDateWithinRequisitionPeriod();
        DateTime futureToday = calculateInvalidDateForRequisitionPeriodWithinTwelveMonths(firstReportedDate);
        LMISTestApp.getInstance().setCurrentTimeMillis(futureToday.getMillis());
        malariaProgram.setReportedDate(firstReportedDate);
        malariaProgram.setCreatedAt(firstReportedDate.toDate());
        when(malariaProgramRepository.getFirstMovement()).thenReturn(malariaProgram);
        List<Period> periods = patientDataService.calculatePeriods(VIAReportType.MALARIA);
        assertThat(periods.size(), is(monthsAfterInitialReportedDate));
    }

    private DateTime calculateDateWithinRequisitionPeriod() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int daysToAddWithinCurrentPeriod = nextInt(0, 3);
        calendar.set(Calendar.DAY_OF_MONTH, REQUISITION_PERIOD_STARTING_DAY);
        DateTime actualDate = new DateTime(calendar).plusDays(daysToAddWithinCurrentPeriod);
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
    public void shouldReturnMalariaProducts() throws Exception {
        List<Product> expectedMalariaProducts = newArrayList(product6x1, product6x2, product6x3, product6x4);
        List<Product> malariaProducts = patientDataService.getMalariaProducts();
        assertThat(malariaProducts.size(), is(expectedMalariaProducts.size()));
        for (Product expectedProduct : expectedMalariaProducts) {
            assertTrue(malariaProducts.contains(expectedProduct));
        }
    }

    @Test
    public void shouldReturnExistingStockCardOfMalariaProducts() throws Exception {
        List<StockCard> expectedMalariaStockCards = newArrayList(stockCard6x1, stockCard6x2, stockCard6x3, stockCard6x4);
        List<StockCard> stocks = patientDataService.getMalariaProductsStockCards();
        assertThat(stocks.size(), is(expectedMalariaStockCards.size()));
        for (StockCard expectedStockCard : expectedMalariaStockCards) {
            assertTrue(stocks.contains(expectedStockCard));
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
    public void shouldSavePatientDataReportSuccessfully() throws LMISException {
        when(malariaProgramRepository.save(malariaProgram)).thenReturn(Optional.of(malariaProgram));
        Optional<MalariaProgram> savedMalariaProgram = patientDataService.save(malariaProgram);
        assertThat(savedMalariaProgram.get(), is(malariaProgram));
    }

    @Test(expected = LMISException.class)
    public void shouldReturnFalseWhenSavingMalariaProgramWasFaulty() throws LMISException {
        doThrow(LMISException.class).when(malariaProgramRepository).save(malariaProgram);
        patientDataService.save(malariaProgram);
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(MalariaProgramRepository.class).toInstance(malariaProgramRepository);
            bind(ProductRepository.class).toInstance(productRepository);
            bind(StockRepository.class).toInstance(stockRepository);
            bind(PTVProgramRepository.class).toInstance(ptvProgramRepository);
        }
    }
}
