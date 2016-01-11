package org.openlmis.core.view.fragment;

import com.google.inject.AbstractModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.presenter.StockCardPresenter;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.FragmentTestUtil;

import roboguice.RoboGuice;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(LMISTestRunner.class)
public class KitStockCardListFragmentTest {

    private StockCardPresenter mockStockCardPresenter;
    private KitStockCardListFragment fragment;

    @Before
    public void setUp() throws Exception {
        mockStockCardPresenter = mock(StockCardPresenter.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(StockCardPresenter.class).toInstance(mockStockCardPresenter);
            }
        });
        fragment = new KitStockCardListFragment();
        FragmentTestUtil.startFragment(fragment);
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldCreateStockCardsForKitsIfNotExist() throws Exception {
        verify(mockStockCardPresenter).loadKits();
    }
}