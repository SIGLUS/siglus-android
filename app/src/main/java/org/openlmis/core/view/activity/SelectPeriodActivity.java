package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.model.Period;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.presenter.SelectPeriodPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ProgramUtil;
import org.openlmis.core.utils.TrackRnREventUtil;
import org.openlmis.core.view.adapter.SelectPeriodAdapter;
import org.openlmis.core.view.fragment.WarningDialogFragment;
import org.openlmis.core.view.fragment.builders.WarningDialogFragmentBuilder;
import org.openlmis.core.view.viewmodel.SelectInventoryViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import java.io.Serializable;
import java.util.List;


import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Observer;
import rx.Subscription;

import static org.openlmis.core.utils.Constants.AL_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.MMIA_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.PTV_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.RAPID_TEST_CODE;
import static org.openlmis.core.utils.Constants.VIA_PROGRAM_CODE;


@ContentView(R.layout.activity_select_period)
public class SelectPeriodActivity extends BaseActivity implements SelectPeriodPresenter.SelectPeriodView {

    private static final String TAG = SelectPeriodActivity.class.getSimpleName();

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
    private Period period;

    @Inject
    private WarningDialogFragmentBuilder warningDialogFragmentBuilder;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.SelectPeriodScreen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.programCode = getIntent().getStringExtra(Constants.PARAM_PROGRAM_CODE);
        isMissedPeriod = getIntent().getBooleanExtra(Constants.PARAM_IS_MISSED_PERIOD, false);
        period = (Period) getIntent().getSerializableExtra(Constants.PARAM_PERIOD);
        super.onCreate(savedInstanceState);

        init();
    }

    @Override
    protected int getThemeRes() {
        return ProgramUtil.getThemeRes(programCode);
    }

    private void init() {
        invalidateNextBtn();

        DateTime date = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
            tvInstruction.setText(Html.fromHtml(this.getString(R.string.label_training_select_close_of_period, date.toString("dd MMM"))));
        } else {
            tvInstruction.setText(Html.fromHtml(this.getString(R.string.label_select_close_of_period, date.monthOfYear().getAsShortText(), date.toString("dd MMM"))));
        }

        presenter.loadData(programCode, period);
        adapter = new SelectPeriodAdapter();
        vgContainer.setAdapter(adapter);

        bindListeners();
    }

    private void bindListeners() {
        vgContainer.setOnItemClickListener((parent, view, position, id) -> {
            selectedInventory = adapter.getItem(position);
            invalidateNextBtn();
        });

        nextBtn.setOnClickListener(new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                if (selectedInventory == null) {
                    tvSelectPeriodWarning.setVisibility(View.VISIBLE);
                    return;
                }
                loading();
                nextBtn.setEnabled(false);
                if (shouldCheckData()) {
                    Subscription subscription = presenter
                            .correctDirtyObservable(getProgramFromCode(programCode))
                            .subscribe(afterCorrectDirtyDataHandler());
                    subscriptions.add(subscription);
                } else {
                    goNextPage();
                }
            }
        });
    }

    private void goNextPage() {
        loaded();
        Intent intent = new Intent();
        intent.putExtra(Constants.PARAM_SELECTED_INVENTORY_DATE, selectedInventory.getInventoryDate());
        if (programCode.equals(RAPID_TEST_CODE)) {
            intent.putExtra(Constants.PARAM_PERIOD, (Serializable) new Period(period.getBegin(), new DateTime(selectedInventory.getInventoryDate())));
        }
        intent.putExtra(Constants.PARAM_IS_MISSED_PERIOD, isMissedPeriod);

        TrackRnREventUtil.trackRnRListEvent(TrackerActions.SelectPeriod, programCode);

        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean shouldCheckData() {
        return AL_PROGRAM_CODE.equals(programCode)
                || MMIA_PROGRAM_CODE.equals(programCode)
                || VIA_PROGRAM_CODE.equals(programCode)
                || RAPID_TEST_CODE.equals(programCode)
                || PTV_PROGRAM_CODE.equals(programCode);
    }

    protected Observer<Pair<Constants.Program, List<StockCard>>> afterCorrectDirtyDataHandler() {
        return new Observer<Pair<Constants.Program, List<StockCard>>>() {
            @Override
            public void onCompleted() {
                goNextPage();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Pair<Constants.Program, List<StockCard>> deletedProgramStocks) {
                loaded();
                List<StockCard> stockCards = deletedProgramStocks.second;
                nextBtn.setEnabled(true);

                WarningDialogFragment warningDialogFragment = warningDialogFragmentBuilder
                        .build(buildWarningDialogFragmentDelegate(deletedProgramStocks.first),
                                getString(R.string.dirty_data_correct_warning, getDeletedProductCodeList(stockCards)),
                                getString(R.string.btn_del),
                                getString(R.string.dialog_cancel));
                warningDialogFragment.show(getFragmentManager(), "deleteProductWarningDialogFragment");
            }
        };
    }


    @NonNull
    private WarningDialogFragment.DialogDelegate buildWarningDialogFragmentDelegate(final Constants.Program program) {
        return () -> finish();
    }

    private Constants.Program getProgramFromCode(String programCode) {
        Constants.Program program = null;
        switch (programCode) {
            case MMIA_PROGRAM_CODE:
                program = Constants.Program.MMIA_PROGRAM;
                break;
            case AL_PROGRAM_CODE:
                program = Constants.Program.AL_PROGRAM;
                break;
            case VIA_PROGRAM_CODE:
                program = Constants.Program.VIA_PROGRAM;
                break;
            case PTV_PROGRAM_CODE:
                program = Constants.Program.PTV_PROGRAM;
                break;
            case RAPID_TEST_CODE:
                program = Constants.Program.RAPID_TEST_PROGRAM;
        }
        return program;
    }

    private void invalidateNextBtn() {
        tvSelectPeriodWarning.setVisibility(View.INVISIBLE);
    }

    public static Intent getIntentToMe(Context context, String programCode) {
        return getIntentToMe(context, programCode, false);
    }

    public static Intent getIntentToMe(Context context, String programCode, Period period) {
        Intent intent = getIntentToMe(context, programCode, false);
        intent.putExtra(Constants.PARAM_PERIOD, (Serializable) period);
        return intent;
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
