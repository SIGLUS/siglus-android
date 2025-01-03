package org.openlmis.core.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import com.google.inject.AbstractModule;
import java.util.List;
import java.util.Random;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.presenter.StockCardPresenter.StockCardListView;
import org.openlmis.core.utils.DateUtil;
import androidx.test.core.app.ApplicationProvider;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class ExpiredStockCardListPresenterTest {

  private ExpiredStockCardListPresenter presenter;
  private StockRepository stockRepository;
  private ExpiredStockCardListPresenter.ExpiredStockCardListView expiredStockCardListView;

  @Before
  public void setUp() throws Exception {
    stockRepository = mock(StockRepository.class);
    expiredStockCardListView = mock(ExpiredStockCardListPresenter.ExpiredStockCardListView.class);

    RoboGuice.overrideApplicationInjector(ApplicationProvider.getApplicationContext(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(StockRepository.class).toInstance(stockRepository);
      }
    });
    presenter = RoboGuice.getInjector(ApplicationProvider.getApplicationContext())
        .getInstance(ExpiredStockCardListPresenter.class);
    presenter.attachView(expiredStockCardListView);
  }

  @Test
  public void shouldReturnEmptyListWhenStockCardIsNull() {
    TestSubscriber<List<StockCard>> afterLoadHandler = new TestSubscriber<>();
    presenter.afterLoadHandler = afterLoadHandler;

    List<StockCard> stockCards = newArrayList(null, null);
    when(stockRepository.list()).thenReturn(stockCards);
    // action
    presenter.loadExpiredStockCards();
    afterLoadHandler.awaitTerminalEvent();
    // verification
    List<StockCard> loadedStockCards = afterLoadHandler.getOnNextEvents().get(0);
    assertEquals(0, loadedStockCards.size());
  }

  @Test
  public void shouldReturnEmptyListWhenStockCardIsInactive() {
    TestSubscriber<List<StockCard>> afterLoadHandler = new TestSubscriber<>();
    presenter.afterLoadHandler = afterLoadHandler;

    List<StockCard> stockCards = newArrayList(inActiveStockCard);
    when(stockRepository.list()).thenReturn(stockCards);
    // action
    presenter.loadExpiredStockCards();
    afterLoadHandler.awaitTerminalEvent();
    // verification
    List<StockCard> loadedStockCards = afterLoadHandler.getOnNextEvents().get(0);
    assertEquals(0, loadedStockCards.size());
  }

  @Test
  public void shouldReturnEmptyListWhenStockCardIsArchived() {
    TestSubscriber<List<StockCard>> afterLoadHandler = new TestSubscriber<>();
    presenter.afterLoadHandler = afterLoadHandler;

    List<StockCard> stockCards = newArrayList(archivedStockCard);
    when(stockRepository.list()).thenReturn(stockCards);
    // action
    presenter.loadExpiredStockCards();
    afterLoadHandler.awaitTerminalEvent();
    // verification
    List<StockCard> loadedStockCards = afterLoadHandler.getOnNextEvents().get(0);
    assertEquals(0, loadedStockCards.size());
  }

  @Test
  public void shouldReturnEmptyListWhenStockCardIsNotArchivedAndActiveAndIsNotExpired() {
    TestSubscriber<List<StockCard>> afterLoadHandler = new TestSubscriber<>();
    presenter.afterLoadHandler = afterLoadHandler;

    List<StockCard> stockCards = newArrayList(notExpiredStockCard);
    when(stockRepository.list()).thenReturn(stockCards);
    // action
    presenter.loadExpiredStockCards();
    afterLoadHandler.awaitTerminalEvent();
    // verification
    List<StockCard> loadedStockCards = afterLoadHandler.getOnNextEvents().get(0);
    assertEquals(0, loadedStockCards.size());
  }

  @Test
  public void shouldReturnEmptyListWhenStockCardIsNotArchivedAndActiveAndIsExpiredAndQualityIs0() {
    TestSubscriber<List<StockCard>> afterLoadHandler = new TestSubscriber<>();
    presenter.afterLoadHandler = afterLoadHandler;

    List<StockCard> stockCards = newArrayList(qualityIsZeroStockCard);
    when(stockRepository.list()).thenReturn(stockCards);
    // action
    presenter.loadExpiredStockCards();
    afterLoadHandler.awaitTerminalEvent();
    // verification
    List<StockCard> loadedStockCards = afterLoadHandler.getOnNextEvents().get(0);
    assertEquals(0, loadedStockCards.size());
  }

  @Test
  public void shouldReturnNonEmptyListWhenStockCardIsNotArchivedAndActiveAndIsExpiredAndQualityIsGreaterThan0() {
    TestSubscriber<List<StockCard>> afterLoadHandler = new TestSubscriber<>();
    presenter.afterLoadHandler = afterLoadHandler;

    List<StockCard> stockCards = newArrayList(validStockCard);
    when(stockRepository.list()).thenReturn(stockCards);
    // action
    presenter.loadExpiredStockCards();
    afterLoadHandler.awaitTerminalEvent();
    // verification
    List<StockCard> loadedStockCards = afterLoadHandler.getOnNextEvents().get(0);
    assertEquals(1, loadedStockCards.size());
  }

  @Test
  public void shouldReturnMatchedTotalQualityWhenStockCardIsNotArchivedAndActiveAndIsExpiredAndQualityIsGreaterThan0() {
    TestSubscriber<List<StockCard>> afterLoadHandler = new TestSubscriber<>();
    presenter.afterLoadHandler = afterLoadHandler;

    int soh = 1;
    long stockId = 200L;

    List<StockCard> stockCards = newArrayList(stockCard(
        false, true, soh, true, stockId));
    when(stockRepository.list()).thenReturn(stockCards);
    // action
    presenter.loadExpiredStockCards();
    afterLoadHandler.awaitTerminalEvent();
    // verification
    assertEquals(String.valueOf(soh), presenter.lotsOnHands.get(String.valueOf(stockId)));
  }

  @Test
  public void shouldShowLoadingWhenRemoveCheckedExpiredProducts() {
    // given
    StockCardListView mockedStockCardListView = mock(StockCardListView.class);
    presenter.view = mockedStockCardListView;
    doNothing().when(mockedStockCardListView).loading();
    // when
    presenter.handleCheckedExpiredProducts("sign");
    // then
    verify(mockedStockCardListView).loading();
  }

  @Test
  public void shouldReturnMatchedMovementItemsWhenConvertLotOnHandsToStockMovementItemsIsCalled() {
    LotOnHand mockedLotOnHand = mock(LotOnHand.class);
    long remainingQuantity = 5;
    when(mockedLotOnHand.getQuantityOnHand()).thenReturn(remainingQuantity);
    validStockCard.getLotOnHandListWrapper().add(mockedLotOnHand);
    validStockCard.setStockOnHand(validStockCard.getStockOnHand() + remainingQuantity);

    LotOnHand lotOnHand = validStockCard.getLotOnHandListWrapper().get(0);
    String signature = "signature";

    User mockedUser = mock(User.class);
    String facilityCode = "facilityCode";
    when(mockedUser.getFacilityCode()).thenReturn(facilityCode);
    UserInfoMgr.getInstance().setUser(mockedUser);
    // when
    List<StockMovementItem> actualStockMovementItems = presenter.convertLotOnHandsToStockMovementItems(
        newArrayList(lotOnHand), signature, DateUtil.getCurrentDate());
    // then
    StockMovementItem stockMovementItem = actualStockMovementItems.get(0);

    String documentNumber = facilityCode + "_" + DateUtil.formatDate(DateUtil.getCurrentDate(),
        DateUtil.DOCUMENT_NO_DATE_TIME_FORMAT);
    assertEquals(documentNumber, stockMovementItem.getDocumentNumber());

    assertEquals(remainingQuantity, stockMovementItem.getStockOnHand());
    assertEquals("EXPIRED_RETURN_TO_SUPPLIER_AND_DISCARD", stockMovementItem.getReason());
    assertEquals(MovementType.NEGATIVE_ADJUST, stockMovementItem.getMovementType());
    assertEquals(signature, stockMovementItem.getSignature());

    long expectedQuantity = lotOnHand.getQuantityOnHand();
    assertEquals(expectedQuantity, stockMovementItem.getMovementQuantity());
  }

  private StockCard inActiveStockCard = stockCard(
      false, false, 1, false);

  private StockCard archivedStockCard = stockCard(
      true, true, 1, false);

  private StockCard notExpiredStockCard = stockCard(
      false, true, 1, false);

  private StockCard qualityIsZeroStockCard = stockCard(
      false, true, 0, true);

  private StockCard validStockCard = stockCard(
      false, true, 1, true);

  private StockCard stockCard(boolean isProductArchived,
      boolean isProductActive,
      int soh,
      boolean isExpired
  ) {
    Product product = ProductBuilder.create()
        .setIsActive(isProductActive)
        .setIsArchived(isProductArchived)
        .setIsKit(true)
        .setCode("")
        .build();

    StockCard stockCard = new StockCard();
    Random rand = new Random();
    stockCard.setId(rand.nextInt(900) + 100);
    stockCard.setProduct(product);
    stockCard.setStockOnHand(soh);

    LotOnHand lotOnHand = new LotOnHand(getLot(isExpired), stockCard, (long) soh);
    stockCard.setLotOnHandListWrapper(newArrayList(lotOnHand));

    return stockCard;
  }

  private StockCard stockCard(boolean isProductArchived,
      boolean isProductActive,
      int soh,
      boolean isExpired,
      long stockId
  ) {
    StockCard stockCard = stockCard(isProductArchived, isProductActive, soh, isExpired);
    stockCard.setId(stockId);

    return stockCard;
  }

  private Lot getLot(boolean isExpired) {
    Lot lot = new Lot();
    int plugDays = 1;
    if (isExpired) {
      plugDays = -1;
    }
    lot.setExpirationDate(new DateTime(DateUtil.getCurrentDate()).plusDays(plugDays).toDate());
    return lot;
  }
}