package org.openlmis.core.presenter;

import android.support.annotation.NonNull;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.KitProductBuilder;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModelBuilder;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModelBuilder;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;
import rx.Subscription;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class UnpackKitPresenterTest {
    private UnpackKitPresenter presenter;
    private ProductRepository productRepository;
    private StockRepository stockRepository;
    private Product product;
    private InventoryViewModel viewModel;
    private String signature;
    private String documentNumber;

    @Before
    public void setup() throws Exception {
        productRepository = mock(ProductRepository.class);
        stockRepository = mock(StockRepository.class);

        List<String> expireDates = new ArrayList<>();
        expireDates.add("15/2/2026");
        expireDates.add("30/5/2026");

        signature = "super";
        documentNumber = "test";

        product = new ProductBuilder().setIsKit(false).setCode("productCode1").setPrimaryName("name1").setProductId(200L).build();
        viewModel = new InventoryViewModelBuilder(product).setChecked(true).setKitExpectQuantity(300).setQuantity("200").setExpiryDates(expireDates).build();

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProductRepository.class).toInstance(productRepository);
                bind(StockRepository.class).toInstance(stockRepository);
            }
        });

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(UnpackKitPresenter.class);
    }

    @Test
    public void shouldLoadKitProductList() throws Exception {
        Product kit = prepareKit();

        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_auto_quantities_in_kit, true);

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        presenter.subscriptions.add(presenter.getKitProductsObservable("KIT_Code", 3).subscribe(subscriber));
        subscriber.awaitTerminalEvent();

        verify(productRepository).queryKitProductByKitCode(kit.getCode());

        List<InventoryViewModel> resultProducts = presenter.getInventoryViewModels();
        assertThat(resultProducts.size()).isEqualTo(2);

        InventoryViewModel viewModel1 = resultProducts.get(0);
        InventoryViewModel viewModel2 = resultProducts.get(1);

        assertThat(viewModel1.getProduct().getCode()).isEqualTo("P1_Code");
        assertThat(viewModel2.getProduct().getCode()).isEqualTo("P2_Code");
        assertThat(viewModel1.getKitExpectQuantity()).isEqualTo(300);
        assertThat(viewModel2.getKitExpectQuantity()).isEqualTo(600);
        assertThat(viewModel2.getQuantity()).isEqualTo("600");
        assertTrue(viewModel1.isChecked());
        assertTrue(viewModel2.isChecked());
    }

    @NonNull
    private Product prepareKit() throws LMISException {
        //given
        Product kit = ProductBuilder.create().setCode("KIT_Code").setIsKit(true).build();
        Product product1 = ProductBuilder.create().setPrimaryName("p1").setProductId(1L).setCode("P1_Code").setIsKit(false).build();
        Product product2 = ProductBuilder.create().setPrimaryName("p2").setProductId(2L).setCode("P2_Code").setIsKit(false).build();
        KitProduct kitProduct1 = KitProductBuilder.create().setKitCode("KIT_Code").setProductCode("P1_Code").setQuantity(100).build();
        KitProduct kitProduct2 = KitProductBuilder.create().setKitCode("KIT_Code").setProductCode("P2_Code").setQuantity(200).build();

        List<KitProduct> kitProducts = Arrays.asList(kitProduct1, kitProduct2);
        kit.setKitProductList(kitProducts);

        when(productRepository.queryKitProductByKitCode(kit.getCode())).thenReturn(kitProducts);
        when(productRepository.getByCode(product1.getCode())).thenReturn(product1);
        when(productRepository.getByCode(product2.getCode())).thenReturn(product2);
        return kit;
    }

    public void shouldNotSetQuantityWhenToggleOff() throws LMISException {
        //given
        Product kit = prepareKit();

        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_auto_quantities_in_kit, false);

        presenter.getKitProductsObservable("KIT_Code", 3);

        verify(productRepository).queryKitProductByKitCode(kit.getCode());

        List<InventoryViewModel> resultProducts = presenter.getInventoryViewModels();
        assertThat(resultProducts.size()).isEqualTo(2);

        InventoryViewModel viewModel1 = resultProducts.get(0);
        InventoryViewModel viewModel2 = resultProducts.get(1);

        assertNull(viewModel1.getQuantity());
        assertNull(viewModel2.getQuantity());
    }

    @Test
    public void shouldSaveStockCardAndStockMovementAndUpdateProductAsNotArchived() throws Exception {
        Product product2 = new ProductBuilder().setIsKit(false).setCode("productCode2").setPrimaryName("name2").setProductId(333L).setIsArchived(true).build();
        InventoryViewModel viewModel2 = new InventoryViewModelBuilder(product2).setChecked(true).setKitExpectQuantity(300).setQuantity("200").build();

        StockCard productStockCard = new StockCardBuilder().setStockCardId(111)
                .setStockOnHand(100)
                .setCreateDate(new Date())
                .setProduct(product)
                .setExpireDates("20/1/2026,15/2/2026")
                .build();

        StockCard productStockCard2 = new StockCardBuilder().setStockCardId(222)
                .setStockOnHand(100)
                .setCreateDate(new Date())
                .setProduct(product2)
                .build();

        Product kit = new ProductBuilder().setIsKit(true).setProductId(888L)
                .setCode("SD1112").setPrimaryName("primary name").build();

        StockCard kitStockCard = new StockCardBuilder().setStockCardId(112)
                .setStockOnHand(1000)
                .setCreateDate(new Date())
                .setProduct(kit)
                .build();


        presenter.getInventoryViewModels().addAll(Arrays.asList(viewModel, viewModel2));
        presenter.kitCode = "SD1112";

        when(stockRepository.queryStockCardByProductId(200L)).thenReturn(productStockCard);
        when(stockRepository.queryStockCardByProductId(333L)).thenReturn(productStockCard2);

        when(productRepository.getByCode("SD1112")).thenReturn(kit);
        when(stockRepository.queryStockCardByProductId(888L)).thenReturn(kitStockCard);

        TestSubscriber subscriber = new TestSubscriber();
        Subscription subscription = presenter.saveUnpackProductsObservable(2, documentNumber, signature).subscribe(subscriber);
        presenter.subscriptions.add(subscription);

        subscriber.awaitTerminalEvent();
        verify(stockRepository).batchSaveUnpackStockCardsWithMovementItemsAndUpdateProduct(anyList());

        assertThat(productStockCard.getStockOnHand()).isEqualTo(300);
        assertThat(productStockCard.getExpireDates()).isEqualTo("20/1/2026,15/2/2026,30/5/2026");
        assertThat(kitStockCard.getStockOnHand()).isEqualTo(998);
        assertThat(product2.isArchived());
    }

    @Test
    public void shouldCreateStockCardWithMovementItemsWhenStockCardNotExists() throws Exception {
        when(stockRepository.queryStockCardByProductId(200L)).thenReturn(null);
        viewModel.setQuantity("10");

        StockCard stockCard = presenter.createStockCardForProduct(viewModel, documentNumber, signature);
        List<StockMovementItem> movementItems = stockCard.getStockMovementItemsWrapper();

        assertThat(stockCard.getStockOnHand()).isEqualTo(10);
        assertThat(movementItems.size()).isEqualTo(2);
        assertThat(movementItems.get(0).getStockOnHand()).isEqualTo(0);
        assertThat(movementItems.get(0).getSignature()).isEqualTo(null);
        assertThat(movementItems.get(1).getStockOnHand()).isEqualTo(10);
        assertThat(movementItems.get(1).getSignature()).isEqualTo(signature);
        assertThat(movementItems.get(1).getDocumentNumber()).isEqualTo(documentNumber);
    }

    @Test
    public void shouldCreateStockCardForProductWithLot() throws Exception {
        when(stockRepository.queryStockCardByProductId(200L)).thenReturn(null);
        viewModel.setQuantity("10");
        LotMovementViewModel lot = new LotMovementViewModelBuilder()
                .setLotNumber("test")
                .setLotSOH("100")
                .setMovementType(MovementReasonManager.MovementType.RECEIVE)
                .setExpiryDate("Aug 2016")
                .setQuantity("100")
                .build();

        viewModel.setLotMovementViewModelList(newArrayList(lot, lot));

        StockCard stockCard = presenter.createStockCardForProductWithLot(viewModel, documentNumber, signature);
        List<StockMovementItem> movementItems = stockCard.getStockMovementItemsWrapper();

        assertThat(stockCard.getStockOnHand()).isEqualTo(200L);
        assertThat(movementItems.size()).isEqualTo(2);
        assertThat(movementItems.get(0).getStockOnHand()).isEqualTo(0);
        assertThat(movementItems.get(0).getSignature()).isEqualTo(null);
        assertThat(movementItems.get(1).getStockOnHand()).isEqualTo(200L);
        assertThat(movementItems.get(1).getSignature()).isEqualTo(signature);
        assertThat(movementItems.get(1).getDocumentNumber()).isEqualTo(documentNumber);
        assertThat(movementItems.get(1).getLotMovementItemListWrapper().size()).isEqualTo(2);
        assertThat(movementItems.get(1).getLotMovementItemListWrapper().get(0).getMovementQuantity()).isEqualTo(100L);
        assertThat(movementItems.get(1).getLotMovementItemListWrapper().get(0).getLot().getLotNumber()).isEqualTo("test");
    }

    @Test
    public void shouldGetStockCardWithMovementItemsWhenStockCardExists() throws Exception {
        StockCard productStockCard = new StockCardBuilder().setStockCardId(111)
                .setStockOnHand(100)
                .setCreateDate(new Date())
                .setProduct(product)
                .setExpireDates("20/1/2026,15/2/2026")
                .build();
        viewModel.setQuantity("10");

        when(stockRepository.queryStockCardByProductId(200L)).thenReturn(productStockCard);

        StockCard stockCard = presenter.createStockCardForProduct(viewModel, documentNumber, signature);
        List<StockMovementItem> movementItems = stockCard.getStockMovementItemsWrapper();

        assertThat(stockCard.getStockOnHand()).isEqualTo(110);
        assertThat(movementItems.size()).isEqualTo(1);
        assertThat(movementItems.get(0).getStockOnHand()).isEqualTo(110);
        assertThat(movementItems.get(0).getSignature()).isEqualTo(signature);
        assertThat(movementItems.get(0).getDocumentNumber()).isEqualTo(documentNumber);
        assertThat(stockCard.getExpireDates()).isEqualTo("20/1/2026,15/2/2026,30/5/2026");
    }

    @Test
    public void shouldRemoveExpiryDateWhenSOHIsZero() throws Exception {
        Product kit = new ProductBuilder().setIsKit(true).setProductId(888L)
                .setCode("SD1112").setPrimaryName("primary name").build();

        StockCard kitStockCard = new StockCardBuilder().setStockCardId(112)
                .setStockOnHand(1000)
                .setCreateDate(new Date())
                .setProduct(kit)
                .setExpireDates("2015-11-11")
                .build();

        presenter.kitCode = kit.getCode();

        when(productRepository.getByCode(kit.getCode())).thenReturn(kit);
        when(stockRepository.queryStockCardByProductId(888)).thenReturn(kitStockCard);

        StockCard stockCardForKit = presenter.getStockCardForKit(1000, documentNumber, signature);

        assertThat(stockCardForKit.getExpireDates()).isEqualTo("");
    }

    @Test
    public void shouldGetKitStockCardWithUnpackMovementItem() throws Exception {
        Product kit = new ProductBuilder().setIsKit(true).setProductId(888L)
                .setCode("SD1112").setPrimaryName("primary name").build();

        StockCard kitStockCard = new StockCardBuilder().setStockCardId(112)
                .setStockOnHand(1000)
                .setCreateDate(new Date())
                .setProduct(kit)
                .build();

        presenter.kitCode = "SD1112";
        when(productRepository.getByCode("SD1112")).thenReturn(kit);
        when(stockRepository.queryStockCardByProductId(888L)).thenReturn(kitStockCard);

        StockCard stockCardWithMovementItems = presenter.getStockCardForKit(1, documentNumber, signature);

        assertThat(stockCardWithMovementItems.getStockOnHand()).isEqualTo(999);
        assertThat(stockCardWithMovementItems.getStockMovementItemsWrapper().size()).isEqualTo(1);
        assertThat(stockCardWithMovementItems.getStockMovementItemsWrapper().get(0).getMovementQuantity()).isEqualTo(1);
        assertThat(stockCardWithMovementItems.getStockMovementItemsWrapper().get(0).getStockOnHand()).isEqualTo(999);
        assertThat(stockCardWithMovementItems.getStockMovementItemsWrapper().get(0).getSignature()).isEqualTo(signature);
        assertThat(stockCardWithMovementItems.getStockMovementItemsWrapper().get(0).getDocumentNumber()).isEqualTo(documentNumber);
    }
}