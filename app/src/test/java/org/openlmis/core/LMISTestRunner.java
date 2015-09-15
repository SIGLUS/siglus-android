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

import android.app.Application;

import org.junit.runners.model.InitializationError;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestLifecycle;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;

import java.lang.reflect.Method;
import java.util.Properties;

public class LMISTestRunner extends RobolectricTestRunner {

    /**
     * Creates a runner to run {@code testClass}. Looks in your working directory for your AndroidManifest.xml file
     * and res directory by default. Use the {@link Config} annotation to configure.
     *
     * @param testClass the test class to be run
     * @throws InitializationError if junit says so
     */
    public LMISTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);

        String buildVariant = (BuildConfig.FLAVOR.isEmpty() ? "" : BuildConfig.FLAVOR+ "/") + BuildConfig.BUILD_TYPE;
        String intermediatesPath = BuildConfig.class.getResource("").toString().replace("file:", "");
        intermediatesPath = intermediatesPath.substring(0, intermediatesPath.indexOf("/classes"));

        System.setProperty("android.package", "org.openlmis.core");
        System.setProperty("android.manifest", intermediatesPath + "/manifests/full/" + buildVariant + "/AndroidManifest.xml");
        System.setProperty("android.resources", intermediatesPath + "/res/" + buildVariant);
        System.setProperty("android.assets", intermediatesPath + "/assets/" + buildVariant);
    }


    protected Config.Implementation overwriteConfig(
            Config config, String key, String value) {
        Properties properties = new Properties();
        properties.setProperty(key, value);
        return new Config.Implementation(config,
                Config.Implementation.fromProperties(properties));
    }


    @Override
    protected int pickSdkVersion(Config config, AndroidManifest manifest) {
        config = overwriteConfig(config, "emulateSdk", "18");
        return super.pickSdkVersion(config, manifest);
    }


    @Override
    protected Class<? extends TestLifecycle> getTestLifecycleClass() {
        return MyTestLifeCycle.class;
    }

    public static class MyTestLifeCycle extends DefaultTestLifecycle {
        @Override
        public Application createApplication(Method method, AndroidManifest appManifest, Config config) {
            return new TestApplication();
        }

        @Override
        public void afterTest(Method method) {
            super.afterTest(method);
            LmisSqliteOpenHelper.getInstance(RuntimeEnvironment.application).close();
        }
    }

    public static class TestApplication extends LMISApp{
        @Override
        protected void setupFabric() {
        }
    }

}
