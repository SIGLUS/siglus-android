package org.openlmis.core.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.AddDrugsToVIAViewHolder;
import org.openlmis.core.view.viewmodel.AddDrugsToViaInventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

public class AddDrugsToVIAAdapter extends InventoryListAdapter<AddDrugsToVIAViewHolder> implements
    FilterableAdapter {

  public AddDrugsToVIAAdapter(List<InventoryViewModel> inventoryViewModels) {
    super(inventoryViewModels);
  }

  @Override
  public AddDrugsToVIAViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new AddDrugsToVIAViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_select_product_in_via, parent, false));
  }

  @Override
  public void onBindViewHolder(AddDrugsToVIAViewHolder holder, int position) {
    holder.populate(queryKeyWord, (AddDrugsToViaInventoryViewModel) filteredList.get(position));
  }
}
