package org.openlmis.core.manager;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class SharedPreferenceMgrTest {

    private SharedPreferenceMgr sharedPreferenceMgr;
    private StockRepository stockRepository;
    private RnrFormRepository rnrFormRepository;

    @Before
    public void setUp() throws Exception {
        stockRepository = mock(StockRepository.class);
        rnrFormRepository = mock(RnrFormRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        sharedPreferenceMgr = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SharedPreferenceMgr.class);
    }

    @Test
    public void shouldGetHasSyncedVersion() throws Exception {
        assertThat(sharedPreferenceMgr.hasSyncedVersion(), is(false));
    }

    @Test
    public void shouldGetHasSyncedVersionWhenSetSynced() throws Exception {
        sharedPreferenceMgr.setHasGetProducts(true);
        boolean hasGetProducts = sharedPreferenceMgr.hasGetProducts();
        assertThat(hasGetProducts, is(true));
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
    public void shouldNotSaveProductNameToProductUpdateBannerListWhenTheNameExisting(){
        sharedPreferenceMgr.setIsNeedShowProductsUpdateBanner(true,"product");
        assertThat(sharedPreferenceMgr.getShowUpdateBannerTexts().size(),is(1));
        assertThat(sharedPreferenceMgr.getShowUpdateBannerTexts().toArray()[0].toString(),is("product"));
    }


    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(stockRepository);
            bind(RnrFormRepository.class).toInstance(rnrFormRepository);
        }
    }
}