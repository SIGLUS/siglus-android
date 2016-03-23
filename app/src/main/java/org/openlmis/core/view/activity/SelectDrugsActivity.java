package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.presenter.ProductPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.SelectDrugsAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

@ContentView(R.layout.activity_select_drugs)
public class SelectDrugsActivity extends BaseActivity {

    public static final String PARAM_REGIME_TYPE = "regime_type";

    @InjectView(R.id.btn_next)
    public View btnNext;

    @InjectView(R.id.products_list)
    public RecyclerView productListRecycleView;

    protected SelectDrugsAdapter mAdapter;

    @InjectPresenter(ProductPresenter.class)
    ProductPresenter presenter;
    protected List<InventoryViewModel> viewModels;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.SelectDrugsScreen;
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_AMBER;
    }

    public static Intent getIntentToMe(Context context, Regimen.RegimeType type) {
        Intent intent = new Intent(context, SelectDrugsActivity.class);
        intent.putExtra(PARAM_REGIME_TYPE, type);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Regimen.RegimeType regimeType = (Regimen.RegimeType) getIntent().getSerializableExtra(PARAM_REGIME_TYPE);

        productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
        viewModels = new ArrayList<>();
        mAdapter = new SelectDrugsAdapter(viewModels);
        productListRecycleView.setAdapter(mAdapter);
        loading();
        Subscription subscription = presenter.loadMMIAProducts().subscribe(subscriber);
        subscriptions.add(subscription);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<InventoryViewModel> checkedViewModels = getCheckedProducts();
                if (checkedViewModels.isEmpty()) {
                    ToastUtil.show(R.string.hint_no_product_has_checked);
                    return;
                }
                loading();
                Subscription subscription = presenter.saveRegimes(checkedViewModels, regimeType).subscribe(saveSubscriber);
                subscriptions.add(subscription);
            }
        });
    }

    private List<InventoryViewModel> getCheckedProducts() {
        return from(viewModels).filter(new Predicate<InventoryViewModel>() {
            @Override
            public boolean apply(InventoryViewModel inventoryViewModel) {
                return inventoryViewModel.isChecked();
            }
        }).toList();
    }

    Subscriber<List<InventoryViewModel>> subscriber = new Subscriber<List<InventoryViewModel>>() {
        @Override
        public void onCompleted() {
            loaded();
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onError(Throwable e) {
            loaded();
            ToastUtil.show(e.getMessage());
        }

        @Override
        public void onNext(List<InventoryViewModel> data) {
            viewModels.clear();
            viewModels.addAll(data);
        }
    };

    Subscriber<Void> saveSubscriber = new Subscriber<Void>() {
        @Override
        public void onCompleted() {
            loaded();
            //TODO code next
        }

        @Override
        public void onError(Throwable e) {
            loaded();
            ToastUtil.show(e.getMessage());
        }

        @Override
        public void onNext(Void data) {
        }
    };
}
