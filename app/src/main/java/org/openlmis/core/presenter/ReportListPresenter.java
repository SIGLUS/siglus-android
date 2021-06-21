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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.view.BaseView;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ReportListPresenter extends Presenter {

  private static final HashMap<String, Integer> PROGRAM_CODE_ORDER = new HashMap<>();

  static {
    PROGRAM_CODE_ORDER.put(Program.VIA_CODE, 1);
    PROGRAM_CODE_ORDER.put(Program.MALARIA_CODE, 2);
    PROGRAM_CODE_ORDER.put(Program.TARV_CODE, 3);
    PROGRAM_CODE_ORDER.put(Program.RAPID_TEST_CODE, 4);
  }

  private ReportListView view;

  private boolean hasVCProgram = false;

  @Inject
  ProgramRepository programRepository;

  @Inject
  RequisitionPeriodService requisitionPeriodService;

  Subscriber<List<Program>> getSupportProgramsSubscriber = new Subscriber<List<Program>>() {
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
      view.loadProgramsError(e);
    }

    @Override
    public void onNext(List<Program> programs) {
      final ArrayList<Program> newData = new ArrayList<>(programs);
      sortPrograms(newData);
      view.updateSupportProgram(newData);
    }
  };

  @Override
  public void attachView(BaseView v) {
    this.view = (ReportListView) v;
  }

  public void getSupportPrograms() {
    Observable.create((OnSubscribe<List<Program>>) subscriber -> {
      try {
        final List<Program> programs = programRepository.queryActiveProgram();
        setHasVCProgram(programs);
        subscriber.onNext(programs);
        subscriber.onCompleted();
      } catch (LMISException e) {
        subscriber.onError(e);
      }
    })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(getSupportProgramsSubscriber);
  }

  public boolean isHasVCProgram() {
    return hasVCProgram;
  }

  public Observable<Boolean> hasMissedPeriod() {
    return Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
      try {
        subscriber.onNext(requisitionPeriodService.hasMissedPeriod(Program.VIA_CODE));
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "hasMissedPeriod").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  private void sortPrograms(List<Program> programs) {
    Collections.sort(programs, new Comparator<Program>() {
      @Override
      public int compare(Program o1, Program o2) {
        final Integer o1Order = PROGRAM_CODE_ORDER.get(o1.getProgramCode());
        final Integer o2Order = PROGRAM_CODE_ORDER.get(o2.getProgramCode());
        return Integer.compare(o1Order == null ? 0 : o1Order, o2Order == null ? 0 : o2Order);
      }
    });
  }

  private void setHasVCProgram(List<Program> programs) {
    for (Program program : programs) {
      if (Program.VIA_CODE.equals(program.getProgramCode())) {
        hasVCProgram = true;
        return;
      }
    }
    hasVCProgram = false;
  }

  public interface ReportListView extends BaseView {

    void updateSupportProgram(List<Program> programs);

    void loadProgramsError(Throwable e);
  }
}
