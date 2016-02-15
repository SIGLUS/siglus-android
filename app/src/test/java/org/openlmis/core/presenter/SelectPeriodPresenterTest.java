package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.repository.InventoryRepository;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class SelectPeriodPresenterTest {



    private SelectPeriodPresenter.SelectPeriodView view;

    private InventoryRepository inventoryRepository;

    private SelectPeriodPresenter selectPeriodPresenter;


    @Before
    public void setUp() throws Exception {
        view = mock(SelectPeriodPresenter.SelectPeriodView.class);
        inventoryRepository = mock(InventoryRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(InventoryRepository.class).toInstance(inventoryRepository);
            }
        });
        selectPeriodPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SelectPeriodPresenter.class);
        selectPeriodPresenter.attachView(view);
    }

    @Test
    public void shouldGetPeriodInventoryWhenLoadData() throws LMISException {
        List<Inventory> inventories = Arrays.asList(new Inventory(), new Inventory());
        when(inventoryRepository.queryPeriodInventory(any(Period.class))).thenReturn(inventories);

        TestSubscriber<List<Inventory>> testSubscriber = new TestSubscriber<>();
        selectPeriodPresenter=spy(selectPeriodPresenter);
        when(selectPeriodPresenter.getSubscriber()).thenReturn(testSubscriber);

        selectPeriodPresenter.loadData();
        testSubscriber.awaitTerminalEvent();

        verify(inventoryRepository).queryPeriodInventory(any(Period.class));
        testSubscriber.assertNoErrors();
        assertThat(testSubscriber.getOnNextEvents().get(0), is(inventories));
        verify(inventoryRepository).queryPeriodInventory(any(Period.class));
    }


    @Test
    public void shouldRefreshDateAfterLoadPeriods() throws LMISException {
        List<Inventory> inventories = Arrays.asList(new Inventory());

        selectPeriodPresenter.getSubscriber().onNext(inventories);

        verify(view).refreshDate(inventories);
    }
}