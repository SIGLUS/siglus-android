package org.openlmis.core.view.activity;

import android.app.Activity;
import android.content.Intent;
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
import org.openlmis.core.view.viewmodel.SelectInventoryViewModel;
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

        Intent intent = new Intent();
        intent.putExtra(Constants.PARAM_PROGRAM_CODE, "MMIA");
        selectPeriodActivity = Robolectric.buildActivity(SelectPeriodActivity.class).withIntent(intent).create().get();
    }

    @Test
    public void shouldShowFormattedInstrumentTextAndLoadDataWhenActivityStarts() throws Exception {
        Spanned expectedFormattedText = Html.fromHtml(RuntimeEnvironment.application.getString(
                R.string.label_select_close_of_period,
                currentDateTime.monthOfYear().getAsShortText(),
                currentDateTime.toString("dd MMM")));

        verify(mockedPresenter).loadData("MMIA");
        assertThat(selectPeriodActivity.tvInstruction.getText().toString(), is(expectedFormattedText.toString()));

    }

    @Test
    public void shouldInVisibleWarningWhenUserChoseTheInventory() throws Exception {
        List<SelectInventoryViewModel> inventoryList = Arrays.asList(
                new SelectInventoryViewModel(generateInventoryWithDate(new DateTime("2016-01-25").toDate())),
                new SelectInventoryViewModel(generateInventoryWithDate(new DateTime("2016-01-22").toDate())),
                new SelectInventoryViewModel(generateInventoryWithDate(new DateTime("2016-01-19").toDate()))
        );
        selectPeriodActivity.refreshDate(inventoryList);

        shadowOf(selectPeriodActivity.vgContainer).performItemClick(2);

        assertThat(selectPeriodActivity.tvSelectPeriodWarning.getVisibility(), is(View.INVISIBLE));

        selectPeriodActivity.nextBtn.performClick();

        assertTrue(selectPeriodActivity.isFinishing());
        assertThat(shadowOf(selectPeriodActivity).getResultCode(), is(Activity.RESULT_OK));
        Date selectedDate = (Date) shadowOf(selectPeriodActivity).getResultIntent().getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE);
        assertThat(selectedDate, is(new DateTime("2016-01-19").toDate()));
    }

    private Inventory generateInventoryWithDate(Date date) {
        Inventory inventory = new Inventory();
        inventory.setCreatedAt(date);
        inventory.setUpdatedAt(date);
        return inventory;
    }
}