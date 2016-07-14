package org.openlmis.core.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.AddDrugsToFormViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.ArrayList;

public class AddDrugsToVIAAdapter extends InventoryListAdapter<AddDrugsToFormViewHolder> implements FilterableAdapter{

    public AddDrugsToVIAAdapter(ArrayList<InventoryViewModel> inventoryViewModels) {
        super(inventoryViewModels);
    }

    @Override
    public AddDrugsToFormViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AddDrugsToFormViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_product_in_via, parent, false));
    }

    @Override
    public void onBindViewHolder(AddDrugsToFormViewHolder holder, int position) {
        holder.populate(queryKeyWord, filteredList.get(position));

    }
}
