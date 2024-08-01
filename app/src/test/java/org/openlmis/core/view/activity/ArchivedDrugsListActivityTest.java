package org.openlmis.core.view.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import android.content.Intent;
import com.google.inject.AbstractModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.presenter.StockCardPresenter;
import org.openlmis.core.utils.Constants;
import org.robolectric.Robolectric;
import androidx.test.core.app.ApplicationProvider;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowApplication;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class ArchivedDrugsListActivityTest {

  private ArchivedDrugsListActivity archivedListActivity;
  private StockCard stockCard;
  private StockCardPresenter mockedPresenter;
  private ActivityController<ArchivedDrugsListActivity> activityController;

  @Before
  public void setUp() {
    mockedPresenter = mock(StockCardPresenter.class);
    RoboGuice.overrideApplicationInjector(ApplicationProvider.getApplicationContext(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(StockCardPresenter.class).toInstance(mockedPresenter);
      }
    });

    activityController = Robolectric.buildActivity(ArchivedDrugsListActivity.class);
    archivedListActivity = activityController.create().get();

    Product product = new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40")
        .setStrength("10mg").setType("VIA").build();
    stockCard = new StockCardBuilder()
        .setProduct(product)
        .setStockCardId(200L)
        .setStockOnHand(100)
        .build();
  }

  @After
  public void tearDown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldViewMovementHistoryActivityWhenHistoryViewClicked() {
    archivedListActivity.archiveStockCardListener.viewMovementHistory(stockCard);

    Intent historyIntent = ShadowApplication.getInstance().getNextStartedActivity();

    assertThat(historyIntent).isNotNull();
    assertThat(historyIntent.getLongExtra(Constants.PARAM_STOCK_CARD_ID, 0)).isEqualTo(200L);
    assertThat(historyIntent.getStringExtra(Constants.PARAM_STOCK_NAME))
        .isEqualTo("Lamivudina 150mg [08S40]");
    assertThat(historyIntent.getBooleanExtra(Constants.PARAM_IS_FROM_ARCHIVE, false)).isTrue();

  }
}