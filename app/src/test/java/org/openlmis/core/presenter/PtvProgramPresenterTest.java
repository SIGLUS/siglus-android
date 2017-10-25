package org.openlmis.core.presenter;

import android.support.annotation.NonNull;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.HealthFacilityService;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.HealthFacilityServiceRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.PTVUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class PtvProgramPresenterTest {

    PtvProgramPresenter ptvProgramPresenter;
    StockRepository stockRepository;
    HealthFacilityServiceRepository healthFacilityServiceRepository;

    private Date today = DateTime.now().toDate();

    @Before
    public void setUp() throws Exception {
        healthFacilityServiceRepository = mock(HealthFacilityServiceRepository.class);
        stockRepository = mock(StockRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new PtvProgramPresenterTest.MyTestModule());
        ptvProgramPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PtvProgramPresenter.class);
    }

    @Test
    public void shouldReturnHealthFacilityServices() throws LMISException {
        List<HealthFacilityService> expectedHealthFacilityServices = PTVUtil.createDummyHealthFacilityServices();
        when(healthFacilityServiceRepository.getAll()).thenReturn(expectedHealthFacilityServices);
        TestSubscriber<List<HealthFacilityService>> subscriber = new TestSubscriber<>();

        Observable<List<HealthFacilityService>> observable = ptvProgramPresenter.getAllHealthFacilityServices();
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        List<HealthFacilityService> actualHealthFacilityServices = subscriber.getOnNextEvents().get(0);

        assertThat(actualHealthFacilityServices, is(expectedHealthFacilityServices));
    }

    @Test
    public void shouldReturnPTVProgramStockInformationForCurrentPTVProducts() throws LMISException {
        List<String> ptvProductCodes = getProductCodes();
        Product product1 = createProductWithCustomCode(Constants.PTV_PRODUCT_1_CODE);
        Product product2 = createProductWithCustomCode(Constants.PTV_PRODUCT_2_CODE);
        int stockOnHand1 = 200;
        int stockOnHand2 = 600;
        int movementQuantity1 = 300;
        int movementQuantity2 = 0;
        StockCard stockCard1 = createStockCardInformation(stockOnHand1, movementQuantity1, product1);
        StockCard stockCard2 = createStockCardInformation(stockOnHand2, movementQuantity2, product2);
        PTVProgramStockInformation ptvProgramStockInformation1 = createPtvProgramStockInformation(product1, stockOnHand1, movementQuantity1);
        PTVProgramStockInformation ptvProgramStockInformation2 = createPtvProgramStockInformation(product2, stockOnHand2, movementQuantity2);
        List<PTVProgramStockInformation> expectedPtvProgramStocksInformation = newArrayList(ptvProgramStockInformation1, ptvProgramStockInformation2);
        when(stockRepository.queryStockCardByProductCode(Constants.PTV_PRODUCT_1_CODE)).thenReturn(stockCard1);
        when(stockRepository.queryStockCardByProductCode(Constants.PTV_PRODUCT_2_CODE)).thenReturn(stockCard2);

        List<PTVProgramStockInformation> ptvProgramStocksInformation = ptvProgramPresenter.generatePTVProgramStockInformation(ptvProductCodes);

        assertThat(ptvProgramStocksInformation, is(expectedPtvProgramStocksInformation));
    }

    private StockCard createStockCardInformation(int stockOnHand, int movementQuantity, Product product) {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(stockOnHand);
        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setMovementType(MovementReasonManager.MovementType.RECEIVE);
        stockMovementItem.setMovementQuantity(movementQuantity);
        stockCard.setForeignStockMovementItems(newArrayList(stockMovementItem));
        stockCard.setProduct(product);
        return stockCard;
    }

    @NonNull
    private Product createProductWithCustomCode(String ptvProductCode) {
        Product product = Product.dummyProduct();
        product.setCode(ptvProductCode);
        return product;
    }

    @NonNull
    private List<String> getProductCodes() {
        List<String> ptvProductCodes = new ArrayList<>();
        ptvProductCodes.add(Constants.PTV_PRODUCT_1_CODE);
        ptvProductCodes.add(Constants.PTV_PRODUCT_2_CODE);
        return ptvProductCodes;
    }

    @NonNull
    private PTVProgramStockInformation createPtvProgramStockInformation(Product product, long stockOnHand, long quantity) {
        PTVProgramStockInformation ptvProgramStockInformation = new PTVProgramStockInformation();
        ptvProgramStockInformation.setProduct(product);
        ptvProgramStockInformation.setInitialStock((int) stockOnHand);
        ptvProgramStockInformation.setEntries((int) quantity);
        ptvProgramStockInformation.setLossesAndAdjustments(0);
        ptvProgramStockInformation.setRequisition(0);
        ptvProgramStockInformation.setPtvProgram(new PTVProgram());
        ptvProgramStockInformation.setCreatedAt(today);
        ptvProgramStockInformation.setUpdatedAt(today);
        return ptvProgramStockInformation;
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(HealthFacilityServiceRepository.class).toInstance(healthFacilityServiceRepository);
            bind(StockRepository.class).toInstance(stockRepository);
        }
    }
}