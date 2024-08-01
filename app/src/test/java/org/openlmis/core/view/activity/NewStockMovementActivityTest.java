package org.openlmis.core.view.activity;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.view.View;
import com.google.inject.AbstractModule;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementViewModelBuilder;
import org.openlmis.core.presenter.NewStockMovementPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModelBuilder;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.robolectric.Robolectric;
import androidx.test.core.app.ApplicationProvider;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class NewStockMovementActivityTest {

  private NewStockMovementActivity newStockMovementActivity;
  private NewStockMovementPresenter mockedPresenter;
  private ActivityController<NewStockMovementActivity> activityController;

  @Before
  public void setUp() throws Exception {
    mockedPresenter = mock(NewStockMovementPresenter.class);
    StockMovementViewModel stockMovementViewModel = new StockMovementViewModelBuilder()
        .withIssued("100")
        .withMovementDate("2011-11-11")
        .withDocumentNo("12345")
        .withNegativeAdjustment(null)
        .withPositiveAdjustment(null)
        .withIssued("100")
        .withReceived(null)
        .withStockExistence("200")
        .withMovementReason(
            new MovementReasonManager.MovementReason(MovementReasonManager.MovementType.ISSUE,
                "ISSUE_1", "issue description")).build();

    LotMovementViewModel lot1 = new LotMovementViewModelBuilder().setLotNumber("test1")
        .setLotSOH("100").setQuantity("50").build();
    LotMovementViewModel lot2 = new LotMovementViewModelBuilder().setLotNumber("test2")
        .setLotSOH("100").setQuantity("50").build();
    stockMovementViewModel.getExistingLotMovementViewModelList().addAll(Arrays.asList(lot1, lot2));

    StockCard stockcard = new StockCardBuilder().setProduct(ProductBuilder.buildAdultProduct())
        .build();

    stockMovementViewModel.setProduct(stockcard.getProduct());
    when(mockedPresenter.getViewModel()).thenReturn(stockMovementViewModel);
    when(mockedPresenter.getStockCard()).thenReturn(stockcard);
    when(mockedPresenter.getMovementType()).thenReturn(MovementReasonManager.MovementType.ISSUE);

    RoboGuice.overrideApplicationInjector(ApplicationProvider.getApplicationContext(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(NewStockMovementPresenter.class).toInstance(mockedPresenter);
      }
    });

    Intent intent = new Intent()
        .putExtra(Constants.PARAM_STOCK_NAME, "test")
        .putExtra(Constants.PARAM_MOVEMENT_TYPE, MovementReasonManager.MovementType.ISSUE)
        .putExtra(Constants.PARAM_STOCK_CARD_ID, 0L)
        .putExtra(Constants.PARAM_IS_KIT, false);
    activityController = Robolectric.buildActivity(NewStockMovementActivity.class, intent);
    newStockMovementActivity = activityController.create().get();
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldShowRequestedQuantityWhenMovementTypeIsIssue() {
    assertEquals(View.VISIBLE,
        newStockMovementActivity.movementDetailsView.findViewById(R.id.ly_requested_quantity)
            .getVisibility());
  }

  @Test
  public void shouldNotShowActionAddNewLotWhenMovementTypeIsIssue() {
    assertEquals(View.GONE,
        newStockMovementActivity.newMovementLotListView.findViewById(R.id.ly_action_panel)
            .getVisibility());
  }

  @Test
  public void shouldInitExistingLotList() {
    assertEquals(View.VISIBLE,
        newStockMovementActivity.newMovementLotListView.findViewById(R.id.rv_existing_lot_list)
            .getVisibility());
  }

}