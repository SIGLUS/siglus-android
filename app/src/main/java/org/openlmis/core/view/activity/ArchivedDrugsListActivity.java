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

package org.openlmis.core.view.activity;

import static org.openlmis.core.presenter.StockCardPresenter.ArchiveStatus.ARCHIVED;
import static org.openlmis.core.view.holder.ArchivedDrugsViewHolder.ArchiveStockCardListener;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.presenter.StockCardPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.ArchivedListAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_archived_drugs)
public class ArchivedDrugsListActivity extends SearchBarActivity implements StockCardPresenter.StockCardListView {

  @InjectView(R.id.archived_list)
  protected RecyclerView archivedList;

  @InjectPresenter(StockCardPresenter.class)
  StockCardPresenter presenter;

  private ArchivedListAdapter mAdapter;

  public static Intent getIntentToMe(Context context) {
    return new Intent(context, ArchivedDrugsListActivity.class);
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.ARCHIVED_DRUGS_LIST_SCREEN;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initUI();
  }

  private void initUI() {
    archivedList.setLayoutManager(new LinearLayoutManager(this));

    mAdapter = new ArchivedListAdapter(new ArrayList<>(),
        archiveStockCardListener);
    archivedList.setAdapter(mAdapter);

    presenter.loadStockCards(ARCHIVED);
  }

  @Override
  public boolean onSearchStart(String query) {
    mAdapter.filter(query);
    return false;
  }

  protected ArchiveStockCardListener archiveStockCardListener = new ArchiveStockCardListener() {
    @Override
    public void viewMovementHistory(StockCard stockCard) {
      startActivity(StockMovementHistoryActivity.getIntentToMe(ArchivedDrugsListActivity.this,
          stockCard.getId(),
          stockCard.getProduct().getFormattedProductName(),
          true,
          false));
    }

    @Override
    public void archiveStockCardBack(StockCard stockCard) {
      presenter.archiveBackStockCard(stockCard);
      presenter.loadStockCards(ARCHIVED);

      setResult(RESULT_OK);
    }
  };

  @Override
  public void refresh(List<InventoryViewModel> data) {
    mAdapter.refreshList(data);
  }

  @Override
  public void refreshBannerText() {
    // do nothing
  }

  @Override
  public void showWarning() {
    // do noting
  }
}
