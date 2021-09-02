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

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;
import lombok.Getter;
import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.view.fragment.IssueVoucherListFragment;
import org.openlmis.core.view.widget.FillPageIndicator;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_issue_voucher_list)
public class IssueVoucherListActivity extends BaseActivity {

  protected static final int[] TITLE_RES = {R.string.label_issue_voucher, R.string.label_pod};

  @Getter
  @InjectView(R.id.mi_issue_voucher)
  private MagicIndicator magicIndicator;

  @Getter
  @InjectView(R.id.vp_issue_voucher)
  private ViewPager2 viewPager;

  private final OnPageChangeCallback pageChangeCallback = new OnPageChangeCallback() {
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
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.menu_issue_voucher_list, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (R.id.action_create_issue_voucher != item.getItemId()) {
      return super.onOptionsItemSelected(item);
    }
    Intent intent = new Intent(LMISApp.getContext(), IssueVoucherInputOrderNumberActivity.class);
    startActivity(intent);
    return true;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    IssueVoucherListPageAdapter pageAdapter = new IssueVoucherListPageAdapter(this);
    viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
    viewPager.setAdapter(pageAdapter);

    // init navigator
    IssueVoucherListNavigatorAdapter navigatorAdapter = new IssueVoucherListNavigatorAdapter();
    CommonNavigator commonNavigator = new CommonNavigator(this);
    commonNavigator.setAdjustMode(true);
    commonNavigator.setAdapter(navigatorAdapter);
    magicIndicator.setNavigator(commonNavigator);

    viewPager.registerOnPageChangeCallback(pageChangeCallback);
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.ISSUE_VOUCHER_AND_POD;
  }

  @Override
  protected int getThemeRes() {
    return R.style.AppTheme_AMBER;
  }

  private static class IssueVoucherListPageAdapter extends FragmentStateAdapter {

    public IssueVoucherListPageAdapter(@NonNull FragmentActivity fragmentActivity) {
      super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
      return IssueVoucherListFragment.newInstance(position == 0);
    }

    @Override
    public int getItemCount() {
      return TITLE_RES.length;
    }
  }

  private class IssueVoucherListNavigatorAdapter extends CommonNavigatorAdapter {

    @Override
    public int getCount() {
      return TITLE_RES.length;
    }

    @Override
    public IPagerTitleView getTitleView(Context context, int index) {
      ColorTransitionPagerTitleView titleView = new ColorTransitionPagerTitleView(context);
      titleView.setSingleLine(false);
      titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen.px_20));
      titleView.setNormalColor(Color.BLACK);
      titleView.setSelectedColor(Color.BLACK);
      titleView.setTypeface(Typeface.DEFAULT_BOLD);
      titleView.setText(context.getString(TITLE_RES[index]));
      titleView.setWidth(context.getResources().getDimensionPixelSize(R.dimen.px_240));
      titleView.setOnClickListener(view -> viewPager.setCurrentItem(index));
      return titleView;
    }

    @Override
    public IPagerIndicator getIndicator(Context context) {
      FillPageIndicator indicator = new FillPageIndicator(context);
      indicator.setSelectedBackgroundColor(ContextCompat.getColor(context, R.color.color_80ffa000));
      indicator.setLineColors(ContextCompat.getColor(context, R.color.color_amber_dark));
      return indicator;
    }
  }
}