/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.view.activity;

import static org.openlmis.core.utils.Constants.AL_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.MMIA_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.RAPID_TEST_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.VIA_PROGRAM_CODE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import com.google.inject.Inject;
import java.util.List;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.annotation.BindEventBus;
import org.openlmis.core.event.DeleteDirtyDataEvent;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.googleanalytics.TrackerActions;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.SelectPeriodPresenter;
import org.openlmis.core.service.DirtyDataManager;
import org.openlmis.core.utils.CompatUtil;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ProgramUtil;
import org.openlmis.core.utils.TrackRnREventUtil;
import org.openlmis.core.view.adapter.SelectPeriodAdapter;
import org.openlmis.core.view.viewmodel.SelectInventoryViewModel;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Observer;
import rx.Subscription;

@BindEventBus
@ContentView(R.layout.activity_select_period)
public class SelectPeriodActivity extends BaseActivity implements
    SelectPeriodPresenter.SelectPeriodView {

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

  @Inject
  SharedPreferenceMgr sharedPreferenceMgr;

  private SelectPeriodAdapter adapter;

  private SelectInventoryViewModel selectedInventory;
  private String programCode;
  private boolean isMissedPeriod;
  private DateTime periodEndMonth;

  @Inject
  DirtyDataManager dirtyDataManager;

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveDeleteDirtyDataEvent(DeleteDirtyDataEvent event) {
    if (DeleteDirtyDataEvent.START == event) {
      loading(getResources().getString(R.string.msg_delete_dirty_data));
    }
    if (DeleteDirtyDataEvent.FINISH == event) {
      loaded();
    }
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.SELECT_PERIOD_SCREEN;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    this.programCode = getIntent().getStringExtra(Constants.PARAM_PROGRAM_CODE);
    isMissedPeriod = getIntent().getBooleanExtra(Constants.PARAM_IS_MISSED_PERIOD, false);
    periodEndMonth = (DateTime) getIntent().getSerializableExtra(Constants.PARAM_PERIOD_END_MONTH);
    super.onCreate(savedInstanceState);

    init();
  }

  @Override
  protected int getThemeRes() {
    return ProgramUtil.getThemeRes(Constants.VIA_PROGRAM_CODE);
  }

  private void init() {
    invalidateNextBtn();

    DateTime date = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
      tvInstruction.setText(CompatUtil.fromHtml(
          this.getString(R.string.label_training_select_close_of_period, date.toString("dd MMM"))));
    } else {
      tvInstruction.setText(CompatUtil.fromHtml(this.getString(R.string.label_select_close_of_period,
          getPreviousPeriodEndMonth(periodEndMonth, date),
          date.toString("dd MMM"))));
    }

    presenter.loadData(programCode);
    adapter = new SelectPeriodAdapter();
    vgContainer.setAdapter(adapter);

    bindListeners();
  }

  private String getPreviousPeriodEndMonth(DateTime periodEndDateTime, DateTime now) {
    return periodEndDateTime != null ? periodEndDateTime.monthOfYear().getAsShortText()
        : now.monthOfYear().getAsShortText();

  }

  private void bindListeners() {
    vgContainer.setOnItemClickListener((parent, view, position, id) -> {
      selectedInventory = adapter.getItem(position);
      invalidateNextBtn();
    });

    nextBtn.setOnClickListener(v -> {
      if (selectedInventory == null) {
        tvSelectPeriodWarning.setVisibility(View.VISIBLE);
        return;
      }
      loading();
      nextBtn.setEnabled(false);
      if (sharedPreferenceMgr.shouldStartHourlyDirtyDataCheck()) {
        Subscription subscription = presenter
            .correctDirtyObservable(getProgramFromCode(programCode))
            .subscribe(afterCorrectDirtyDataHandler());
        subscriptions.add(subscription);
      } else {
        goNextPage();
      }
    });
  }

  private void goNextPage() {
    loaded();
    Intent intent = new Intent();
    intent.putExtra(Constants.PARAM_SELECTED_INVENTORY_DATE, selectedInventory.getInventoryDate());
    intent.putExtra(Constants.PARAM_IS_MISSED_PERIOD, isMissedPeriod);

    TrackRnREventUtil.trackRnRListEvent(TrackerActions.SELECT_PERIOD, programCode);

    setResult(RESULT_OK, intent);
    finish();
  }

  protected Observer<Constants.Program> afterCorrectDirtyDataHandler() {
    return new Observer<Constants.Program>() {
      @Override
      public void onCompleted() {
        goNextPage();
      }

      @Override
      public void onError(Throwable e) {
        loaded();
      }

      @Override
      public void onNext(Constants.Program from) {
        loaded();
        nextBtn.setEnabled(true);
        showDirtyDataWarningDialog(() -> goNextPage());
      }
    };
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
      case RAPID_TEST_PROGRAM_CODE:
        program = Constants.Program.RAPID_TEST_PROGRAM;
        break;
      default:
        // do nothing
    }
    return program;
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

  public static Intent getIntentToMe(Context context, String programCode, boolean isMissedPeriod,
      DateTime periodEnd) {
    Intent intent = new Intent(context, SelectPeriodActivity.class);
    intent.putExtra(Constants.PARAM_PROGRAM_CODE, programCode);
    intent.putExtra(Constants.PARAM_IS_MISSED_PERIOD, isMissedPeriod);
    intent.putExtra(Constants.PARAM_PERIOD_END_MONTH, periodEnd);
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
