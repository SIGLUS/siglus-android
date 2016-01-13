package org.openlmis.core.view.widget;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;

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

    @Inject
    SharedPreferenceMgr preferenceMgr;

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

        if (preferenceMgr.isNeedShowProductsUpdateBanner()) {
            setVisibility(VISIBLE);
            refreshBannerText();
        } else {
            setVisibility(GONE);
        }
    }

    public void refreshBannerText() {
        if (!preferenceMgr.isNeedShowProductsUpdateBanner()) {
            return;
        }

        if (getVisibility() == View.GONE) {
            setVisibility(View.VISIBLE);
        }

        Set<String> showUpdateBannerTexts = preferenceMgr.getShowUpdateBannerTexts();
        if (showUpdateBannerTexts.size() == 1) {
            tvProductUpdate.setText(Html.fromHtml(getContext().getString(R.string.hint_update_banner_tips, showUpdateBannerTexts.toArray()[0])));
        } else {
            tvProductUpdate.setText(Html.fromHtml(getContext().getString(R.string.hint_update_banner_tips, showUpdateBannerTexts.size() + " Products")));
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
        preferenceMgr.setIsNeedShowProductsUpdateBanner(false, null);
    }
}
