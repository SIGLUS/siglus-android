package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.service.PatientDataService;
import org.openlmis.core.view.viewmodel.PatientDataReportViewModel;
import org.robolectric.RuntimeEnvironment;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import roboguice.RoboGuice;

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
    public static final int INDEX_TOTAL = 2;
    private static final String APE = "APE";
    public static final String EMPTY_VALUE = "";

    public static final int INDEX_US = 0;
    private static final int INDEX_APE = 1;
    public static final Long STOCK_ON_HAND_VALUE = Long.valueOf(123);
    public static final int TOTAL_MALARIA_PRODUCTS = 4;
    public static final String TOTAL = "TOTAL";

    private PatientDataService patientDataService;
    private PatientDataReportFormPresenter presenter;

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
        String [] stockOnHand = new String []{String.valueOf(stockCard6x1.getStockOnHand()), String.valueOf(stockCard6x2.getStockOnHand()), String.valueOf(stockCard6x3.getStockOnHand()), String.valueOf(stockCard6x4.getStockOnHand())};
        when(patientDataService.getMalariaProductsStockHand()).thenReturn(Arrays.asList(stockOnHand));
        presenter.generateViewModelsForAvailablePeriods();
        PatientDataReportViewModel patientDataReportUsViewModel = presenter.getViewModels().get(INDEX_US);
        String usApe = patientDataReportUsViewModel.getUsApe();
        List<String> currentTreatments = patientDataReportUsViewModel.getCurrentTreatments();
        List<String> existingStockProducts = patientDataReportUsViewModel.getExistingStock();
        assertThat(presenter.getViewModels().size(), is (2));
        assertThat(usApe, is(US));
        assertThat(currentTreatments.size(), is(4));
        assertThat(existingStockProducts.size(), is(4));
        for (int index = 0; index < existingStockProducts.size(); index ++) {
            String treatment = currentTreatments.get(index);
            String stock = existingStockProducts.get(index);
            assertThat(treatment, is(EMPTY_VALUE));
            assertThat(stock, is(String.valueOf(STOCK_ON_HAND_VALUE)));
        }
    }

    @Test
    public void shouldGenerateViewModelsForPatientDataReportInCurrentPeriodWhenIsAPE() {
        presenter.generateViewModelsForAvailablePeriods();
        PatientDataReportViewModel patientDataReportUsViewModel = presenter.getViewModels().get(INDEX_APE);
        String usApe = patientDataReportUsViewModel.getUsApe();
        List<String> currentTreatments = patientDataReportUsViewModel.getCurrentTreatments();
        List<String> existingStockProducts = patientDataReportUsViewModel.getExistingStock();
        assertThat(presenter.getViewModels().size(), is (2));
        assertThat(usApe, is(APE));
        assertThat(currentTreatments.size(), is(TOTAL_MALARIA_PRODUCTS));
        for (int index = 0; index < TOTAL_MALARIA_PRODUCTS; index ++) {
            String treatment = currentTreatments.get(index);
            String stocks = existingStockProducts.get(index);
            assertThat(treatment, is(EMPTY_VALUE));
            assertThat(stocks, is(EMPTY_VALUE));
        }
    }

    @Test
    public void shouldGenerateViewModelsForPatientDataReportInCurrentPeriodWhenIsTotal() {
        String [] stockOnHand = new String []{String.valueOf(stockCard6x1.getStockOnHand()), String.valueOf(stockCard6x2.getStockOnHand()), String.valueOf(stockCard6x3.getStockOnHand()), String.valueOf(stockCard6x4.getStockOnHand())};
        when(patientDataService.getMalariaProductsStockHand()).thenReturn(Arrays.asList(stockOnHand));
        presenter.generateViewModelsForAvailablePeriods();
        PatientDataReportViewModel patientDataReportUsViewModel = presenter.getViewModels().get(INDEX_US);
        PatientDataReportViewModel patientDataReportApeViewModel = presenter.getViewModels().get(INDEX_APE);
        PatientDataReportViewModel patientDataReportTotalViewModel = presenter.getViewModels().get(INDEX_TOTAL);
        String usApe = patientDataReportTotalViewModel.getUsApe();
        List<String> currentTreatmentsUs = patientDataReportUsViewModel.getCurrentTreatments();
        List<String> existingStocksUs = patientDataReportUsViewModel.getExistingStock();
        List<String> currentTreatmentsApe = patientDataReportApeViewModel.getCurrentTreatments();
        List<String> existingStocksApe = patientDataReportApeViewModel.getExistingStock();
        List<String> currentTreatmentsTotal = patientDataReportTotalViewModel.getCurrentTreatments();
        List<String> existingStocksTotal = patientDataReportTotalViewModel.getExistingStock();
        assertThat(usApe, is(TOTAL));
        for (int index = 0; index < TOTAL_MALARIA_PRODUCTS; index ++) {
            String currentTreatmentUs = currentTreatmentsUs.get(index);
            String existingStockUs = existingStocksUs.get(index);
            String currentTreatmentApe = currentTreatmentsApe.get(index);
            String existingStockApe = existingStocksApe.get(index);
            String currentTreatmentTotal = currentTreatmentsTotal.get(index);
            String existingStockTotal = existingStocksTotal.get(index);
            assertThat(currentTreatmentTotal, is(String.valueOf(calculateCurrentTotal(currentTreatmentUs, currentTreatmentApe))));
            assertThat(existingStockTotal, is(String.valueOf(calculateCurrentTotal(existingStockUs, existingStockApe))));
        }
    }

    private int calculateCurrentTotal(String currentUs, String currentTreatmentApe) {
        return Integer.parseInt(currentUs) + Integer.parseInt(currentTreatmentApe);
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(PatientDataService.class).toInstance(patientDataService);
        }
    }
}