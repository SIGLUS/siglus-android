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

import android.util.Log;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.IssueVoucherListViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class IssueVoucherListPresenter extends Presenter {

  private IssueVoucherListView view;

  @Inject
  private PodRepository podRepository;

  @Inject
  private ProgramRepository programRepository;

  @Inject
  private SyncErrorsRepository syncErrorsRepository;

  @Setter
  private boolean isIssueVoucher;

  @Getter
  private final List<IssueVoucherListViewModel> viewModels = new ArrayList<>();

  Observer<Object> viewModelsSubscribe = new Observer<Object>() {
    @Override
    public void onCompleted() {
      Collections.sort(viewModels);
      view.loaded();
      view.onRefreshList();
    }

    @Override
    public void onError(Throwable e) {
      LMISException lmisException = new LMISException(e, "get data failed");
      lmisException.reportToFabric();
      view.loaded();
      view.onLoadDataFailed(lmisException);
    }

    @Override
    public void onNext(Object o) {
      // do nothing
    }
  };

  @Override
  public void attachView(BaseView v) {
    view = (IssueVoucherListView) v;
  }

  public void loadData() {
    view.loading();
    Subscription loadDataSubscribe = Observable.create(subscriber -> {
      try {
        refreshViewModels();
        subscriber.onNext(null);
        subscriber.onCompleted();
      } catch (Exception e) {
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(viewModelsSubscribe);
    subscriptions.add(loadDataSubscribe);
  }

  public void deleteIssueVoucher(String orderCode) {
    view.loading();
    Subscription subscribe = Observable.create(subscriber -> {
      try {
        podRepository.deleteByOrderCode(orderCode);
        refreshViewModels();
      } catch (Exception e) {
        new LMISException(e, "delete issue voucher failed").reportToFabric();
        // do nothing
      }
      subscriber.onNext(null);
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(o -> {
      view.loaded();
      view.onRefreshList();
    });
    subscriptions.add(subscribe);
  }

  private void refreshViewModels() throws LMISException {
    List<Pod> pods = podRepository.queryPodsByStatus(isIssueVoucher ? OrderStatus.SHIPPED : OrderStatus.RECEIVED);
    viewModels.clear();
    viewModels.addAll(FluentIterable.from(pods).transform(this::transformPodToViewModel).toList());
  }

  private IssueVoucherListViewModel transformPodToViewModel(Pod pod) {
    String programName = "";
    SyncError syncError = null;
    try {
      Program program = programRepository.queryByCode(pod.getRequisitionProgramCode());
      programName = program.getProgramName();
      List<SyncError> syncErrors = syncErrorsRepository.getBySyncTypeAndObjectId(SyncType.POD, pod.getId());
      if (CollectionUtils.isNotEmpty(syncErrors)) {
        syncError = syncErrors.get(0);
      }
    } catch (LMISException e) {
      Log.w("IVListPresenter", e);
      // do nothing
    }
    return IssueVoucherListViewModel.builder().pod(pod).programName(programName).syncError(syncError).build();
  }

  public interface IssueVoucherListView extends BaseView {

    void onRefreshList();

    void onLoadDataFailed(LMISException lmisException);
  }
}
