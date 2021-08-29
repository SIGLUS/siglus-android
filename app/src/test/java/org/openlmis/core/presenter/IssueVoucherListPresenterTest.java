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
import static org.mockito.Matchers.anyByte;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.builder.PodBuilder;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.presenter.IssueVoucherListPresenter.IssueVoucherListView;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class IssueVoucherListPresenterTest {

  private IssueVoucherListPresenter presenter;

  private IssueVoucherListView mockView;

  @Inject
  private PodRepository mockPodRepository;

  @Inject
  private ProgramRepository mockProgramRepository;

  @Inject
  private SyncErrorsRepository mockSyncErrorsRepository;

  @Before
  public void setup() {
    mockView = mock(IssueVoucherListView.class);
    mockPodRepository = mock(PodRepository.class);
    mockProgramRepository = mock(ProgramRepository.class);
    mockSyncErrorsRepository = mock(SyncErrorsRepository.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(PodRepository.class).toInstance(mockPodRepository);
        bind(ProgramRepository.class).toInstance(mockProgramRepository);
        bind(SyncErrorsRepository.class).toInstance(mockSyncErrorsRepository);
      }
    });
    presenter = RoboGuice.getInjector(LMISApp.getContext()).getInstance(IssueVoucherListPresenter.class);
    presenter.attachView(mockView);
  }

  @Test
  public void shouldCorrectLoadData() throws Exception {
    // given
    Program mockProgram = mock(Program.class);
    List<Pod> pods = Collections.singletonList(PodBuilder.generatePod());
    when(mockPodRepository.queryPodsByStatus(any())).thenReturn(pods);
    when(mockProgramRepository.queryByCode(any())).thenReturn(mockProgram);
    when(mockSyncErrorsRepository.getBySyncTypeAndObjectId(any(), anyByte())).thenReturn(null);
    TestSubscriber<Object> testSubscriber = new TestSubscriber<>(presenter.viewModelsSubscribe);
    presenter.viewModelsSubscribe = testSubscriber;

    // when
    presenter.loadData();
    testSubscriber.awaitTerminalEvent();

    // then
    verify(mockView, times(1)).loading();
    verify(mockView, times(1)).loaded();
    verify(mockView, times(1)).onRefreshList();
    Assert.assertEquals(1, presenter.getViewModels().size());
  }

  @Test
  public void shouldCorrectSetErrorWhenLoadData() throws Exception {
    // given
    Program mockProgram = mock(Program.class);
    List<Pod> pods = Collections.singletonList(PodBuilder.generatePod());
    when(mockPodRepository.queryPodsByStatus(any())).thenReturn(pods);
    when(mockProgramRepository.queryByCode(any())).thenReturn(mockProgram);
    SyncError syncError = new SyncError("message", SyncType.POD, 1);
    when(mockSyncErrorsRepository.getBySyncTypeAndObjectId(any(), anyByte()))
        .thenReturn(Collections.singletonList(syncError));
    TestSubscriber<Object> testSubscriber = new TestSubscriber<>(presenter.viewModelsSubscribe);
    presenter.viewModelsSubscribe = testSubscriber;

    // when
    presenter.loadData();
    testSubscriber.awaitTerminalEvent();

    // then
    verify(mockView, times(1)).loading();
    verify(mockView, times(1)).loaded();
    verify(mockView, times(1)).onRefreshList();
    Assert.assertEquals(1, presenter.getViewModels().size());
    Assert.assertEquals(syncError, presenter.getViewModels().get(0).getSyncError());
  }

  @Test
  public void shouldCallLoadDataFailedAfterThrow() throws Exception {
    // given
    Mockito.doThrow(LMISException.class).when(mockPodRepository).queryPodsByStatus(any());
    TestSubscriber<Object> testSubscriber = new TestSubscriber<>(presenter.viewModelsSubscribe);
    presenter.viewModelsSubscribe = testSubscriber;

    // when
    presenter.loadData();
    testSubscriber.awaitTerminalEvent();

    // then
    verify(mockView, times(1)).loading();
    verify(mockView, times(1)).loaded();
    verify(mockView, times(1)).onLoadDataFailed(any(LMISException.class));
  }

  @Test
  public void shouldCorrectDeleteIssueVoucher() {
    // given
    TestSubscriber<Object> testSubscriber = new TestSubscriber<>(presenter.viewModelsSubscribe);
    presenter.deleteIssueVoucherSubscribe = testSubscriber;

    // when
    presenter.deleteIssueVoucher("");
    testSubscriber.awaitTerminalEvent();

    // then
    verify(mockView, times(1)).loaded();
    verify(mockView, times(1)).onRefreshList();
  }
}