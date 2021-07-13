package org.openlmis.core.presenter;

import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.util.LongSparseArray;
import androidx.annotation.NonNull;
import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModelBuilder;
import org.openlmis.core.view.viewmodel.LotMovementViewModelBuilder;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.Observable;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

@RunWith(LMISTestRunner.class)
public class InitialInventoryPresenterTest extends LMISRepositoryUnitTest {

  private final String product1PrimaryName = "Product 1";
  private final String product4PrimaryName = "Product 4";
  private InitialInventoryPresenter initialInventoryPresenter;

  StockRepository stockRepositoryMock;
  ProductRepository productRepositoryMock;
  InventoryPresenter.InventoryView view;
  private Product product;
  private SharedPreferenceMgr sharedPreferenceMgr;
  private InventoryRepository mockInventoryRepository;
  private LongSparseArray<InventoryViewModel> models;
  private final int DEFAULT_ID = 0;

  @Before
  public void setup() throws Exception {
    stockRepositoryMock = mock(StockRepository.class);
    productRepositoryMock = mock(ProductRepository.class);
    mockInventoryRepository = mock(InventoryRepository.class);
    sharedPreferenceMgr = mock(SharedPreferenceMgr.class);

    view = mock(InventoryPresenter.InventoryView.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

    initialInventoryPresenter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(InitialInventoryPresenter.class);
    initialInventoryPresenter.attachView(view);

    product = new Product();
    product.setPrimaryName("Test Product");
    product.setCode("ABC");

    RxAndroidPlugins.getInstance().reset();
    RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
      @Override
      public Scheduler getMainThreadScheduler() {
        return Schedulers.immediate();
      }
    });
    models = new LongSparseArray<>();
    getInventoryViewModelList();
  }

  @NonNull
  private void getInventoryViewModelList() {
    product.setBasic(true);
    product.setPrimaryName(product1PrimaryName);
    product.setId(1L);
    InventoryViewModel inventoryViewModel = new InventoryViewModel(product);
    Product product2 = new Product();
    product2.setPrimaryName("Product 2");
    product2.setCode("BCD");
    product2.setBasic(true);
    product2.setId(2L);
    InventoryViewModel inventoryViewModel2 = new InventoryViewModel(product2);
    Product product3 = new Product();
    product3.setPrimaryName("Product 3");
    product3.setCode("BCDE");
    product3.setBasic(true);
    product3.setId(3L);
    InventoryViewModel inventoryViewModel3 = new InventoryViewModel(product3);
    Product product4 = new Product();
    product4.setPrimaryName(product4PrimaryName);
    product4.setCode("BCDEF");
    product4.setBasic(false);
    product4.setId(4L);
    InventoryViewModel inventoryViewModel4 = new InventoryViewModel(product4);

    models.put(inventoryViewModel.getProduct().getId(), inventoryViewModel);
    models.put(inventoryViewModel2.getProduct().getId(), inventoryViewModel2);
    models.put(inventoryViewModel3.getProduct().getId(), inventoryViewModel3);
    models.put(inventoryViewModel4.getProduct().getId(), inventoryViewModel4);
  }

  @After
  public void tearDown() {
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldLoadMasterProductsList() throws LMISException {
    StockCard stockCardVIA = StockCardBuilder.buildStockCard();
    Product productVIA = new ProductBuilder().setPrimaryName("VIA Product").setCode("VIA Code")
        .build();
    stockCardVIA.setProduct(productVIA);
    StockCard stockCardMMIA = StockCardBuilder.buildStockCard();
    Product productMMIA = new ProductBuilder().setProductId(10L).setPrimaryName("MMIA Product")
        .setCode("MMIA Code").setIsArchived(true).build();
    stockCardMMIA.setProduct(productMMIA);

    StockCard unknownAStockCard = StockCardBuilder.buildStockCard();
    Product productUnknownA = new ProductBuilder().setPrimaryName("A Unknown Product")
        .setCode("A Code").build();
    unknownAStockCard.setProduct(productUnknownA);
    StockCard unknownBStockCard = StockCardBuilder.buildStockCard();
    Product productUnknownB = new ProductBuilder().setPrimaryName("B Unknown Product")
        .setCode("B Code").build();
    unknownBStockCard.setProduct(productUnknownB);

    when(productRepositoryMock.listProductsArchivedOrNotInStockCard())
        .thenReturn(Arrays.asList(productMMIA, productVIA, productUnknownB, productUnknownA));
    when(stockRepositoryMock.queryStockCardByProductId(10L)).thenReturn(stockCardMMIA);

    TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
    Observable<List<InventoryViewModel>> observable = initialInventoryPresenter.loadInventory();
    observable.subscribe(subscriber);

    subscriber.awaitTerminalEvent();

    subscriber.assertNoErrors();
    List<InventoryViewModel> receivedInventoryViewModels = subscriber.getOnNextEvents().get(0);
    assertEquals(4, receivedInventoryViewModels.size());
  }

  @Test
  public void shouldOnlyActivatedProductsInInventoryList() throws LMISException {
    Product activeProduct1 = ProductBuilder.create().setPrimaryName("active product").setCode("P2")
        .build();
    Product activeProduct2 = ProductBuilder.create().setPrimaryName("active product").setCode("P3")
        .build();

    when(stockRepositoryMock.list()).thenReturn(new ArrayList<>());
    when(productRepositoryMock.listProductsArchivedOrNotInStockCard())
        .thenReturn(Arrays.asList(activeProduct1, activeProduct2));

    TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
    Observable<List<InventoryViewModel>> observable = initialInventoryPresenter.loadInventory();
    observable.subscribe(subscriber);

    subscriber.awaitTerminalEvent();

    subscriber.assertNoErrors();
    List<InventoryViewModel> receivedInventoryViewModels = subscriber.getOnNextEvents().get(0);

    assertEquals(2, receivedInventoryViewModels.size());
  }

  @Test
  public void shouldLoadBasicProductsForInventory() throws LMISException {
    Product basicProduct1 = ProductBuilder.create().setPrimaryName("product").setCode("P1")
        .setIsBasic(true).build();
    Product basicProduct2 = ProductBuilder.create().setPrimaryName("product").setCode("P2")
        .setIsBasic(true).build();

    when(productRepositoryMock.listBasicProducts())
        .thenReturn(Arrays.asList(basicProduct1, basicProduct2));

    TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
    Observable<List<InventoryViewModel>> observable = initialInventoryPresenter
        .loadInventoryWithBasicProducts();
    observable.subscribe(subscriber);

    subscriber.awaitTerminalEvent();

    subscriber.assertNoErrors();
    List<InventoryViewModel> receivedInventoryViewModels = subscriber.getOnNextEvents().get(0);

    assertEquals(2, receivedInventoryViewModels.size());
  }

  @Test
  public void shouldInitStockCardAndCreateAInitInventoryMovementItem() throws LMISException {
    StockCard stockcard = new StockCard();
    stockcard.setProduct(product);
    InventoryViewModel model = new InventoryViewModelBuilder(stockcard).setChecked(true).build();
    InventoryViewModel model2 = new InventoryViewModelBuilder(product).setChecked(true).build();

    model2.getNewLotMovementViewModelList()
        .add(new LotMovementViewModelBuilder().setExpiryDate("Jan 2016").setQuantity("20").build());
    model.getNewLotMovementViewModelList().add(
        new LotMovementViewModelBuilder().setExpiryDate("Feb 2015").setQuantity("2020").build());
    model.getProduct().setArchived(true);

    initialInventoryPresenter.getInventoryViewModelList().add(model);
    initialInventoryPresenter.getInventoryViewModelList().add(model2);
    initialInventoryPresenter.getDefaultViewModelList().add(model);
    initialInventoryPresenter.getDefaultViewModelList().add(model2);
    initialInventoryPresenter.initOrArchiveBackStockCards();

    verify(stockRepositoryMock, times(1)).updateStockCardWithProduct(any(StockCard.class));
    verify(stockRepositoryMock, times(1))
        .addStockMovementAndUpdateStockCard(any(StockMovementItem.class));
  }

  @Test
  public void shouldReInventoryArchivedStockCard() throws LMISException {
    InventoryViewModel uncheckedModel = new InventoryViewModelBuilder(product)
        .setChecked(false)
        .build();

    Product archivedProduct = new ProductBuilder().setPrimaryName("Archived product").setCode("BBC")
        .setIsArchived(true).build();
    StockCard archivedStockCard = new StockCardBuilder().setStockOnHand(0)
        .setProduct(archivedProduct).build();
    InventoryViewModel archivedViewModel = new InventoryViewModelBuilder(archivedStockCard)
        .setChecked(true)
        .build();

    List<InventoryViewModel> inventoryViewModelList = newArrayList(uncheckedModel,
        archivedViewModel);

    initialInventoryPresenter.getInventoryViewModelList().addAll(inventoryViewModelList);
    initialInventoryPresenter.getDefaultViewModelList().addAll(inventoryViewModelList);
    initialInventoryPresenter.initOrArchiveBackStockCards();

    assertFalse(archivedStockCard.getProduct().isArchived());
    verify(stockRepositoryMock, times(1)).updateStockCardWithProduct(archivedStockCard);
  }

  @Test
  public void shouldInitStockCardAndCreateAInitInventoryMovementItemWithLot() {
    product.setArchived(false);

    InventoryViewModel model = new InventoryViewModelBuilder(product).setChecked(true).build();

    initialInventoryPresenter.getInventoryViewModelList().add(model);
    initialInventoryPresenter.getDefaultViewModelList().add(model);
    initialInventoryPresenter.initOrArchiveBackStockCards();

    ArgumentCaptor<StockMovementItem> argument = ArgumentCaptor.forClass(StockMovementItem.class);
    verify(stockRepositoryMock).addStockMovementAndUpdateStockCard(argument.capture());
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(StockRepository.class).toInstance(stockRepositoryMock);
      bind(ProductRepository.class).toInstance(productRepositoryMock);
      bind(InventoryRepository.class).toInstance(mockInventoryRepository);
      bind(SharedPreferenceMgr.class).toInstance(sharedPreferenceMgr);
    }

  }

  @Test
  public void shouldHaveTheSameNumberOfDefaultElementsPlusTwoHeaders() {
    initialInventoryPresenter.getDefaultViewModelList()
        .addAll(newArrayList(models.get(1L), models.get(2L), models.get(3L), models.get(4L)));

    initialInventoryPresenter.filterViewModels("");

    assertEquals(6, initialInventoryPresenter.getInventoryViewModelList().size());
  }

  @Test
  public void shouldHaveOneBasicAndOneNonBasicHeader() {
    initialInventoryPresenter.getDefaultViewModelList()
        .addAll(newArrayList(models.get(1L), models.get(2L), models.get(3L), models.get(4L)));

    initialInventoryPresenter.filterViewModels("");

    assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(0).isBasic());
    assertEquals(DEFAULT_ID, initialInventoryPresenter.getInventoryViewModelList().get(0).getProductId());
    assertFalse(initialInventoryPresenter.getInventoryViewModelList().get(4).isBasic());
    assertEquals(DEFAULT_ID, initialInventoryPresenter.getInventoryViewModelList().get(4).getProductId());
  }

  @Test
  public void shouldHaveOneNonBasicHeader() {
    initialInventoryPresenter.getDefaultViewModelList()
        .addAll(newArrayList(models.get(1L), models.get(2L), models.get(3L), models.get(4L)));

    initialInventoryPresenter.filterViewModels(product4PrimaryName);

    assertEquals(2, initialInventoryPresenter.getInventoryViewModelList().size());
    assertFalse(initialInventoryPresenter.getInventoryViewModelList().get(0).isBasic());
    assertEquals(DEFAULT_ID, initialInventoryPresenter.getInventoryViewModelList().get(0).getProductId());
    assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(1).getProductId() != DEFAULT_ID);
  }

  @Test
  public void shouldHaveOneBasicHeader() {
    initialInventoryPresenter.getDefaultViewModelList()
        .addAll(newArrayList(models.get(1L), models.get(2L), models.get(3L), models.get(4L)));

    initialInventoryPresenter.filterViewModels(product1PrimaryName);

    assertEquals(2, initialInventoryPresenter.getInventoryViewModelList().size());
    assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(0).isBasic());
    assertEquals(DEFAULT_ID, initialInventoryPresenter.getInventoryViewModelList().get(0).getProductId());
    assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(1).getProductId() != DEFAULT_ID);
  }


  @Test
  public void shouldArrangeViewModelsWithNonEmptyQueryStringResultingInNonBasicInventoryViewModels() {
    initialInventoryPresenter.getDefaultViewModelList()
        .addAll(newArrayList(models.get(1L), models.get(2L), models.get(3L), models.get(4L)));

    initialInventoryPresenter.filterViewModels(product4PrimaryName);

    assertEquals(2, initialInventoryPresenter.getInventoryViewModelList().size());
    assertEquals(4L, initialInventoryPresenter.getInventoryViewModelList().get(1).getProductId());
  }


  @Test
  public void shouldArrangeViewModelsWithNonEmptyQueryStringResultingInBasicInventoryViewModels() {
    initialInventoryPresenter.getDefaultViewModelList()
        .addAll(newArrayList(models.get(1L), models.get(2L), models.get(3L), models.get(4L)));

    initialInventoryPresenter.filterViewModels(product1PrimaryName);

    assertEquals(2, initialInventoryPresenter.getInventoryViewModelList().size());
    assertEquals(0, initialInventoryPresenter.getInventoryViewModelList().get(0).getProductId());
    assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(0).isBasic());
    assertEquals(1L, initialInventoryPresenter.getInventoryViewModelList().get(1).getProductId());
  }

  @Test
  public void shouldArrangeViewModelsWithNonEmptyQueryStringResultingInBasicAndNonBasicInventoryViewModels() {
    models.get(3L).getProduct().setPrimaryName("Product 43");
    initialInventoryPresenter.getDefaultViewModelList()
        .addAll(newArrayList(models.get(1L), models.get(2L), models.get(3L), models.get(4L)));

    initialInventoryPresenter.filterViewModels(product4PrimaryName);

    assertEquals(4, initialInventoryPresenter.getInventoryViewModelList().size());
    assertEquals(0, initialInventoryPresenter.getInventoryViewModelList().get(0).getProductId());
    assertTrue(initialInventoryPresenter.getInventoryViewModelList().get(0).isBasic());
    assertEquals(3L, initialInventoryPresenter.getInventoryViewModelList().get(1).getProductId());
    assertEquals(0, initialInventoryPresenter.getInventoryViewModelList().get(2).getProductId());
    assertFalse(initialInventoryPresenter.getInventoryViewModelList().get(2).isBasic());
    assertEquals(4L, initialInventoryPresenter.getInventoryViewModelList().get(3).getProductId());
  }
}