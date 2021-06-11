package org.openlmis.core.presenter;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import androidx.annotation.NonNull;
import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openlmis.core.LMISTestRunner;
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
import roboguice.RoboGuice;
import rx.Subscription;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
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

    product = new ProductBuilder().setIsKit(false).setCode("productCode1").setPrimaryName("name1")
        .setProductId(200L).build();
    viewModel = new InventoryViewModelBuilder(product).setChecked(true).setKitExpectQuantity(300)
        .build();

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProductRepository.class).toInstance(productRepository);
        bind(StockRepository.class).toInstance(stockRepository);
      }
    });

    presenter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(UnpackKitPresenter.class);
  }

  @Test
  public void shouldLoadKitProductList() throws Exception {
    Product kit = prepareKit();

    TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
    presenter.subscriptions
        .add(presenter.getKitProductsObservable("KIT_Code", 3).subscribe(subscriber));
    subscriber.awaitTerminalEvent();

    verify(productRepository).queryKitProductByKitCode(kit.getCode());

    List<InventoryViewModel> resultProducts = presenter.getInventoryViewModels();
    assertThat(resultProducts.size(), is(2));

    InventoryViewModel viewModel1 = resultProducts.get(0);
    InventoryViewModel viewModel2 = resultProducts.get(1);

    assertThat(viewModel1.getProduct().getCode(), is("P1_Code"));
    assertThat(viewModel2.getProduct().getCode(), is("P2_Code"));
    assertThat(viewModel1.getKitExpectQuantity(), is(300L));
    assertThat(viewModel2.getKitExpectQuantity(), is(600L));
    assertTrue(viewModel1.isChecked());
    assertTrue(viewModel2.isChecked());
  }

  @NonNull
  private Product prepareKit() throws LMISException {
    //given
    Product kit = ProductBuilder.create().setCode("KIT_Code").setIsKit(true).build();
    Product product1 = ProductBuilder.create().setPrimaryName("p1").setProductId(1L)
        .setCode("P1_Code").setIsKit(false).build();
    Product product2 = ProductBuilder.create().setPrimaryName("p2").setProductId(2L)
        .setCode("P2_Code").setIsKit(false).build();
    KitProduct kitProduct1 = KitProductBuilder.create().setKitCode("KIT_Code")
        .setProductCode("P1_Code").setQuantity(100).build();
    KitProduct kitProduct2 = KitProductBuilder.create().setKitCode("KIT_Code")
        .setProductCode("P2_Code").setQuantity(200).build();

    List<KitProduct> kitProducts = Arrays.asList(kitProduct1, kitProduct2);
    kit.setKitProductList(kitProducts);

    when(productRepository.queryKitProductByKitCode(kit.getCode())).thenReturn(kitProducts);
    when(productRepository.getByCode(product1.getCode())).thenReturn(product1);
    when(productRepository.getByCode(product2.getCode())).thenReturn(product2);
    return kit;
  }

  @Test
  public void shouldSaveStockCardAndStockMovementAndUpdateProductAsNotArchived() throws Exception {
    //product without stock card
    Product product1 = new ProductBuilder().setIsKit(false).setCode("p1").setPrimaryName("name1")
        .setProductId(1L).setIsArchived(false).build();
    when(stockRepository.queryStockCardByProductId(1L)).thenReturn(null);

    //product with stock card but archived
    Product product2 = new ProductBuilder().setIsKit(false).setCode("p2").setPrimaryName("name2")
        .setProductId(2L).setIsArchived(true).build();
    StockCard stockCard = new StockCardBuilder().setProduct(product2).setStockOnHand(10L)
        .setCreateDate(new Date()).build();
    when(stockRepository.queryStockCardByProductId(2L)).thenReturn(stockCard);

    //kit product
    Product kit = new ProductBuilder().setIsKit(true).setProductId(3L).setCode("SD1112")
        .setPrimaryName("primary name").build();
    when(productRepository.getByCode(anyString())).thenReturn(kit);
    StockCard kitStockCard = new StockCardBuilder().setStockCardId(112).setStockOnHand(1000)
        .setCreateDate(new Date()).setProduct(kit).build();
    when(stockRepository.queryStockCardByProductId(3L)).thenReturn(kitStockCard);

    InventoryViewModel product1VM = new InventoryViewModelBuilder(product1).setChecked(true)
        .setKitExpectQuantity(300).build();
    product1VM.getNewLotMovementViewModelList().add(
        new LotMovementViewModelBuilder().setLotNumber("some lot").setExpiryDate("Feb 2022")
            .setQuantity("200").build());
    InventoryViewModel product2VM = new InventoryViewModelBuilder(product2).setChecked(true)
        .setKitExpectQuantity(100).build();
    product2VM.getNewLotMovementViewModelList().add(
        new LotMovementViewModelBuilder().setLotNumber("some lot").setExpiryDate("Feb 2022")
            .setQuantity("100").build());

    presenter.getInventoryViewModels().addAll(Arrays.asList(product1VM, product2VM));
    presenter.kitCode = "SD1112";

    TestSubscriber subscriber = new TestSubscriber();
    Subscription subscription = presenter.saveUnpackProductsObservable(2, documentNumber, signature)
        .subscribe(subscriber);
    presenter.subscriptions.add(subscription);

    subscriber.awaitTerminalEvent();

    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    verify(stockRepository)
        .batchSaveUnpackStockCardsWithMovementItemsAndUpdateProduct(argument.capture());
    List<List> stockCardsArguments = argument.getAllValues();
    assertEquals(200L, ((StockCard) stockCardsArguments.get(0).get(0)).getStockOnHand(), 0L);
    assertFalse(
        ((StockCard) stockCardsArguments.get(0).get(0)).getStockMovementItemsWrapper().isEmpty());
    assertEquals(110L, ((StockCard) stockCardsArguments.get(0).get(1)).getStockOnHand(), 0L);
    assertFalse(((StockCard) stockCardsArguments.get(0).get(1)).getProduct().isArchived());
    assertFalse(
        ((StockCard) stockCardsArguments.get(0).get(1)).getStockMovementItemsWrapper().isEmpty());
    assertEquals(998L, ((StockCard) stockCardsArguments.get(0).get(2)).getStockOnHand(), 0L);
  }

  @Test
  public void shouldCreateStockCardForProductWithLot() throws Exception {
    when(stockRepository.queryStockCardByProductId(200L)).thenReturn(null);
    LotMovementViewModel lot = new LotMovementViewModelBuilder()
        .setLotNumber("test")
        .setLotSOH("100")
        .setMovementType(MovementReasonManager.MovementType.RECEIVE)
        .setExpiryDate("Aug 2016")
        .setQuantity("100")
        .build();

    viewModel.setNewLotMovementViewModelList(newArrayList(lot, lot));

    StockCard stockCard = presenter
        .createStockCardForProductWithLot(viewModel, documentNumber, signature);
    List<StockMovementItem> movementItems = stockCard.getStockMovementItemsWrapper();

    assertThat(stockCard.getStockOnHand(), is(200L));
    assertThat(movementItems.size(), is(2));
    assertThat(movementItems.get(0).getStockOnHand(), is(0L));
    assertNull(movementItems.get(0).getSignature());
    assertThat(movementItems.get(1).getStockOnHand(), is(200L));
    assertThat(movementItems.get(1).getSignature(), is(signature));
    assertThat(movementItems.get(1).getDocumentNumber(), is(documentNumber));
    assertThat(movementItems.get(1).getLotMovementItemListWrapper().size(), is(2));
    assertThat(movementItems.get(1).getLotMovementItemListWrapper().get(0).getMovementQuantity(),
        is(100L));
    assertThat(movementItems.get(1).getLotMovementItemListWrapper().get(0).getLot().getLotNumber(),
        is("test"));
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

    StockCard stockCardWithMovementItems = presenter
        .getStockCardForKit(1, documentNumber, signature);

    assertThat(stockCardWithMovementItems.getStockOnHand(), is(999L));
    assertThat(stockCardWithMovementItems.getStockMovementItemsWrapper().size(), is(1));
    assertThat(
        stockCardWithMovementItems.getStockMovementItemsWrapper().get(0).getMovementQuantity(),
        is(1L));
    assertThat(stockCardWithMovementItems.getStockMovementItemsWrapper().get(0).getStockOnHand(),
        is(999L));
    assertThat(stockCardWithMovementItems.getStockMovementItemsWrapper().get(0).getSignature(),
        is(signature));
    assertThat(stockCardWithMovementItems.getStockMovementItemsWrapper().get(0).getDocumentNumber(),
        is(documentNumber));
  }
}