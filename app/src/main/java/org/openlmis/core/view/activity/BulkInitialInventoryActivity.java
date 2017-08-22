package org.openlmis.core.view.activity;

import android.os.Bundle;

import org.openlmis.core.R;
import org.openlmis.core.presenter.InitialInventoryPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.BulkInitialInventoryAdapter;

import roboguice.inject.ContentView;
import rx.Subscription;

@ContentView(R.layout.activity_bulk_initial_inventory)
public class BulkInitialInventoryActivity extends InventoryActivity {

    @InjectPresenter(InitialInventoryPresenter.class)
    InitialInventoryPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initRecyclerView() {
        mAdapter = new BulkInitialInventoryAdapter(presenter.getInventoryViewModelList());
        productListRecycleView.setAdapter(mAdapter);
    }

    @Override
    public void initUI() {
        super.initUI();
        initRecyclerView();
        Subscription subscription = presenter.loadInventoryWithBasicProducts().subscribe(getOnViewModelsLoadedSubscriber());
        subscriptions.add(subscription);
    }

    @Override
    protected void goToNextPage() {
        preferencesMgr.setIsNeedsInventory(false);
        startActivity(HomeActivity.getIntentToMe(this));
        this.finish();
    }
}
