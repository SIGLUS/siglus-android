package org.openlmis.core.service;


import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.repository.DirtyDataRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class DirtyDataManagerTest {

    DirtyDataManager dirtyDataManager;
    StockMovementRepository stockMovementRepository;
    StockRepository stockRepository;
    RnrFormRepository rnrFormRepository;
    ProgramRepository programRepository;
    DirtyDataRepository dirtyDataRepository;
    Product product1;
    Product product2;
    Product product3;
    Product product4;
    StockCard stockCard1;
    StockCard stockCard2;
    StockCard stockCard3;
    StockCard stockCard4;

    StockMovementItem stockMovementItem11;
    StockMovementItem stockMovementItem12;
    StockMovementItem stockMovementItem21;
    StockMovementItem stockMovementItem22;
    StockMovementItem stockMovementItem31;
    StockMovementItem stockMovementItem32;
    StockMovementItem stockMovementItem41;
    StockMovementItem stockMovementItem42;
    StockMovementItem stockMovementItem43;


    @Before
    public void setUp() {
        dirtyDataManager = mock(DirtyDataManager.class);
        stockMovementRepository = mock(StockMovementRepository.class);
        stockRepository = mock(StockRepository.class);
        rnrFormRepository = mock(RnrFormRepository.class);
        programRepository = mock(ProgramRepository.class);
        dirtyDataRepository = mock(DirtyDataRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        dirtyDataManager = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(DirtyDataManager.class);
        mockAllStockCard();
        User user = new User("user", "123");
        user.setFacilityCode("FC1");
        user.setFacilityId("123");
        UserInfoMgr.getInstance().setUser(user);
    }

    @Test
    public void shouldScanTheNewTwoAndDeleteTheWrongMovement() throws LMISException {
        //Given
        List<StockCard> list = Arrays.asList(stockCard1, stockCard2);
        List<StockMovementItem> stockMovementsForStockCard1 = Arrays.asList(stockMovementItem11, stockMovementItem12);
        List<StockMovementItem> stockMovementsForStockCard2 = Arrays.asList(stockMovementItem21, stockMovementItem22);
        when(stockRepository.list()).thenReturn(list);
        when(stockMovementRepository.listLastTwoStockMovements()).thenReturn(stockMovementsForStockCard1);
        when(stockMovementRepository.listLastTwoStockMovements()).thenReturn(stockMovementsForStockCard2);
        when(stockMovementRepository.queryMovementByStockCardId(2)).thenReturn(stockMovementsForStockCard2);

        //When
        List<StockCard> wrongStockCards = dirtyDataManager.correctData();

        //Then
        assertThat(wrongStockCards.size(), is(1));
        assertThat(wrongStockCards.get(0).getProduct().getCode(), is("productCode2"));
    }

    @Test
    public void shouldScanAllMovementAndDeleteTheWrongMovement() throws LMISException {
        //Given
        List<StockCard> list = Arrays.asList(stockCard3, stockCard4);
        List<StockMovementItem> stockMovementItems3 = Arrays.asList(stockMovementItem31, stockMovementItem32);
        List<StockMovementItem> stockMovementItems4 = Arrays.asList(stockMovementItem41, stockMovementItem42, stockMovementItem43);

        //When
        when(stockRepository.list()).thenReturn(list);
        when(stockMovementRepository.queryMovementByStockCardId(3)).thenReturn(stockMovementItems3);
        when(stockMovementRepository.queryMovementByStockCardId(4)).thenReturn(stockMovementItems4);
        List<StockCard> wrongStockCards = dirtyDataManager.scanAllStockMovements();

        //Then
        assertThat(wrongStockCards.size(), is(1));
        assertThat(wrongStockCards.get(0).getProduct().getCode(), is("productCode4"));
    }

    @Test
    public void shouldScanDuplicateInventory() throws LMISException {
        Product product = ProductBuilder.create()
                .setCode("productCode1")
                .setProductId(1l)
                .setIsActive(true)
                .setStrength("serious1")
                .setPrimaryName("Primary product name1").build();
        StockCard stockCard = new StockCard();
        StockMovementItem stockMovementItem = new StockMovementItemBuilder()
                .withStockOnHand(12)
                .withQuantity(1)
                .withMovementType(MovementReasonManager.MovementType.INITIAL_INVENTORY)
                .build();
        StockMovementItem stockMovementItem1 = new StockMovementItemBuilder()
                .withStockOnHand(11)
                .withQuantity(0)
                .withMovementType(MovementReasonManager.MovementType.INITIAL_INVENTORY)
                .build();
        stockCard.setId(1l);
        stockCard.setProduct(product);
        stockCard.setStockOnHand(11);
        stockMovementItem.setStockCard(stockCard);
        stockMovementItem1.setStockCard(stockCard);

        List<StockCard> list = Arrays.asList(stockCard);
        List<StockMovementItem> duplicateMovement = Arrays.asList(stockMovementItem, stockMovementItem1);

        when(stockMovementRepository.listLastTwoStockMovements()).thenReturn(duplicateMovement);

        List<StockCard> wrongStockCards = dirtyDataManager.correctDataForStockCardOverView(list);

        assertThat(wrongStockCards.size(), is(1));
        assertThat(wrongStockCards.get(0).getProduct().getCode(), is("productCode1"));
    }


    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockMovementRepository.class).toInstance(stockMovementRepository);
            bind(StockRepository.class).toInstance(stockRepository);
            bind(RnrFormRepository.class).toInstance(rnrFormRepository);
            bind(ProgramRepository.class).toInstance(programRepository);
            bind(DirtyDataRepository.class).toInstance(dirtyDataRepository);
        }
    }

    private void mockAllStockCard() {
        product1 = ProductBuilder.create()
                .setCode("productCode1")
                .setProductId(1l)
                .setIsActive(true)
                .setStrength("serious1")
                .setPrimaryName("Primary product name1").build();
        stockCard1 = new StockCard();
        stockMovementItem11 = new StockMovementItemBuilder()
                .withStockOnHand(12)
                .withQuantity(1)
                .withMovementType(MovementReasonManager.MovementType.NEGATIVE_ADJUST)
                .build();
        stockMovementItem12 = new StockMovementItemBuilder()
                .withStockOnHand(11)
                .withQuantity(1)
                .withMovementType(MovementReasonManager.MovementType.NEGATIVE_ADJUST)
                .build();
        stockCard1.setId(1l);
        stockCard1.setProduct(product1);
        stockCard1.setStockOnHand(11);
        stockMovementItem11.setStockCard(stockCard1);
        stockMovementItem12.setStockCard(stockCard1);

        product2 = ProductBuilder.create()
                .setCode("productCode2")
                .setProductId(2l)
                .setIsActive(true)
                .setStrength("serious2")
                .setPrimaryName("Primary product name2").build();
        stockCard2 = new StockCard();
        stockMovementItem21 = new StockMovementItemBuilder()
                .withStockOnHand(12)
                .withQuantity(0)
                .withMovementType(MovementReasonManager.MovementType.INITIAL_INVENTORY)
                .build();
        stockMovementItem22 = new StockMovementItemBuilder()
                .withStockOnHand(13)
                .withQuantity(2)
                .withMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST)
                .build();
        stockCard2.setId(2l);
        stockCard2.setProduct(product2);
        stockCard2.setStockOnHand(13);
        stockMovementItem21.setStockCard(stockCard2);
        stockMovementItem22.setStockCard(stockCard2);


        product3 = ProductBuilder.create()
                .setCode("productCode3")
                .setProductId(3l)
                .setIsActive(true)
                .setStrength("serious3")
                .setPrimaryName("Primary product name3").build();
        stockCard3 = new StockCard();
        stockMovementItem31 = new StockMovementItemBuilder()
                .withStockOnHand(12)
                .withQuantity(0)
                .withMovementType(MovementReasonManager.MovementType.INITIAL_INVENTORY)
                .build();
        stockMovementItem31.setId(31);
        stockMovementItem32 = new StockMovementItemBuilder()
                .withStockOnHand(13)
                .withQuantity(1)
                .withMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST)
                .build();
        stockMovementItem32.setId(32);
        stockCard3.setId(3l);
        stockCard3.setProduct(product3);
        stockCard3.setStockOnHand(13);
        stockMovementItem31.setStockCard(stockCard3);
        stockMovementItem32.setStockCard(stockCard3);


        product4 = ProductBuilder.create()
                .setCode("productCode4")
                .setProductId(4l)
                .setIsActive(true)
                .setStrength("serious4")
                .setPrimaryName("Primary product name4").build();
        stockCard4 = new StockCard();
        stockMovementItem41 = new StockMovementItemBuilder()
                .withStockOnHand(12)
                .withQuantity(0)
                .withMovementType(MovementReasonManager.MovementType.INITIAL_INVENTORY)
                .build();
        stockMovementItem41.setId(41);
        stockMovementItem42 = new StockMovementItemBuilder()
                .withStockOnHand(13)
                .withQuantity(2)
                .withMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST)
                .build();
        stockMovementItem42.setId(42);
        stockMovementItem43 = new StockMovementItemBuilder()
                .withStockOnHand(15)
                .withQuantity(2)
                .withMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST)
                .build();
        stockMovementItem43.setId(43);
        stockCard4.setId(4l);
        stockCard4.setProduct(product4);
        stockCard4.setStockOnHand(15);
        stockMovementItem41.setStockCard(stockCard4);
        stockMovementItem42.setStockCard(stockCard4);
        stockMovementItem43.setStockCard(stockCard4);
    }
}
