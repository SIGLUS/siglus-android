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

package org.openlmis.core;

import java.lang.reflect.Method;
import org.junit.runners.model.InitializationError;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.training.TrainingSqliteOpenHelper;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestLifecycle;
import org.robolectric.annotation.Config;
import roboguice.RoboGuice;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.schedulers.Schedulers;

public class LMISTestRunner extends RobolectricTestRunner {

  /**
   * Creates a runner to run {@code testClass}. Looks in your working directory for your
   * AndroidManifest.xml file and res directory by default. Use the {@link Config} annotation to
   * configure.
   *
   * @param testClass the test class to be run
   * @throws InitializationError if junit says so
   */
  public LMISTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  protected Config buildGlobalConfig() {
    return new Config.Builder()
        .setApplication(LMISTestApp.class)
        .setSdk(19)
        .build();
  }

  @Override
  protected Class<? extends TestLifecycle> getTestLifecycleClass() {
    return MyTestLifeCycle.class;
  }

  public static class MyTestLifeCycle extends DefaultTestLifecycle {

    @Override
    public void beforeTest(Method method) {
      super.beforeTest(method);
      RxAndroidPlugins.getInstance().reset();
      RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
        @Override
        public Scheduler getMainThreadScheduler() {
          return Schedulers.immediate();
        }
      });
    }

    @Override
    public void afterTest(Method method) {
      super.afterTest(method);
      LmisSqliteOpenHelper.getInstance(RuntimeEnvironment.application).close();
      TrainingSqliteOpenHelper.getInstance(RuntimeEnvironment.application).close();
      RoboGuice.Util.reset();
    }
  }
}
