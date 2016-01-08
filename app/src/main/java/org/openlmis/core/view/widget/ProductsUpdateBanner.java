package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import org.openlmis.core.R;

import roboguice.RoboGuice;

public class ProductsUpdateBanner extends LinearLayout {

    public ProductsUpdateBanner(Context context) {
        super(context);
        init(context);
    }

    public ProductsUpdateBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_products_update_banner, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
    }
}
