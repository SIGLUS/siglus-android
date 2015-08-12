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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.MMIAFormPresenter;
import org.openlmis.core.view.widget.MMIAInfoList;
import org.openlmis.core.view.widget.MMIARegimeList;
import org.openlmis.core.view.widget.MMIARnrForm;

import java.sql.SQLException;
import java.util.ArrayList;

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

    @Inject
    MMIAFormPresenter presenter;

    @Override
    public MMIAFormPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI() {
        //TODO  init once code
        RnRForm rnRForm = presenter.getRnrForm();

        if (rnRForm != null) {
            rnrFromListView.initView(new ArrayList<>(rnRForm.getRnrFormItemList()));

            regimeListView.initView(rnRForm.getRegimenItemListWrapper(), tvRegimeTotal);

            mmiaInfoListView.initView(rnRForm.getBaseInfoItemListWrapper());

            btnComplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (regimeListView.complete() && mmiaInfoListView.complete() && (regimeListView.getTotal() == mmiaInfoListView.getTotal())) {
                        try {
                            presenter.saveForm();
                            startActivity(HomeActivity.class, true);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            showErrorMessage(e.getMessage());
                        }
                    }
                }
            });
        }
    }

    public static Intent getIntent2Me(Context mContext) {
        return new Intent(mContext, MMIASpreadActivity.class);
    }

    @Override
    public void showValidationAlert() {

    }

    @Override
    public void showErrorMessage(String msg) {

    }
}
