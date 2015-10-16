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

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;


import org.openlmis.core.R;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.presenter.RnRFormListPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.RnRFormListAdapter;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;

@ContentView(R.layout.activity_rnr_list)
public class RnRFormListActivity extends BaseActivity implements RnRFormListPresenter.RnRFormListView {

    public static final String PARAM_PROGRAM_CODE = "programCode";

    @InjectView(R.id.rnr_form_list)
    RecyclerView listView;

    private String programCode;

    private RnRFormListAdapter adapter;

    @InjectPresenter(RnRFormListPresenter.class)
    RnRFormListPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        programCode = getIntent().getStringExtra(PARAM_PROGRAM_CODE);
        presenter.setProgramCode(programCode);
        initUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    private void initUI() {
        setTitle(MMIARepository.MMIA_PROGRAM_CODE.equals(programCode) ? R.string.title_mmia_list : R.string.title_requisition_list);

        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setHasFixedSize(true);

        adapter = new RnRFormListAdapter(this, programCode, new ArrayList<RnRFormViewModel>());
        listView.setAdapter(adapter);

        loading();
        presenter.loadRnRFormList().subscribe(subscriber);
    }

    protected Subscriber<List<RnRFormViewModel>> subscriber = new Subscriber<List<RnRFormViewModel>>() {
        @Override
        public void onCompleted() {
            loaded();
        }

        @Override
        public void onError(Throwable e) {
            loaded();
            showMessage(e.getMessage());
        }

        @Override
        public void onNext(List<RnRFormViewModel> rnRFormViewModels) {
            adapter.refreshList(rnRFormViewModels);
        }
    };
}
