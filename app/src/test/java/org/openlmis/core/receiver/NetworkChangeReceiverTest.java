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

package org.openlmis.core.receiver;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import android.content.Intent;
import com.google.inject.AbstractModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.network.InternetCheckListener;
import org.openlmis.core.service.SyncService;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class NetworkChangeReceiverTest {

  NetworkChangeReceiver receiver;
  Intent intent;
  SyncService syncService;

  @Before
  public void setup() throws LMISException {
    intent = mock(Intent.class);
    syncService = mock(SyncService.class);
  }

  @Test
  public void shouldKickOffSyncServiceWhenInternetConnectionIsAvailable() {
    // given
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new NetworkChangeReceiverTest.MyTestModule());
    receiver = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(NetworkChangeReceiver.class);
    InternetCheckListener internetCheckListener = receiver.synchronizeListener(syncService);

    // when
    internetCheckListener.onResult(true);

    // then
    Mockito.verify(syncService, times(1)).kickOff();
  }

  @Test
  public void shouldShutdownSyncServiceWhenInternetConnectionIsNotAvailable() {
    // given
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new NetworkChangeReceiverTest.MyTestModule());
    receiver = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(NetworkChangeReceiver.class);
    InternetCheckListener internetCheckListener = receiver.synchronizeListener(syncService);

    // when
    internetCheckListener.onResult(false);

    // then
    Mockito.verify(syncService, times(1)).shutDown();
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(SyncService.class).toInstance(syncService);
    }
  }
}
