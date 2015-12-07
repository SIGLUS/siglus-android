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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.presenter.StockMovementPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.StockMovementAdapter;
import org.openlmis.core.view.holder.StockMovementViewHolder;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.openlmis.core.view.widget.ExpireDateViewGroup;
import org.openlmis.core.view.widget.SignatureDialog;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_stock_movement)
public class StockMovementActivity extends BaseActivity implements StockMovementPresenter.StockMovementView {

    @InjectView(R.id.list_stock_movement)
    ListView stockMovementList;

    @InjectView(R.id.btn_complete)
    View btnComplete;

    @InjectView(R.id.btn_cancel)
    TextView tvCancel;

    @InjectView(R.id.vg_expire_date_container)
    ExpireDateViewGroup expireDateViewGroup;

    @InjectPresenter(StockMovementPresenter.class)
    StockMovementPresenter presenter;

    @InjectView(R.id.action_panel)
    View buttonView;

    @Inject
    LayoutInflater layoutInflater;

    private long stockId;
    private String stockName;

    private StockMovementAdapter stockMovementAdapter;
    private boolean isStockCardArchivable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stockId = getIntent().getLongExtra(Constants.PARAM_STOCK_CARD_ID, 0);
        stockName = getIntent().getStringExtra(Constants.PARAM_STOCK_NAME);
        try {
            presenter.setStockCard(stockId);
        } catch (LMISException e) {
            ToastUtil.show(R.string.msg_db_error);
            finish();
        }

        initUI();
    }

    private void initUI() {
        setTitle(stockName);

        expireDateViewGroup.initExpireDateViewGroup(new StockCardViewModel(presenter.getStockCard()), true);

        buttonView.setVisibility(View.GONE);

        stockMovementAdapter = new StockMovementAdapter(presenter.getStockMovementModelList(), presenter.getStockCard());
        stockMovementAdapter.setMovementChangeListener(new StockMovementAdapter.MovementChangedListener() {

            @Override
            public void movementChange() {
                showBottomBtn();
            }
        });


        View headerView = layoutInflater.inflate(R.layout.item_stock_movement_header, stockMovementList, false);
        stockMovementList.addHeaderView(headerView);
        stockMovementList.setAdapter(stockMovementAdapter);
        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.display_stock_movement_signature)) {
            headerView.findViewById(R.id.tx_signature).setVisibility(View.GONE);
        }

        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.submitStockMovement(stockMovementAdapter.getEditableStockMovement());
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StockMovementViewHolder viewHolder = (StockMovementViewHolder) stockMovementList.getChildAt(stockMovementList.getChildCount() - 1).getTag();

                stockMovementAdapter.cancelStockMovement(viewHolder);

                deactivatedStockDraft();
            }
        });

        loading();
        presenter.loadStockMovementViewModels();
    }

    @Override
    public void showSignDialog() {
        SignatureDialog signatureDialog = new SignatureDialog();
        signatureDialog.setArguments(SignatureDialog.getBundleToMe(getString(R.string.label_stock_movement_signature_title)));
        signatureDialog.setDelegate(signatureDialogDelegate);
        signatureDialog.show(getFragmentManager());
    }

    @Override
    public void updateArchiveMenus(boolean isArchivable) {
        isStockCardArchivable = isArchivable;
        invalidateOptionsMenu();
    }

    protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
        @Override
        public void onCancel() {
        }

        @Override
        public void onSign(String sign) {
            StockMovementViewModel stockMovementViewModel = stockMovementAdapter.getEditableStockMovement();
            stockMovementViewModel.setSignature(sign);
            presenter.saveAndRefresh(stockMovementViewModel);
        }
    };

    public void deactivatedStockDraft() {
        StockMovementViewHolder viewHolder = (StockMovementViewHolder) stockMovementList.getChildAt(stockMovementList.getChildCount() - 1).getTag();
        buttonView.setVisibility(View.GONE);
        stockMovementAdapter.cleanHighLight(viewHolder);
    }

    @Override
    public void showErrorAlert(String msg) {
        ToastUtil.show(msg);
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void refreshStockMovement() {
        stockMovementAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isPrepared = super.onPrepareOptionsMenu(menu);
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_archive_drugs_346)) {
            menu.findItem(R.id.action_archive).setVisible(isStockCardArchivable);
        }
        return isPrepared;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stock_movement, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_history:
                startActivity(StockMovementHistoryActivity.getIntentToMe(this, stockId, stockName, false));
                return true;
            case R.id.action_archive:
                presenter.archiveStockCard();
                ToastUtil.show(getString(R.string.msg_drug_archived));
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showBottomBtn() {
        if (buttonView.getVisibility() != View.VISIBLE) {
            buttonView.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_from_bottom_in);
            buttonView.startAnimation(animation);
            stockMovementList.post(new Runnable() {
                @Override
                public void run() {
                    stockMovementList.setSelection(stockMovementList.getCount() - 1);
                }
            });
        }
    }
}
