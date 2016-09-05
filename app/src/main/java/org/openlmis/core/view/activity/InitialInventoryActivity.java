package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.InitialInventoryAdapter;
import org.openlmis.core.view.holder.InitialInventoryViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_inventory)
public class InitialInventoryActivity extends InventoryActivity {
    protected boolean isAddNewDrug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isAddNewDrug = getIntent().getBooleanExtra(Constants.PARAM_IS_ADD_NEW_DRUG, false);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initUI() {
        btnSave.setVisibility(View.GONE);
        if (isAddNewDrug) {
            setTitle(getResources().getString(R.string.title_add_new_drug));
        } else if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        mAdapter = new InitialInventoryAdapter(new ArrayList<InventoryViewModel>(), viewHistoryListener);
        productListRecycleView.setAdapter(mAdapter);

        loading();

        Subscription subscription = presenter.loadInventory().subscribe(loadMasterSubscriber);
        subscriptions.add(subscription);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.doInitialInventory(mAdapter.getData());
            }
        });
    }

    protected Subscriber<List<InventoryViewModel>> loadMasterSubscriber = new Subscriber<List<InventoryViewModel>>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            loaded();
            ToastUtil.show(e.getMessage());
        }

        @Override
        public void onNext(List<InventoryViewModel> inventoryViewModels) {
            mAdapter.refreshList(inventoryViewModels);
            setTotal(inventoryViewModels.size());
            loaded();
        }
    };

    @Override
    public void goToParentPage() {
        goToMainPage();
    }

    @Override
    public void onBackPressed() {
        if (isSearchViewActivity()) {
            searchView.onActionViewCollapsed();
        } else {
            ToastUtil.show(R.string.msg_save_before_exit);
        }
    }


    @Override
    public void showSignDialog() {

    }

    private void goToMainPage() {
        preferencesMgr.setIsNeedsInventory(false);
        startActivity(isAddNewDrug ? StockCardListActivity.getIntentToMe(this) : HomeActivity.getIntentToMe(this));
        this.finish();
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
