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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.openlmis.core.R;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.RequisitionPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.RequisitionFormAdapter;
import org.openlmis.core.view.fragment.OnBackConfirmDialog;
import org.openlmis.core.view.fragment.RetainedFragment;

import roboguice.RoboGuice;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_requisition)
public class RequisitionActivity extends BaseActivity implements RequisitionPresenter.RequisitionView, View.OnClickListener {

    @InjectView(R.id.requisition_form)
    ListView requisitionForm;

    @InjectView(R.id.product_name_list_view)
    ListView requisitionNameList;

    @InjectView(R.id.btn_complete)
    private Button btnComplete;

    @InjectView(R.id.btn_save)
    private View btnSave;

    @InjectView(R.id.edit_text)
    private EditText etConsultationNumbers;

    LayoutInflater inflater;

    RequisitionPresenter presenter;

    Boolean hasDataChanged;

    private RetainedFragment dataFragment;

    View bodyHeaderView;
    View productHeaderView;

    RequisitionFormAdapter productListAdapter;
    RequisitionFormAdapter requisitionFormAdapter;
    private boolean consultationNumbersHasChanged;


    private void initPresenter() {
        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        dataFragment = (RetainedFragment) fm.findFragmentByTag("RetainedFragment");

        if (dataFragment == null) {
            dataFragment = new RetainedFragment();
            fm.beginTransaction().add(dataFragment, "RetainedFragment").commit();
            presenter = RoboGuice.getInjector(getApplicationContext()).getInstance(RequisitionPresenter.class);
            dataFragment.putData("presenter", presenter);
        } else {
            presenter = (RequisitionPresenter) dataFragment.getData("presenter");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = LayoutInflater.from(this);
        hasDataChanged = (Boolean) dataFragment.getData("hasDataChanged");
        initUI();
        presenter.loadRequisitionFormList();
    }


    @Override
    public void refreshRequisitionForm() {
        productListAdapter.notifyDataSetChanged();
        requisitionFormAdapter.notifyDataSetChanged();
        setConsultationNumbers();
    }

    private void setConsultationNumbers() {
        etConsultationNumbers.setText(presenter.getConsultationNumbers());
        etConsultationNumbers.post(new Runnable() {
            @Override
            public void run() {
                etConsultationNumbers.addTextChangedListener(etConsultationNumbersTextWatcher);
            }
        });
    }

    @Override
    public void showErrorMessage(String msg) {
        ToastUtil.show(msg);
    }

    @Override
    public Presenter getPresenter() {
        initPresenter();
        return presenter;
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

    @Override
    public void showListInputError(int index) {
        // +1  Header View
        final int position = index + 1;
        requisitionForm.setSelection(position);
        requisitionForm.post(new Runnable() {
            @Override
            public void run() {
                View childAt = getViewByPosition(position, requisitionForm);
                final EditText viewById = (EditText) childAt.findViewById(R.id.et_request_amount);
                viewById.requestFocus();
                viewById.setError(getString(R.string.hint_error_input));
            }
        });
    }

    @Override
    protected void onDestroy() {
        dataFragment.putData("presenter", presenter);
        if (hasDataChanged()) {
            dataFragment.putData("hasDataChanged", hasDataChanged());
        }
        super.onDestroy();
    }

    private void initUI() {
        initRequisitionBodyList();
        initRequisitionProductList();

        requisitionNameList.post(new Runnable() {
            @Override
            public void run() {
                productHeaderView.getLayoutParams().height = bodyHeaderView.getHeight();
            }
        });

        setListViewOnTouchAndScrollListener(requisitionForm, requisitionNameList);
        btnComplete.setOnClickListener(this);
        btnSave.setOnClickListener(this);

    }

    private void initRequisitionBodyList() {
        bodyHeaderView = inflater.inflate(R.layout.item_requisition_header, requisitionForm, false);
        requisitionForm.addHeaderView(bodyHeaderView);

        requisitionFormAdapter = new RequisitionFormAdapter(this, presenter.getRequisitionViewModelList(), false);
        requisitionForm.setAdapter(requisitionFormAdapter);
    }

    private void initRequisitionProductList() {
        productHeaderView = inflater.inflate(R.layout.layout_requisition_header_left, requisitionNameList, false);
        requisitionNameList.addHeaderView(productHeaderView);

        productListAdapter = new RequisitionFormAdapter(this, presenter.getRequisitionViewModelList(), true);
        requisitionNameList.setAdapter(productListAdapter);
    }

    private void onCompleteBtnClick() {

        String consultationNumbers = etConsultationNumbers.getText().toString();
        if (TextUtils.isEmpty(consultationNumbers)) {
            etConsultationNumbers.setError(getString(R.string.hint_error_input));
            return;
        }
        if (presenter.isRequisitionFormAmountCompleted()) {
            presenter.saveRequisition(consultationNumbers);
            ToastUtil.show(R.string.msg_requisition_submit_tip);
            goToHomePage();
        }
    }


    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private void onSaveBtnClick() {
        if (hasDataChanged()) {
            String consultationNumbers = etConsultationNumbers.getText().toString();
            presenter.saveDraftRequisition(consultationNumbers);
        }
        goToHomePage();
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

    private void goToHomePage() {
        Intent intent = new Intent(RequisitionActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(HomeActivity.class, true);
    }

    private boolean hasDataChanged() {
        if (hasDataChanged == null) {
            hasDataChanged = requisitionFormAdapter.hasDataChanged() || consultationNumbersHasChanged;
        }
        return hasDataChanged;
    }

    TextWatcher etConsultationNumbersTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            presenter.setConsultationNumbers(etConsultationNumbers.getText().toString());
            consultationNumbersHasChanged = true;
        }
    };

    @Override
    public void onBackPressed() {
        if (hasDataChanged()) {
            OnBackConfirmDialog.showDialog(this, new OnBackConfirmDialog.ResultCallBack() {
                @Override
                public void callback(boolean flag) {
                    if (flag) {
                        presenter.removeRnrForm();
                        finish();
                    }
                }
            });
        } else {
            presenter.removeRnrForm();
            super.onBackPressed();
        }
    }
}
