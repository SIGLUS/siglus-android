package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
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
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;
@RunWith(LMISTestRunner.class)
public class StockCardPresenterTest {

    private StockCardPresenter presenter;
    private List<StockCardViewModel> stockCardViewModels;
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
    public void shouldPostStockCardListWithLoadStockCardsObserver() throws Exception {

        ArrayList<StockCard> cardList = new ArrayList<>();
        cardList.addAll(this.stockCardList);

        when(stockRepository.list()).thenReturn(cardList);
        TestSubscriber<List<StockCard>> subscriber = new TestSubscriber<>();

        presenter.getLoadStockCardsObserver().subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertValue(cardList);
        verify(stockRepository).list();
    }

    @Test
    public void shouldRefreshStockCardModelListWithGetLoadStockCardsSubscriber() throws Exception{
        ArrayList<StockCard> cardList = new ArrayList<>();
        cardList.addAll(this.stockCardList);

        presenter.getLoadStockCardsSubscriber().onNext(cardList);

        assertThat(presenter.stockCardViewModels.size()).isEqualTo(3);
        verify(stockCardListView).refresh();

        presenter.getLoadStockCardsSubscriber().onCompleted();

        verify(stockCardListView).loaded();
    }
}