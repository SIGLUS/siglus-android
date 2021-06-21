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

package org.openlmis.core.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.googleanalytics.TrackerActions;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.RnRFormListPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.utils.TrackRnREventUtil;
import org.openlmis.core.view.activity.ALRequisitionActivity;
import org.openlmis.core.view.activity.MMIARequisitionActivity;
import org.openlmis.core.view.activity.PhysicalInventoryActivity;
import org.openlmis.core.view.activity.SelectPeriodActivity;
import org.openlmis.core.view.activity.VIARequisitionActivity;
import org.openlmis.core.view.adapter.RnRFormListAdapter;
import org.openlmis.core.view.fragment.builders.WarningDialogFragmentBuilder;
import org.openlmis.core.view.holder.RnRFormViewHolder.RnRFormItemClickListener;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

public class ReportListFragment extends BaseReportListFragment {

  public static final int DEFAULT_FORM_ID_OF_NOT_AUTHORIZED = 0;

  static final String PARAMS_REPORT_PROGRAM = "params_report_program";

  @InjectView(R.id.rv_report_list)
  RecyclerView rvRequisitionList;

  @InjectView(R.id.tv_archived_old_data)
  TextView tvArchivedOldData;

  @Inject
  RnRFormListPresenter presenter;

  long rnrFormId = DEFAULT_FORM_ID_OF_NOT_AUTHORIZED;

  private RnRFormListAdapter adapter;

  Program program;

  private ArrayList<RnRFormViewModel> data;

  private WarningDialogFragment warningDialog;

  private boolean isLoading = false;

  public static ReportListFragment newInstance(Program program) {
    final ReportListFragment reportListFragment = new ReportListFragment();
    final Bundle params = new Bundle();
    params.putSerializable(PARAMS_REPORT_PROGRAM, program);
    reportListFragment.setArguments(params);
    return reportListFragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_requisition_list, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    program = (Program) requireArguments().getSerializable(PARAMS_REPORT_PROGRAM);
    presenter.setProgramCode(program.getProgramCode());

    if (!SharedPreferenceMgr.getInstance().hasDeletedOldRnr()) {
      tvArchivedOldData.setVisibility(View.GONE);
    }

    rvRequisitionList.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvRequisitionList.setHasFixedSize(true);
    data = new ArrayList<>();
    adapter = new RnRFormListAdapter(requireContext(), data, rnRFormItemClickListener);
    rvRequisitionList.setAdapter(adapter);

    loadForms();
  }

  @Override
  public Presenter initPresenter() {
    return presenter;
  }

  @Override
  protected void loadForms() {
    if (!isLoading) {
      loading();
      Subscription subscription = presenter.loadRnRFormList().subscribe(getRnRFormSubscriber());
      subscriptions.add(subscription);
      isLoading = true;
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode,
      @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != Activity.RESULT_OK) {
      return;
    }

    switch (requestCode) {
      case Constants.REQUEST_FROM_RNR_LIST_PAGE:
        loadForms();
        break;
      case Constants.REQUEST_SELECT_PERIOD_END:
        if (data == null) {
          return;
        }
        Date periodEndDate = (Date) data.getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE);
        boolean isMissedPeriod = data.getBooleanExtra(Constants.PARAM_IS_MISSED_PERIOD, false);
        createRequisition(periodEndDate, isMissedPeriod);
        break;
      default:
        // do nothing
    }
  }

  @NonNull
  private WarningDialogFragment.DialogDelegate buildWarningDialogFragmentDelegate(
      final RnRForm form) {
    return () -> deleteRnRForm(form);
  }

  private void goToRequisitionPage(long rnrFormId) {
    Intent intent = null;
    switch (program.getProgramCode()) {
      case Program.VIA_CODE:
        intent = VIARequisitionActivity.getIntentToMe(requireContext(), rnrFormId);
        break;
      case Program.MALARIA_CODE:
        intent = ALRequisitionActivity.getIntentToMe(requireContext(), rnrFormId);
        break;
      case Program.TARV_CODE:
        intent = MMIARequisitionActivity.getIntentToMe(requireContext(), rnrFormId);
        break;
      default:
        // do nothing
    }
    startActivityForResult(intent, Constants.REQUEST_FROM_RNR_LIST_PAGE);
  }

  private void createRequisition(Date periodEndDate, boolean isMissedPeriod) {
    Intent intent = null;
    switch (program.getProgramCode()) {
      case Program.VIA_CODE:
        intent = VIARequisitionActivity.getIntentToMe(requireContext(), periodEndDate, isMissedPeriod);
        break;
      case Program.MALARIA_CODE:
        intent = ALRequisitionActivity.getIntentToMe(requireContext(), periodEndDate);
        break;
      case Program.TARV_CODE:
        intent = createMMIARequisitionIntent(periodEndDate);
        break;
      default:
        // do nothing
    }
    startActivityForResult(intent, Constants.REQUEST_FROM_RNR_LIST_PAGE);
  }

  private Intent createMMIARequisitionIntent(Date periodEndDate) {
    RnRFormViewModel viewModel = data.size() > 1 ? data.get(data.size() - 2) : null;
    return MMIARequisitionActivity.getIntentToMe(requireContext(), periodEndDate, viewModel);
  }

  private void deleteRnRForm(RnRForm form) {
    try {
      presenter.deleteRnRForm(form);
      Subscription subscription = presenter.loadRnRFormList().subscribe(getRnRFormSubscriber());
      subscriptions.add(subscription);
    } catch (LMISException e) {
      ToastUtil.show(getString(R.string.requisition_delete_failed));
      Log.w("ReportListFragment", e);
    }
  }

  protected Subscriber<List<RnRFormViewModel>> getRnRFormSubscriber() {
    return new Subscriber<List<RnRFormViewModel>>() {
      @Override
      public void onCompleted() {
        loaded();
        isLoading = true;
      }

      @Override
      public void onError(Throwable e) {
        loaded();
        isLoading = true;
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

  protected RnRFormItemClickListener rnRFormItemClickListener = new RnRFormItemClickListener() {
    @Override
    public void deleteForm(final RnRForm form) {
      if (warningDialog.isAdded()) {
        warningDialog.dismissAllowingStateLoss();
      }
      warningDialog = new WarningDialogFragmentBuilder()
          .build(buildWarningDialogFragmentDelegate(form), R.string.msg_del_requisition,
              R.string.btn_del, R.string.dialog_cancel);
      warningDialog.show(getParentFragmentManager(), "WarningDialogFragment");
    }

    @Override
    public void clickBtnView(RnRFormViewModel model, View view) {
      switch (model.getType()) {
        case RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD:
          startActivityForResult(PhysicalInventoryActivity.getIntentToMe(requireContext()),
              Constants.REQUEST_FROM_RNR_LIST_PAGE);
          break;
        case RnRFormViewModel.TYPE_INVENTORY_DONE:
          startActivityForResult(
              SelectPeriodActivity.getIntentToMe(requireContext(), model.getProgramCode()),
              Constants.REQUEST_SELECT_PERIOD_END);
          TrackRnREventUtil.trackRnRListEvent(TrackerActions.CREATE_RNR, program.getProgramCode());
          break;
        case RnRFormViewModel.TYPE_SYNCED_HISTORICAL:
          rnrFormId = model.getId();
          goToRequisitionPage(rnrFormId);
          break;
        case RnRFormViewModel.TYPE_FIRST_MISSED_PERIOD:
          startActivityForResult(SelectPeriodActivity.getIntentToMe(requireContext(),
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
}