package org.openlmis.core.view.activity;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.view.MenuItem;
import com.google.inject.AbstractModule;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.presenter.BulkEntriesPresenter;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.fakes.RoboMenuItem;
import roboguice.RoboGuice;
import rx.Observable;

@RunWith(LMISTestRunner.class)
public class BulkEntriesActivityTest {

  private BulkEntriesActivity bulkEntriesActivity;
  private BulkEntriesPresenter mockedPresenter;
  private ActivityController<BulkEntriesActivity> bulkEntriesActivityActivityController;

  @Before
  public void setUp() throws Exception {
    mockedPresenter = mock(BulkEntriesPresenter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(BulkEntriesPresenter.class).toInstance(mockedPresenter);
      }
    });
    Observable<List<BulkEntriesViewModel>> value = Observable
        .create(subscriber -> {

        });
    when(mockedPresenter.getBulkEntriesViewModelsFromDraft()).thenReturn(value);

    bulkEntriesActivityActivityController = Robolectric.buildActivity(BulkEntriesActivity.class);
    bulkEntriesActivity = bulkEntriesActivityActivityController.create().get();
  }

  @After
  public void tearDown() {
    bulkEntriesActivityActivityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldGetScreenNameSuccess() {
    // then
    assertEquals(bulkEntriesActivity.getScreenName(), ScreenName.BULK_ENTRIES_SCREEN);
  }

  @Test
  public void shouldGoToAddProductsPage() {
    // given
    MenuItem menuItem = new RoboMenuItem(R.id.action_add_product);
    // when
    bulkEntriesActivity.onOptionsItemSelected(menuItem);
    // then
    Intent startedIntent = shadowOf(bulkEntriesActivity).getNextStartedActivity();
    assertThat(startedIntent.getComponent().getClassName(), equalTo(AddProductsToBulkEntriesActivity.class.getName()));
  }
}