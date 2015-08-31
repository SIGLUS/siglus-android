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

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.presenter.MMIAFormPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.fragment.OnBackConfirmDialog;
import org.openlmis.core.view.fragment.RetainedFragment;
import org.openlmis.core.view.widget.MMIAInfoList;
import org.openlmis.core.view.widget.MMIARegimeList;
import org.openlmis.core.view.widget.MMIARnrForm;

import java.util.ArrayList;

import roboguice.RoboGuice;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_mmia)
public class MMIAActivity extends BaseActivity implements MMIAFormPresenter.MIMIAFormView, View.OnClickListener {

    @InjectView(R.id.rnr_form_list)
    private MMIARnrForm rnrFromListView;

    @InjectView(R.id.regime_list)
    public MMIARegimeList regimeListView;

    @InjectView(R.id.mmia_info_list)
    private MMIAInfoList mmiaInfoListView;

    @InjectView(R.id.btn_complete)
    private Button btnComplete;

    @InjectView(R.id.tv_regime_total)
    private TextView tvRegimeTotal;

    @InjectView(R.id.et_comment)
    private TextView etComment;

    @InjectView(R.id.scrollview)
    private ScrollView scrollView;

    @InjectView(R.id.btn_save)
    private View btnSave;

    MMIAFormPresenter presenter;

    Boolean hasDataChanged;

    private RetainedFragment dataFragment;
    private boolean commentHasChanged = false;

    @Override
    public MMIAFormPresenter getPresenter() {
        initPresenter();
        return presenter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    private void initPresenter() {
        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        dataFragment = (RetainedFragment) fm.findFragmentByTag("RetainedFragment");

        if (dataFragment == null) {
            dataFragment = new RetainedFragment();
            fm.beginTransaction().add(dataFragment, "RetainedFragment").commit();
            presenter = RoboGuice.getInjector(getApplicationContext()).getInstance(MMIAFormPresenter.class);
            dataFragment.putData("presenter", presenter);
        } else {
            presenter = (MMIAFormPresenter) dataFragment.getData("presenter");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hasDataChanged = (Boolean) dataFragment.getData("hasDataChanged");
        if (presenter.getRnrForm() != null) {
            initUI();
        }
    }

    public void initUI() {

        rnrFromListView.initView(new ArrayList<>(presenter.getRnrForm().getRnrFormItemList()));

        regimeListView.initView(presenter.getRnrForm().getRegimenItemListWrapper(), tvRegimeTotal);

        mmiaInfoListView.initView(presenter.getRnrForm().getBaseInfoItemListWrapper());

        etComment.setText(presenter.getRnrForm().getComments());

        etComment.addTextChangedListener(textWatcher);

        btnSave.setOnClickListener(this);

        btnComplete.setOnClickListener(this);

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideImm();
                return false;
            }
        });
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            commentHasChanged = true;
        }
    };

    @Override
    public void onBackPressed() {
        if (hasDataChanged()) {
            OnBackConfirmDialog.showDialog(getFragmentManager());
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasDataChanged() {
        if (hasDataChanged == null) {
            hasDataChanged = regimeListView.hasDataChanged() || mmiaInfoListView.hasDataChanged() || commentHasChanged;
        }
        return hasDataChanged;
    }

    private void goToHomePage() {
        Intent intent = new Intent(MMIAActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(HomeActivity.class, true);
    }

    @Override
    public void showValidationAlert() {

    }

    @Override
    public void showErrorMessage(String msg) {

    }

    @Override
    protected void onDestroy() {

        dataFragment.putData("presenter", presenter);
        dataFragment.putData("hasDataChanged", hasDataChanged());
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                onSaveBtnClick();
                break;
            case R.id.btn_complete:
                onCompleteBtnClick();
                break;
            default:
                break;

        }
    }

    private void onCompleteBtnClick() {
        if (regimeListView.isCompleted() && mmiaInfoListView.isCompleted() && isTotalEqual()) {
            presenter.saveForm(regimeListView.getDataList(), mmiaInfoListView.getDataList(), etComment.getText().toString());
            ToastUtil.show(R.string.msg_mmia_submit_tip);
            goToHomePage();
        }
    }

    private boolean isTotalEqual() {
        return regimeListView.getTotal() == mmiaInfoListView.getTotal();
    }

    private void onSaveBtnClick() {
        if (hasDataChanged()) {
            presenter.saveDraftForm(regimeListView.getDataList(), mmiaInfoListView.getDataList(), etComment.getText().toString());
        }
        goToHomePage();
    }
}
