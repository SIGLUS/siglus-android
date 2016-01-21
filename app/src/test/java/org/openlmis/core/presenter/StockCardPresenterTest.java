package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Product.IsKit;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.presenter.StockCardPresenter.ArchiveStatus.Active;
import static org.openlmis.core.presenter.StockCardPresenter.ArchiveStatus.Archived;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class StockCardPresenterTest {

    private StockCardPresenter presenter;
    private StockRepository stockRepository;
    private ProductRepository productRepository;
    private StockCardPresenter.StockCardListView stockCardListView;
    private ArrayList<StockCard> stockCardList;

    @Before
    public void setUp() throws Exception {
        stockRepository = mock(StockRepository.class);
        productRepository = mock(ProductRepository.class);
        stockCardListView = mock(StockCardPresenter.StockCardListView.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(StockRepository.class).toInstance(stockRepository);
                bind(ProductRepository.class).toInstance(productRepository);
            }
        });

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockCardPresenter.class);
        presenter.attachView(stockCardListView);

        StockCard stockCard1 = StockCardBuilder.buildStockCard();
        StockCard stockCard2 = StockCardBuilder.buildStockCard();
        stockCardList = newArrayList(stockCard1, stockCard2, stockCard1);
        StockCardViewModel viewModel1 = new StockCardViewModel(stockCard1);
        presenter.stockCardViewModels = newArrayList(viewModel1, viewModel1);
    }

    @Test
    public void testRefreshStockCardViewModelsSOH() throws Exception {

        presenter.refreshStockCardViewModelsSOH();

        verify(stockRepository, times(2)).refresh(any(StockCard.class));
    }

    @Test
    public void shouldRefreshStockCardModelListWithGetLoadStockCardsSubscriber() throws Exception {
        ArrayList<StockCard> cardList = new ArrayList<>();
        cardList.addAll(this.stockCardList);

        presenter.afterLoadHandler.onNext(cardList);

        assertThat(presenter.stockCardViewModels.size()).isEqualTo(3);
        verify(stockCardListView).refresh();

        presenter.afterLoadHandler.onCompleted();

        verify(stockCardListView).loaded();
    }

    @Test
    public void shouldLoadActiveOrArchivedStockCards() throws Exception {
        testLoadStockCard(Archived);
        testLoadStockCard(Active);
    }

    @Test
    public void shouldLoadStockCardsWithActiveProductsWithNoSOH() throws Exception {
        when(stockRepository.list()).thenReturn(newArrayList(stockCard(false, true, false, 0), stockCard(false, false, false, 0), stockCard(false, true, true, 0)));
        TestSubscriber<List<StockCard>> afterLoadHandler = new TestSubscriber<>();
        presenter.afterLoadHandler = afterLoadHandler;

        presenter.loadStockCards(StockCardPresenter.ArchiveStatus.Active);
        afterLoadHandler.awaitTerminalEvent();

        List<StockCard> loadedStockCards = afterLoadHandler.getOnNextEvents().get(0);
        assertEquals(1, loadedStockCards.size());
        assertTrue(loadedStockCards.get(0).getProduct().isActive());
    }

    @Test
    public void shouldLoadStockCardsWithDeactivatedProductWithSOH() throws Exception {
        when(stockRepository.list()).thenReturn(newArrayList(stockCard(false, true, false, 10), stockCard(false, false, false, 10)));
        TestSubscriber<List<StockCard>> afterLoadHandler = new TestSubscriber<>();
        presenter.afterLoadHandler = afterLoadHandler;

        presenter.loadStockCards(StockCardPresenter.ArchiveStatus.Active);
        afterLoadHandler.awaitTerminalEvent();

        assertEquals(2, afterLoadHandler.getOnNextEvents().get(0).size());
    }

    @Test
    public void shouldCreateStockCardsForKitsIfNotExist() throws Exception {
        //given
        Product kit = new Product();
        kit.setKit(true);
        kit.setPrimaryName("kit a");
        kit.setId(123);

        ArrayList<Product> kits = new ArrayList<>();
        kits.add(kit);
        when(productRepository.listActiveProducts(IsKit.Yes)).thenReturn(kits);
        when(stockRepository.queryStockCardByProductId(123)).thenReturn(null);

        //when
        TestSubscriber<List<StockCard>> subscriber = new TestSubscriber();
        presenter.afterLoadHandler = subscriber;
        presenter.loadKits();
        subscriber.awaitTerminalEvent();

        //then
        verify(stockRepository).initStockCard(any(StockCard.class));
        StockCard createdKitStockCard = subscriber.getOnNextEvents().get(0).get(0);
        assertThat(createdKitStockCard.getProduct().getPrimaryName()).isEqualTo("kit a");
    }

    private void testLoadStockCard(StockCardPresenter.ArchiveStatus status) throws LMISException {
        //given
        when(stockRepository.list()).thenReturn(newArrayList(stockCard(true, true, false, 0), stockCard(false, true, false, 0)));
        TestSubscriber<List<StockCard>> afterLoadHandler = new TestSubscriber<>();
        presenter.afterLoadHandler = afterLoadHandler;

        //when
        presenter.loadStockCards(status);
        afterLoadHandler.awaitTerminalEvent();

        //then
        assertThat(afterLoadHandler.getOnNextEvents().get(0).get(0).getProduct().isArchived()).isEqualTo(status.isArchived());
    }

    private StockCard stockCard(boolean isProductArchived, boolean isProductActive, boolean isKit, int soh) {
        Product product = ProductBuilder.create()
                .setIsActive(isProductActive)
                .setIsArchived(isProductArchived)
                .setIsKit(isKit)
                .build();
        StockCard stockCard = new StockCard();
        stockCard.setProduct(product);
        stockCard.setStockOnHand(soh);
        return stockCard;
    }
}