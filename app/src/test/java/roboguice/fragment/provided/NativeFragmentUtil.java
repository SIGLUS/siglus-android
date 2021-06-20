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

package roboguice.fragment.provided;

import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import roboguice.config.DefaultRoboModule;
import roboguice.fragment.FragmentUtil.f;
import roboguice.inject.ContextSingleton;

/**
 * see {@link DefaultRoboModule#bindDynamicBindings()}.
 * single run test is different from ./gradlew test.
 * avoid jetifier gradle replace support package to androidx.* package.
 */
public class NativeFragmentUtil implements f<Fragment, FragmentManager> {

  public NativeFragmentUtil() throws ClassNotFoundException {
    Class.forName(Fragment.class.getName());
    Class.forName(FragmentManager.class.getName());
  }

  @Override
  public View getView(Fragment frag) {
    return frag.getView();
  }

  @Override
  public Fragment findFragmentById(FragmentManager fm, int id) {
    return fm.findFragmentById(id);
  }

  @Override
  public Fragment findFragmentByTag(FragmentManager fm, String tag) {
    return fm.findFragmentByTag(tag);
  }

  @Override
  public Class<Fragment> fragmentType() {
    return Fragment.class;
  }

  @Override
  public Class<FragmentManager> fragmentManagerType() {
    return FragmentManager.class;
  }

  @SuppressWarnings("rawtypes") //not technically a Class<Provider<FragmentManager>>
  @Override
  public Class fragmentManagerProviderType() {
    return FragmentManagerProvider.class;
  }

  @ContextSingleton
  public static class FragmentManagerProvider implements Provider<FragmentManager> {

    @Inject
    protected FragmentActivity activity;

    @Override
    public FragmentManager get() {
      return activity.getSupportFragmentManager();
    }
  }
}
