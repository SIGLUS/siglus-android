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
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.RequisitionPresenter;
import org.openlmis.core.view.adapter.RequisitionFormAdapter;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;


import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


@ContentView(R.layout.activity_requisition)
public class RequisitionActivity extends BaseActivity implements RequisitionPresenter.RequisitionView {

    @Inject
    RequisitionPresenter presenter;

    @InjectView(R.id.requisition_form)
    ListView requisitionForm;

    @InjectView(R.id.product_name_list_view)
    ListView requisitionNameList;

    List<RequisitionFormItemViewModel> productList;
    LayoutInflater inflater;

    View bodyHeaderView;
    View productHeaderView;

    RequisitionFormAdapter productListAdapter;
    RequisitionFormAdapter requisitionFormAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = LayoutInflater.from(this);

        initUI();

        startLoading();
        Observable.create(new Observable.OnSubscribe<List<RequisitionFormItemViewModel>>() {
            @Override
            public void call(Subscriber<? super  List<RequisitionFormItemViewModel>> subscriber) {
                subscriber.onNext(presenter.getRequisitionViewModelList());
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Action1<List<RequisitionFormItemViewModel>>() {
            @Override
            public void call(List<RequisitionFormItemViewModel> requisitionFormItemViewModels) {
                productList.addAll(requisitionFormItemViewModels);
                productListAdapter.notifyDataSetChanged();
                requisitionFormAdapter.notifyDataSetChanged();
                stopLoading();
            }
        });
    }

    private void initUI() {
        productList = new ArrayList<>();

        initRequisitionBodyList();
        initRequisitionProductList();

        requisitionNameList.post(new Runnable() {
            @Override
            public void run() {
                productHeaderView.getLayoutParams().height = bodyHeaderView.getHeight();
            }
        });

        setListViewOnTouchAndScrollListener(requisitionForm, requisitionNameList);
    }


    private void initRequisitionBodyList() {
        bodyHeaderView = inflater.inflate(R.layout.item_requisition_header, requisitionForm, false);
        requisitionForm.addHeaderView(bodyHeaderView);

        requisitionFormAdapter = new RequisitionFormAdapter(this, productList, false);
        requisitionForm.setAdapter(requisitionFormAdapter);
    }

    private void initRequisitionProductList() {
        productHeaderView = inflater.inflate(R.layout.layout_requisition_header_left, requisitionNameList, false);
        requisitionNameList.addHeaderView(productHeaderView);

        productListAdapter = new RequisitionFormAdapter(this, productList, true);
        requisitionNameList.setAdapter(productListAdapter);
    }


    @Override
    public Presenter getPresenter() {
        return presenter;
    }


    private class MyScrollListener implements AbsListView.OnScrollListener {

        ListView list1;
        ListView list2;

        public MyScrollListener(ListView list1, ListView list2) {
            this.list1 = list1;
            this.list2 = list2;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == 0 || scrollState == 1) {
                View subView1 = view.getChildAt(0);

                if (subView1 != null) {
                    final int top1 = subView1.getTop();
                    View subview2 = list2.getChildAt(0);
                    if (subview2 != null) {
                        int top2 = subview2.getTop();
                        int position = view.getFirstVisiblePosition();

                        if (top1 != top2) {
                            list2.setSelectionFromTop(position, top1);
                        }
                    }
                }
            }
        }

        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            View subView1 = view.getChildAt(0);
            if (subView1 != null) {
                int top1 = subView1.getTop();

                View subView2 = list2.getChildAt(0);
                if (subView2 != null) {
                    int top2 = list2.getChildAt(0).getTop();
                    if (top1 != top2) {
                        list1.setSelectionFromTop(firstVisibleItem, top1);
                        list2.setSelectionFromTop(firstVisibleItem, top1);
                    }
                }
            }
        }
    }


    private void setListViewOnTouchAndScrollListener(ListView listView1, ListView listView2) {
        listView2.setOnScrollListener(new MyScrollListener(listView2, listView1));
        listView1.setOnScrollListener(new MyScrollListener(listView1, listView2));
    }
}
