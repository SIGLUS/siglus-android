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
import android.widget.Button;
import android.widget.ListView;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.MMIAFormPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.view.adapter.MMIAInfoListAdapter;
import org.openlmis.core.view.adapter.RegimeListAdapter;
import org.openlmis.core.view.adapter.RnrFromListAdapter;
import org.openlmis.core.view.adapter.RnrFromNameListAdapter;

import java.util.ArrayList;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_mmia_spread)
public class MMIASpreadActivity extends BaseActivity {

    @InjectView(R.id.rnr_from_list)
    private ListView rnrFromList;

    @InjectView(R.id.rnr_from_list_product_name)
    private ListView rnrFromListView;

    @InjectView(R.id.regime_list)
    private ListView regimeListView;


    @InjectView(R.id.mmia_info_list)
    private ListView mmiaInfoListView;

    @InjectView(R.id.btn_complete)
    private Button btnComplete;


    @Inject
    MMIAFormPresenter presenter;

    @Override
    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI() {
        RnRForm rnRForm = presenter.initMIMIA();

        rnrFromListView.setAdapter(new RnrFromNameListAdapter(this, new ArrayList(rnRForm.getRnrFormItemList())));

        rnrFromList.setAdapter(new RnrFromListAdapter(this, new ArrayList(rnRForm.getRnrFormItemList())));

        regimeListView.setAdapter(new RegimeListAdapter(this, new ArrayList(rnRForm.getRegimenItemList())));

        mmiaInfoListView.setAdapter(new MMIAInfoListAdapter(this, new ArrayList(rnRForm.getBaseInfoItemList())));


    }

    public static Intent getIntent2Me(Context mContext) {
        Intent intent = new Intent(mContext, MMIASpreadActivity.class);
        return intent;
    }

}
