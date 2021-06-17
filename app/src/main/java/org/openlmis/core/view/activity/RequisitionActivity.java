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

package org.openlmis.core.view.activity;

import android.os.Bundle;
import android.util.Log;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;
import java.util.List;
import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.Program;
import org.openlmis.core.presenter.RequisitionPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.RequisitionNavigatorAdapter;
import org.openlmis.core.view.adapter.RequisitionPageAdapter;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_requisition)
public class RequisitionActivity extends BaseActivity implements RequisitionPresenter.RequisitionView {

  @InjectView(R.id.mi_requisition_type)
  private MagicIndicator magicIndicator;

  @InjectView(R.id.vp_requisition)
  private ViewPager2 requisitionViewpager;

  @InjectPresenter(RequisitionPresenter.class)
  private RequisitionPresenter requisitionPresenter;

  private RequisitionNavigatorAdapter navigatorAdapter;

  private RequisitionPageAdapter pageAdapter;

  private OnPageChangeCallback pageChangeCallback = new OnPageChangeCallback() {
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
      magicIndicator.onPageSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
      magicIndicator.onPageScrollStateChanged(state);
    }
  };

  @Override
  public void updateSupportProgram(List<Program> programs) {
    navigatorAdapter.setData(programs);
    pageAdapter.setData(programs);
    Log.d("TAG", "updateSupportProgram: programs" + programs.toString());
  }

  @Override
  public void loadProgramsError(Throwable e) {

  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.REQUISITION_SCREEN;
  }

  @Override
  protected int getThemeRes() {
    return R.style.AppTheme_PURPLE;
  }

  @Override
  public void injectPresenter() {
    super.injectPresenter();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // init viewpager
    pageAdapter = new RequisitionPageAdapter(this);
    requisitionViewpager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
    requisitionViewpager.setAdapter(pageAdapter);

    // init navigator
    navigatorAdapter = new RequisitionNavigatorAdapter(requisitionViewpager);
    final CommonNavigator commonNavigator = new CommonNavigator(this);
    commonNavigator.setAdjustMode(true);
    commonNavigator.setAdapter(navigatorAdapter);
    magicIndicator.setNavigator(commonNavigator);
    requisitionViewpager.registerOnPageChangeCallback(pageChangeCallback);

    // load data
    requisitionPresenter.getSupportPrograms();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    requisitionViewpager.unregisterOnPageChangeCallback(pageChangeCallback);
  }
}