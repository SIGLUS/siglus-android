package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.presenter.InitialInventoryPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.InitialInventoryAdapter;
import org.openlmis.core.view.holder.InitialInventoryViewHolder;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscription;

@ContentView(R.layout.activity_initial_inventory)
public class InitialInventoryActivity extends InventoryActivity {

    @InjectView(R.id.layout_action_buttons)
    private LinearLayout llSave;

    @InjectPresenter(InitialInventoryPresenter.class)
    InitialInventoryPresenter presenter;

    protected boolean isAddNewDrug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isAddNewDrug = getIntent().getBooleanExtra(Constants.PARAM_IS_ADD_NEW_DRUG, false);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initUI() {
        super.initUI();
        initButtonPanel();
        initTitle();

        initRecyclerView();
        Subscription subscription = presenter.loadInventory().subscribe(getOnViewModelsLoadedSubscriber());
        subscriptions.add(subscription);
    }

    private void initTitle() {
        if (isAddNewDrug) {
            setTitle(getResources().getString(R.string.title_add_new_drug));
        } else if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void initButtonPanel() {
        llSave.setVisibility(View.GONE);
        btnDone.setOnClickListener(new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                btnDone.setEnabled(false);
                if (validateInventory()) {
                    loading();
                    Subscription subscription = presenter.initStockCardObservable().subscribe(onNextMainPageAction);
                    subscriptions.add(subscription);
                } else {
                    btnDone.setEnabled(true);
                }
            }
        });
    }

    @Override
    protected void initRecyclerView() {
        mAdapter = new InitialInventoryAdapter(presenter.getInventoryViewModelList(), viewHistoryListener);
        productListRecycleView.setAdapter(mAdapter);
    }

    @Override
    public void goToNextPage() {
        preferencesMgr.setIsNeedsInventory(false);
        startActivity(isAddNewDrug ? StockCardListActivity.getIntentToMe(this) : HomeActivity.getIntentToMe(this));
        this.finish();
    }

    @Override
    public void onBackPressed() {
        if (isSearchViewActivity()) {
            searchView.onActionViewCollapsed();
            return;
        }
        if (!isAddNewDrug) {
            ToastUtil.show(R.string.msg_save_before_exit);
            return;
        }
        super.onBackPressed();
    }

    public static Intent getIntentToMe(Context context, boolean isAddNewDrug) {
        return new Intent(context, InitialInventoryActivity.class)
                .putExtra(Constants.PARAM_IS_ADD_NEW_DRUG, isAddNewDrug);
    }

    public static Intent getIntentToMe(Context context) {
        return getIntentToMe(context, false);
    }

    protected InitialInventoryViewHolder.ViewHistoryListener viewHistoryListener = new InitialInventoryViewHolder.ViewHistoryListener() {
        @Override
        public void viewHistory(StockCard stockCard) {
            startActivity(StockMovementHistoryActivity.getIntentToMe(InitialInventoryActivity.this,
                    stockCard.getId(),
                    stockCard.getProduct().getFormattedProductName(),
                    true,
                    false));
        }
    };

}
