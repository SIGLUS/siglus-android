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
import lombok.Getter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.IssueVoucherReportViewModel;
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
  IssueVoucherView issueVoucherView;
  @Getter
  IssueVoucherReportViewModel issueVoucherReportViewModel;
  private Pod pod;

  public void loadData(long formId) {
    Subscription subscription = getRnrFormObservable(formId)
        .subscribe(loadDataOnNextAction, loadDataOnErrorAction);
    subscriptions.add(subscription);
  }

  protected Observable<Pod> getRnrFormObservable(final long formId) {
    return Observable.create((Observable.OnSubscribe<Pod>) subscriber -> {
      try {
        pod = podRepository.queryPod(formId);
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

  protected Action1<Pod> loadDataOnNextAction = podContent -> {
    pod = podContent;
    issueVoucherReportViewModel = new IssueVoucherReportViewModel(pod);
    issueVoucherView.loaded();
    issueVoucherView.refreshIssueVoucherForm(pod);
  };

  protected Action1<Throwable> loadDataOnErrorAction = throwable -> {
    issueVoucherView.loaded();
    ToastUtil.show(throwable.getMessage());
  };

  public interface IssueVoucherView extends BaseView {

    void refreshIssueVoucherForm(Pod pod);
  }
}
