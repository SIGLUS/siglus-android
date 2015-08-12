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

import com.google.inject.Binder;
import com.google.inject.Module;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.response.RequisitionResponse;
import org.robolectric.Robolectric;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class SyncManagerTest {

    SyncManager syncManager;
    RnrFormRepository rnrFormRepository;
    LMISRestApi lmisRestApi;

    @Before
    public void setup() throws LMISException {
        rnrFormRepository = mock(RnrFormRepository.class);
        lmisRestApi = mock(LMISRestApi.class);

        RoboGuice.overrideApplicationInjector(Robolectric.application, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(RnrFormRepository.class).toInstance(rnrFormRepository);
            }
        });

        syncManager = RoboGuice.getInjector(Robolectric.application).getInstance(SyncManager.class);
        syncManager.lmisRestApi = lmisRestApi;
    }

    @Test
    public void shouldSubmitAllUnsyncedRequisitions() throws LMISException, SQLException {
        List<RnRForm> unSyncedList = new ArrayList<>();
        for (int i=0;i<10;i++){
            RnRForm form = new RnRForm();
            unSyncedList.add(form);
        }

        when(rnrFormRepository.listUnSynced()).thenReturn(unSyncedList);

        RequisitionResponse response =  new RequisitionResponse();
        response.setRequisitionId("1");
        when(lmisRestApi.submitRequisition(any(RnRForm.class))).thenReturn(response);

        syncManager.syncRnr();
        verify(lmisRestApi, times(10)).submitRequisition(any(RnRForm.class));
        verify(rnrFormRepository, times(10)).save(any(RnRForm.class));

    }

}
