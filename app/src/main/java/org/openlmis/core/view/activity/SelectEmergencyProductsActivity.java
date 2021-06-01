package org.openlmis.core.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.presenter.ProductPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.SelectEmergencyProductAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

@ContentView(R.layout.activity_select_drugs)
public class SelectEmergencyProductsActivity extends SearchBarActivity {

    @InjectView(R.id.btn_next)
    public View btnNext;

    @InjectView(R.id.products_list)
    public RecyclerView productListRecycleView;

    @InjectPresenter(ProductPresenter.class)
    ProductPresenter presenter;

    protected SelectEmergencyProductAdapter mAdapter;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.SELECT_EMERGENCY_PRODUCTS_SCREEN;
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_PURPLE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SelectEmergencyProductAdapter(new ArrayList<InventoryViewModel>());
        productListRecycleView.setAdapter(mAdapter);
        loading();
        Subscription subscription = presenter.loadEmergencyProducts().subscribe(subscriber);
        subscriptions.add(subscription);

        btnNext.setOnClickListener((v) -> validateAndGotoRnrPage());
    }

    private void validateAndGotoRnrPage() {
        List<InventoryViewModel> checkedViewModels = mAdapter.getCheckedProducts();
        if (checkedViewModels.isEmpty()) {
            ToastUtil.show(R.string.hint_no_product_has_checked);
            return;
        }
        btnNext.setEnabled(false);

        ImmutableList<StockCard> immutableList = from(checkedViewModels).transform(inventoryViewModel -> inventoryViewModel.getStockCard()).toList();
        ArrayList<StockCard> stockCards = new ArrayList<>();
        for (StockCard stockCard : immutableList) {
            stockCards.add(stockCard);
        }
        startActivityForResult(VIARequisitionActivity.getIntentToMe(this, stockCards), Constants.REQUEST_FROM_RNR_LIST_PAGE);
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
            mAdapter.refreshList(data);
        }
    };

    public static Intent getIntentToMe(Context context) {
        Intent intent = new Intent(context, SelectEmergencyProductsActivity.class);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_FROM_RNR_LIST_PAGE) {
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public boolean onSearchStart(String query) {
        mAdapter.filter(query);
        return false;
    }
}
