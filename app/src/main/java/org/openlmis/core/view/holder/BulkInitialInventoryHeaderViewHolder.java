package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;

import roboguice.inject.InjectView;

public class BulkInitialInventoryHeaderViewHolder extends BaseViewHolder {

    @InjectView(R.id.headerTitle)
    TextView tvHeaderTitle;

    public BulkInitialInventoryHeaderViewHolder(View itemView, int textHeader) {
        super(itemView);
        tvHeaderTitle.setText(textHeader);
    }
}
