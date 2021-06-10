package org.openlmis.core.view.activity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.presenter.SelectPeriodPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.SelectInventoryViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

@RunWith(LMISTestRunner.class)
public class SelectPeriodActivityTest {

  private SelectPeriodPresenter mockedPresenter;
  private SelectPeriodActivity selectPeriodActivity;
  private DateTime currentDateTime;
  private final List<SelectInventoryViewModel> inventoryList = new ArrayList<>();
  private StockMovementRepository stockMovementRepository;
  private StockRepository stockRepository;

  @Before
  public void setUp() throws Exception {
    currentDateTime = new DateTime("2016-02-04");
    LMISTestApp.getInstance().setCurrentTimeMillis(currentDateTime.getMillis());

    mockedPresenter = mock(SelectPeriodPresenter.class);
    stockMovementRepository = mock(StockMovementRepository.class);
    stockRepository = mock(StockRepository.class);

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(SelectPeriodPresenter.class).toInstance(mockedPresenter);
        bind(StockMovementRepository.class).toInstance(stockMovementRepository);
        bind(StockRepository.class).toInstance(stockRepository);
      }
    });

    Intent intent = new Intent();
    intent.putExtra(Constants.PARAM_PROGRAM_CODE, "MMIA");
    intent.putExtra(Constants.PARAM_IS_MISSED_PERIOD, true);
    selectPeriodActivity = Robolectric.buildActivity(SelectPeriodActivity.class, intent).create()
        .get();

    List<SelectInventoryViewModel> selectInventoryViewModels = Arrays.asList(
        new SelectInventoryViewModel(
            generateInventoryWithDate(new DateTime("2016-01-25").toDate())),
        new SelectInventoryViewModel(
            generateInventoryWithDate(new DateTime("2016-01-22").toDate())),
        new SelectInventoryViewModel(generateInventoryWithDate(new DateTime("2016-01-19").toDate()))
    );
    inventoryList.addAll(selectInventoryViewModels);

    RxAndroidPlugins.getInstance().reset();
    RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
      @Override
      public Scheduler getMainThreadScheduler() {
        return Schedulers.immediate();
      }
    });
  }

  @Test
  public void shouldShowFormattedInstrumentTextAndLoadDataWhenActivityStarts() throws Exception {
    Spanned expectedFormattedText = Html.fromHtml(RuntimeEnvironment.application.getString(
        R.string.label_select_close_of_period,
        currentDateTime.monthOfYear().getAsShortText(),
        currentDateTime.toString("dd MMM")));

    verify(mockedPresenter).loadData("MMIA", null);
    assertThat(selectPeriodActivity.tvInstruction.getText().toString(),
        is(expectedFormattedText.toString()));
  }

  @Test
  @Ignore
  public void shouldInVisibleWarningWhenUserChoseTheInventory() throws Exception {
    LMISTestApp.getInstance().setCurrentTimeMillis(100000);
    SingleClickButtonListener.isViewClicked = false;

    when(stockMovementRepository.listLastFiveStockMovements(anyLong()))
        .thenReturn(newArrayList(new StockMovementItem()));
    when(stockMovementRepository.queryFirstStockMovementByStockCardId(anyLong()))
        .thenReturn(new StockMovementItem());
    when(stockRepository.list()).thenReturn(newArrayList(new StockCard()));

    selectPeriodActivity.refreshDate(inventoryList);

    shadowOf(selectPeriodActivity.vgContainer).performItemClick(2);

    assertThat(selectPeriodActivity.tvSelectPeriodWarning.getVisibility(), is(View.INVISIBLE));

    Subscriber subscriber = new TestSubscriber();

    Observable observable = selectPeriodActivity.presenter
        .correctDirtyObservable(Constants.Program.MMIA_PROGRAM);

    observable.subscribe(subscriber);

    selectPeriodActivity.nextBtn.performClick();
//
//        assertTrue(selectPeriodActivity.isFinishing());
//        assertThat(shadowOf(selectPeriodActivity).getResultCode(), is(Activity.RESULT_OK));
//        Date selectedDate = (Date) shadowOf(selectPeriodActivity).getResultIntent().getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE);
//        assertThat(selectedDate, is(new DateTime("2016-01-19").toDate()));
  }

  @Test
  public void shouldCheckedDefaultInventoryDay() throws Exception {
    inventoryList.get(1).setChecked(true);

    selectPeriodActivity.refreshDate(inventoryList);
    assertThat(selectPeriodActivity.vgContainer.getCheckedItemPosition(), is(1));
  }

  private Inventory generateInventoryWithDate(Date date) {
    Inventory inventory = new Inventory();
    inventory.setCreatedAt(date);
    inventory.setUpdatedAt(date);
    return inventory;
  }
}