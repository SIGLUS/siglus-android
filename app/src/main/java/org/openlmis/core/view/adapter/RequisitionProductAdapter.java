package org.openlmis.core.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.openlmis.core.R;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.view.holder.RequisitionProductViewHolder;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;

import java.util.List;

public class RequisitionProductAdapter extends BaseAdapter {

    private Context context;

    private VIARequisitionPresenter presenter;

    public RequisitionProductAdapter(Context context, VIARequisitionPresenter presenter) {
        this.context = context;
        this.presenter = presenter;
    }

    @Override
    public int getCount() {
        return data() == null ? 0 : data().size();
    }

    @Override
    public RequisitionFormItemViewModel getItem(int position) {
        return data() == null && data().size() == 0 ? null : data().get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RequisitionProductViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_requisition_body_left, parent, false);
            viewHolder = new RequisitionProductViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (RequisitionProductViewHolder) convertView.getTag();
        }
        viewHolder.populate(getItem(position), presenter, context);
        return convertView;
    }

    private List<RequisitionFormItemViewModel> data() {
        return presenter.getRequisitionFormItemViewModels();
    }
}
