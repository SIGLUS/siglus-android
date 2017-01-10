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
        super.onCreate(savedInstanceState);
        presenter = injectPresenter();
    }

    protected void finish() {
        getActivity().finish();
    }


}
