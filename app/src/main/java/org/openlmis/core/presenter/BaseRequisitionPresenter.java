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

import android.content.Context;
import android.util.Log;
import com.google.inject.Inject;
import java.util.Date;
import lombok.Getter;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.googleanalytics.TrackerActions;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.network.InternetCheck;
import org.openlmis.core.network.InternetCheckListener;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.utils.TrackRnREventUtil;
import org.openlmis.core.view.BaseView;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public abstract class BaseRequisitionPresenter extends BaseReportPresenter {

  RnrFormRepository rnrFormRepository;

  @Inject
  Context context;

  @Inject
  StockService stockService;

  private BaseRequisitionView view;

  protected Date periodEndDate;

  @Getter
  protected RnRForm rnRForm;

  @Getter
  protected boolean isHistoryForm = false;

  @Inject
  InternetCheck internetCheck;

  protected BaseRequisitionPresenter() {
    rnrFormRepository = initRnrFormRepository();
  }

  protected abstract RnrFormRepository initRnrFormRepository();

  @Override
  public void attachView(BaseView baseView) throws ViewNotMatchException {
    if (baseView instanceof BaseRequisitionView) {
      this.view = (BaseRequisitionView) baseView;
    }
  }

  public abstract void loadData(final long formId, Date periodEndDate);

  protected Action1<RnRForm> loadDataOnNextAction = form -> {
    rnRForm = form;
    updateFormUI();
    loadAlertDialogIsFormStatusIsDraft();
    view.loaded();
  };

  protected Action1<Throwable> loadDataOnErrorAction = throwable -> {
    view.loaded();
    ToastUtil.show(throwable.getMessage());
  };

  public void loadAlertDialogIsFormStatusIsDraft() {
    if (rnRForm.isSubmitted()) {
      view.showMessageNotifyDialog();
    }
  }

  public RnRForm getRnrForm(long formId) throws LMISException {
    if (formId != 0) {
      isHistoryForm = true;
    }
    if (rnRForm != null) {
      return rnRForm;
    }
    //three branches: history, half completed draft, new draft
    if (isHistoryForm()) {
      return rnrFormRepository.queryRnRForm(formId);
    }
    RnRForm draftRequisition = rnrFormRepository.queryUnAuthorized();
    if (draftRequisition != null) {
      return draftRequisition;
    }
    return rnrFormRepository.initNormalRnrForm(periodEndDate);
  }

  public void submitRequisition() {
    view.loading();
    Subscription submitSubscription = createOrUpdateRnrForm()
        .subscribe(getSubmitRequisitionSubscriber());
    subscriptions.add(submitSubscription);
  }

  protected Subscriber<Void> getSubmitRequisitionSubscriber() {
    return new Subscriber<Void>() {
      @Override
      public void onCompleted() {
        // do nothing
      }

      @Override
      public void onError(Throwable e) {
        view.loaded();
        ToastUtil.show(e.getMessage());
      }

      @Override
      public void onNext(Void aVoid) {
        view.loaded();
        updateUIAfterSubmit();

        TrackRnREventUtil
            .trackRnRListEvent(TrackerActions.SUBMIT_RNR, rnRForm.getProgram().getProgramCode());
      }
    };
  }

  public void authoriseRequisition() {
    view.loading();
    Subscription authoriseSubscription = createOrUpdateRnrForm()
        .subscribe(getAuthoriseRequisitionSubscriber());
    subscriptions.add(authoriseSubscription);
  }

  protected Subscriber<Void> getAuthoriseRequisitionSubscriber() {
    return new Subscriber<Void>() {

      @Override
      public void onCompleted() {
        // do nothing
      }

      @Override
      public void onError(Throwable e) {
        view.loaded();
        ToastUtil.show(getCompleteErrorMessage());
      }

      @Override
      public void onNext(Void aVoid) {
        view.loaded();
        view.completeSuccess();
        Log.d("BaseReqPresenter", "Signature signed, requesting immediate sync");
        TrackRnREventUtil.trackRnRListEvent(TrackerActions.AUTHORISE_RNR, rnRForm.getProgram().getProgramCode());
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
          syncService.requestSyncImmediatelyByTask();
        } else {
          internetCheck.check(checkInternetListener());
        }
      }
    };
  }

  private InternetCheckListener checkInternetListener() {

    return result -> {
      if (result) {
        syncService.requestSyncImmediatelyByTask();
      } else {
        Log.d("Internet", "No hay conexion");
      }
    };
  }

  @SuppressWarnings("squid:S1905")
  protected Observable<Void> createOrUpdateRnrForm() {
    return Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
      try {
        rnrFormRepository.createOrUpdateWithItems(rnRForm);
        subscriber.onNext(null);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "BaseRequisitionPresenter,createOrUpdateRnrForm").reportToFabric();
        subscriber.onError(e);
      } finally {
        stockService.monthlyUpdateAvgMonthlyConsumption();
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  @Override
  public void deleteDraft() {
    if (!isHistoryForm()) {
      try {
        rnrFormRepository.removeRnrForm(rnRForm);
      } catch (LMISException e) {
        ToastUtil.show(context.getString(R.string.delete_rnr_form_failed_warning));
        new LMISException(e, "BaseRequisitionPresenter,deleteDraft").reportToFabric();
      }
    }
  }

  public void processSign(String signature) {
    addSignature(signature);
    if (rnRForm.isSubmitted()) {
      submitRequisition();
      view.showMessageNotifyDialog();
    } else {
      authoriseRequisition();
    }
  }

  public void addSignature(String signature) {
    rnRForm.addSignature(signature);
  }

  public Status getRnrFormStatus() {
    if (rnRForm != null) {
      return rnRForm.getStatus();
    } else {
      return Status.DRAFT;
    }
  }

  public boolean validateFormPeriod() {
    return rnrFormRepository.isPeriodUnique(rnRForm);
  }

  public abstract void updateUIAfterSubmit();

  protected abstract void updateFormUI();

  protected abstract Observable<RnRForm> getRnrFormObservable(long formId);

  protected abstract int getCompleteErrorMessage();

  public boolean isFormProductEditable() {
    return !isHistoryForm() && !(getRnRForm() != null && getRnRForm().isEmergency());
  }

  public boolean isDraft() {
    return getRnrFormStatus() == Status.DRAFT
        || getRnrFormStatus() == Status.DRAFT_MISSED;
  }

  public boolean isSubmit() {
    return getRnrFormStatus() == Status.SUBMITTED
        || getRnrFormStatus() == Status.SUBMITTED_MISSED;
  }

  public boolean isDraftOrDraftMissed() {
    return rnRForm.isDraft();
  }

  public interface BaseRequisitionView extends BaseView {

    void refreshRequisitionForm(RnRForm rnRForm);

    void completeSuccess();

    void showMessageNotifyDialog();
  }
}
