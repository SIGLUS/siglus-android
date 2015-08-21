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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.RequisitionPresenter;
import org.openlmis.core.view.adapter.RequisitionFormAdapter;


import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_requisition)
public class RequisitionActivity extends BaseActivity implements RequisitionPresenter.RequisitionView{

    @Inject
    RequisitionPresenter presenter;

    @InjectView(R.id.requisition_form)
    ListView requisitionForm;

    @InjectView(R.id.product_name_list_view)
    ListView requisitionNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(this);


        RnRForm rnRForm = presenter.loadRnrForm();
        final View headerView = inflater.inflate(R.layout.item_requisition_header, requisitionForm, false);
        requisitionForm.addHeaderView(headerView);
        requisitionForm.setAdapter(new RequisitionFormAdapter(this, rnRForm, false));

        final View nameListHeader = inflater.inflate(R.layout.layout_requisition_header_left, requisitionNameList, false);
        requisitionNameList.addHeaderView(nameListHeader);
        requisitionNameList.setAdapter(new RequisitionFormAdapter(this, rnRForm, true));

        requisitionNameList.post(new Runnable() {
            @Override
            public void run() {
                nameListHeader.getLayoutParams().height = headerView.getHeight();
            }
        });
        setListViewOnTouchAndScrollListener(requisitionForm, requisitionNameList);
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }


    public void setListViewOnTouchAndScrollListener(final ListView listView1, final ListView listView2) {
        listView2.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0 || scrollState == 1) {
                    View subView = view.getChildAt(0);

                    if (subView != null) {
                        final int top = subView.getTop();
                        final int top1 = listView1.getChildAt(0).getTop();
                        final int position = view.getFirstVisiblePosition();

                        if (top != top1) {
                            listView1.setSelectionFromTop(position, top);
                        }
                    }
                }

            }

            public void onScroll(AbsListView view, final int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                View subView = view.getChildAt(0);
                if (subView != null) {
                    final int top = subView.getTop();

                    int top1 = listView1.getChildAt(0).getTop();
                    if (!(top1 - 7 < top && top < top1 + 7)) {
                        listView1.setSelectionFromTop(firstVisibleItem, top);
                        listView2.setSelectionFromTop(firstVisibleItem, top);
                    }

                }
            }
        });

        listView1.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0 || scrollState == 1) {
                    View subView = view.getChildAt(0);

                    if (subView != null) {
                        final int top = subView.getTop();
                        final int top1 = listView2.getChildAt(0).getTop();
                        final int position = view.getFirstVisiblePosition();

                        if (top != top1) {
                            listView1.setSelectionFromTop(position, top);
                            listView2.setSelectionFromTop(position, top);
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, final int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                View subView = view.getChildAt(0);
                if (subView != null) {
                    final int top = subView.getTop();
                    listView1.setSelectionFromTop(firstVisibleItem, top);
                    listView2.setSelectionFromTop(firstVisibleItem, top);

                }
            }
        });
    }
}
