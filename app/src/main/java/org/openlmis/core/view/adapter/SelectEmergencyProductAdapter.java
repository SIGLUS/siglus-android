package org.openlmis.core.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.SelectEmergencyProductsViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.List;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class SelectEmergencyProductAdapter extends InventoryListAdapter<SelectEmergencyProductsViewHolder> implements FilterableAdapter {
    public static final int MAX_CHECKED_LIMIT = 10;

    public SelectEmergencyProductAdapter(List<InventoryViewModel> data) {
        super(data);
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
    public SelectEmergencyProductsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SelectEmergencyProductsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_product, parent, false));
    }

    @Override
    public void onBindViewHolder(SelectEmergencyProductsViewHolder holder, int position) {
        holder.populate(this, queryKeyWord, filteredList.get(position));
    }

    public boolean isReachLimit() {
        return getCheckedProducts().size() == MAX_CHECKED_LIMIT;
    }
}
