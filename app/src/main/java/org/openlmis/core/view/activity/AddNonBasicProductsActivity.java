package org.openlmis.core.view.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.AddNonBasicProductsPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.AddNonBasicProductsAdapter;
import org.openlmis.core.view.viewmodel.NonBasicProductsViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_add_non_basic_products)
public class AddNonBasicProductsActivity extends SearchBarActivity {

    public static final String EMPTY_STRING = "";
    public static final int RESULT_CODE = 2000;
    public static final String SELECTED_NON_BASIC_PRODUCTS = "SELECTED_PRODUCTS";
    @InjectView(R.id.non_basic_products)
    RecyclerView rvNonBasicProducts;

    @InjectView(R.id.btn_cancel)
    Button btnCancel;

    @InjectView(R.id.btn_add_products)
    Button btnAddProducts;

    @InjectPresenter(AddNonBasicProductsPresenter.class)
    AddNonBasicProductsPresenter presenter;

    AddNonBasicProductsAdapter adapter;

    private List<String> previouslyProductCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRecyclerView();
        loading(getString(R.string.add_products_loading_message));
        previouslyProductCodes = (List<String>) getIntent().getSerializableExtra(SELECTED_NON_BASIC_PRODUCTS);
        btnCancel.setOnClickListener(cancelListener());
        btnAddProducts.setOnClickListener(addProductsListener());
        Subscription subscription = presenter.getAllNonBasicProductsViewModels(previouslyProductCodes).subscribe(getOnViewModelsLoadedSubscriber());
        subscriptions.add(subscription);

    }

    @NonNull
    private View.OnClickListener addProductsListener() {
        return v -> {
            List<Product> selectedProducts = new ArrayList<>();
            for (NonBasicProductsViewModel model : adapter.getModels()) {
                if (model.isChecked()) {
                    selectedProducts.add(model.getProduct());
                }
            }
            Intent intent = new Intent();
            intent.putExtra(SELECTED_NON_BASIC_PRODUCTS, (Serializable) selectedProducts);
            setResult(RESULT_CODE, intent);
            finish();
        };
    }

    @NonNull
    private View.OnClickListener cancelListener() {
        return v -> finish();
    }

    private void initRecyclerView() {
        adapter = new AddNonBasicProductsAdapter(presenter.getModels());
        rvNonBasicProducts.setLayoutManager(new LinearLayoutManager(this));
        rvNonBasicProducts.setAdapter(adapter);
    }

    @Override
    public boolean onSearchStart(String query) {
        adapter.filter(query);
        return false;
    }

    @Override
    protected ScreenName getScreenName() {
        return null;
    }

    @NonNull
    protected Subscriber<List<NonBasicProductsViewModel>> getOnViewModelsLoadedSubscriber() {
        return new Subscriber<List<NonBasicProductsViewModel>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.show(e.getMessage());
                loaded();
            }

            @Override
            public void onNext(List<NonBasicProductsViewModel> inventoryViewModels) {
                loaded();
                adapter.filter(EMPTY_STRING);
                adapter.notifyDataSetChanged();
            }
        };
    }

}
