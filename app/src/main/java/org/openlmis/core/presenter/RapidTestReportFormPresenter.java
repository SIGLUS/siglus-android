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
import java.util.Date;
import lombok.Getter;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RapidTestRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import roboguice.RoboGuice;
import roboguice.inject.ContextSingleton;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@ContextSingleton
public class RapidTestReportFormPresenter extends BaseRequisitionPresenter {

  RapidTestReportView view;

  @Inject
  ProgramRepository programRepository;

  @Getter
  protected RapidTestReportViewModel viewModel;

  @Override
  protected RnrFormRepository initRnrFormRepository() {
    return RoboGuice.getInjector(LMISApp.getContext()).getInstance(RapidTestRepository.class);
  }

  @Override
  public void attachView(BaseView baseView) throws ViewNotMatchException {
    if (baseView instanceof RapidTestReportView) {
      this.view = (RapidTestReportView) baseView;
    } else {
      throw new ViewNotMatchException(RapidTestReportView.class.getName());
    }
    super.attachView(baseView);
  }

  @Override
  public void loadData(long formId, Date periodEndDate) {
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
        convertProgramDataFormToRapidTestReportViewModel(rnRForm);
        subscriber.onNext(rnRForm);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "RapidTestReportFormPresenter.getRnrFormObservable").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  @Override
  protected int getCompleteErrorMessage() {
    return R.string.hint_rapid_test_complete_failed;
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

  @Override
  public boolean isDraft() {
    return viewModel.isDraft();
  }


  private void convertProgramDataFormToRapidTestReportViewModel(RnRForm programDataForm) {
    viewModel = new RapidTestReportViewModel(programDataForm);
  }

  public Observable<RapidTestReportViewModel> createOrUpdateRapidTest() {
    return Observable.create((Observable.OnSubscribe<RapidTestReportViewModel>) subscriber -> {
      try {
        viewModel.convertFormViewModelToDataModel(
            programRepository.queryByCode(Constants.RAPID_TEST_PROGRAM_CODE));
        rnrFormRepository.createOrUpdateWithItems(viewModel.getRapidTestForm());
        subscriber.onNext(viewModel);
        subscriber.onCompleted();
      } catch (Exception e) {
        new LMISException(e, "RapidTestReportFormPresenter.getSaveFormObservable").reportToFabric();
        subscriber.onError(e);
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }

  public boolean isSubmitted() {
    return viewModel.isSubmitted();
  }

//  private void generateNewRapidTestForm(Period period) {
//    viewModel = new RapidTestReportViewModel(period);
//    viewModel.setStatus(RapidTestReportViewModel.Status.INCOMPLETE);
//  }
//  public void addSignature(String sign) {
//    viewModel.addSignature(sign);
//  }
//  public Observable<RapidTestReportViewModel> loadViewModel(final long formId,
//      final Date periodEndDate) {
//    return Observable.create((Observable.OnSubscribe<RapidTestReportViewModel>) subscriber -> {
//      try {
//        if (formId == 0) {
//          Period reportPeriod = period;
//          if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(period.getEnd().toDate());
//            int year = calendar.get(Calendar.YEAR);
//            int month = calendar.get(Calendar.MONTH);
//            int date = calendar.get(Calendar.DATE);
//            calendar.set(year, month, date, 23, 59, 59);
//            reportPeriod = new Period(period.getBegin(), new DateTime(calendar.getTime()));
//          }
//          generateNewRapidTestForm(reportPeriod);
//          saveForm();
//          List<ProgramDataFormBasicItem> basicItems = programBasicItemsRepository
//              .createInitProgramForm(viewModel.getRapidTestForm(), reportPeriod.getEnd().toDate());
//          viewModel.setBasicItems(basicItems);
//          saveForm();
//        } else {
//          convertProgramDataFormToRapidTestReportViewModel(
//              programDataFormRepository.queryById(formId));
//        }
//        subscriber.onNext(viewModel);
//        subscriber.onCompleted();
//      } catch (LMISException e) {
//        subscriber.onError(e);
//        e.reportToFabric();
//      }
//    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
//  }
//@Override
//public void deleteDraft() {
//  if (viewModel.getRapidTestForm().getId() != 0L) {
//    try {
//      programDataFormRepository.delete(viewModel.getRapidTestForm());
//    } catch (Exception e) {
//      new LMISException(e).reportToFabric();
//    }
//  }
//}

public interface RapidTestReportView extends BaseRequisitionView {
  void setProcessButtonName(String buttonName);
}

}
