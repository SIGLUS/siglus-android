package org.openlmis.core.presenter;

import com.google.inject.Inject;
import java.util.Date;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class IssueVoucherReportPresenter extends BaseReportPresenter {
  @Inject
  PodRepository podRepository;

  IssueVoucherView issueVoucherView;

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
