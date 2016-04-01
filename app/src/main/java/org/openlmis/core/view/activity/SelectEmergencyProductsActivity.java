package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.presenter.ProductPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.SelectEmergencyProductAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

@ContentView(R.layout.activity_select_drugs)
public class SelectEmergencyProductsActivity extends BaseActivity {

    @InjectView(R.id.btn_next)
    public View btnNext;

    @InjectView(R.id.products_list)
    public RecyclerView productListRecycleView;

    @InjectPresenter(ProductPresenter.class)
    ProductPresenter presenter;

    protected List<InventoryViewModel> viewModels;

    protected SelectEmergencyProductAdapter mAdapter;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.SelectEmergencyProductsScreen;
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_PURPLE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
        viewModels = new ArrayList<>();
        mAdapter = new SelectEmergencyProductAdapter(viewModels);
        productListRecycleView.setAdapter(mAdapter);
        loading();
        Subscription subscription = presenter.loadEmergencyProducts().subscribe(subscriber);
        subscriptions.add(subscription);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndGotoRnrPage();
            }
        });
    }

    private void validateAndGotoRnrPage() {
        List<InventoryViewModel> checkedViewModels = mAdapter.getCheckedProducts();
        if (checkedViewModels.isEmpty()) {
            ToastUtil.show(R.string.hint_no_product_has_checked);
            return;
        }

        ImmutableList<StockCard> immutableList = from(checkedViewModels).transform(new Function<InventoryViewModel, StockCard>() {
            @Override
            public StockCard apply(InventoryViewModel inventoryViewModel) {
                return inventoryViewModel.getStockCard();
            }
        }).toList();
        ArrayList<StockCard> stockCards = new ArrayList<>();
        stockCards.addAll(immutableList);
        startActivity(VIARequisitionActivity.getIntentToMe(this, stockCards));
        finish();
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

    public static Intent getIntentToMe(Context context) {
        Intent intent = new Intent(context, SelectEmergencyProductsActivity.class);
        return intent;
    }
}
