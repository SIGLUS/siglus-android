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

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;
import java.util.List;
import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.presenter.ReportListPresenter;
import org.openlmis.core.presenter.ReportListPresenter.ReportListView;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.ReportListNavigatorAdapter;
import org.openlmis.core.view.adapter.ReportListPageAdapter;
import org.openlmis.core.view.fragment.ReportListFragment;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;

@ContentView(R.layout.activity_report_list)
public class ReportListActivity extends BaseActivity implements ReportListView {

  @InjectView(R.id.mi_report_type)
  private MagicIndicator magicIndicator;

  @InjectView(R.id.vp_requisition)
  private ViewPager2 reportListViewpager;

  @InjectPresenter(ReportListPresenter.class)
  private ReportListPresenter reportListPresenter;

  ReportListNavigatorAdapter navigatorAdapter;

  ReportListPageAdapter pageAdapter;

  Menu emergencyMenu;

  private final ActivityResultLauncher<Intent> toSelectEmergencyProductsLauncher = registerForActivityResult(
      new StartActivityForResult(), result -> {
        ReportListFragment viaRequisitionFragment =
            (ReportListFragment) getSupportFragmentManager().findFragmentById(0);
        viaRequisitionFragment.getCreateRequisitionCallback().onActivityResult(result);
      });

  private final OnPageChangeCallback pageChangeCallback = new OnPageChangeCallback() {
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
      setMenuVisible(position);
      magicIndicator.onPageSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
      magicIndicator.onPageScrollStateChanged(state);
    }
  };

  @Override
  public void updateSupportReportTypes(List<ReportTypeForm> reportTypeForms) {
    navigatorAdapter.setData(reportTypeForms);
    pageAdapter.setData(reportTypeForms);
  }

  @Override
  public void loadReportTypesError(Throwable e) {
    ToastUtil.show(e.getMessage());
    finish();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    emergencyMenu = menu;
    getMenuInflater().inflate(R.menu.menu_rnr_list, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    if (item.getItemId() == R.id.action_create_emergency_rnr) {
      checkAndGotoEmergencyPage();
      return true;
    }
    return false;
  }

  public void setMenuVisible(int position) {
    if (emergencyMenu != null) {
      emergencyMenu.findItem(R.id.action_create_emergency_rnr).setVisible(position == 0);
    }
  }

  protected void checkAndGotoEmergencyPage() {
    if (!reportListPresenter.isHasVCReportType()) {
      ToastUtil.show(getString(R.string.msg_cannot_create_emergency_requisition));
      return;
    }
    int dayOfMonth = new DateTime(LMISApp.getInstance().getCurrentTimeMillis()).getDayOfMonth();
    if (dayOfMonth < Period.EMERGENCY_BEGIN_DAY || dayOfMonth > Period.EMERGENCY_END_DAY) {
      ToastUtil.show(R.string.msg_create_emergency_date_invalid);
      return;
    }

    loading();
    reportListPresenter.hasMissedViaProgramPeriod().subscribe(new Subscriber<Boolean>() {
      @Override
      public void onCompleted() {
      }

      @Override
      public void onError(Throwable e) {
        handleError(e);
      }

      @Override
      public void onNext(Boolean hasMissed) {
        if (Boolean.TRUE.equals(hasMissed)) {
          ToastUtil.show(R.string.msg_create_emergency_has_missed);
          loaded();
        } else {
          checkViaProgramEmergencyRequisitionsCount();
        }
      }
    });
  }

  private void checkViaProgramEmergencyRequisitionsCount() {
    reportListPresenter.hasMoreThan2ViaProgramEmergencyRequisition().subscribe(new Subscriber<Boolean>() {

      @Override
      public void onCompleted() {
        loaded();
      }

      @Override
      public void onError(Throwable e) {
        handleError(e);
      }

      @Override
      public void onNext(Boolean isMoreThan2EmergencyRequisitions) {
        if (Boolean.TRUE.equals(isMoreThan2EmergencyRequisitions)) {
          ToastUtil.show(R.string.msg_create_emergency_has_over_limit);
        } else {
          launchSelectEmergencyProductsActivity();
        }
      }
    });
  }

  private void handleError(Throwable e) {
    loaded();
    ToastUtil.show(e.getMessage());
  }

  private void launchSelectEmergencyProductsActivity() {
    reportListViewpager.setCurrentItem(0);
    toSelectEmergencyProductsLauncher.launch(
        SelectEmergencyProductsActivity.getIntentToMe(ReportListActivity.this));
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
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // init viewpager
    pageAdapter = new ReportListPageAdapter(this);
    reportListViewpager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
    reportListViewpager.setAdapter(pageAdapter);

    // init navigator
    navigatorAdapter = new ReportListNavigatorAdapter(reportListViewpager);
    final CommonNavigator commonNavigator = new CommonNavigator(this);
    commonNavigator.setAdjustMode(true);
    commonNavigator.setAdapter(navigatorAdapter);
    magicIndicator.setNavigator(commonNavigator);
    reportListViewpager.registerOnPageChangeCallback(pageChangeCallback);

    // load data
    reportListPresenter.getSupportReportTypes();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    reportListViewpager.unregisterOnPageChangeCallback(pageChangeCallback);
  }
}