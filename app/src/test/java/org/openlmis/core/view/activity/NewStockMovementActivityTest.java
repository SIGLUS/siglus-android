package org.openlmis.core.view.activity;

import android.content.Intent;
import android.view.View;

import com.google.inject.AbstractModule;

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
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class NewStockMovementActivityTest {

    private NewStockMovementActivity newStockMovementActivity;
    private NewStockMovementPresenter mockedPresenter;

    @Before
    public void setUp() throws Exception {
        mockedPresenter = mock(NewStockMovementPresenter.class);
        StockMovementViewModel stockMovementViewModel = new StockMovementViewModelBuilder().withIssued("100")
                .withMovementDate("2011-11-11")
                .withDocumentNo("12345")
                .withNegativeAdjustment(null)
                .withPositiveAdjustment(null)
                .withIssued("100")
                .withReceived(null)
                .withStockExistence("200")
                .withMovementReason(new MovementReasonManager.MovementReason(MovementReasonManager.MovementType.ISSUE, "ISSUE_1", "issue description")).build();

        LotMovementViewModel lot1 = new LotMovementViewModelBuilder().setLotNumber("test1").setLotSOH("100").setQuantity("50").build();
        LotMovementViewModel lot2 = new LotMovementViewModelBuilder().setLotNumber("test2").setLotSOH("100").setQuantity("50").build();
        stockMovementViewModel.getExistingLotMovementViewModelList().addAll(Arrays.asList(lot1,lot2));

        StockCard stockcard = new StockCardBuilder().setProduct(ProductBuilder.buildAdultProduct()).build();

        when(mockedPresenter.getStockMovementViewModel()).thenReturn(stockMovementViewModel);
        when(mockedPresenter.getStockCard()).thenReturn(stockcard);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
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
        newStockMovementActivity = Robolectric.buildActivity(NewStockMovementActivity.class).withIntent(intent).create().get();
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldShowRequestedQuantityWhenMovementTypeIsIssue() {
        assertEquals(View.VISIBLE, newStockMovementActivity.movementDetailsView.findViewById(R.id.ly_requested_quantity).getVisibility());
    }

    @Test
    public void shouldNotShowActionAddNewLotWhenMovementTypeIsIssue() {
        assertEquals(View.GONE, newStockMovementActivity.newMovementLotListView.findViewById(R.id.action_add_new_lot).getVisibility());
    }

    @Test
    public void shouldInitExistingLotList() {
        assertEquals(View.VISIBLE, newStockMovementActivity.newMovementLotListView.findViewById(R.id.rv_existing_lot_list).getVisibility());
    }

}