package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.assertj.core.api.Assertions.assertThat;
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
    private StockCardPresenter.StockCardListView stockCardListView;
    private ArrayList<StockCard> stockCardList;

    @Before
    public void setUp() throws Exception {
        stockRepository = mock(StockRepository.class);
        stockCardListView = mock(StockCardPresenter.StockCardListView.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(StockRepository.class).toInstance(stockRepository);
            }
        });

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockCardPresenter.class);
        presenter.attachView(stockCardListView);

        StockCard stockCard1 = StockCardBuilder.buildStockCard();
        StockCard stockCard2 = StockCardBuilder.buildStockCard();
        stockCardList = newArrayList(stockCard1, stockCard2, stockCard1);
        StockCardViewModel viewModel1 = new StockCardViewModel(stockCard1);
        presenter.stockCardViewModels = newArrayList(viewModel1, viewModel1);

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
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

    private void testLoadStockCard(StockCardPresenter.ArchiveStatus status) throws LMISException {
        //given
        when(stockRepository.list()).thenReturn(newArrayList(stockCard(true), stockCard(false)));
        TestSubscriber<List<StockCard>> afterLoadHandler = new TestSubscriber<>();
        presenter.afterLoadHandler = afterLoadHandler;

        //when
        presenter.loadStockCards(status);
        afterLoadHandler.awaitTerminalEvent();

        //then
        assertThat(afterLoadHandler.getOnNextEvents().get(0).get(0).getProduct().isArchived()).isEqualTo(status.isArchived());
    }

    private StockCard stockCard(boolean isArchived) {
        Product product = new Product();
        product.setIsArchived(isArchived);
        StockCard stockCard = new StockCard();
        stockCard.setProduct(product);
        return stockCard;
    }
}