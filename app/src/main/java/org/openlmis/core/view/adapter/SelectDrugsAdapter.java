package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.SelectProductsViewHolder;
import org.openlmis.core.view.viewmodel.RegimeProductViewModel;

import java.util.List;

public class SelectDrugsAdapter extends RecyclerView.Adapter<SelectProductsViewHolder> {

    private List<RegimeProductViewModel> products;

    public SelectDrugsAdapter(List<RegimeProductViewModel> products) {
        this.products = products;
    }

    @Override
    public SelectProductsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SelectProductsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_product, parent, false));
    }

    @Override
    public void onBindViewHolder(SelectProductsViewHolder holder, int position) {
        RegimeProductViewModel product = products.get(position);
        holder.populate(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }
}
