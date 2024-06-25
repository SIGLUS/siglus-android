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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.TranslationUtil;
import org.openlmis.core.view.BaseView;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ReportListPresenter extends Presenter {

  private static final Map<String, Integer> PROGRAM_CODE_ORDER;

  static {
    Map<String, Integer> map = new HashMap<>();
    map.put(Program.VIA_CODE, 1);
    map.put(Program.MALARIA_CODE, 2);
    map.put(Program.TARV_CODE, 3);
    map.put(Program.RAPID_TEST_CODE, 4);
    map.put(Program.MMTB_CODE, 5);
    PROGRAM_CODE_ORDER = Collections.unmodifiableMap(map);
  }

  private ReportListView view;

  private boolean hasVCReportType = false;

  @Inject
  ReportTypeFormRepository reportTypeFormRepository;

  @Inject
  RequisitionPeriodService requisitionPeriodService;

  Subscriber<List<ReportTypeForm>> getSupportReportTypesSubscriber = new Subscriber<List<ReportTypeForm>>() {
    @Override
    public void onCompleted() {
      // do nothing
    }

    @Override
    public void onError(Throwable e) {
      view.loadReportTypesError(e);
    }

    @Override
    public void onNext(List<ReportTypeForm> reportTypeForms) {
      TranslationUtil.translateReportName(reportTypeForms);
      sortReportTypes(reportTypeForms);
      view.updateSupportReportTypes(reportTypeForms);
    }

    private void sortReportTypes(List<ReportTypeForm> programs) {
      Collections.sort(programs, (o1, o2) -> {
        Integer o1Order = PROGRAM_CODE_ORDER.get(o1.getCode());
        Integer o2Order = PROGRAM_CODE_ORDER.get(o2.getCode());
        return Integer.compare(o1Order == null ? 0 : o1Order, o2Order == null ? 0 : o2Order);
      });
    }

  };

  @Override
  public void attachView(BaseView v) {
    this.view = (ReportListView) v;
  }

  public void getSupportReportTypes() {
    Observable.create((OnSubscribe<List<ReportTypeForm>>) subscriber -> {
      List<ReportTypeForm> reportTypeForms = reportTypeFormRepository.listAllWithActive();
      setHasVCReportType(reportTypeForms);
      subscriber.onNext(reportTypeForms);
      subscriber.onCompleted();
    })
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(getSupportReportTypesSubscriber);
  }

  public boolean isHasVCReportType() {
    return hasVCReportType;
  }

  public Observable<Boolean> hasMissedViaProgramPeriod() {
    return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
      try {
        subscriber.onNext(requisitionPeriodService.hasMissedPeriod(Program.VIA_CODE));
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "hasMissedViaProgramPeriod").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  private void setHasVCReportType(List<ReportTypeForm> reportTypeForms) {
    for (ReportTypeForm reportTypeForm : reportTypeForms) {
      if (Program.VIA_CODE.equals(reportTypeForm.getCode())) {
        hasVCReportType = true;
        return;
      }
    }
    hasVCReportType = false;
  }

  public Observable<Boolean> hasMoreThan2ViaProgramEmergencyRequisition() {
    return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
      try {
        boolean hasOverLimit = requisitionPeriodService.hasOverLimit(
            Program.VIA_CODE, 2,
            DateUtil.getFirstDayForCurrentMonthByDate(DateUtil.getCurrentDate()));

        subscriber.onNext(hasOverLimit);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "hasMoreThan2ViaProgramEmergencyRequisition").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public interface ReportListView extends BaseView {

    void updateSupportReportTypes(List<ReportTypeForm> reportTypeForms);

    void loadReportTypesError(Throwable e);
  }
}
