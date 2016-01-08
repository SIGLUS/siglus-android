package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class ProductsUpdateBanner extends LinearLayout implements View.OnClickListener{

    @InjectView(R.id.iv_product_update_banner_clear)
    View ivClear;

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

        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_show_products_update_banner_529)) {
            setVisibility(GONE);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ivClear.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        setVisibility(GONE);
    }
}
