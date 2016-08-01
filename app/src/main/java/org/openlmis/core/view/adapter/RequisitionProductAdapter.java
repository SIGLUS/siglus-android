package org.openlmis.core.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.view.holder.RequisitionProductViewHolder;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;

import java.util.List;

public class RequisitionProductAdapter extends BaseAdapter {

    @Inject
    private Context context;

    public Context contextFormFragment;

    @Inject
    private VIARequisitionPresenter presenter;

    public RequisitionProductAdapter() {
    }

    public void setContextFormFragment(Context contextFormFragment) {
        this.contextFormFragment = contextFormFragment;
    }

    @Override
    public int getCount() {
        return data() == null ? 0 : data().size();
    }

    @Override
    public RequisitionFormItemViewModel getItem(int position) {
        return data() == null ? null : data().get(position);
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
        viewHolder.populate(getItem(position), presenter, contextFormFragment);
        return convertView;
    }

    private List<RequisitionFormItemViewModel> data() {
        return presenter.getRequisitionFormItemViewModels();
    }
}
