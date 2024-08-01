package org.openlmis.core.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import androidx.test.core.app.ApplicationProvider;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class SharedPreferenceMgrTest {

  private SharedPreferenceMgr sharedPreferenceMgr;
  private StockRepository stockRepository;
  private RnrFormRepository rnrFormRepository;
  private DateTime nowDateTime;

  @Before
  public void setUp() throws Exception {
    stockRepository = mock(StockRepository.class);
    rnrFormRepository = mock(RnrFormRepository.class);
    RoboGuice.overrideApplicationInjector(ApplicationProvider.getApplicationContext(), new MyTestModule());
    sharedPreferenceMgr = RoboGuice.getInjector(ApplicationProvider.getApplicationContext()).getInstance(SharedPreferenceMgr.class);
    sharedPreferenceMgr.stockRepository = stockRepository;
    nowDateTime = new DateTime();
  }

  @Test
  public void shouldGetLastMonthSyncedIsTrueWhenHaveLocalData() throws Exception {
    when(stockRepository.hasStockData()).thenReturn(true);
    boolean lastMonthStockDataSynced = sharedPreferenceMgr.isLastMonthStockDataSynced();
    assertThat(lastMonthStockDataSynced, is(true));
  }

  @Test
  public void shouldGetLastMonthSyncedIsTrueWhenSetSynced() throws Exception {
    sharedPreferenceMgr.setLastMonthStockCardDataSynced(true);
    boolean lastMonthStockDataSynced = sharedPreferenceMgr.isLastMonthStockDataSynced();
    assertThat(lastMonthStockDataSynced, is(true));
  }

  @Test
  public void shouldGetRequisitionDataSyncedWhenHaveLocalData() throws Exception {
    when(rnrFormRepository.hasRequisitionData()).thenReturn(true);
    boolean requisitionDataSynced = sharedPreferenceMgr.isRequisitionDataSynced();
    assertThat(requisitionDataSynced, is(true));
  }

  @Test
  public void shouldGetRequisitionDataSyncedWhenSetSynced() throws Exception {
    sharedPreferenceMgr.setRequisitionDataSynced(true);
    boolean lastMonthStockDataSynced = sharedPreferenceMgr.isRequisitionDataSynced();
    assertThat(lastMonthStockDataSynced, is(true));
  }

  @Test
  public void shouldNotSaveProductNameToProductUpdateBannerListWhenTheNameExisting() {
    sharedPreferenceMgr.setIsNeedShowProductsUpdateBanner(true, "product");
    assertThat(sharedPreferenceMgr.getShowUpdateBannerTexts().size(), is(1));
    assertThat(sharedPreferenceMgr.getShowUpdateBannerTexts().toArray()[0].toString(),
        is("product"));
  }

  @Test
  public void shouldRemoveUpdateBannerList() {
    sharedPreferenceMgr.setIsNeedShowProductsUpdateBanner(true, "product");

    sharedPreferenceMgr.removeShowUpdateBannerTextWhenReactiveProduct("product");
    assertThat(sharedPreferenceMgr.getShowUpdateBannerTexts().size(), is(0));
  }

  @Test
  public void shouldNotRemoveUpdateBannerListWhenTheProductHasBeenRemoved() {
    //given
    sharedPreferenceMgr.setIsNeedShowProductsUpdateBanner(true, "product");
    //when
    sharedPreferenceMgr.removeShowUpdateBannerTextWhenReactiveProduct("not existing product");
    //then
    assertThat(sharedPreferenceMgr.getShowUpdateBannerTexts().size(), is(1));
    assertThat(sharedPreferenceMgr.getShowUpdateBannerTexts().toArray()[0].toString(),
        is("product"));
  }

  @Test
  public void shouldSetBannerVisibilityGoneWhenNotifyListIsEmpty() {
    //given
    sharedPreferenceMgr.setIsNeedShowProductsUpdateBanner(true, "product");
    //when
    sharedPreferenceMgr.removeShowUpdateBannerTextWhenReactiveProduct("product");
    //then
    assertThat(sharedPreferenceMgr.getShowUpdateBannerTexts().size(), is(0));
    assertThat(sharedPreferenceMgr.isNeedShowProductsUpdateBanner(), is(false));
  }

  @Test
  public void shouldReturnTrueIfLastSyncUpDateIsToday() throws Exception {
    ((LMISTestApp) ApplicationProvider.getApplicationContext()).setCurrentTimeMillis(System.currentTimeMillis());
    sharedPreferenceMgr.setLastMovementHandShakeDateToToday();

    boolean hasSyncedUpLatestMovementToday = sharedPreferenceMgr.hasSyncedUpLatestMovementLastDay();

    Assert.assertTrue(hasSyncedUpLatestMovementToday);
  }

  @Test
  public void shouldReturnNullWhenDoesNotSaveNewShippedPods() {
    assertThat(sharedPreferenceMgr.getNewShippedProgramNames() == null, is(true));
  }

  @Test
  public void shouldReturnSavedStringWhenSavedNewShippedPods() {
    // given
    String newShippedProgramNames = "MMIA";
    sharedPreferenceMgr.setNewShippedProgramNames(newShippedProgramNames);
    // when
    String actualShippedProgramNames = sharedPreferenceMgr.getNewShippedProgramNames();
    // then
    assertThat(actualShippedProgramNames, is(newShippedProgramNames));
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(StockRepository.class).toInstance(stockRepository);
      bind(RnrFormRepository.class).toInstance(rnrFormRepository);
    }
  }
}