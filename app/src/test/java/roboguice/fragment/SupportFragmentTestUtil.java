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

package roboguice.fragment;

import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import org.openlmis.core.view.activity.BaseActivity;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import roboguice.actiivty.FragmentControllerActivity;

public class SupportFragmentTestUtil {

  public static void startFragment(Fragment fragment) {
    buildFragmentManager(FragmentControllerActivity.class)
        .beginTransaction().add(fragment, null).commit();
    shadowMainLooper().idleIfPaused();
  }

  public static void startFragment(Fragment fragment, Class<? extends FragmentActivity> activityClass) {
    buildFragmentManager(activityClass)
        .beginTransaction().add(fragment, null).commit();
    shadowMainLooper().idleIfPaused();
  }

  public static void startVisibleFragment(Fragment fragment) {
    buildFragmentManager(BaseActivity.class)
        .beginTransaction().add(1, fragment, null).commit();
    shadowMainLooper().idleIfPaused();
  }

  public static void startVisibleFragment(Fragment fragment,
      Class<? extends FragmentActivity> activityClass, int containerViewId) {
    buildFragmentManager(activityClass)
        .beginTransaction().add(containerViewId, fragment, null).commit();
    shadowMainLooper().idleIfPaused();
  }

  private static FragmentManager buildFragmentManager(Class<? extends FragmentActivity> activityClass) {
    ActivityController<? extends FragmentActivity> activityController = Robolectric.buildActivity(activityClass, null);
    FragmentActivity activity = activityController.setup().get();
    return activity.getSupportFragmentManager();
  }
}
