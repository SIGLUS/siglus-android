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

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class NetworkChangeListenerTest {

    NetworkChangeListener listener;
    Intent intent;

    SyncManager syncManager;


    @Before
    public void setup() throws LMISException {
        listener = new NetworkChangeListener();
        intent = mock(Intent.class);

        syncManager = mock(SyncManager.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(SyncManager.class).toInstance(syncManager);
            }
        });
    }

    @Test
    public void shouldKickOffSyncServiceWhenConnectionAvailable() {
        shadowOf(getConnectivityManager().getActiveNetworkInfo()).setConnectionStatus(true);
        listener.onReceive(RuntimeEnvironment.application, intent);
        verify(syncManager).kickOff();
    }

    @Test
    public void shouldShutdownSyncServiceWhenConnectionNotAvailable(){
        shadowOf(getConnectivityManager().getActiveNetworkInfo()).setConnectionStatus(false);
        listener.onReceive(RuntimeEnvironment.application, intent);
        verify(syncManager).shutDown();
    }


    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
}
