package org.openlmis.core.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.openlmis.core.R;
import org.openlmis.core.view.activity.StockMovementActivity;
import org.openlmis.core.view.adapter.KitStockCardListAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.ProductsUpdateBanner;

import roboguice.inject.InjectView;

public class KitStockCardListFragment extends StockCardListFragment {
    @Override
    protected void loadStockCards() {
        presenter.loadKits();
    }

    protected Intent getStockMovementIntent(InventoryViewModel inventoryViewModel) {
        return StockMovementActivity.getIntentToMe(getActivity(), inventoryViewModel, true);
    }

    @InjectView(R.id.product_update_banner)
    ProductsUpdateBanner productsUpdateBanner;

    @Override
    protected void createAdapter() {
        mAdapter = new KitStockCardListAdapter(inventoryViewModels, onItemViewClickListener);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productsUpdateBanner.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        productsUpdateBanner.setVisibility(View.GONE);
    }
}
