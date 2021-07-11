package org.openlmis.core.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftBulkEntriesProduct;
import org.openlmis.core.model.DraftBulkEntriesProductLotItem;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.BulkEntriesRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.Observable;
import rx.observers.TestSubscriber;


@RunWith(LMISTestRunner.class)
public class BulkEntriesPresenterTest {

  private BulkEntriesPresenter bulkEntriesPresenter;
  private BulkEntriesRepository bulkEntriesRepository;
  private StockRepository stockRepository;
  private List<DraftBulkEntriesProduct> bulkEntriesViewModels = new ArrayList<>();
  private final static String productCode = "22A07";

  @Before
  public void setup() throws Exception {
    bulkEntriesRepository = mock(BulkEntriesRepository.class);
    stockRepository = mock(StockRepository.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(BulkEntriesRepository.class).toInstance(bulkEntriesRepository);
        bind(StockRepository.class).toInstance(stockRepository);
      }
    });
    bulkEntriesPresenter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(BulkEntriesPresenter.class);
  }

  @Test
  public void shouldRestoreDraftBulkEntriesViewModel() throws LMISException {
    // given
    Product product = Product.builder()
        .isActive(true)
        .isArchived(false)
        .isBasic(true)
        .code(productCode)
        .isHiv(false)
        .isKit(false)
        .build();
    DraftBulkEntriesProductLotItem draftBulkEntriesProductLotItem = DraftBulkEntriesProductLotItem
        .builder()
        .lotNumber("yyy66")
        .lotSoh(Long.valueOf(100))
        .quantity(Long.valueOf(200))
        .reason("District( DDM)")
        .expirationDate(new Date("2023/07/13"))
        .newAdded(true)
        .build();

    List<DraftBulkEntriesProductLotItem> draftBulkEntriesProductLotItems = new ArrayList<>();
    draftBulkEntriesProductLotItems.add(draftBulkEntriesProductLotItem);

    DraftBulkEntriesProduct draftBulkEntriesProduct = DraftBulkEntriesProduct.builder()
        .product(product)
        .draftLotItemListWrapper(draftBulkEntriesProductLotItems)
        .quantity(Long.valueOf(300))
        .done(true)
        .build();
    bulkEntriesViewModels.add(draftBulkEntriesProduct);
    when(bulkEntriesRepository.queryAllBulkEntriesDraft()).thenReturn(bulkEntriesViewModels);
    // when
    bulkEntriesPresenter.restoreDraftInventory();
    // then
    assertEquals(Long.valueOf(300),
        bulkEntriesPresenter.getBulkEntriesViewModels().get(0).getQuantity());
  }

  @Test
  public void shouldRestoreDraftFromLotOnHand() throws LMISException {
    // given
    Product product = Product.builder()
        .isActive(true)
        .isArchived(false)
        .isBasic(true)
        .code(productCode)
        .isHiv(false)
        .isKit(false)
        .build();
    DraftBulkEntriesProductLotItem draftBulkEntriesProductLotItem = DraftBulkEntriesProductLotItem
        .builder()
        .lotNumber("yyy66")
        .lotSoh(Long.valueOf(100))
        .quantity(Long.valueOf(200))
        .reason("District( DDM)")
        .expirationDate(new Date("2023/07/13"))
        .newAdded(true)
        .build();
    Lot lot = new Lot();
    lot.setLotNumber("yyy66");
    lot.setProduct(product);
    lot.setExpirationDate(new Date("2024/01/01"));
    StockCard stockCard = new StockCard();
    List<LotOnHand> lotOnHands = new ArrayList<>();
    LotOnHand lotOnHand = new LotOnHand();
    lotOnHand.setLot(lot);
    lotOnHand.setQuantityOnHand(Long.valueOf(1000));
    lotOnHands.add(lotOnHand);
    stockCard.setLotOnHandListWrapper(lotOnHands);

    List<DraftBulkEntriesProductLotItem> draftBulkEntriesProductLotItems = new ArrayList<>();
    draftBulkEntriesProductLotItems.add(draftBulkEntriesProductLotItem);

    DraftBulkEntriesProduct draftBulkEntriesProduct = DraftBulkEntriesProduct.builder()
        .product(product)
        .draftLotItemListWrapper(draftBulkEntriesProductLotItems)
        .quantity(Long.valueOf(300))
        .done(true)
        .build();
    bulkEntriesViewModels.add(draftBulkEntriesProduct);
    when(bulkEntriesRepository.queryAllBulkEntriesDraft()).thenReturn(bulkEntriesViewModels);
    when(stockRepository.queryStockCardByProductId(anyLong())).thenReturn(stockCard);
    // when
    bulkEntriesPresenter.restoreDraftInventory();
    // then
    assertEquals("1000",
        bulkEntriesPresenter.getBulkEntriesViewModels().get(0).getExistingLotMovementViewModelList()
            .get(0).getLotSoh());
  }

  @Test
  public void shouldGetAddedProductsCode() {
    // given
    Product product = Product.builder()
        .isActive(true)
        .isArchived(false)
        .isBasic(true)
        .code(productCode)
        .isHiv(false)
        .isKit(false)
        .build();
    BulkEntriesViewModel bulkEntriesViewModel = new BulkEntriesViewModel(product);
    bulkEntriesPresenter.getBulkEntriesViewModels().add(bulkEntriesViewModel);
    // when
    List<String> productCodes = bulkEntriesPresenter.getAddedProductCodes();
    // then
    assertEquals(productCode, productCodes.get(0));
  }

  @Test
  public void shouldAddNewProductToBulkEntriesViewModels() {
    // given
    Product product = Product.builder()
        .isActive(true)
        .isArchived(false)
        .isBasic(true)
        .code(productCode)
        .isHiv(false)
        .isKit(false)
        .build();
    List<Product> products = new ArrayList<>();
    products.add(product);
    // when
    bulkEntriesPresenter.addNewProductsToBulkEntriesViewModels(products);
    // then
    assertEquals(productCode,
        bulkEntriesPresenter.getBulkEntriesViewModels().get(0).getProduct().getCode());
  }

  @Test
  public void shouldReturnFalseWhenNoDraft() throws LMISException {
    // given
    bulkEntriesPresenter.getBulkEntriesViewModels().addAll(new ArrayList<>());
    List<DraftBulkEntriesProduct> draftBulkEntriesProducts = new ArrayList<>();
    when(bulkEntriesRepository.queryAllBulkEntriesDraft()).thenReturn(draftBulkEntriesProducts);

    // when
    boolean flag = bulkEntriesPresenter.isDraftExisted();

    // then
    assertFalse(flag);
  }

  @Test
  public void shouldGetBulkEntriesFromDraft() throws LMISException {
    // given
    Product product = Product.builder()
        .code(productCode)
        .primaryName("test")
        .build();
    BulkEntriesViewModel bulkEntriesViewModel = new BulkEntriesViewModel(product);
    bulkEntriesPresenter.getBulkEntriesViewModels().add(bulkEntriesViewModel);

    // when
    TestSubscriber<List<BulkEntriesViewModel>> subscriber = new TestSubscriber<>();
    Observable<List<BulkEntriesViewModel>> observable = bulkEntriesPresenter.getBulkEntriesViewModelsFromDraft();
    observable.subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    // when
    subscriber.assertNoErrors();
    List<BulkEntriesViewModel> bulkEntriesViewModels = subscriber.getOnNextEvents().get(0);
    assertEquals(productCode,bulkEntriesViewModels.get(0).getProduct().getCode());
  }

  @Test
  public void shouldSaveDraftBulkEntriesObservable() {
    // given
    Product product = Product.builder()
        .code(productCode)
        .primaryName("test")
        .build();
    BulkEntriesViewModel bulkEntriesViewModel = new BulkEntriesViewModel(product);
    bulkEntriesPresenter.getBulkEntriesViewModels().add(bulkEntriesViewModel);

    // when
    TestSubscriber<Object> subscriber = new TestSubscriber<>();
    Observable<Object> observable = bulkEntriesPresenter.saveDraftBulkEntriesObservable();
    observable.subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    // then
    subscriber.assertNoErrors();
  }

  @Test
  public void shouldDeleteDraft() throws LMISException {
    // when
    bulkEntriesPresenter.deleteDraft();
    // then
    verify(bulkEntriesRepository,times(1)).clearBulkEntriesDraft();
  }

}