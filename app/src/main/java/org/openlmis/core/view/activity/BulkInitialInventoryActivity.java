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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

@ContentView(R.layout.activity_bulk_initial_inventory)
public class BulkInitialInventoryActivity extends InventoryActivity {

    public static final int REQUEST_CODE = 1050;
    public final String EMPTY_STRING = "";

    private List<Product> selectedProducts;

    @InjectView(R.id.btn_add_products)
    private TextView btnAddProducts;

    @InjectPresenter(InitialInventoryPresenter.class)
    InitialInventoryPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedProducts = new ArrayList<>();
    }

    @Override
    protected void initRecyclerView() {
        mAdapter = new BulkInitialInventoryAdapter(presenter.getInventoryViewModelList());
        productListRecycleView.setAdapter(mAdapter);
        ((BulkInitialInventoryAdapter)mAdapter).setRemoveNonBasicProductListener(removeNonBasicProductListener());
    }

    @Override
    public void initUI() {
        super.initUI();
        btnAddProducts.setOnClickListener(goToAddNonBasicProductsLister());
        initRecyclerView();
        Subscription subscription = presenter.loadInventoryWithBasicProducts().subscribe(getOnViewModelsLoadedSubscriber());
        subscriptions.add(subscription);
        btnDone.setOnClickListener(doneListener());
        btnSave.setOnClickListener(saveListener());
    }

    @NonNull
    private SingleClickButtonListener saveListener() {
        return new SingleClickButtonListener() {
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
        };
    }

    @NonNull
    private SingleClickButtonListener doneListener() {
        return new SingleClickButtonListener() {
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
        };
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
                presenter.filterViewModels(EMPTY_STRING);
                mAdapter.notifyDataSetChanged();
                setTotal();
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
        presenter.filterViewModels(query);
        mAdapter.notifyDataSetChanged();
        return false;
    }

    public View.OnClickListener goToAddNonBasicProductsLister() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddNonBasicProductsActivity.class);
                intent.putExtra(AddNonBasicProductsActivity.SELECTED_PRODUCTS, (Serializable) selectedProducts);
                startActivityForResult(intent, REQUEST_CODE);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (areThereSelectedProducts(requestCode, resultCode, data)) {
            this.selectedProducts.clear();
            this.selectedProducts.addAll((ArrayList<Product>) data.getSerializableExtra(AddNonBasicProductsActivity.SELECTED_PRODUCTS));
            presenter.addNonBasicProductsToInventory(selectedProducts);
            setUpFastScroller(presenter.getInventoryViewModelList());
            mAdapter.notifyDataSetChanged();
            setTotal();
        }
    }

    private boolean areThereSelectedProducts(int requestCode, int resultCode, Intent data) {
        return requestCode == REQUEST_CODE && resultCode == AddNonBasicProductsActivity.RESULT_CODE && data.getExtras().
                containsKey(AddNonBasicProductsActivity.SELECTED_PRODUCTS);
    }

    protected void setTotal() {
        int total = 0;
        for (InventoryViewModel model: presenter.getInventoryViewModelList()){
            if(model.getProductId()!=0){
                total++;
            }
        }
        tvTotal.setText(getString(R.string.label_total, String.valueOf(total)));
    }

    @NonNull
    private View.OnClickListener removeNonBasicProductListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               InventoryViewModel model = (InventoryViewModel)v.getTag();
                presenter.removeNonBasicProductElement(model);
                mAdapter.notifyDataSetChanged();
                selectedProducts.remove(model.getProduct());
                setTotal();
            }
        };
    }
}
