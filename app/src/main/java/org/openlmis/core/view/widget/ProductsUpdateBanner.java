package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;

import java.util.Set;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class ProductsUpdateBanner extends LinearLayout implements View.OnClickListener {

    @InjectView(R.id.iv_product_update_banner_clear)
    View ivClear;

    @InjectView(R.id.tv_product_update)
    TextView tvProductUpdate;

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

        if (SharedPreferenceMgr.getInstance().isNeedShowProductsUpdateBanner()) {
            setVisibility(VISIBLE);
            setBannerText();
        } else {
            setVisibility(GONE);
        }

        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_show_products_update_banner_529)) {
            setVisibility(GONE);
        }
    }

    private void setBannerText() {
        Set<String> showUpdateBannerText = SharedPreferenceMgr.getInstance().getShowUpdateBannerText();
        if (showUpdateBannerText.size() == 1) {
            tvProductUpdate.setText(getContext().getString(R.string.hint_update_banner_tips, showUpdateBannerText.toArray()[0]));
        } else {
            tvProductUpdate.setText(getContext().getString(R.string.hint_update_banner_tips, showUpdateBannerText.size() + " Products"));
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
        SharedPreferenceMgr.getInstance().setIsNeedShowProductsUpdateBanner(false, null);
    }
}
