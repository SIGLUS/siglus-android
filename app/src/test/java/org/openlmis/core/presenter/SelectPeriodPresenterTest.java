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
import org.openlmis.core.model.service.PeriodService;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.SelectInventoryViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
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
    private PeriodService mockPeriodService;


    @Before
    public void setUp() throws Exception {
        view = mock(SelectPeriodPresenter.SelectPeriodView.class);
        inventoryRepository = mock(InventoryRepository.class);
        mockPeriodService = mock(PeriodService.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(InventoryRepository.class).toInstance(inventoryRepository);
                bind(PeriodService.class).toInstance(mockPeriodService);
            }
        });
        selectPeriodPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SelectPeriodPresenter.class);
        selectPeriodPresenter.attachView(view);
    }

    @Test
    public void shouldGetPeriodInventoryWhenLoadData() throws LMISException {
        List<Inventory> inventories = Arrays.asList(
                generateInventoryWithDate("2020-02-21 13:00:00"),
                generateInventoryWithDate("2020-02-21 09:00:00"),
                generateInventoryWithDate("2020-02-19 12:00:00"));
        when(inventoryRepository.queryPeriodInventory(any(Period.class))).thenReturn(inventories);

        TestSubscriber<List<SelectInventoryViewModel>> testSubscriber = new TestSubscriber<>();
        selectPeriodPresenter=spy(selectPeriodPresenter);
        when(selectPeriodPresenter.getSubscriber()).thenReturn(testSubscriber);

        selectPeriodPresenter.loadData("MMIA");
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        verify(mockPeriodService).generatePeriod("MMIA", null);
        verify(inventoryRepository).queryPeriodInventory(any(Period.class));
        assertThat(testSubscriber.getOnNextEvents().get(0).size(), is(3));
        assertTrue(testSubscriber.getOnNextEvents().get(0).get(0).isShowTime());
        assertFalse(testSubscriber.getOnNextEvents().get(0).get(2).isShowTime());
    }

    private Inventory generateInventoryWithDate(String formattedDate) {
        Inventory inventory = new Inventory();
        inventory.setCreatedAt(DateUtil.parseString(formattedDate, DateUtil.DATE_TIME_FORMAT));
        inventory.setUpdatedAt(DateUtil.parseString(formattedDate, DateUtil.DATE_TIME_FORMAT));
        return inventory;
    }


    @Test
    public void shouldRefreshDateAfterLoadPeriods() throws LMISException {
        List<SelectInventoryViewModel> inventories = Arrays.asList(new SelectInventoryViewModel(new Inventory()));

        selectPeriodPresenter.getSubscriber().onNext(inventories);

        verify(view).refreshDate(inventories);
    }
}