package org.openlmis.core.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.AddDrugsToVIAViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.ArrayList;
import java.util.List;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class AddDrugsToVIAAdapter extends InventoryListAdapter<AddDrugsToVIAViewHolder> implements FilterableAdapter{

    public AddDrugsToVIAAdapter(ArrayList<InventoryViewModel> inventoryViewModels) {
        super(inventoryViewModels);
    }


    public List<InventoryViewModel> getCheckedProducts() {
        return from(data).filter(new Predicate<InventoryViewModel>() {
            @Override
            public boolean apply(InventoryViewModel viewModel) {
                return viewModel.isChecked();
            }
        }).toList();
    }

    @Override
    public AddDrugsToVIAViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AddDrugsToVIAViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_product_in_via, parent, false));
    }

    @Override
    public void onBindViewHolder(AddDrugsToVIAViewHolder holder, int position) {
        holder.populate(queryKeyWord, filteredList.get(position));
    }
}
