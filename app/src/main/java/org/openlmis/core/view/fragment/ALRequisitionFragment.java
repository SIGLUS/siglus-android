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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.presenter.ALRequisitionPresenter;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.adapter.ALReportAdapter;
import org.openlmis.core.view.adapter.RapidTestReportRowAdapter;

import java.util.Date;
import roboguice.inject.InjectView;

import roboguice.RoboGuice;
import rx.functions.Action1;

public class ALRequisitionFragment extends BaseReportFragment {

    private long formId;
    protected View containerView;
    private Date periodEndDate;
    ALRequisitionPresenter presenter;
    ALReportAdapter adapter;

    @InjectView(R.id.rv_al_row_item_list)
    RecyclerView rvALRowItemListView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        formId = getActivity().getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0);
        periodEndDate = ((Date) getActivity().getIntent().getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
        setUpRowItems();
        rvALRowItemListView.setNestedScrollingEnabled(false);
    }

    @Override
    protected String getSignatureDialogTitle() {
        return null;
    }

    @Override
    protected Action1<? super Void> getOnSignedAction() {
        return null;
    }

    @Override
    protected String getNotifyDialogMsg() {
        return null;
    }

    @Override
    protected BaseReportPresenter injectPresenter() {
        return null;
    }

    @Override
    public Presenter initPresenter() {
        return null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        containerView = inflater.inflate(R.layout.fragment_al_requisition, container, false);
        return containerView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    protected void initUI() {

    }

    private void setUpRowItems() {
        adapter = new ALReportAdapter();
        rvALRowItemListView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        rvALRowItemListView.setAdapter(adapter);
    }


}