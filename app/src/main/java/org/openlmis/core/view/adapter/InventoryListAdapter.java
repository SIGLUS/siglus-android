package org.openlmis.core.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;


import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.InventoryPresenter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class InventoryListAdapter extends RecyclerView.Adapter<InventoryListAdapter.ViewHolder> {

    LayoutInflater inflater;
    InventoryPresenter presenter;
    Context context;
    List<InventoryViewModel> inventoryList;


    public InventoryListAdapter(Context context, InventoryPresenter presenter){
        inflater = LayoutInflater.from(context);
        this.presenter = presenter;
        this.context = context;
        inventoryList = wrapByViewModel(presenter.loadMasterProductList());
    }

    private List<InventoryViewModel> wrapByViewModel(List<Product> productList){
        List<InventoryViewModel> inventoryList = new ArrayList<>();

        for(Product product : productList){
            inventoryList.add(new InventoryViewModel(product));
        }

        return  inventoryList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_inventory, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final InventoryViewModel viewModel = inventoryList.get(position);

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    holder.actionDivider.setVisibility(View.VISIBLE);
                    holder.actionPanel.setVisibility(View.VISIBLE);
                } else {
                    holder.actionDivider.setVisibility(View.GONE);
                    holder.actionPanel.setVisibility(View.GONE);
                    viewModel.reset();
                    holder.reset();
                }
                viewModel.setChecked(isChecked);
            }
        });

        holder.txQuantity.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                viewModel.setQuantity(holder.txQuantity.getText().toString());
                return false;
            }
        });

        holder.txExpireDate.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                viewModel.setExpireDate(holder.txExpireDate.getText().toString());
                return false;
            }
        });

        holder.checkBox.setChecked(viewModel.isChecked());
        holder.productName.setText(viewModel.getProduct().getName());
        holder.productUnit.setText(viewModel.getProduct().getUnit());
        holder.txQuantity.setText(viewModel.getQuantity());
        holder.txExpireDate.setText(viewModel.getExpireDate());
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView productName;
        public TextView productUnit;
        public EditText txQuantity;
        public EditText txExpireDate;
        public View actionDivider;
        public CheckBox checkBox;
        public View actionPanel;

        public ViewHolder(View itemView) {
            super(itemView);

            productName = (TextView)itemView.findViewById(R.id.product_name);
            productUnit = (TextView)itemView.findViewById(R.id.product_unit);
            txQuantity = (EditText)itemView.findViewById(R.id.tx_quantity);
            txExpireDate = (EditText)itemView.findViewById(R.id.tx_expire_date);
            checkBox = (CheckBox)itemView.findViewById(R.id.checkbox);
            actionDivider = itemView.findViewById(R.id.action_divider);
            actionPanel = itemView.findViewById(R.id.action_panel);
        }

        public void reset(){
            txQuantity.setText(StringUtils.EMPTY);
            txExpireDate.setText(StringUtils.EMPTY);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
