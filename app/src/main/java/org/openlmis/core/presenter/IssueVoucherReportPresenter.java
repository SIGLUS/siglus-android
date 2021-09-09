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

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.IssueVoucherReportViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.inject.ContextSingleton;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

@ContextSingleton
public class IssueVoucherReportPresenter extends BaseReportPresenter {

  @Inject
  PodRepository podRepository;

  @Inject
  private ProgramRepository programRepository;

  public String reasonCode;

  IssueVoucherView issueVoucherView;
  @Getter
  IssueVoucherReportViewModel issueVoucherReportViewModel;

  public Pod pod;

  public void loadData(long podId) {
    Subscription subscription = getRnrFormObservable(podId)
        .subscribe(loadDataOnNextAction, loadDataOnErrorAction);
    subscriptions.add(subscription);
  }

  protected Observable<Pod> getRnrFormObservable(final long formId) {
    return Observable.create((Observable.OnSubscribe<Pod>) subscriber -> {
      try {
        pod = podRepository.queryById(formId);
        subscriber.onNext(pod);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "VIARequisitionPresenter.getRnrFormObservable").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  @Override
  public void deleteDraft() {

  }

  @Override
  public boolean isDraft() {
    return false;
  }

  @Override
  protected void addSignature(String signature) {

  }

  @Override
  public void attachView(BaseView v) throws ViewNotMatchException {
    issueVoucherView = (IssueVoucherView) v;
  }

  public void loadViewModelByPod(Pod podContent, boolean isBackToCurrentPage) {
    if (isBackToCurrentPage) {
      List<PodProductItem> existedProducts = new ArrayList<>(pod.getPodProductItemsWrapper());
      existedProducts.addAll(podContent.getPodProductItemsWrapper());
      pod.setPodProductItemsWrapper(existedProducts);
    } else {
      pod = podContent;
    }
    try {
      Program program = programRepository.queryByCode(pod.getRequisitionProgramCode());
      issueVoucherReportViewModel = new IssueVoucherReportViewModel(pod);
      issueVoucherReportViewModel.setProgram(program);
      issueVoucherView.loaded();
      issueVoucherView.refreshIssueVoucherForm(pod);
    } catch (LMISException e) {
      new LMISException(e, "IssueVoucherReport.getProgram").reportToFabric();
    }
  }

  protected Action1<Pod> loadDataOnNextAction = podContent -> {
    loadViewModelByPod(podContent, false);
  };

  public void deleteIssueVoucher() {
    try {
      podRepository.deleteByOrderCode(pod.getOrderCode());
    } catch (Exception e) {
      new LMISException(e, "deleteIssueVoucher").reportToFabric();
    }
  }

  public Observable<Void> getSaveFormObservable() {
    return Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
      try {
        pod.setDraft(true);
        if (reasonCode != null) {
          pod.setStockManagementReason(reasonCode);
        }
        setPodItems();
        podRepository.createOrUpdateWithItems(pod);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "Pod.getSaveFormObservable").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public Observable<Void> getCompleteFormObservable(String deliveredBy, String receivedBy) {
    return Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
      try {
        pod.setDraft(true);
        pod.setReceivedBy(receivedBy);
        pod.setDeliveredBy(deliveredBy);
        pod.setReceivedDate(DateUtil.getCurrentDate());
        if (reasonCode != null) {
          pod.setStockManagementReason(reasonCode);
        }
        setPodItems();
        podRepository.createOrUpdateWithItems(pod);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "Pod.getCompleteFormObservable").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public void setPodItems() {
    pod.setPodProductItemsWrapper(FluentIterable.from(
        issueVoucherReportViewModel.getProductViewModels())
        .transform(productViewModel -> productViewModel.convertToPodProductModel()).toList());
  }

  public List<String> getAddedProductCodeList() {
    return FluentIterable.from(issueVoucherReportViewModel.getProductViewModels())
        .transform(viewModel -> Objects.requireNonNull(viewModel).getProduct().getCode())
        .toList();
  }

  protected Action1<Throwable> loadDataOnErrorAction = throwable -> {
    issueVoucherView.loaded();
    ToastUtil.show(throwable.getMessage());
  };

  public interface IssueVoucherView extends BaseView {

    void refreshIssueVoucherForm(Pod pod);
  }
}
