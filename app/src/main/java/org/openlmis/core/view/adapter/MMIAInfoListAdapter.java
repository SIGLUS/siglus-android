package org.openlmis.core.view.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.BaseInfoItem;

import java.util.List;

public class MMIAInfoListAdapter extends BaseAdapter {

    private final Context context;
    private final List<BaseInfoItem> list;

    public MMIAInfoListAdapter(Context context, List<BaseInfoItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public BaseInfoItem getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View inflate = View.inflate(context, R.layout.item_mmia_info, viewGroup);
        TextView tvName = (TextView) inflate.findViewById(R.id.tv_name);
        EditText etTotal = (EditText) inflate.findViewById(R.id.et_total);

        if (i == 0) {
            tvName.setText(R.string.list_mmia_info_header_name);
            etTotal.setText(R.string.TOTAL);
            etTotal.setEnabled(false);
            etTotal.setGravity(Gravity.CENTER);
            inflate.setBackgroundResource(R.color.color_mmia_info_name);
        } else {
            BaseInfoItem item = getItem(i - 1);
            tvName.setText(item.getName());
        }

        return inflate;
    }
}
