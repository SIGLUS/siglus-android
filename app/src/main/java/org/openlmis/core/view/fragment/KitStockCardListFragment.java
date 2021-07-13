/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;
import org.openlmis.core.R;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.activity.StockMovementsWithLotActivity;
import org.openlmis.core.view.adapter.KitStockCardListAdapter;
import org.openlmis.core.view.holder.StockCardViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.ProductsUpdateBanner;
import roboguice.inject.InjectView;

public class KitStockCardListFragment extends StockCardListFragment {

  @InjectView(R.id.product_update_banner)
  ProductsUpdateBanner kitProductsUpdateBanner;

  protected StockCardViewHolder.OnItemViewClickListener viewClickListener =
      inventoryViewModel -> {
        Intent intent = getStockMovementIntent(inventoryViewModel);
        startActivityForResult(intent, Constants.REQUEST_UNPACK_KIT);
      };

  @Override
  protected void createAdapter() {
    mAdapter = new KitStockCardListAdapter(new ArrayList<>(), viewClickListener);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    kitProductsUpdateBanner.setVisibility(View.GONE);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    kitProductsUpdateBanner.setVisibility(View.GONE);
  }

  @Override
  protected void loadStockCards() {
    presenter.loadKits();
  }

  @Override
  protected Intent getStockMovementIntent(InventoryViewModel inventoryViewModel) {
    return StockMovementsWithLotActivity.getIntentToMe(getActivity(), inventoryViewModel, true);
  }
}
