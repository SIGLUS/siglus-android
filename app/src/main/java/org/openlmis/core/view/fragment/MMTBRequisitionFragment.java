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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import java.util.Date;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.MMTBRequisitionPresenter;
import org.openlmis.core.presenter.MMTBRequisitionPresenter.MMTBRequisitionView;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import roboguice.RoboGuice;
import rx.functions.Action1;

public class MMTBRequisitionFragment extends BaseReportFragment implements MMTBRequisitionView {

  private long formId;
  private Date periodEndDate;
  private MMTBRequisitionPresenter presenter;

  @Override
  protected BaseReportPresenter injectPresenter() {
    presenter = RoboGuice.getInjector(requireActivity()).getInstance(MMTBRequisitionPresenter.class);
    return presenter;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    formId = requireActivity().getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0);
    periodEndDate = ((Date) requireActivity().getIntent()
        .getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_mmtb_requsition, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (SharedPreferenceMgr.getInstance().shouldSyncLastYearStockData()) {
      ToastUtil.showInCenter(R.string.msg_stock_movement_is_not_ready);
      finish();
      return;
    }
    if (isSavedInstanceState && presenter.getRnRForm() != null) {
      presenter.updateFormUI();
    } else {
      presenter.loadData(formId, periodEndDate);
    }
  }

  @Override
  public void refreshRequisitionForm(RnRForm form) {
    requireActivity().setTitle(getString(R.string.label_mmtb_title,
        DateUtil.formatDateWithoutYear(form.getPeriodBegin()),
        DateUtil.formatDateWithoutYear(form.getPeriodEnd()))
    );
    // 1. refresh rnr form items
    // 2. refresh three line items
    // 3. refresh base info
    // 4. consider how to save treatment phase and consumption table info.
  }

  @Override
  public void setProcessButtonName(String buttonName) {
    actionPanelView.setPositiveButtonText(buttonName);
  }

  @Override
  public void completeSuccess() {
    ToastUtil.show(R.string.msg_mmtb_submit_tip);
    finish();
  }

  @NonNull
  public String getSignatureDialogTitle() {
    return presenter.isDraftOrDraftMissed()
        ? getResources().getString(R.string.msg_mmtb_submit_signature)
        : getResources().getString(R.string.msg_approve_signature_mmtb);
  }

  @Override
  protected void finish() {
    requireActivity().setResult(Activity.RESULT_OK);
    super.finish();
  }

  protected Action1<Void> getOnSignedAction() {
    return aVoid -> {
      if (presenter.getRnRForm().isSubmitted()) {
        presenter.submitRequisition();
        showMessageNotifyDialog();
      } else {
        presenter.authoriseRequisition();
      }
    };
  }

  @Override
  protected String getNotifyDialogMsg() {
    return getString(R.string.msg_requisition_signature_message_notify_mmtb);
  }
}