package org.openlmis.core.view.widget;

import android.view.View;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.presenter.SyncErrorsPresenter;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.FragmentTestUtil;

import java.util.Date;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class SyncDateBottomSheetTest {

    protected SyncDateBottomSheet fragment;
    protected SyncErrorsPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = mock(SyncErrorsPresenter.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(SyncErrorsPresenter.class).toInstance(presenter);
            }
        });

        fragment = new SyncDateBottomSheet();
        fragment.setArguments(fragment.getArgumentsToMe(1, 1));
        FragmentTestUtil.startFragment(fragment);
    }

    @Test
    public void shouldShowRnrFormLastSyncedTimeCorrectly() {
        String formatRnrLastSyncTimeWithMinute = fragment.formatRnrLastSyncTime(new Date().getTime() - 20 * DateUtil.MILLISECONDS_MINUTE);
        assertThat(formatRnrLastSyncTimeWithMinute, equalTo(LMISTestApp.getContext().getString(R.string.label_rnr_form_last_synced_mins_ago, "20")));

        String formatRnrLastSyncTimeWithHour = fragment.formatRnrLastSyncTime(new Date().getTime() - 20 * DateUtil.MILLISECONDS_HOUR);
        assertThat(formatRnrLastSyncTimeWithHour, equalTo(LMISTestApp.getContext().getString(R.string.label_rnr_form_last_synced_hours_ago, "20")));

        String formatRnrLastSyncTimeWithDay = fragment.formatRnrLastSyncTime(new Date().getTime() - 20 * DateUtil.MILLISECONDS_DAY);
        assertThat(formatRnrLastSyncTimeWithDay, equalTo(LMISTestApp.getContext().getString(R.string.label_rnr_form_last_synced_days_ago, "20")));
    }

    @Test
    public void shouldShowStockCardLastSyncedTimeCorrectly() {
        String formatStockCardLastSyncTimeWithMinute = fragment.formatStockCardLastSyncTime(new Date().getTime() - 20 * DateUtil.MILLISECONDS_MINUTE);
        assertThat(formatStockCardLastSyncTimeWithMinute, equalTo(LMISTestApp.getContext().getString(R.string.label_stock_card_last_synced_mins_ago, "20")));

        String formatStockCardLastSyncTimeWithHour = fragment.formatStockCardLastSyncTime(new Date().getTime() - 20 * DateUtil.MILLISECONDS_HOUR);
        assertThat(formatStockCardLastSyncTimeWithHour, equalTo(LMISTestApp.getContext().getString(R.string.label_stock_card_last_synced_hours_ago, "20")));

        String formatStockCardLastSyncTimeWithDay = fragment.formatStockCardLastSyncTime(new Date().getTime() - 20 * DateUtil.MILLISECONDS_DAY);
        assertThat(formatStockCardLastSyncTimeWithDay, equalTo(LMISTestApp.getContext().getString(R.string.label_stock_card_last_synced_days_ago, "20")));
    }

    @Test
    public void shouldShowErrorIconWhenHasSyncError() throws Exception {
        when(presenter.hasSyncError(SyncType.RnRForm)).thenReturn(true);
        when(presenter.hasSyncError(SyncType.StockCards)).thenReturn(true);
        fragment.onViewCreated(null, null);

        assertThat(fragment.ivRnRError.getVisibility(), is(View.VISIBLE));
        assertThat(fragment.ivStockcardError.getVisibility(), is(View.VISIBLE));
    }
}