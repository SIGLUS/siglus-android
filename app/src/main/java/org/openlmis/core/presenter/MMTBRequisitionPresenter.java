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

import java.util.Date;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.MMTBRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.view.BaseView;
import roboguice.RoboGuice;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MMTBRequisitionPresenter extends BaseRequisitionPresenter {

  MMTBRequisitionView view;

  @Override
  protected RnrFormRepository initRnrFormRepository() {
    return RoboGuice.getInjector(LMISApp.getContext()).getInstance(MMTBRepository.class);
  }

  @Override
  public void attachView(BaseView baseView) throws ViewNotMatchException {
    if (baseView instanceof MMTBRequisitionView) {
      this.view = (MMTBRequisitionView) baseView;
    } else {
      throw new ViewNotMatchException(MMTBRequisitionView.class.getName());
    }
    super.attachView(baseView);
  }

  @Override
  public void loadData(long formId, Date periodEndDate) {
    this.periodEndDate = periodEndDate;
    view.loading();
    subscriptions.add(getRnrFormObservable(formId).subscribe(loadDataOnNextAction, loadDataOnErrorAction));
  }

  @Override
  public void updateUIAfterSubmit() {
    view.setProcessButtonName(context.getResources().getString(R.string.btn_complete));
  }

  @Override
  protected Observable<RnRForm> getRnrFormObservable(final long formId) {
    return Observable.create((Observable.OnSubscribe<RnRForm>) subscriber -> {
      try {
        rnRForm = getRnrForm(formId);
        subscriber.onNext(rnRForm);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "MMTBRequisitionPresenter.getRnrFormObservable").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  @Override
  public void updateFormUI() {
    if (rnRForm != null) {
      view.refreshRequisitionForm(rnRForm);
      view.setProcessButtonName(
          rnRForm.isDraft()
              ? context.getResources().getString(R.string.btn_submit)
              : context.getResources().getString(R.string.btn_complete));
    }
  }

  @Override
  protected int getCompleteErrorMessage() {
    return R.string.hint_mmtb_complete_failed;
  }

  public interface MMTBRequisitionView extends BaseRequisitionView {

    void setProcessButtonName(String buttonName);
  }
}
