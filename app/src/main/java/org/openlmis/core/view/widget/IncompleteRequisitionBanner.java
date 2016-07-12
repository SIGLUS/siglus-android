package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;


public class IncompleteRequisitionBanner extends LinearLayout {
    @InjectView(R.id.tx_incomplete_requisition)
    TextView txIncompleteRequisition;

    protected Context context;

    public IncompleteRequisitionBanner(Context context) {
        super(context);
        init(context);
    }

    public IncompleteRequisitionBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.view_incomplete_requisition_banner, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
        txIncompleteRequisition.setText("Should submit missed test");
    }
}
