package org.openlmis.core.view.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.InitialInventoryPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.BulkInitialInventoryAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.List;

import roboguice.inject.ContentView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_bulk_initial_inventory)
public class BulkInitialInventoryActivity extends InventoryActivity {

    public static final int FIRST_ELEMENT_POSITION_OF_THE_LIST = 0;
    public static final int ELEMENTS_ADDED_TO_HEADER = 1;

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

    @NonNull
    @Override
    protected Subscriber<List<InventoryViewModel>> getOnViewModelsLoadedSubscriber() {
        return new Subscriber<List<InventoryViewModel>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.show(e.getMessage());
                loaded();
            }

            @Override
            public void onNext(List<InventoryViewModel> inventoryViewModels) {
                inventoryViewModels.add(FIRST_ELEMENT_POSITION_OF_THE_LIST, new InventoryViewModel(new Product()));
                setUpFastScroller(inventoryViewModels);
                mAdapter.refresh();
                setTotal(inventoryViewModels.size() - ELEMENTS_ADDED_TO_HEADER);
                loaded();
            }
        };
    }

    @Override
    protected void goToNextPage() {
        preferencesMgr.setIsNeedsInventory(false);
        startActivity(HomeActivity.getIntentToMe(this));
        this.finish();
    }
}
