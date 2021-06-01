package org.openlmis.core.view.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openlmis.core.R;

import java.util.List;

public class RapidTestTopProductCodeAdapter extends RecyclerView.Adapter<RapidTestTopProductCodeAdapter.RapidTestTopProductCodeViewHolder> {

    private final List<String> productCodes;

    public RapidTestTopProductCodeAdapter(List<String> productCodes) {
        this.productCodes = productCodes;
    }

    @Override
    public RapidTestTopProductCodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rapid_test_top_left, parent, false);
        return new RapidTestTopProductCodeViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RapidTestTopProductCodeViewHolder holder, int position) {
        holder.tvProductCode.setText(productCodes.get(position));
    }

    @Override
    public int getItemCount() {
        return productCodes == null ? 0 : productCodes.size();
    }

    protected static class RapidTestTopProductCodeViewHolder extends RecyclerView.ViewHolder {

        TextView tvProductCode;

        public RapidTestTopProductCodeViewHolder(View itemView) {
            super(itemView);
            tvProductCode = itemView.findViewById(R.id.left_tv_code);
        }
    }
}
