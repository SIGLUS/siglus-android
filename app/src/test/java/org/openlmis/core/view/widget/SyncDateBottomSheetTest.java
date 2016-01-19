package org.openlmis.core.view.widget;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(LMISTestRunner.class)
public class SyncDateBottomSheetTest{

    protected SyncDateBottomSheet syncDateBottomSheet;

    @Before
    public void setUp() throws Exception {
        syncDateBottomSheet = new SyncDateBottomSheet();
    }

    @Test
    public void shouldShowRnrFormLastSyncedTimeCorrectly() {
        String formatRnrLastSyncTimeWithMinute = syncDateBottomSheet.formatRnrLastSyncTime(new Date().getTime() - 20 * DateUtil.MILLISECONDS_MINUTE);
        assertThat(formatRnrLastSyncTimeWithMinute, equalTo(LMISTestApp.getContext().getString(R.string.label_rnr_form_last_synced_mins_ago, "20")));

        String formatRnrLastSyncTimeWithHour = syncDateBottomSheet.formatRnrLastSyncTime(new Date().getTime() - 20 * DateUtil.MILLISECONDS_HOUR);
        assertThat(formatRnrLastSyncTimeWithHour, equalTo(LMISTestApp.getContext().getString(R.string.label_rnr_form_last_synced_hours_ago, "20")));

        String formatRnrLastSyncTimeWithDay = syncDateBottomSheet.formatRnrLastSyncTime(new Date().getTime() - 20 * DateUtil.MILLISECONDS_DAY);
        assertThat(formatRnrLastSyncTimeWithDay, equalTo(LMISTestApp.getContext().getString(R.string.label_rnr_form_last_synced_days_ago, "20")));
    }

    @Test
    public void shouldShowStockCardLastSyncedTimeCorrectly() {
        String formatStockCardLastSyncTimeWithMinute = syncDateBottomSheet.formatStockCardLastSyncTime(new Date().getTime() - 20 * DateUtil.MILLISECONDS_MINUTE);
        assertThat(formatStockCardLastSyncTimeWithMinute, equalTo(LMISTestApp.getContext().getString(R.string.label_stock_card_last_synced_mins_ago, "20")));

        String formatStockCardLastSyncTimeWithHour = syncDateBottomSheet.formatStockCardLastSyncTime(new Date().getTime() - 20 * DateUtil.MILLISECONDS_HOUR);
        assertThat(formatStockCardLastSyncTimeWithHour, equalTo(LMISTestApp.getContext().getString(R.string.label_stock_card_last_synced_hours_ago, "20")));

        String formatStockCardLastSyncTimeWithDay = syncDateBottomSheet.formatStockCardLastSyncTime(new Date().getTime() - 20 * DateUtil.MILLISECONDS_DAY);
        assertThat(formatStockCardLastSyncTimeWithDay, equalTo(LMISTestApp.getContext().getString(R.string.label_stock_card_last_synced_days_ago, "20")));
    }
}