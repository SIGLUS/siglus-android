package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.service.PatientDataService;
import org.openlmis.core.view.viewmodel.PatientDataReportViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class PatientDataReportFormPresenterTest {

    public static final String MALARIA_PRODUCT_CODE_6X1 = "08O05";
    public static final String MALARIA_PRODUCT_CODE_6X2 = "08O05Z";
    public static final String MALARIA_PRODUCT_CODE_6X3 = "08O05X";
    public static final String MALARIA_PRODUCT_CODE_6X4 = "08O05Y";

    public static final String US = "US";
    private static final String APE = "APE";
    public static final String TOTAL = "TOTAL";

    public static final int INDEX_TOTAL = 2;

    public static final int INDEX_US = 0;
    private static final int INDEX_APE = 1;
    public static final Long STOCK_ON_HAND_VALUE_WITHOUT_VALUE = Long.valueOf(0);
    public static final Long STOCK_ON_HAND_VALUE = Long.valueOf(123);
    public static final int TOTAL_MALARIA_PRODUCTS = 4;
    public static final int TOTAL_PATIENT_DATA_FORM_ROWS = 3;
    private static final int REQUISITION_START_DAY = 18;

    private Period period;
    private PatientDataService patientDataService;
    private PatientDataReportFormPresenter presenter;
    private Boolean isUpdate = Boolean.FALSE;

    private Product product6x1 = new Product();
    private Product product6x2 = new Product();
    private Product product6x3 = new Product();
    private Product product6x4 = new Product();

    private StockCard stockCard6x1 = new StockCard();
    private StockCard stockCard6x2 = new StockCard();
    private StockCard stockCard6x3 = new StockCard();
    private StockCard stockCard6x4 = new StockCard();

    @Before
    public void setup() {
        DateTime avaibleDateForRequisitions = calculateDateWithinRequisitionPeriod();
        period = new Period(avaibleDateForRequisitions);
        product6x1.setId(1);
        product6x2.setId(2);
        product6x3.setId(3);
        product6x4.setId(4);
        product6x1.setCode(MALARIA_PRODUCT_CODE_6X1);
        product6x2.setCode(MALARIA_PRODUCT_CODE_6X2);
        product6x3.setCode(MALARIA_PRODUCT_CODE_6X3);
        product6x4.setCode(MALARIA_PRODUCT_CODE_6X4);
        stockCard6x1.setProduct(product6x1);
        stockCard6x2.setProduct(product6x2);
        stockCard6x3.setProduct(product6x3);
        stockCard6x4.setProduct(product6x4);
        stockCard6x1.setStockOnHand(STOCK_ON_HAND_VALUE);
        stockCard6x2.setStockOnHand(STOCK_ON_HAND_VALUE);
        stockCard6x3.setStockOnHand(STOCK_ON_HAND_VALUE);
        stockCard6x4.setStockOnHand(STOCK_ON_HAND_VALUE);
        patientDataService = mock(PatientDataService.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new PatientDataReportFormPresenterTest.MyTestModule());
        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PatientDataReportFormPresenter.class);
    }

    @Test
    public void shouldGenerateViewModelsForPatientDataReportInCurrentPeriodWhenIsUS() {
        Long[] stockOnHand = new Long[]{stockCard6x1.getStockOnHand(), stockCard6x2.getStockOnHand(), stockCard6x3.getStockOnHand(), stockCard6x4.getStockOnHand()};
        when(patientDataService.getMalariaProductsStockHand()).thenReturn(Arrays.asList(stockOnHand));
        presenter.generateViewModelsBySpecificPeriod(period, isUpdate);
        PatientDataReportViewModel patientDataReportUsViewModel = presenter.getViewModels(period, isUpdate).get(INDEX_US);
        String usApe = patientDataReportUsViewModel.getType();
        List<Long> currentTreatments = patientDataReportUsViewModel.getCurrentTreatments();
        List<Long> existingStockProducts = patientDataReportUsViewModel.getExistingStock();
        assertThat(presenter.getViewModels(period, isUpdate).size(), is(TOTAL_PATIENT_DATA_FORM_ROWS));
        assertThat(usApe, is(US));
        assertThat(currentTreatments.size(), is(TOTAL_MALARIA_PRODUCTS));
        assertThat(existingStockProducts.size(), is(TOTAL_MALARIA_PRODUCTS));
        for (int index = 0; index < existingStockProducts.size(); index++) {
            long treatment = currentTreatments.get(index);
            long stock = existingStockProducts.get(index);
            assertThat(treatment, is(STOCK_ON_HAND_VALUE_WITHOUT_VALUE));
            assertThat(stock, is(STOCK_ON_HAND_VALUE));
        }
    }

    @Test
    public void shouldGenerateViewModelsForPatientDataReportInCurrentPeriodWhenIsAPE() {
        Long[] stockOnHand = new Long[]{stockCard6x1.getStockOnHand(), stockCard6x2.getStockOnHand(), stockCard6x3.getStockOnHand(), stockCard6x4.getStockOnHand()};
        when(patientDataService.getMalariaProductsStockHand()).thenReturn(Arrays.asList(stockOnHand));
        presenter.generateViewModelsBySpecificPeriod(period, isUpdate);
        PatientDataReportViewModel patientDataReportUsViewModel = presenter.getViewModels(period, isUpdate).get(INDEX_APE);
        String usApe = patientDataReportUsViewModel.getType();
        List<Long> currentTreatments = patientDataReportUsViewModel.getCurrentTreatments();
        List<Long> existingStockProducts = patientDataReportUsViewModel.getExistingStock();
        assertThat(presenter.getViewModels(period,isUpdate).size(), is(TOTAL_PATIENT_DATA_FORM_ROWS));
        assertThat(usApe, is(APE));
        assertThat(currentTreatments.size(), is(TOTAL_MALARIA_PRODUCTS));
        for (int index = 0; index < TOTAL_MALARIA_PRODUCTS; index++) {
            long treatment = currentTreatments.get(index);
            long stocks = existingStockProducts.get(index);
            assertThat(treatment, is(STOCK_ON_HAND_VALUE_WITHOUT_VALUE));
            assertThat(stocks, is(STOCK_ON_HAND_VALUE_WITHOUT_VALUE));
        }
    }

    @Test
    public void shouldGenerateViewModelsForPatientDataReportInCurrentPeriodWhenIsTotal() {
        Long[] stockOnHand = new Long[]{stockCard6x1.getStockOnHand(), stockCard6x2.getStockOnHand(), stockCard6x3.getStockOnHand(), stockCard6x4.getStockOnHand()};
        when(patientDataService.getMalariaProductsStockHand()).thenReturn(Arrays.asList(stockOnHand));
        presenter.generateViewModelsBySpecificPeriod(period, isUpdate);
        PatientDataReportViewModel patientDataReportUsViewModel = presenter.getViewModels(period, isUpdate).get(INDEX_US);
        PatientDataReportViewModel patientDataReportApeViewModel = presenter.getViewModels(period, isUpdate).get(INDEX_APE);
        PatientDataReportViewModel patientDataReportTotalViewModel = presenter.getViewModels(period, isUpdate).get(INDEX_TOTAL);
        String usApe = patientDataReportTotalViewModel.getType();
        List<Long> currentTreatmentsUs = patientDataReportUsViewModel.getCurrentTreatments();
        List<Long> existingStocksUs = patientDataReportUsViewModel.getExistingStock();
        List<Long> currentTreatmentsApe = patientDataReportApeViewModel.getCurrentTreatments();
        List<Long> existingStocksApe = patientDataReportApeViewModel.getExistingStock();
        List<Long> currentTreatmentsTotal = patientDataReportTotalViewModel.getCurrentTreatments();
        List<Long> existingStocksTotal = patientDataReportTotalViewModel.getExistingStock();
        assertThat(presenter.getViewModels(period, isUpdate).size(), is(TOTAL_PATIENT_DATA_FORM_ROWS));
        assertThat(usApe, is(TOTAL));
        for (int index = 0; index < TOTAL_MALARIA_PRODUCTS; index++) {
            long currentTreatmentUs = currentTreatmentsUs.get(index);
            long existingStockUs = existingStocksUs.get(index);
            long currentTreatmentApe = currentTreatmentsApe.get(index);
            long existingStockApe = existingStocksApe.get(index);
            long currentTreatmentTotal = currentTreatmentsTotal.get(index);
            long existingStockTotal = existingStocksTotal.get(index);
            assertThat(currentTreatmentTotal, is(calculateCurrentTotal(currentTreatmentUs, currentTreatmentApe)));
            assertThat(existingStockTotal, is(calculateCurrentTotal(existingStockUs, existingStockApe)));
        }
    }

    private DateTime calculateDateWithinRequisitionPeriod() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int daysToAddWithinCurrentPeriod = nextInt(0, 3);
        calendar.set(Calendar.DAY_OF_MONTH, REQUISITION_START_DAY);
        DateTime actualDate = new DateTime(calendar.getTime().getTime()).plusDays(daysToAddWithinCurrentPeriod);
        return actualDate;
    }

    private long calculateCurrentTotal(long currentUs, long currentTreatmentApe) {
        return currentUs + currentTreatmentApe;
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(PatientDataService.class).toInstance(patientDataService);
        }
    }
}