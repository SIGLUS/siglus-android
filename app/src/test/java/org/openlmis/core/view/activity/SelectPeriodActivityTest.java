package org.openlmis.core.view.activity;

import android.app.Activity;
import android.text.Html;
import android.text.Spanned;
import android.view.View;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.presenter.SelectPeriodPresenter;
import org.openlmis.core.utils.Constants;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class SelectPeriodActivityTest {

    private SelectPeriodPresenter mockedPresenter;
    private SelectPeriodActivity selectPeriodActivity;
    private DateTime currentDateTime;

    @Before
    public void setUp() throws Exception {
        currentDateTime = new DateTime("2016-02-04");
        LMISTestApp.getInstance().setCurrentTimeMillis(currentDateTime.getMillis());

        mockedPresenter = mock(SelectPeriodPresenter.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(SelectPeriodPresenter.class).toInstance(mockedPresenter);
            }
        });

        selectPeriodActivity = Robolectric.buildActivity(SelectPeriodActivity.class).create().get();
    }

    @Test
    public void shouldShowFormattedInstrumentTextAndLoadDataWhenActivityStarts() throws Exception {
        Spanned expectedFormattedText = Html.fromHtml(RuntimeEnvironment.application.getString(
                R.string.label_select_close_of_period,
                currentDateTime.monthOfYear().getAsShortText(),
                currentDateTime.toString("dd MMM")));

        verify(mockedPresenter).loadData();
        assertThat(selectPeriodActivity.tvInstruction.getText().toString(), is(expectedFormattedText.toString()));

    }

    @Test
    public void shouldInVisibleWarningWhenUserChoseTheInventory() throws Exception {
        List<Inventory> inventoryList = Arrays.asList(
                generateInventoryWithDate(new DateTime("2016-01-25").toDate()),
                generateInventoryWithDate(new DateTime("2016-01-22").toDate()),
                generateInventoryWithDate(new DateTime("2016-01-19").toDate())
        );
        selectPeriodActivity.refreshDate(inventoryList);

        shadowOf(selectPeriodActivity.vgContainer).performItemClick(2);

        assertThat(selectPeriodActivity.tvSelectPeriodWarning.getVisibility(), is(View.INVISIBLE));

        selectPeriodActivity.nextBtn.performClick();

        assertTrue(selectPeriodActivity.isFinishing());
        assertThat(shadowOf(selectPeriodActivity).getResultCode(), is(Activity.RESULT_OK));
        Inventory selectedInventory = (Inventory) shadowOf(selectPeriodActivity).getResultIntent().getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY);
        assertThat(selectedInventory.getUpdatedAt().toString(), is(new DateTime("2016-01-19").toDate().toString()));
    }

    private Inventory generateInventoryWithDate(Date date) {
        Inventory inventory = new Inventory();
        inventory.setCreatedAt(date);
        inventory.setUpdatedAt(date);
        return inventory;
    }
}