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


import android.support.annotation.NonNull;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockCardBuilder;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.RequisitionResponse;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class SyncManagerTest {

    SyncManager syncManager;
    RnrFormRepository rnrFormRepository;
    LMISRestApi lmisRestApi;
    StockRepository stockRepository;

    @Before
    public void setup() throws LMISException {
        rnrFormRepository = mock(RnrFormRepository.class);
        lmisRestApi = mock(LMISRestApi.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(RnrFormRepository.class).toInstance(rnrFormRepository);
            }
        });

        syncManager = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SyncManager.class);
        syncManager.lmisRestApi = lmisRestApi;

        stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockRepository.class);

        User user = new User("user", "123");
        user.setFacilityCode("FC1");
        user.setFacilityId("123");
        UserInfoMgr.getInstance().setUser(user);
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


    @Test
    public void shouldPushUnSyncedStockMovementData() throws LMISException, SQLException {
        StockCard stockCard = createTestStockCardData();

        doNothing().when(lmisRestApi).pushStockMovementData(anyString(), anyList());
        syncManager.syncStockCards();
        stockRepository.refresh(stockCard);
        List<StockMovementItem> items = newArrayList(stockCard.getStockMovementItems());

        assertThat(items.size(), is(2));
        assertThat(items.get(0).isSynced(), is(true));
        assertThat(items.get(1).isSynced(), is(true));
    }

    @Test
    public void shouldNotMarkAsSyncedWhenStockMovementSyncFailed() throws LMISException{
        StockCard stockCard = createTestStockCardData();

        doThrow(new RuntimeException("Sync Failed")).when(lmisRestApi).pushStockMovementData(anyString(), anyList());

        syncManager.syncStockCards();
        stockRepository.refresh(stockCard);
        List<StockMovementItem> items = newArrayList(stockCard.getStockMovementItems());

        assertThat(items.size(), is(2));
        assertThat(items.get(0).isSynced(), is(false));
        assertThat(items.get(1).isSynced(), is(false));
    }

    @NonNull
    private StockCard createTestStockCardData() throws LMISException {
        ProductRepository productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);
        StockCard stockCard = StockCardBuilder.buildStockCardWithOneMovement(stockRepository);

        Product product = new Product();
        product.setCode("PD1");
        productRepository.create(product);

        stockCard.setProduct(product);
        stockRepository.update(stockCard);

        //ready to sync
        StockMovementItem item = new StockMovementItem();
        item.setMovementQuantity(100L);
        item.setStockOnHand(-1);
        item.setMovementDate(DateUtil.today());
        item.setMovementType(StockMovementItem.MovementType.RECEIVE);
        item.setSynced(false);

        stockRepository.addStockMovementAndUpdateStockCard(stockCard, item);
        stockRepository.refresh(stockCard);
        return stockCard;
    }
}
