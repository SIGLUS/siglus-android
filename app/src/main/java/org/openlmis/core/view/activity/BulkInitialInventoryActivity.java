package org.openlmis.core.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.InitialInventoryPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.BulkInitialInventoryAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

@ContentView(R.layout.activity_bulk_initial_inventory)
public class BulkInitialInventoryActivity extends InventoryActivity {

    @InjectView(R.id.btn_add_products)
    private TextView btnAddProducts;

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
        btnAddProducts.setOnClickListener(goToAddNonBasicProductsLister());
        initRecyclerView();
        Subscription subscription = presenter.loadInventoryWithBasicProducts().subscribe(getOnViewModelsLoadedSubscriber());
        subscriptions.add(subscription);
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
                    ToastUtil.show(getResources().getString(R.string.msg_error_basic_products));
                }


            }
        });
        btnSave.setOnClickListener(new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                btnSave.setEnabled(false);
                loading();
                Subscription subscription = presenter.initStockCardObservable().subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        Toast.makeText(getApplicationContext(), R.string.succesfully_saved, Toast.LENGTH_LONG).show();
                        loaded();
                        btnSave.setEnabled(true);
                    }
                });
                subscriptions.add(subscription);
            }
        });
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
                InventoryViewModel inventoryModel = new InventoryViewModel(Product.dummyProduct());
                inventoryModel.setDummyModel(true);
                inventoryViewModels.add(FIRST_ELEMENT_POSITION_OF_THE_LIST, inventoryModel);
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

    @Override
    public boolean onSearchStart(String query) {
        mAdapter.filter(query);
        if (!query.isEmpty()) {
            mAdapter.getFilteredList().add(FIRST_ELEMENT_POSITION_OF_THE_LIST, new InventoryViewModel(Product.dummyProduct()));
        }
        setUpFastScroller(mAdapter.getFilteredList());
        return false;
    }

    public View.OnClickListener goToAddNonBasicProductsLister() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddNonBasicProductsActivity.class);
                startActivityForResult(intent, 0);
            }
        };
    }
}
