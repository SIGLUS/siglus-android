package org.openlmis.core.view.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.viethoa.RecyclerViewFastScroller;
import com.viethoa.models.AlphabetItem;

import org.openlmis.core.R;
import org.openlmis.core.presenter.InitialInventoryPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.BulkInitialInventoryAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_bulk_initial_inventory)
public class BulkInitialInventoryActivity extends InventoryActivity {
    @InjectView(R.id.fast_scroller)
    RecyclerViewFastScroller fastScroller;

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

    public void setUpFastScroller(List<InventoryViewModel> viewModels) {
        List<AlphabetItem> mAlphabetItems = new ArrayList<>();
        List<String> strAlphabets = new ArrayList<>();
        for (int i = 0; i < viewModels.size(); i++) {
            String name = viewModels.get(i).getProductName();
            if (name == null || name.trim().isEmpty())
                continue;

            String word = name.substring(0, 1);
            if (!strAlphabets.contains(word)) {
                strAlphabets.add(word);
                mAlphabetItems.add(new AlphabetItem(i, word, false));
            }
        }

        fastScroller.setRecyclerView(productListRecycleView);
        fastScroller.setUpAlphabet(mAlphabetItems);
    }

    @Override
    public boolean onSearchStart(String query) {
        mAdapter.filter(query);
        setUpFastScroller(mAdapter.getFilteredList());
        return false;
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
                setUpFastScroller(inventoryViewModels);
                mAdapter.refresh();
                setTotal(inventoryViewModels.size());
                loaded();
            }
        };
    }

}
