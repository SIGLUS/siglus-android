package org.openlmis.core.view.fragment;

import android.os.Bundle;

import org.openlmis.core.R;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.view.widget.ActionPanelView;

import roboguice.inject.InjectView;

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

    protected void showConfirmDialog() {
        SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
                null,
                getString(R.string.msg_back_confirm),
                getString(R.string.btn_positive),
                getString(R.string.btn_negative),
                "back_confirm_dialog");
        dialogFragment.show(getActivity().getFragmentManager(), "");
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
}
