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

package org.openlmis.core.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.presenter.EditOrderNumberPresenter.EditOrderNumberView;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class EditOrderNumberPresenterTest {

  private EditOrderNumberPresenter presenter;

  private EditOrderNumberView mockView;

  @Inject
  private PodRepository mockPodRepository;

  @Before
  public void setup() {
    mockView = mock(EditOrderNumberView.class);
    mockPodRepository = mock(PodRepository.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(PodRepository.class).toInstance(mockPodRepository);
      }
    });
    presenter = RoboGuice.getInjector(LMISApp.getContext()).getInstance(EditOrderNumberPresenter.class);
    presenter.attachView(mockView);
  }

  @Test
  public void shouldCorrectGetData() {
    // given
    ArrayList<String> sameOrderCods = new ArrayList<>();
    sameOrderCods.add(FieldConstants.ORDER_CODE);
    Mockito.when(mockPodRepository.querySameProgramIssueVoucherByOrderCode(any())).thenReturn(sameOrderCods);
    TestSubscriber<Object> testSubscriber = new TestSubscriber<>(presenter.loadDataObserver);
    presenter.loadDataObserver = testSubscriber;

    // when
    presenter.loadData("");
    testSubscriber.awaitTerminalEvent();

    // then
    Mockito.verify(mockView, times(1)).loading();
    Mockito.verify(mockView, times(1)).loaded();
    Assert.assertEquals(1, presenter.getOrderNumbers().size());
    Assert.assertEquals(FieldConstants.ORDER_CODE, presenter.getOrderNumbers().get(0));
  }

  @Test
  public void shouldInvokeLoadDataFailedWhenGetDataError() {
    // given
    doThrow(LMISException.class).when(mockPodRepository).querySameProgramIssueVoucherByOrderCode(any());
    TestSubscriber<Object> testSubscriber = new TestSubscriber<>(presenter.loadDataObserver);
    presenter.loadDataObserver = testSubscriber;

    // when
    presenter.loadData("");
    testSubscriber.awaitTerminalEvent();

    // then
    Mockito.verify(mockView, times(1)).loading();
    Mockito.verify(mockView, times(1)).loaded();
    Mockito.verify(mockView, times(1)).loadDataFailed();
  }

  @Test
  public void testChangeOrderDataSuccess() {
    // given
    TestSubscriber<Object> testSubscriber = new TestSubscriber<>(presenter.changeOrderNumberObserver);
    presenter.changeOrderNumberObserver = testSubscriber;

    // when
    presenter.updateOrderNumber(StringUtils.EMPTY, StringUtils.EMPTY);
    testSubscriber.awaitTerminalEvent();

    // then
    Mockito.verify(mockView, times(1)).loading();
    Mockito.verify(mockView, times(1)).loaded();
    Mockito.verify(mockView, times(1)).updateOrderNumberSuccess();
  }

  @Test
  public void testChangeOrderDataFailed() throws LMISException {
    // given
    doThrow(LMISException.class).when(mockPodRepository).updateOrderCode(anyString(), anyString());
    TestSubscriber<Object> testSubscriber = new TestSubscriber<>(presenter.changeOrderNumberObserver);
    presenter.changeOrderNumberObserver = testSubscriber;

    // when
    presenter.updateOrderNumber(StringUtils.EMPTY, StringUtils.EMPTY);
    testSubscriber.awaitTerminalEvent();

    // then
    Mockito.verify(mockView, times(1)).loading();
    Mockito.verify(mockView, times(1)).loaded();
    Mockito.verify(mockView, times(1)).updateOrderNumberFailed();
  }
}