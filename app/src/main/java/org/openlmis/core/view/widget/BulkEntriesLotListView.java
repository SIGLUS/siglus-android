package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.openlmis.core.view.adapter.BulkEntriesLotMovementAdapter;


public class BulkEntriesLotListView extends BaseLotListView{

  private BulkEntriesLotMovementAdapter existingBulkEntriesLotMovementAdapter;


  public BulkEntriesLotListView(Context context) {
    super(context);
  }

  public BulkEntriesLotListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void initExistingLotListView() {
    super.initExistingLotListView();
    existingLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
    existingBulkEntriesLotMovementAdapter = new BulkEntriesLotMovementAdapter(
        viewModel.getExistingLotMovementViewModelList());
    existingLotListView.setAdapter(existingBulkEntriesLotMovementAdapter);
  }

  @Override
  public void initNewLotListView() {
    super.initNewLotListView();
  }
}
