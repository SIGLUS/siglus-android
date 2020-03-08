package org.openlmis.core.view.activity;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.BulkInitialInventoryPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.BulkInitialInventoryAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.holder.BulkInitialInventoryWithLotViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscription;
@ContentView(R.layout.activity_bulk_initial_inventory)
public class BulkInitialInventoryActivity extends InventoryActivity {
    private static final String TAG = BulkInitialInventoryActivity.class.getSimpleName();

    public static final int REQUEST_CODE = 1050;
    public final String EMPTY_STRING = "";

    private List<Product> selectedProducts;

    @InjectView(R.id.btn_add_products)
    private TextView btnAddProducts;

    @InjectPresenter(BulkInitialInventoryPresenter.class)
    BulkInitialInventoryPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedProducts = new ArrayList<>();
    }

    @Override
    protected void initRecyclerView() {
        mAdapter = new BulkInitialInventoryAdapter(presenter.getInventoryViewModelList(),
                removeNonBasicProductListener(),
                getSaveListener(),
                getDoneListener(),
                getRefreshCompleteCountListener());
        productListRecycleView.setAdapter(mAdapter);
    }

    @Override
    public void initUI() {
        super.initUI();
        btnAddProducts.setOnClickListener(goToAddNonBasicProductsLister());
        initRecyclerView();
        Subscription subscription = presenter.loadInventory().subscribe(getOnViewModelsLoadedSubscriber());
        subscriptions.add(subscription);
        btnDone.setOnClickListener(getDoneListener());
        btnSave.setOnClickListener(getSaveListener());
    }


    private BulkInitialInventoryWithLotViewHolder.InventoryItemStatusChangeListener getRefreshCompleteCountListener(){
        return done -> setTotal(presenter.getInventoryViewModelList().size());
    }
    @NonNull
    private SingleClickButtonListener getSaveListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                btnSave.setEnabled(false);
                loading();
                Subscription subscription = presenter.saveDraftInventoryObservable().subscribe( o -> {
                    Toast.makeText(getApplicationContext(), R.string.succesfully_saved, Toast.LENGTH_LONG).show();
                    loaded();
                    btnSave.setEnabled(true);
                });
                subscriptions.add(subscription);
            }
        };
    }

    @NonNull
    private SingleClickButtonListener getDoneListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                btnDone.setEnabled(false);
                if (validateInventory()) {
                    loading();
                    Subscription subscription = presenter.doInventory().subscribe(onNextMainPageAction);
                    subscriptions.add(subscription);
                } else {
                    btnDone.setEnabled(true);
                    ToastUtil.show(getResources().getString(R.string.msg_error_basic_products));
                }
            }
        };
    }

    @Override
    public boolean validateInventory() {
        int position = mAdapter.validateAll();
        setTotal(presenter.getInventoryViewModelList().size());
        if (position >= 0) {
            clearSearch();
            productListRecycleView.scrollToPosition(position);
            return false;
        }
        return true;
    }
    @Override
    protected void goToNextPage() {
        preferencesMgr.setIsNeedsInventory(false);
        startActivity(HomeActivity.getIntentToMe(this));
        this.finish();
    }

    public View.OnClickListener goToAddNonBasicProductsLister() {
        return v -> {
            Intent intent = new Intent(getApplicationContext(), AddNonBasicProductsActivity.class);
            intent.putExtra(AddNonBasicProductsActivity.SELECTED_PRODUCTS, (Serializable) selectedProducts);
            startActivityForResult(intent, REQUEST_CODE);
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
    @Override
    public void onBackPressed() {
        if (isSearchViewActivity()) {
            searchView.onActionViewCollapsed();
            return;
        }
        if (isDataChange()) {
            showDataChangeConfirmDialog();
            return;
        }
        super.onBackPressed();
    }
    private boolean isDataChange() {
        return ((BulkInitialInventoryAdapter) mAdapter).isHasDataChanged();
    }
    private void showDataChangeConfirmDialog() {
        DialogFragment dialogFragment = SimpleDialogFragment.newInstance(
                null,
                getString(R.string.msg_back_confirm),
                getString(R.string.btn_positive),
                getString(R.string.btn_negative),
                "onBackPressed");
        dialogFragment.show(getFragmentManager(), "");
    }
    private boolean areThereSelectedProducts(int requestCode, int resultCode, Intent data) {
        return requestCode == REQUEST_CODE
                && resultCode == AddNonBasicProductsActivity.RESULT_CODE
                && data.getExtras() != null
                && data.getExtras().containsKey(AddNonBasicProductsActivity.SELECTED_PRODUCTS);
    }

    protected void setTotal() {
        int total = 0;
        for (InventoryViewModel model : presenter.getInventoryViewModelList()) {
            if (model.getProductId() != 0) {
                total++;
            }
        }
        tvTotal.setText(getString(R.string.label_total, total));
    }

    @NonNull
    private View.OnClickListener removeNonBasicProductListener() {
        return v -> {
            InventoryViewModel model = (InventoryViewModel) v.getTag();
            presenter.removeNonBasicProductElement(model);
            mAdapter.notifyDataSetChanged();
            selectedProducts.remove(model.getProduct());
            setTotal();
        };
    }
}
