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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.builder.PodBuilder;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.presenter.IssueVoucherReportPresenter.IssueVoucherView;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.IssueVoucherReportProductViewModel;
import org.openlmis.core.view.viewmodel.IssueVoucherReportSummaryViewModel;
import org.openlmis.core.view.viewmodel.IssueVoucherReportViewModel;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class IssueVoucherReportPresenterTest {
  @Mock
  PodRepository podRepository;

  @Mock
  StockRepository stockRepository;

  @Mock
  ProductRepository productRepository;

  @Mock
  ProgramRepository programRepository;

  @Mock
  IssueVoucherView issueVoucherView;

  @InjectMocks
  IssueVoucherReportPresenter presenter;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldCorrectLoadViewModel() throws Exception {
    // given
    Program program = buildProgram();
    when(programRepository.queryByCode(any())).thenReturn(program);

    // when
    presenter.loadViewModelByPod(PodBuilder.generatePod(), false);

    //then
    assertEquals(program.getProgramName(), presenter.getIssueVoucherReportViewModel().getProgram().getProgramName());
    assertEquals(2, presenter.getIssueVoucherReportViewModel().getViewModels().size());
    IssueVoucherReportSummaryViewModel viewModel = (IssueVoucherReportSummaryViewModel) presenter
        .getIssueVoucherReportViewModel().getViewModels().get(1);
    assertEquals("20.00", viewModel.getTotal().toString());
    assertEquals(1, presenter.getIssueVoucherReportViewModel().getProductViewModels().size());
    verify(issueVoucherView, times(1)).loaded();
  }

  @NonNull
  private Program buildProgram() {
    String programName = "Program Name";
    Program program = new Program();
    program.setId(123);
    program.setProgramCode(Constants.MMIA_PROGRAM_CODE);
    program.setProgramName(programName);
    return program;
  }

  @Test
  public void shouldDeleteDraftForLocal() throws Exception {
    // given
    Pod pod = PodBuilder.generatePod();
    pod.setLocal(true);
    presenter.pod = pod;

    // when
    presenter.deleteIssueVoucher();

    // then
    verify(podRepository,times(0)).deleteByOrderCode(pod.getOrderCode());
  }

  @Test
  public void shouldSearchPod() throws Exception {
    // given
    long podId = 122;

    // when
    TestSubscriber<Pod> subscriber = new TestSubscriber<>();
    Observable<Pod> loadObservable = presenter.getRnrFormObservable(podId);
    loadObservable.subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    // then
    verify(podRepository,times(1)).queryById(any(long.class));
  }

  @Test
  public void shouldloadAddPodItemForAddButton() throws Exception {
    // given
    Pod pod = PodBuilder.generatePod();
    pod.setLocal(false);
    presenter.pod = pod;

    // when
    presenter.loadViewModelByPod(pod, true);

    // then
    assertEquals(2, presenter.getPod().getPodProductItemsWrapper().size());
  }

  @Test
  public void shouldDeleteDraftForRemote() throws Exception {
    // given
    Pod pod = PodBuilder.generatePod();
    pod.setLocal(false);
    presenter.pod = pod;
    presenter.issueVoucherReportViewModel = new IssueVoucherReportViewModel(pod);

    // when
    presenter.deleteIssueVoucher();

    // then
    verify(podRepository,times(1)).createOrUpdateWithItems(any(Pod.class));
  }

  @Test
  public void shouldDraftForSave() throws Exception {
    // given
    Pod pod = PodBuilder.generatePod();
    pod.setLocal(false);
    presenter.pod = pod;
    presenter.issueVoucherReportViewModel = new IssueVoucherReportViewModel(pod);

    // when
    TestSubscriber<Void> subscriber = new TestSubscriber<>();
    Observable<Void> saveObservable  = presenter.getSaveFormObservable();
    saveObservable.subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    // then
    verify(podRepository,times(1)).createOrUpdateWithItems(any(Pod.class));
  }

  @Test
  public void shouldDraftForComplete() throws Exception {
    // given
    Pod pod = PodBuilder.generatePod();
    pod.setLocal(false);
    pod.getPodProductItemsWrapper().get(0).getPodProductLotItemsWrapper().get(0).setAcceptedQuantity(Long.valueOf(1));
    presenter.pod = pod;
    presenter.issueVoucherReportViewModel = new IssueVoucherReportViewModel(pod);

    // when
    TestSubscriber<Void> subscriber = new TestSubscriber<>();
    Observable<Void> completeObservable  = presenter
        .getCompleteFormObservable("received");
    completeObservable.subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    // then
    verify(podRepository,times(1)).createOrUpdateWithItems(any(Pod.class));
    verify(productRepository,times(1)).updateProductInArchived(any());
    verify(stockRepository,times(1)).addStockMovementsAndUpdateStockCards(any(), any());
  }

  @Test
  public void addNewLot_shouldAddNewLotWithSpecificRejectionReason() {
    // given
    IssueVoucherReportProductViewModel mockedProductViewModel =
        mock(IssueVoucherReportProductViewModel.class);
    String lotNumber = "lotNumber";
    String expireDateString = "2024-05-02";
    Date expirationDate = DateUtil.parseString(expireDateString, DateUtil.DB_DATE_FORMAT);
    OrderStatus orderStatus = OrderStatus.SHIPPED;
    long shippedQuantity = 0L;
    String rejectedReasonCode = "LOT_NOT_SPECIFIED";

    doNothing().when(mockedProductViewModel).addNewLot(
        lotNumber,
        expirationDate,
        rejectedReasonCode,
        orderStatus,
        shippedQuantity
    );
    // when
    presenter.addNewLot(mockedProductViewModel, lotNumber, expireDateString);
    // then
    verify(mockedProductViewModel).addNewLot(
        lotNumber,
        expirationDate,
        rejectedReasonCode,
        orderStatus,
        shippedQuantity
    );
  }
}
