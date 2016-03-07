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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.presenter.RnRFormListPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.RnRFormListAdapter;
import org.openlmis.core.view.fragment.WarningDialog;
import org.openlmis.core.view.holder.RnRFormViewHolder.RnRFormItemClickListener;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_rnr_list)
public class RnRFormListActivity extends BaseActivity implements RnRFormListPresenter.RnRFormListView {

    public static final int DEFAULT_FORM_ID_OF_NOT_AUTHORIZED = 0;
    long rnrFormId = DEFAULT_FORM_ID_OF_NOT_AUTHORIZED;

    @InjectView(R.id.rnr_form_list)
    RecyclerView listView;

    @InjectPresenter(RnRFormListPresenter.class)
    RnRFormListPresenter presenter;
    private ArrayList<RnRFormViewModel> data;

    private String programCode;
    private RnRFormListAdapter adapter;

    @Override
    protected void sendScreenToGoogleAnalytics() {
        LMISApp.getInstance().sendScreenToGoogleAnalytics(ScreenName.RnRFormHistoryScreen.getScreenName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        programCode = getIntent().getStringExtra(Constants.PARAM_PROGRAM_CODE);
        if (MMIARepository.MMIA_PROGRAM_CODE.equals(programCode)) {
            setTitle(R.string.mmia_list);
        } else {
            setTitle(R.string.requisition_list);
        }

        super.onCreate(savedInstanceState);
        presenter.setProgramCode(programCode);
        initUI();
    }

    @Override
    protected int getThemeRes() {
        if (MMIARepository.MMIA_PROGRAM_CODE.equals(programCode)) {
            return R.style.AppTheme_AMBER;
        } else {
            return R.style.AppTheme_PURPLE;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case Constants.REQUEST_FROM_RNR_LIST_PAGE:
                initUI();
                break;
            case Constants.REQUEST_SELECT_PERIOD_END:
                Date periodEndDate = (Date) data.getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE);
                boolean isMissedPeriod = data.getBooleanExtra(Constants.PARAM_IS_MISSED_PERIOD, false);
                createRequisition(periodEndDate, isMissedPeriod);
                break;
        }
    }

    private void initUI() {
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setHasFixedSize(true);

        data = new ArrayList<>();
        adapter = new RnRFormListAdapter(this, data, rnRFormItemClickListener);
        listView.setAdapter(adapter);

        loading();
        presenter.loadRnRFormList().subscribe(getRnRFormSubscriber());
    }

    protected RnRFormItemClickListener rnRFormItemClickListener = new RnRFormItemClickListener() {
        @Override
        public void deleteForm(final RnRForm form) {
            WarningDialog warningDialog = new WarningDialog();
            warningDialog.setDelegate(new WarningDialog.DialogDelegate() {
                @Override
                public void onDelete() {
                    deleteRnRForm(form);
                }
            });
            warningDialog.show(getFragmentManager(), "WarningDialog");
        }

        @Override
        public void clickBtnView(RnRFormViewModel model) {
            switch (model.getType()) {
                case RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD:
                    Intent intent = new Intent(RnRFormListActivity.this, InventoryActivity.class);
                    intent.putExtra(Constants.PARAM_IS_PHYSICAL_INVENTORY, true);
                    startActivityForResult(intent, Constants.REQUEST_FROM_RNR_LIST_PAGE);
                    break;
                case RnRFormViewModel.TYPE_INVENTORY_DONE:
                    startActivityForResult(SelectPeriodActivity.getIntentToMe(RnRFormListActivity.this, model.getProgramCode()), Constants.REQUEST_SELECT_PERIOD_END);
                    break;
                case RnRFormViewModel.TYPE_SYNCED_HISTORICAL:
                    rnrFormId = model.getId();
                    goToRequisitionPage(rnrFormId);
                    break;
                case RnRFormViewModel.TYPE_FIRST_MISSED_PERIOD:
                    startActivityForResult(SelectPeriodActivity.getIntentToMe(RnRFormListActivity.this, model.getProgramCode(), true), Constants.REQUEST_SELECT_PERIOD_END);
                    break;
                default:
                    rnrFormId = DEFAULT_FORM_ID_OF_NOT_AUTHORIZED;
                    goToRequisitionPage(rnrFormId);
                    break;
            }
        }
    };

    private void goToRequisitionPage(long rnrFormId) {
        if (MMIARepository.MMIA_PROGRAM_CODE.equals(programCode)) {
            startActivityForResult(MMIARequisitionActivity.getIntentToMe(this, rnrFormId), Constants.REQUEST_FROM_RNR_LIST_PAGE);
        } else if (VIARepository.VIA_PROGRAM_CODE.equals(programCode)) {
            startActivityForResult(VIARequisitionActivity.getIntentToMe(this, rnrFormId), Constants.REQUEST_FROM_RNR_LIST_PAGE);
        }
    }

    private void createRequisition(Date periodEndDate, boolean isMissedPeriod) {
        if (MMIARepository.MMIA_PROGRAM_CODE.equals(programCode)) {
            startActivityForResult(MMIARequisitionActivity.getIntentToMe(this, periodEndDate), Constants.REQUEST_FROM_RNR_LIST_PAGE);
        } else if (VIARepository.VIA_PROGRAM_CODE.equals(programCode)) {
            startActivityForResult(VIARequisitionActivity.getIntentToMe(this, periodEndDate, isMissedPeriod), Constants.REQUEST_FROM_RNR_LIST_PAGE);
        }
    }

    private void deleteRnRForm(RnRForm form) {
        try {
            presenter.deleteRnRForm(form);
            Subscription subscription = presenter.loadRnRFormList().subscribe(getRnRFormSubscriber());
            subscriptions.add(subscription);
        } catch (LMISException e) {
            ToastUtil.show(getString(R.string.requisition_delete_failed));
            e.printStackTrace();
        }
    }

    protected Subscriber<List<RnRFormViewModel>> getRnRFormSubscriber() {
        return new Subscriber<List<RnRFormViewModel>>() {
            @Override
            public void onCompleted() {
                loaded();
            }

            @Override
            public void onError(Throwable e) {
                loaded();
                ToastUtil.show(e.getMessage());
            }

            @Override
            public void onNext(List<RnRFormViewModel> rnRFormViewModels) {
                data.clear();
                data.addAll(rnRFormViewModels);
                adapter.notifyDataSetChanged();
            }
        };
    }

}
