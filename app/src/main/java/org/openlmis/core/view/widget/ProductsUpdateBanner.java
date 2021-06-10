/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

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

    setVisibility(View.VISIBLE);

    if (preferenceMgr.getShowUpdateBannerTexts().size() == 1) {
      tvProductUpdate.setText(Html.fromHtml(getContext().getString(R.string.hint_update_banner_tips,
          preferenceMgr.getShowUpdateBannerTexts().toArray()[0])));
    } else {
      tvProductUpdate.setText(Html.fromHtml(getContext().getString(R.string.hint_update_banner_tips,
          preferenceMgr.getShowUpdateBannerTexts().size() + " Products")));
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
