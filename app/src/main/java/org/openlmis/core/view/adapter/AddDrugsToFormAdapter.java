package org.openlmis.core.view.adapter;

import android.view.ViewGroup;

import org.openlmis.core.view.holder.AddDrugsToFormViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.ArrayList;

public class AddDrugsToFormAdapter extends InventoryListAdapter<AddDrugsToFormViewHolder> implements FilterableAdapter{
    public AddDrugsToFormAdapter(ArrayList<InventoryViewModel> inventoryViewModels) {
        super(inventoryViewModels);
    }

    @Override
    public AddDrugsToFormViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(AddDrugsToFormViewHolder holder, int position) {

    }
}
