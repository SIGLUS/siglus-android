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

package org.openlmis.core.service;

import android.content.Intent;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.network.InternetCheck;
import org.openlmis.core.receiver.NetworkChangeReceiver;
import org.openlmis.core.service.mocks.InternetCheckMockForNetworkChangeReceiver;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.mockito.Mockito.mock;

@RunWith(LMISTestRunner.class)
public class NetworkChangeReceiverTest {

    NetworkChangeReceiver listener;
    Intent intent;
    SyncService syncService;
    private InternetCheck internetCheck;


    @Before
    public void setup() throws LMISException {
        intent = mock(Intent.class);
        syncService = mock(SyncService.class);
    }

    @Test
    public void shouldKickOffSyncServiceWhenInternetConnectionIsAvailable() {
        internetCheck = new InternetCheckMockForNetworkChangeReceiver(true, syncService);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new NetworkChangeReceiverTest.MyTestModule());
        listener = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(NetworkChangeReceiver.class);

        listener.onReceive(RuntimeEnvironment.application, intent);
    }

    @Test
    public void shouldShutdownSyncServiceWhenInternetConnectionIsNotAvailable() {
        internetCheck = new InternetCheckMockForNetworkChangeReceiver(false, syncService);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new NetworkChangeReceiverTest.MyTestModule());
        listener = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(NetworkChangeReceiver.class);

        listener.onReceive(RuntimeEnvironment.application, intent);
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(SyncService.class).toInstance(syncService);
            bind(InternetCheck.class).toInstance(internetCheck);
        }
    }
}
