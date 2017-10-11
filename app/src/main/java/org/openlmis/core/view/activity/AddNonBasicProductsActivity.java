package org.openlmis.core.view.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.presenter.AddNonBasicProductsPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.AddNonBasicProductsAdapter;
import org.openlmis.core.view.viewmodel.NonBasicProductsViewModel;

import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_add_non_basic_products)
public class AddNonBasicProductsActivity extends SearchBarActivity {

    public static final String EMPTY_STRING = "";
    @InjectView(R.id.non_basic_products)
    RecyclerView rvNonBasicProducts;

    @InjectView(R.id.btn_cancel)
    Button btnCancel;

    @InjectView(R.id.btn_add_products)
    Button btnAddProducts;

    @InjectPresenter(AddNonBasicProductsPresenter.class)
    AddNonBasicProductsPresenter presenter;

    AddNonBasicProductsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRecyclerView();
        Subscription subscription = presenter.getAllNonBasicProductsViewModels().subscribe(getOnViewModelsLoadedSubscriber());
        subscriptions.add(subscription);

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
