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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.RegimenItemRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.view.BaseView;
import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MMIARequisitionPresenter extends BaseRequisitionPresenter {

  MMIARequisitionView view;
  private MMIARepository mmiaRepository;

  @Inject
  private RegimenItemRepository regimenItemRepository;
  @Inject
  private RnrFormRepository rnrFormRepository;

  @Override
  protected RnrFormRepository initRnrFormRepository() {
    mmiaRepository = RoboGuice.getInjector(LMISApp.getContext()).getInstance(MMIARepository.class);
    return mmiaRepository;
  }

  @Override
  public void attachView(BaseView baseView) throws ViewNotMatchException {
    if (baseView instanceof MMIARequisitionView) {
      this.view = (MMIARequisitionView) baseView;
    } else {
      throw new ViewNotMatchException(MMIARequisitionView.class.getName());
    }
    super.attachView(baseView);
  }

  @Override
  public void loadData(final long formId, Date periodEndDate) {
    this.periodEndDate = periodEndDate;
    view.loading();
    Subscription subscription = getRnrFormObservable(formId)
        .subscribe(loadDataOnNextAction, loadDataOnErrorAction);
    subscriptions.add(subscription);
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
        new LMISException(e, "MMIARequisitionPresenter.getRnrFormObservable").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  @Override
  protected int getCompleteErrorMessage() {
    return R.string.hint_mmia_complete_failed;
  }

  @Override
  public void updateFormUI() {
    if (rnRForm != null) {
      view.refreshRequisitionForm(rnRForm);
      view.setProcessButtonName(
          rnRForm.isDraft() ? context.getResources().getString(R.string.btn_submit)
              : context.getResources().getString(R.string.btn_complete));
    }
  }

  public RnRForm getLastRnrForm() {
    try {
      List<RnRForm> rnRForms = rnrFormRepository.listInclude(RnRForm.Emergency.NO, "MMIA");
      if (rnRForms == null || rnRForms.size() == 1) {
        return null;
      }
      Collections
          .sort(rnRForms, (lhs, rhs) -> rhs.getPeriodBegin().compareTo(lhs.getPeriodBegin()));
      return rnRForms.get(1);
    } catch (LMISException e) {
      Log.w("MMIAPresenter", e);
    }
    return null;
  }

  public void setViewModels(List<RnrFormItem> formItems,
      List<RegimenItem> regimenItemList,
      List<BaseInfoItem> baseInfoItemList,
      List<RegimenItemThreeLines> regimenItemthreeList,
      String comments) {
    rnRForm.setRnrFormItemListWrapper(formItems);
    rnRForm.setRegimenItemListWrapper(regimenItemList);
    rnRForm.setBaseInfoItemListWrapper(baseInfoItemList);
    rnRForm.setRegimenThreeLinesWrapper(regimenItemthreeList);
    rnRForm.setComments(comments);
  }

  public boolean viewModelHasNull() {
    for (RnrFormItem rnrFormItem : rnRForm.getRnrFormItemListWrapper()) {
      if (rnrFormItem.getIssued() == null || rnrFormItem.getAdjustment() == null
          || rnrFormItem.getInventory() == null) {
        return true;
      }
    }
    return false;
  }


  public void setComments(String comments) {
    rnRForm.setComments(comments);
  }

  public Observable<Void> addCustomRegimenItem(final Regimen regimen) {
    return Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
      try {
        if (!isRegimeItemExists(regimen)) {
          RegimenItem regimenItem = createRegimenItem(regimen);
          regimenItemRepository.create(regimenItem);
          rnRForm.getRegimenItemListWrapper().add(regimenItem);
        }
      } catch (LMISException e) {
        new LMISException(e, "MMIARequisitionPresenter.addCustomRegimenItem").reportToFabric();
        subscriber.onError(e);
      }
      subscriber.onCompleted();
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public boolean isRegimeItemExists(Regimen regimen) {
    for (RegimenItem item : rnRForm.getRegimenItemListWrapper()) {
      Regimen itemRegimen = item.getRegimen();
      if (equalRegimen(regimen, itemRegimen)) {
        return true;
      }
    }
    return false;
  }

  private boolean equalRegimen(Regimen regimen, Regimen regimenExist) {
    return regimen.getName().equals(regimenExist.getName()) && regimen.getType()
        .equals(regimenExist.getType());
  }

  private RegimenItem createRegimenItem(Regimen regimen) throws LMISException {
    RegimenItem regimenItem = new RegimenItem();
    regimenItem.setRegimen(regimen);
    regimenItem.setForm(rnRForm);
    return regimenItem;
  }

  public Observable<Void> deleteRegimeItem(final RegimenItem item) {
    return Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
      try {
        rnRForm.getRegimenItemListWrapper().remove(item);
        regimenItemRepository.deleteRegimeItem(item);
      } catch (LMISException e) {
        new LMISException(e, "MMIARequisitionPresenter.deleteRegimeItem").reportToFabric();
        subscriber.onError(e);
      }
      subscriber.onCompleted();
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public Observable<Void> getSaveFormObservable(final List<RnrFormItem> rnrFormItems,
      final List<RegimenItem> regimenItems,
      final List<BaseInfoItem> baseInfoItems,
      final List<RegimenItemThreeLines> threeLineItems,
      final String comment) {
    return Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
      try {
        setViewModels(rnrFormItems, regimenItems, baseInfoItems, threeLineItems, comment);
        rnrFormRepository.createOrUpdateWithItems(rnRForm);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "MMIARequisitionPresenter.getSaveFormObservable").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public interface MMIARequisitionView extends BaseRequisitionView {

    void showValidationAlert();

    void setProcessButtonName(String buttonName);
  }
}
