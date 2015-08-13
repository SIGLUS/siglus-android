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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.MMIAFormPresenter;
import org.openlmis.core.view.fragment.RetainedFragment;
import org.openlmis.core.view.widget.MMIAInfoList;
import org.openlmis.core.view.widget.MMIARegimeList;
import org.openlmis.core.view.widget.MMIARnrForm;

import java.sql.SQLException;
import java.util.ArrayList;

import roboguice.RoboGuice;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_mmia_spread)
public class MMIASpreadActivity extends BaseActivity implements MMIAFormPresenter.MIMIAFormView {

    @InjectView(R.id.rnr_form_list)
    private MMIARnrForm rnrFromListView;

    @InjectView(R.id.regime_list)
    private MMIARegimeList regimeListView;

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

    MMIAFormPresenter presenter;

    private RetainedFragment dataFragment;
    private RnRForm rnRForm;

    @Override
    public MMIAFormPresenter getPresenter() {
        initPresenter();
        return presenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rnRForm = presenter.getRnrForm();
        if (rnRForm != null) {
            initUI();
        }
    }

    private void initPresenter() {
        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        dataFragment = (RetainedFragment) fm.findFragmentByTag("RetainedFragment");

        if (dataFragment == null) {
            dataFragment = new RetainedFragment();
            fm.beginTransaction().add(dataFragment, "RetainedFragment").commit();
            presenter = RoboGuice.getInjector(getApplicationContext()).getInstance(MMIAFormPresenter.class);
            dataFragment.setData(presenter);
        } else {
            presenter = (MMIAFormPresenter) dataFragment.getData();
        }
    }

    private void initUI() {

        etComment.setText(rnRForm.getComments());

        rnrFromListView.initView(new ArrayList<>(rnRForm.getRnrFormItemList()));

        regimeListView.initView(rnRForm.getRegimenItemListWrapper(), tvRegimeTotal);

        mmiaInfoListView.initView(rnRForm.getBaseInfoItemListWrapper());

        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (regimeListView.complete() && mmiaInfoListView.complete() && (regimeListView.getTotal() == mmiaInfoListView.getTotal())) {
                    try {
                        rnRForm.setComments(etComment.getText().toString());
                        presenter.saveForm();
                        Intent intent = new Intent(MMIASpreadActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(HomeActivity.class, true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showErrorMessage(e.getMessage());
                    }
                }
            }
        });

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideImm();
                return false;
            }
        });
    }

    @Override
    public void showValidationAlert() {

    }

    @Override
    public void showErrorMessage(String msg) {

    }

    @Override
    protected void onDestroy() {
        dataFragment.setData(presenter);
        super.onDestroy();
    }
}
