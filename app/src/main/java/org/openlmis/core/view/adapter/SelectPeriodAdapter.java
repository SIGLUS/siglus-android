package org.openlmis.core.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.openlmis.core.R;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.view.viewmodel.SelectInventoryViewModel;
import org.openlmis.core.view.widget.SelectPeriodCardView;

import java.util.ArrayList;
import java.util.List;

public class SelectPeriodAdapter extends BaseAdapter {

    private final List<SelectInventoryViewModel> list;

    public SelectPeriodAdapter() {
        list = new ArrayList();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public SelectInventoryViewModel getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SelectPeriodCardView inventoryCardView;

        if (convertView == null) {
            inventoryCardView = (SelectPeriodCardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_date, null, false);
        } else {
            inventoryCardView = (SelectPeriodCardView) convertView;
        }

        inventoryCardView.populate(getItem(position));
        return inventoryCardView;
    }

    public void refreshDate(List<SelectInventoryViewModel> inventories) {
        list.clear();
        list.addAll(inventories);
        notifyDataSetChanged();
    }
}
