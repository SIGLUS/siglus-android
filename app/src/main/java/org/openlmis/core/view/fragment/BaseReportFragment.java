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

import android.os.Bundle;
import org.openlmis.core.R;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.view.widget.ActionPanelView;
import org.openlmis.core.view.widget.SignatureDialog;
import roboguice.inject.InjectView;
import rx.Subscription;
import rx.functions.Action1;

public abstract class BaseReportFragment extends BaseFragment {

  @InjectView(R.id.action_panel)
  ActionPanelView actionPanelView;

  BaseReportPresenter baseReportFragmentPresenter;

  protected abstract BaseReportPresenter injectPresenter();

  @Override
  public Presenter initPresenter() {
    return baseReportFragmentPresenter;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    baseReportFragmentPresenter = injectPresenter();
    super.onCreate(savedInstanceState);
  }

  protected void finish() {
    getActivity().finish();
  }

  public void onBackPressed() {
    if (baseReportFragmentPresenter.isDraft()) {
      showConfirmDialog();
    } else {
      finish();
    }
  }

  private void showConfirmDialog() {
    SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
        null,
        getString(R.string.msg_back_confirm),
        getString(R.string.btn_positive),
        getString(R.string.btn_negative),
        "back_confirm_dialog");
    dialogFragment.show(getParentFragmentManager(), "back_confirm_dialog");
    dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
      @Override
      public void positiveClick(String tag) {
        baseReportFragmentPresenter.deleteDraft();
        finish();
      }

      @Override
      public void negativeClick(String tag) {
      }
    });
  }

  public void showSignDialog() {
    SignatureDialog signatureDialog = new SignatureDialog();
    String signatureDialogTitle = getSignatureDialogTitle();
    signatureDialog.setArguments(SignatureDialog.getBundleToMe(signatureDialogTitle));
    signatureDialog.setDelegate(signatureDialogDelegate);

    signatureDialog.show(this.getParentFragmentManager());
  }

  protected abstract String getSignatureDialogTitle();

  protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
    public void onSign(String sign) {
      Subscription subscription = baseReportFragmentPresenter.getOnSignObservable(sign)
          .subscribe(getOnSignedAction());
      subscriptions.add(subscription);
    }
  };

  protected abstract Action1<? super Void> getOnSignedAction();

  public void showMessageNotifyDialog() {
    SimpleDialogFragment notifyDialog = SimpleDialogFragment.newInstance(null,
        getNotifyDialogMsg(), null, getString(R.string.btn_continue), "showMessageNotifyDialog");

    notifyDialog.show(getParentFragmentManager(), "showMessageNotifyDialog");
  }

  protected abstract String getNotifyDialogMsg();
}
