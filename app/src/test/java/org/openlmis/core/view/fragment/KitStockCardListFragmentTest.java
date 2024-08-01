package org.openlmis.core.view.fragment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.inject.AbstractModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.presenter.StockCardPresenter;
import org.openlmis.core.view.adapter.KitStockCardListAdapter;
import androidx.test.core.app.ApplicationProvider;
import roboguice.RoboGuice;
import roboguice.fragment.SupportFragmentController;

@RunWith(LMISTestRunner.class)
public class KitStockCardListFragmentTest {

  private StockCardPresenter mockStockCardPresenter;
  private KitStockCardListFragment fragment;

  @Before
  public void setUp() throws Exception {
    mockStockCardPresenter = mock(StockCardPresenter.class);
    RoboGuice.overrideApplicationInjector(ApplicationProvider.getApplicationContext(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(StockCardPresenter.class).toInstance(mockStockCardPresenter);
      }
    });
    fragment = SupportFragmentController.of(KitStockCardListFragment.class).create().get();
  }

  @After
  public void teardown() {
    RoboGuice.Util.reset();
  }

  @Ignore("robolectric.android.controller.FragmentController with RoboContext")
  @Test
  public void shouldCreateStockCardsForKitsIfNotExist() throws Exception {
    verify(mockStockCardPresenter).loadKits();
  }

  @Ignore("robolectric.android.controller.FragmentController with RoboContext")
  @Test
  public void shouldUseKitStockCardListAdapter() throws Exception {
    assertThat(fragment.mAdapter).isInstanceOf(KitStockCardListAdapter.class);
  }
}