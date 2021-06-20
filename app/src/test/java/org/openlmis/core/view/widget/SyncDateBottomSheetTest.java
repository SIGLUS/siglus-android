package org.openlmis.core.view.widget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.view.View;
import com.google.inject.AbstractModule;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.presenter.SyncErrorsPresenter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.RobolectricUtils;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import roboguice.fragment.SupportDialogFragmentController;

@RunWith(LMISTestRunner.class)
public class SyncDateBottomSheetTest {

  protected SupportDialogFragmentController<SyncDateBottomSheet> fragmentController;
  SyncErrorsPresenter presenter;
  private long timeMills;

  @Before
  public void setUp() throws Exception {
    presenter = mock(SyncErrorsPresenter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(SyncErrorsPresenter.class).toInstance(presenter);
      }
    });
    fragmentController = SupportDialogFragmentController
        .of(SyncDateBottomSheet.class, SyncDateBottomSheet.getArgumentsToMe(1, 1));
    RobolectricUtils.waitLooperIdle();
    timeMills = new DateTime().getMillis();
  }

  @Test
  public void shouldShowRnrFormLastSyncedTimeCorrectly() {
    // given
    SyncDateBottomSheet fragment = fragmentController.setupDialogFragment();
    RobolectricUtils.waitLooperIdle();

    // when
    String formatRnrLastSyncTimeWithMinute = fragment
        .formatRnrLastSyncTime(timeMills - 20 * DateUtil.MILLISECONDS_MINUTE);
    String formatRnrLastSyncTimeWithHour = fragment
        .formatRnrLastSyncTime(timeMills - 20 * DateUtil.MILLISECONDS_HOUR);
    String formatRnrLastSyncTimeWithDay = fragment
        .formatRnrLastSyncTime(timeMills - 1 * DateUtil.MILLISECONDS_DAY);
    String formatRnrLastSyncTimeWithDays = fragment
        .formatRnrLastSyncTime(timeMills - 20 * DateUtil.MILLISECONDS_DAY);

    // that
    assertThat(formatRnrLastSyncTimeWithMinute, equalTo("Requisition last synced 20 minutes ago"));
    assertThat(formatRnrLastSyncTimeWithHour, equalTo("Requisition last synced 20 hours ago"));
    assertThat(formatRnrLastSyncTimeWithDay, equalTo("Requisition last synced 1 day ago"));
    assertThat(formatRnrLastSyncTimeWithDays, equalTo("Requisition last synced 20 days ago"));
  }

  @Test
  public void shouldShowStockCardLastSyncedTimeCorrectly() {
    // given
    SyncDateBottomSheet fragment = fragmentController.setupDialogFragment();
    RobolectricUtils.waitLooperIdle();

    // when
    String formatStockCardLastSyncTimeWithMinute = fragment
        .formatStockCardLastSyncTime(timeMills - DateUtil.MILLISECONDS_MINUTE);
    String formatStockCardLastSyncTimeWithMinutes = fragment
        .formatStockCardLastSyncTime(timeMills - 20 * DateUtil.MILLISECONDS_MINUTE);
    String formatStockCardLastSyncTimeWithHour = fragment
        .formatStockCardLastSyncTime(timeMills - 20 * DateUtil.MILLISECONDS_HOUR);
    String formatStockCardLastSyncTimeWithDay = fragment
        .formatStockCardLastSyncTime(timeMills - 20 * DateUtil.MILLISECONDS_DAY);

    // that
    assertThat(formatStockCardLastSyncTimeWithMinute,
        equalTo("Stock cards last synced 1 minute ago"));
    assertThat(formatStockCardLastSyncTimeWithMinutes,
        equalTo("Stock cards last synced 20 minutes ago"));
    assertThat(formatStockCardLastSyncTimeWithHour,
        equalTo("Stock cards last synced 20 hours ago"));
    assertThat(formatStockCardLastSyncTimeWithDay, equalTo("Stock cards last synced 20 days ago"));
  }

  @Test
  public void shouldShowErrorMsgWhenFirstSyncFailed() throws Exception {
    // given
    when(presenter.hasRnrSyncError()).thenReturn(true);
    when(presenter.hasStockCardSyncError()).thenReturn(true);
    SyncDateBottomSheet fragment = fragmentController.setupDialogFragment();
    RobolectricUtils.waitLooperIdle();

    // when
    String formatRnrLastSyncTime = fragment.formatRnrLastSyncTime(0);
    String formatStockCardLastSyncTimeWithMinute = fragment.formatStockCardLastSyncTime(0);

    // that
    assertThat(formatRnrLastSyncTime, equalTo("Initial requisition sync failed, please retry."));
    assertThat(formatStockCardLastSyncTimeWithMinute,
        equalTo("Initial stock card sync failed, please retry."));
  }

  @Test
  public void shouldShowEmptyMsgWhenHasNotSynced() throws Exception {
    // given
    SyncDateBottomSheet fragment = fragmentController.setupDialogFragment();
    RobolectricUtils.waitLooperIdle();

    // when
    String formatRnrLastSyncTime = fragment.formatRnrLastSyncTime(0);
    String formatStockCardLastSyncTimeWithMinute = fragment.formatStockCardLastSyncTime(0);

    // that
    assertThat(formatRnrLastSyncTime, equalTo(""));
    assertThat(formatStockCardLastSyncTimeWithMinute, equalTo(""));
  }

  @Test
  public void shouldShowErrorIconWhenHasSyncError() throws Exception {
    // given
    when(presenter.hasRnrSyncError()).thenReturn(true);
    when(presenter.hasStockCardSyncError()).thenReturn(true);

    // when
    SyncDateBottomSheet fragment = fragmentController.setupDialogFragment();
    RobolectricUtils.waitLooperIdle();

    // that
    assertThat(fragment.ivRnRError.getVisibility(), is(View.VISIBLE));
    assertThat(fragment.ivStockcardError.getVisibility(), is(View.VISIBLE));
  }
}