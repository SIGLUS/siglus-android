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

  BaseReportPresenter presenter;

  protected abstract BaseReportPresenter injectPresenter();

  @Override
  public Presenter initPresenter() {
    return presenter;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    presenter = injectPresenter();
    super.onCreate(savedInstanceState);
  }

  protected void finish() {
    getActivity().finish();
  }

  public void onBackPressed() {
    if (presenter.isDraft()) {
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
    dialogFragment.show(getActivity().getFragmentManager(), "back_confirm_dialog");
    dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
      @Override
      public void positiveClick(String tag) {
        presenter.deleteDraft();
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

    signatureDialog.show(this.getFragmentManager());
  }

  protected abstract String getSignatureDialogTitle();

  protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
    public void onSign(String sign) {
      Subscription subscription = presenter.getOnSignObservable(sign)
          .subscribe(getOnSignedAction());
      subscriptions.add(subscription);
    }
  };

  protected abstract Action1<? super Void> getOnSignedAction();

  public void showMessageNotifyDialog() {
    SimpleDialogFragment notifyDialog = SimpleDialogFragment.newInstance(null,
        getNotifyDialogMsg(), null, getString(R.string.btn_continue), "showMessageNotifyDialog");

    notifyDialog.show(getActivity().getFragmentManager(), "showMessageNotifyDialog");
  }

  protected abstract String getNotifyDialogMsg();
}
