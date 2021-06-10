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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.googleanalytics.TrackerActions;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.RnRFormListPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ProgramUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.utils.TrackRnREventUtil;
import org.openlmis.core.view.adapter.RnRFormListAdapter;
import org.openlmis.core.view.fragment.WarningDialogFragment;
import org.openlmis.core.view.fragment.builders.WarningDialogFragmentBuilder;
import org.openlmis.core.view.holder.RnRFormViewHolder.RnRFormItemClickListener;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_rnr_list)
public class RnRFormListActivity extends BaseReportListActivity {

  public static final int DEFAULT_FORM_ID_OF_NOT_AUTHORIZED = 0;
  long rnrFormId = DEFAULT_FORM_ID_OF_NOT_AUTHORIZED;

  @InjectView(R.id.rnr_form_list)
  RecyclerView listView;

  @InjectView(R.id.tv_archived_old_data)
  TextView tvArchivedOldData;

  @InjectPresenter(RnRFormListPresenter.class)
  RnRFormListPresenter presenter;
  private ArrayList<RnRFormViewModel> data;

  private Constants.Program program;
  private RnRFormListAdapter adapter;
  @Inject
  private WarningDialogFragmentBuilder warningDialogFragmentBuilder;

  public static Intent getIntentToMe(Context context, Constants.Program programCode) {
    Intent intent = new Intent(context, RnRFormListActivity.class);
    intent.putExtra(Constants.PARAM_PROGRAM_CODE, programCode);
    return intent;
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.RN_R_FORM_HISTORY_SCREEN;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    program = getProgram();

    setTitle(program.getTitle());

    if (!SharedPreferenceMgr.getInstance().hasDeletedOldRnr()) {
      tvArchivedOldData.setVisibility(View.GONE);
    }

    presenter.setViewProgram(program);
    presenter.setProgramCode(program.getCode());

    listView.setLayoutManager(new LinearLayoutManager(this));
    listView.setHasFixedSize(true);
    data = new ArrayList<>();
    adapter = new RnRFormListAdapter(this, data, rnRFormItemClickListener);
    listView.setAdapter(adapter);

    loadForms();
  }

  private Constants.Program getProgram() {
    if (program == null) {
      program = (Constants.Program) getIntent().getSerializableExtra(Constants.PARAM_PROGRAM_CODE);
    }
    return program;
  }

  @Override
  protected int getThemeRes() {
    if (program == null) {
      program = getProgram();
    }
    return ProgramUtil.getThemeRes(program.getCode());
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode != Activity.RESULT_OK) {
      return;
    }

    switch (requestCode) {
      case Constants.REQUEST_FROM_RNR_LIST_PAGE:
        loadForms();
        break;
      case Constants.REQUEST_SELECT_PERIOD_END:
        Date periodEndDate = (Date) data
            .getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE);
        boolean isMissedPeriod = data.getBooleanExtra(Constants.PARAM_IS_MISSED_PERIOD, false);
        createRequisition(periodEndDate, isMissedPeriod);
        break;
    }
  }

  protected void loadForms() {
    if (!isLoading) {
      loading();
      Subscription subscription = presenter.loadRnRFormList().subscribe(getRnRFormSubscriber());
      subscriptions.add(subscription);
    }
  }

  protected RnRFormItemClickListener rnRFormItemClickListener = new RnRFormItemClickListener() {
    @Override
    public void deleteForm(final RnRForm form) {

      WarningDialogFragment warningDialogFragment = warningDialogFragmentBuilder
          .build(buildWarningDialogFragmentDelegate(form), R.string.msg_del_requisition,
              R.string.btn_del, R.string.dialog_cancel);
      getSupportFragmentManager().beginTransaction()
          .add(warningDialogFragment, "WarningDialogFragment").commitNow();
    }

    @Override
    public void clickBtnView(RnRFormViewModel model, View view) {
      switch (model.getType()) {
        case RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD:
          startActivityForResult(PhysicalInventoryActivity.getIntentToMe(RnRFormListActivity.this),
              Constants.REQUEST_FROM_RNR_LIST_PAGE);
          break;
        case RnRFormViewModel.TYPE_INVENTORY_DONE:
          startActivityForResult(
              SelectPeriodActivity.getIntentToMe(RnRFormListActivity.this, model.getProgramCode()),
              Constants.REQUEST_SELECT_PERIOD_END);
          TrackRnREventUtil.trackRnRListEvent(TrackerActions.CREATE_RNR, program.getCode());
          break;
        case RnRFormViewModel.TYPE_SYNCED_HISTORICAL:
          rnrFormId = model.getId();
          goToRequisitionPage(rnrFormId);
          break;
        case RnRFormViewModel.TYPE_FIRST_MISSED_PERIOD:
          startActivityForResult(SelectPeriodActivity.getIntentToMe(RnRFormListActivity.this,
              model.getProgramCode(),
              true,
              model.getPeriodEndMonth()),
              Constants.REQUEST_SELECT_PERIOD_END);
          break;
        default:
          rnrFormId = DEFAULT_FORM_ID_OF_NOT_AUTHORIZED;
          goToRequisitionPage(rnrFormId);
          break;
      }
      view.setEnabled(true);
    }
  };

  @NonNull
  private WarningDialogFragment.DialogDelegate buildWarningDialogFragmentDelegate(
      final RnRForm form) {
    return () -> deleteRnRForm(form);
  }

  private void goToRequisitionPage(long rnrFormId) {
    Intent intent = null;
    switch (program) {
      case MMIA_PROGRAM:
        intent = MMIARequisitionActivity.getIntentToMe(this, rnrFormId);
        break;
      case VIA_PROGRAM:
        intent = VIARequisitionActivity.getIntentToMe(this, rnrFormId);
        break;
      case AL_PROGRAM:
        intent = ALRequisitionActivity.getIntentToMe(this, rnrFormId);
        break;
      case PTV_PROGRAM:
        intent = PTVRequisitionActivity.getIntentToMe(this, rnrFormId);

    }
    startActivityForResult(intent, Constants.REQUEST_FROM_RNR_LIST_PAGE);
  }

  private void createRequisition(Date periodEndDate, boolean isMissedPeriod) {
    Intent intent = null;
    switch (program) {
      case MMIA_PROGRAM:
        intent = createMMIARequisitionIntent(periodEndDate);
        break;
      case VIA_PROGRAM:
        intent = VIARequisitionActivity.getIntentToMe(this, periodEndDate, isMissedPeriod);
        break;
      case AL_PROGRAM:
        intent = ALRequisitionActivity.getIntentToMe(this, periodEndDate);
        break;
      case PTV_PROGRAM:
        intent = PTVRequisitionActivity.getIntentToMe(this, periodEndDate);
    }
    startActivityForResult(intent, Constants.REQUEST_FROM_RNR_LIST_PAGE);
  }

  private Intent createMMIARequisitionIntent(Date periodEndDate) {
    RnRFormViewModel viewModel = data.size() > 1 ? data.get(data.size() - 2) : null;
    return MMIARequisitionActivity.getIntentToMe(this, periodEndDate, viewModel);
  }


  private void deleteRnRForm(RnRForm form) {
    try {
      presenter.deleteRnRForm(form);
      Subscription subscription = presenter.loadRnRFormList().subscribe(getRnRFormSubscriber());
      subscriptions.add(subscription);
    } catch (LMISException e) {
      ToastUtil.show(getString(R.string.requisition_delete_failed));
      Log.w("RnRFormListActivity", e);
    }
  }

  protected Subscriber<List<RnRFormViewModel>> getRnRFormSubscriber() {
    return new Subscriber<List<RnRFormViewModel>>() {
      @Override
      public void onCompleted() {
        loaded();
      }

      @Override
      public void onError(Throwable e) {
        loaded();
        ToastUtil.show(e.getMessage());
      }

      @Override
      public void onNext(List<RnRFormViewModel> rnRFormViewModels) {
        data.clear();
        data.addAll(rnRFormViewModels);
        adapter.notifyDataSetChanged();
      }
    };
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.menu_rnr_list, menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    boolean isPrepare = super.onPrepareOptionsMenu(menu);
    menu.findItem(R.id.action_create_emergency_rnr)
        .setVisible(program.getCode().equals(Constants.VIA_PROGRAM_CODE));
    return isPrepare;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (R.id.action_create_emergency_rnr == item.getItemId()) {
      checkAndGotoEmergencyPage();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  protected void checkAndGotoEmergencyPage() {
    int dayOfMonth = new DateTime(LMISApp.getInstance().getCurrentTimeMillis()).getDayOfMonth();
    if (dayOfMonth >= Period.INVENTORY_BEGIN_DAY && dayOfMonth < Period.INVENTORY_END_DAY_NEXT) {
      ToastUtil.showForLongTime(R.string.msg_create_emergency_date_invalid);
      return;
    }

    loading();
    presenter.hasMissedPeriod().subscribe(new Subscriber<Boolean>() {
      @Override
      public void onCompleted() {
        loaded();
      }

      @Override
      public void onError(Throwable e) {
        loaded();
        ToastUtil.show(e.getMessage());
      }

      @Override
      public void onNext(Boolean hasMissed) {
        if (hasMissed) {
          ToastUtil.showForLongTime(R.string.msg_create_emergency_has_missed);
        } else {
          startActivityForResult(
              SelectEmergencyProductsActivity.getIntentToMe(RnRFormListActivity.this),
              Constants.REQUEST_FROM_RNR_LIST_PAGE);
        }
      }
    });
  }
}
