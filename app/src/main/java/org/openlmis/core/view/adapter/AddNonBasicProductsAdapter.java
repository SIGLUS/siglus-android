package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.NonBasicProductViewHolder;
import org.openlmis.core.view.viewmodel.NonBasicProductsViewModel;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class AddNonBasicProductsAdapter extends RecyclerView.Adapter<NonBasicProductViewHolder> {

    @Getter
    private List<NonBasicProductsViewModel> models;

    private List<NonBasicProductsViewModel> filteredList;

    public AddNonBasicProductsAdapter(List<NonBasicProductsViewModel> models) {
        filteredList = new ArrayList<>();
        this.models = models;
    }

    @Override
    public NonBasicProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NonBasicProductViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_non_basic_product, parent, false));
    }

    @Override
    public void onBindViewHolder(NonBasicProductViewHolder holder, int position) {
        holder.putOnChangedListener(filteredList.get(position));
        holder.populate(filteredList.get(position));
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(final String keyword) {
        List<NonBasicProductsViewModel> filteredViewModels;

        if (TextUtils.isEmpty(keyword)) {
            filteredViewModels = models;
        } else {
            filteredViewModels = from(models)
                    .filter(nonBasicProductsViewModel ->
                            nonBasicProductsViewModel.getProduct()
                                    .getProductFullName().toLowerCase()
                                    .contains(keyword.toLowerCase())).toList();
        }

        filteredList.clear();
        filteredList.addAll(filteredViewModels);
        this.notifyDataSetChanged();
    }

}
