package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.presenter.ProductPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.SelectDrugsAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_select_drugs)
public class SelectDrugsActivity extends BaseActivity {

    @InjectView(R.id.btn_next)
    public View btnNext;

    @InjectView(R.id.products_list)
    public RecyclerView productListRecycleView;

    protected SelectDrugsAdapter mAdapter;

    @InjectPresenter(ProductPresenter.class)
    ProductPresenter presenter;
    private List<InventoryViewModel> viewModels;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.SelectDrugsScreen;
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_AMBER;
    }

    public static Intent getIntentToMe(Context context) {
        Intent intent = new Intent(context, SelectDrugsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                presenter.saveRegimes();
            }
        });
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
}
