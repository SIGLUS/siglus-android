package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.presenter.SelectPeriodPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.TrackRnREventUtil;
import org.openlmis.core.view.adapter.SelectPeriodAdapter;
import org.openlmis.core.view.viewmodel.SelectInventoryViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_select_period)
public class SelectPeriodActivity extends BaseActivity implements SelectPeriodPresenter.SelectPeriodView {

    @InjectView(R.id.tv_select_period_instruction)
    protected TextView tvInstruction;

    @InjectView(R.id.vg_inventory_date_container)
    protected GridView vgContainer;

    @InjectView(R.id.btn_select_period_next)
    protected Button nextBtn;

    @InjectView(R.id.tv_select_period_warning)
    protected TextView tvSelectPeriodWarning;

    @InjectPresenter(SelectPeriodPresenter.class)
    SelectPeriodPresenter presenter;

    private SelectPeriodAdapter adapter;

    private SelectInventoryViewModel selectedInventory;
    private String programCode;
    private boolean isMissedPeriod;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.SelectPeriodScreen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.programCode = getIntent().getStringExtra(Constants.PARAM_PROGRAM_CODE);
        isMissedPeriod = getIntent().getBooleanExtra(Constants.PARAM_IS_MISSED_PERIOD, false);
        super.onCreate(savedInstanceState);

        init();
    }

    @Override
    protected int getThemeRes() {
        switch (programCode) {
            case Constants.MMIA_PROGRAM_CODE:
                return R.style.AppTheme_AMBER;
            case Constants.VIA_PROGRAM_CODE:
                return R.style.AppTheme_PURPLE;
            default:
                return super.getThemeRes();
        }
    }

    private void init() {
        invalidateNextBtn();

        DateTime date = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
            tvInstruction.setText(Html.fromHtml(this.getString(R.string.label_training_select_close_of_period, date.monthOfYear().getAsShortText(), date.toString("dd MMM"))));
        } else {
            tvInstruction.setText(Html.fromHtml(this.getString(R.string.label_select_close_of_period, date.monthOfYear().getAsShortText(), date.toString("dd MMM"))));
        }

        presenter.loadData(programCode);
        adapter = new SelectPeriodAdapter();
        vgContainer.setAdapter(adapter);

        bindListeners();
    }

    private void bindListeners() {
        vgContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedInventory = adapter.getItem(position);
                invalidateNextBtn();
            }
        });

        nextBtn.setOnClickListener(new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                if (selectedInventory == null) {
                    tvSelectPeriodWarning.setVisibility(View.VISIBLE);
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(Constants.PARAM_SELECTED_INVENTORY_DATE, selectedInventory.getInventoryDate());
                intent.putExtra(Constants.PARAM_IS_MISSED_PERIOD, isMissedPeriod);

                TrackRnREventUtil.trackRnRListEvent(TrackerActions.SelectPeriod, programCode);

                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void invalidateNextBtn() {
        tvSelectPeriodWarning.setVisibility(View.INVISIBLE);
    }

    public static Intent getIntentToMe(Context context, String programCode) {
        return getIntentToMe(context, programCode, false);
    }

    public static Intent getIntentToMe(Context context, String programCode, boolean isMissedPeriod) {
        Intent intent = new Intent(context, SelectPeriodActivity.class);
        intent.putExtra(Constants.PARAM_PROGRAM_CODE, programCode);
        intent.putExtra(Constants.PARAM_IS_MISSED_PERIOD, isMissedPeriod);
        return intent;
    }

    @Override
    public void refreshDate(List<SelectInventoryViewModel> inventories) {
        adapter.refreshDate(inventories);

        for (SelectInventoryViewModel selectInventoryViewModel : inventories) {
            if (selectInventoryViewModel.isChecked()) {
                int position = inventories.indexOf(selectInventoryViewModel);
                vgContainer.setItemChecked(position, true);
                selectedInventory = adapter.getItem(position);
            }
        }
    }
}
