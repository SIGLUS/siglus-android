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

package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.greenrobot.eventbus.EventBus;
import org.openlmis.core.R;
import org.openlmis.core.event.RefreshBulkEntriesBackgroundEvent;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.adapter.BulkEntriesAdapter;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel;
import org.openlmis.core.view.widget.BulkEntriesLotListView;
import roboguice.inject.InjectView;

public class BulkEntriesViewHolder extends BaseViewHolder {


  @InjectView(R.id.tv_product_name)
  TextView productName;

  @InjectView(R.id.ic_trashcan)
  ImageView icTrashcan;

  @InjectView(R.id.rv_bulk_entries_lots)
  BulkEntriesLotListView bulkEntriesLotListView;

  public BulkEntriesViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(final BulkEntriesViewModel bulkEntriesViewModel,
      final BulkEntriesAdapter bulkEntriesAdapter) {
    productName.setText(TextStyleUtil.formatStyledProductName(bulkEntriesViewModel.getProduct()));
    if (bulkEntriesLotListView == null) {
      return;
    }
    bulkEntriesLotListView.initLotListView(bulkEntriesViewModel);
    icTrashcan.setOnClickListener(v -> {
      bulkEntriesAdapter.remove(bulkEntriesViewModel);
      EventBus.getDefault().post(new RefreshBulkEntriesBackgroundEvent(true));
    });
  }


}
