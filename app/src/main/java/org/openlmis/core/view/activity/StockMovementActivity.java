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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.googleAnalytics.TrackerCategories;
import org.openlmis.core.presenter.StockMovementPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.StockMovementAdapter;
import org.openlmis.core.view.holder.StockMovementViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.openlmis.core.view.widget.ExpireDateViewGroup;
import org.openlmis.core.view.widget.SignatureDialog;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_stock_movement)
public class StockMovementActivity extends BaseActivity implements StockMovementPresenter.StockMovementView, View.OnClickListener {

    @InjectView(R.id.list_stock_movement)
    ListView stockMovementList;

    @InjectView(R.id.stock_movement_banner)
    View banner;

    @InjectView(R.id.btn_complete)
    View btnComplete;

    @InjectView(R.id.btn_cancel)
    TextView tvCancel;

    @InjectView(R.id.tv_cmm)
    TextView tvCmm;

    @InjectView(R.id.tv_cmm_label)
    TextView tvCmmLabel;

    @InjectView(R.id.vg_expire_date_container)
    ExpireDateViewGroup expireDateViewGroup;

    @InjectView(R.id.action_panel)
    View buttonView;

    @InjectView(R.id.stock_unpack_container)
    View unpackContainer;

    @InjectView(R.id.btn_unpack)
    Button btnUnpack;

    @InjectPresenter(StockMovementPresenter.class)
    StockMovementPresenter presenter;

    @Inject
    LayoutInflater layoutInflater;

    private final String TRACKER_LABEL = "Movement Completed";
    private long stockId;
    private String stockName;

    private StockMovementAdapter stockMovementAdapter;
    private boolean isStockCardArchivable;
    private boolean isStockCardUnpackable;
    private boolean isActivated;
    private boolean isKit;

    @Override
    protected void sendScreenToGoogleAnalytics() {
        LMISApp.getInstance().trackerScreen(ScreenName.StockCardMovementScreen.getScreenName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        stockId = getIntent().getLongExtra(Constants.PARAM_STOCK_CARD_ID, 0);
        stockName = getIntent().getStringExtra(Constants.PARAM_STOCK_NAME);
        isActivated = getIntent().getBooleanExtra(Constants.PARAM_IS_ACTIVATED, true);
        isKit = getIntent().getBooleanExtra(Constants.PARAM_IS_KIT, false);

        super.onCreate(savedInstanceState);

        loadStockCard();

        initUI();

        if (savedInstanceState == null) {
            presenter.loadStockMovementViewModels();
        }
    }

    private void loadStockCard() {
        try {
            presenter.setStockCard(stockId);
        } catch (LMISException e) {
            ToastUtil.show(R.string.msg_db_error);
            finish();
        }
    }

    @Override
    protected int getThemeRes() {
        return isKit ? R.style.AppTheme_TEAL : super.getThemeRes();
    }

    private void initUI() {
        setTitle(stockName);

        showBanner();

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

        btnComplete.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        btnUnpack.setOnClickListener(this);
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_show_cmm_on_stock_movement_page_227)) {
            tvCmm.setText(presenter.getStockCardCmm());
        } else {
            tvCmm.setVisibility(View.GONE);
            tvCmmLabel.setVisibility(View.GONE);
        }

        updateExpiryDateViewGroup();
    }

    private void showBanner() {
        if (isActivated) {
            banner.setVisibility(View.GONE);
        } else {
            banner.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showSignDialog() {
        SignatureDialog signatureDialog = new SignatureDialog();
        signatureDialog.setArguments(SignatureDialog.getBundleToMe(getString(R.string.dialog_request_signature)));
        signatureDialog.setDelegate(signatureDialogDelegate);
        signatureDialog.show(getFragmentManager());
    }

    @Override
    public void updateArchiveMenus(boolean isArchivable) {
        isStockCardArchivable = isArchivable;
        invalidateOptionsMenu();
    }

    @Override
    public void updateUnpackKitMenu(boolean unpackable) {
        isStockCardUnpackable = unpackable;

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_unpack_btn_update_580)) {
            unpackContainer.setVisibility(unpackable ? View.VISIBLE : View.GONE);
        } else {
            invalidateOptionsMenu();
        }
    }

    @Override
    public void updateExpiryDateViewGroup() {
        StockCard stockCard = presenter.getStockCard();
        expireDateViewGroup.initExpireDateViewGroup(new InventoryViewModel(stockCard), true);
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_remove_expiry_date_when_soh_is_0_393)) {
            expireDateViewGroup.setVisibility(stockCard.getStockOnHand() == 0 ? View.INVISIBLE : View.VISIBLE);
        }
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
            LMISApp.getInstance().trackerEvent(TrackerCategories.StockMovement.getString(), TrackerActions.SelectApprove.getString(), TRACKER_LABEL);
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
        menu.findItem(R.id.action_archive).setVisible(isStockCardArchivable);

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_unpack_btn_update_580)) {
            // The unpack action should be directly removed from R.menu.menu_stock_movement when toggle enabled
            menu.findItem(R.id.action_unpack).setVisible(false);
        } else {
            menu.findItem(R.id.action_unpack).setVisible(isStockCardUnpackable);
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
            case R.id.action_unpack:
                unpackKit();
                return true;
            case R.id.action_history:
                startActivity(StockMovementHistoryActivity.getIntentToMe(this, stockId, stockName, false, isKit));
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

    private void unpackKit() {
        Product product = presenter.getStockCard().getProduct();
        startActivityForResult(SelectUnpackKitNumActivity.getIntentToMe(this, product.getPrimaryName(), product.getCode(), presenter.getStockCard().getStockOnHand()), Constants.REQUEST_UNPACK_KIT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_UNPACK_KIT) {
            loadStockCard();
            presenter.loadStockMovementViewModels();
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

    public static Intent getIntentToMe(Context context, InventoryViewModel inventoryViewModel, boolean isKit) {
        Intent intent = new Intent(context, StockMovementActivity.class);
        intent.putExtra(Constants.PARAM_STOCK_CARD_ID, inventoryViewModel.getStockCardId());
        intent.putExtra(Constants.PARAM_STOCK_NAME, inventoryViewModel.getProduct().getFormattedProductName());
        intent.putExtra(Constants.PARAM_IS_ACTIVATED, inventoryViewModel.getProduct().isActive());
        intent.putExtra(Constants.PARAM_IS_KIT, isKit);
        return intent;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_complete:
                presenter.submitStockMovement(stockMovementAdapter.getEditableStockMovement());
                LMISApp.getInstance().trackerEvent(TrackerCategories.StockMovement.getString(), TrackerActions.SelectComplete.getString(), "");
                break;
            case R.id.btn_cancel:
                StockMovementViewHolder viewHolder = (StockMovementViewHolder) stockMovementList.getChildAt(stockMovementList.getChildCount() - 1).getTag();
                stockMovementAdapter.cancelStockMovement(viewHolder);
                deactivatedStockDraft();
                break;
            case R.id.btn_unpack:
                unpackKit();
                break;
        }
    }
}
