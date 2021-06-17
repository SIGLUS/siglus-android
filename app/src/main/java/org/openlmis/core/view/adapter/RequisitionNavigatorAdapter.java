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

package org.openlmis.core.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import java.util.List;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import org.openlmis.core.R;
import org.openlmis.core.model.Program;
import org.openlmis.core.view.widget.RequisitionPageIndicator;

public class RequisitionNavigatorAdapter extends CommonNavigatorAdapter {

  private List<Program> data;

  private ViewPager2 viewPager;

  public RequisitionNavigatorAdapter(ViewPager2 viewPager) {
    this.viewPager = viewPager;
  }

  public void setData(List<Program> data) {
    this.data = data;
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return data == null ? 0 : data.size();
  }

  @Override
  public IPagerTitleView getTitleView(Context context, int index) {
    ColorTransitionPagerTitleView titleView = new ColorTransitionPagerTitleView(context);
    titleView.setSingleLine(false);
    titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.getResources().getDimensionPixelSize(R.dimen.px_20));
    titleView.setNormalColor(Color.BLACK);
    titleView.setSelectedColor(Color.BLACK);
    titleView.setText(data.get(index).getProgramName());
    titleView.setOnClickListener(view -> viewPager.setCurrentItem(index));
    return titleView;
  }

  @Override
  public IPagerIndicator getIndicator(Context context) {
    RequisitionPageIndicator indicator = new RequisitionPageIndicator(context);
    indicator.setLineColors(ContextCompat.getColor(context, R.color.color_purple));
    indicator.setSelectedBackgroundColor(ContextCompat.getColor(context, R.color.color_requisition_background));
    return indicator;
  }
}
