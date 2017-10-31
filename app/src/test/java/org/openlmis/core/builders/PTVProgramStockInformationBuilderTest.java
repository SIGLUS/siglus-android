package org.openlmis.core.builders;

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
import org.openlmis.core.model.ServiceDispensation;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.HealthFacilityServiceRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.PTVUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import roboguice.RoboGuice;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class PTVProgramStockInformationBuilderTest {

    public static final long DEFAULT_QUANTITY = 0;
    private HealthFacilityServiceRepository healthFacilityServiceRepository;
    private ServiceDispensationBuilder serviceDispensationBuilder;
    private StockRepository stockRepository;
    private ProductRepository productRepository;
    private PTVProgramStockInformationBuilder ptvProgramStockInformationBuilder;

    private Date today = DateTime.now().toDate();
    private List<HealthFacilityService> expectedHealthFacilityServices;
    private long stockOnHand;
    private long movementQuantity;
    private PTVProgram ptvProgram;

    @Before
    public void setUp() throws Exception {
        ptvProgram = new PTVProgram();
        expectedHealthFacilityServices = PTVUtil.createDummyHealthFacilityServices();

        healthFacilityServiceRepository = mock(HealthFacilityServiceRepository.class);
        stockRepository = mock(StockRepository.class);
        productRepository = mock(ProductRepository.class);
        serviceDispensationBuilder = mock(ServiceDispensationBuilder.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new PTVProgramStockInformationBuilderTest.MyTestModule());
        ptvProgramStockInformationBuilder = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PTVProgramStockInformationBuilder.class);
    }

    @Test
    public void shouldReturnPTVProgramStocksInformationWhenStockCardInformationExistsForPTVProducts() throws LMISException {
        List<PTVProgramStockInformation> expectedPtvProgramStocksInformation = new ArrayList<>();
        when(healthFacilityServiceRepository.getAll()).thenReturn(expectedHealthFacilityServices);
        List<Product> products = PTVUtil.getProductsWithPTVProductCodes();
        when(productRepository.getProductsByCodes(PTVUtil.ptvProductCodes)).thenReturn(products);
        for (Product product : products) {
            stockOnHand = getRandomLong();
            movementQuantity = getRandomLong();
            StockCard stockCard = createStockCardInformation(product);
            PTVProgramStockInformation ptvProgramStockInformation = createPtvProgramStockInformation(product);
            ptvProgramStockInformation.setInitialStock(setInitialStock());
            List<ServiceDispensation> serviceDispensations = PTVUtil.createDummyServiceDispensations(ptvProgramStockInformation);
            when(serviceDispensationBuilder.buildInitialServiceDispensations(ptvProgramStockInformation)).thenReturn(serviceDispensations);
            when(stockRepository.queryStockCardByProductId(product.getId())).thenReturn(stockCard);
            expectedPtvProgramStocksInformation.add(ptvProgramStockInformation);
        }

        List<PTVProgramStockInformation> actualPtvProgramStocksInformation = ptvProgramStockInformationBuilder.buildPTVProgramStockInformation(ptvProgram);

        assertThat(actualPtvProgramStocksInformation, is(expectedPtvProgramStocksInformation));
    }

    private long getRandomLong() {
        return 1 + (long) (Math.random() * (999));
    }

    private long setInitialStock() {
        long initialStock = stockOnHand - movementQuantity;
        return initialStock > 0 ? initialStock : 0;
    }

    @Test
    public void shouldReturnPTVProgramStocksInformationWhenStockCardInformationDoesNotExist() throws LMISException {
        stockOnHand = DEFAULT_QUANTITY;
        movementQuantity = DEFAULT_QUANTITY;
        List<PTVProgramStockInformation> expectedPtvProgramStocksInformation = new ArrayList<>();
        when(healthFacilityServiceRepository.getAll()).thenReturn(expectedHealthFacilityServices);
        List<Product> products = PTVUtil.getProductsWithPTVProductCodes();
        when(productRepository.getProductsByCodes(PTVUtil.ptvProductCodes)).thenReturn(products);
        for (Product product : products) {
            StockCard stockCard = null;
            PTVProgramStockInformation ptvProgramStockInformation = createPtvProgramStockInformation(product);
            List<ServiceDispensation> serviceDispensations = PTVUtil.createDummyServiceDispensations(ptvProgramStockInformation);
            when(serviceDispensationBuilder.buildInitialServiceDispensations(ptvProgramStockInformation)).thenReturn(serviceDispensations);
            when(stockRepository.queryStockCardByProductCode(product.getCode())).thenReturn(stockCard);
            expectedPtvProgramStocksInformation.add(ptvProgramStockInformation);
        }

        List<PTVProgramStockInformation> actualPtvProgramStocksInformation = ptvProgramStockInformationBuilder.buildPTVProgramStockInformation(ptvProgram);

        assertThat(actualPtvProgramStocksInformation, is(expectedPtvProgramStocksInformation));
    }

    private StockCard createStockCardInformation(Product product) {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(stockOnHand);
        StockMovementItem stockMovementItemReceive = new StockMovementItem();
        stockMovementItemReceive.setMovementType(MovementReasonManager.MovementType.RECEIVE);
        stockMovementItemReceive.setMovementQuantity(movementQuantity);
        stockCard.setForeignStockMovementItems(newArrayList(stockMovementItemReceive));
        stockCard.setProduct(product);
        return stockCard;
    }

    @NonNull
    private PTVProgramStockInformation createPtvProgramStockInformation(Product product) throws LMISException {
        PTVProgramStockInformation ptvProgramStockInformation = new PTVProgramStockInformation();
        ptvProgramStockInformation.setProduct(product);
        ptvProgramStockInformation.setInitialStock(stockOnHand);
        ptvProgramStockInformation.setEntries(movementQuantity);
        ptvProgramStockInformation.setLossesAndAdjustments(DEFAULT_QUANTITY);
        ptvProgramStockInformation.setRequisition(DEFAULT_QUANTITY);
        ptvProgramStockInformation.setPtvProgram(new PTVProgram());
        ptvProgramStockInformation.setCreatedAt(today);
        ptvProgramStockInformation.setUpdatedAt(today);
        ptvProgramStockInformation.setServiceDispensations(serviceDispensationBuilder.buildInitialServiceDispensations(ptvProgramStockInformation));
        return ptvProgramStockInformation;
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(HealthFacilityServiceRepository.class).toInstance(healthFacilityServiceRepository);
            bind(ServiceDispensationBuilder.class).toInstance(serviceDispensationBuilder);
            bind(StockRepository.class).toInstance(stockRepository);
            bind(ProductRepository.class).toInstance(productRepository);
        }
    }
}