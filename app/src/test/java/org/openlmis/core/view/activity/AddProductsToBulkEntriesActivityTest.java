package org.openlmis.core.view.activity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.presenter.AddProductsToBulkEntriesPresenter;
import org.openlmis.core.view.adapter.AddProductsToBulkEntriesAdapter;
import org.openlmis.core.view.viewmodel.ProductsToBulkEntriesViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;
import rx.Observable;


@RunWith(LMISTestRunner.class)
public class AddProductsToBulkEntriesActivityTest {

  private AddProductsToBulkEntriesActivity activity;
  private AddProductsToBulkEntriesPresenter mockedPresenter;
  private AddProductsToBulkEntriesAdapter mockedAdapter;
  private ActivityController<AddProductsToBulkEntriesActivity> activityController;

  @Before
  public void setUp() throws Exception {
    mockedPresenter = mock(AddProductsToBulkEntriesPresenter.class);
    mockedAdapter = mock(AddProductsToBulkEntriesAdapter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(AddProductsToBulkEntriesPresenter.class).toInstance(mockedPresenter);
        bind(AddProductsToBulkEntriesAdapter.class).toInstance(mockedAdapter);
      }
    });
    Observable<List<ProductsToBulkEntriesViewModel>> value = Observable.create(subscriber -> {});
    when(mockedPresenter.getProducts(any(),anyBoolean(), anyString())).thenReturn(value);

    activityController = Robolectric.buildActivity(AddProductsToBulkEntriesActivity.class);
    activity = activityController.create().get();
  }

  @After
  public void tearDown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldGetScreenNameSuccess() {
    // then
    assertEquals(ScreenName.ADD_PRODUCT_TO_BULK_ENTRIES_SCREEN, activity.getScreenName());
  }

  @Test
  public void shouldSetTotalWhenOnSearchStart() {
    // given
    when(mockedAdapter.getItemCount()).thenReturn(100);

    // when
    activity.onSearchStart(null);

    // then
    assertEquals("Total:0", activity.tvTotal.getText().toString());
  }
}