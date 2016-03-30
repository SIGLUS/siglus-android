package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.SelectEmergencyProductsViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.List;

public class SelectEmergencyProductAdapter extends RecyclerView.Adapter<SelectEmergencyProductsViewHolder> {

    private List<InventoryViewModel> products;

    public SelectEmergencyProductAdapter(List<InventoryViewModel> products) {
        this.products = products;
    }

    @Override
    public SelectEmergencyProductsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SelectEmergencyProductsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_product, parent, false));
    }

    @Override
    public void onBindViewHolder(SelectEmergencyProductsViewHolder holder, int position) {
        InventoryViewModel product = products.get(position);
        holder.populate(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }
}
