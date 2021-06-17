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
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.view.BaseView;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RequisitionPresenter extends Presenter {

  private RequisitionView view;

  @Inject
  ProgramRepository programRepository;

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
      view.updateSupportProgram(programs);
    }
  };

  @Override
  public void attachView(BaseView v) {
    this.view = (RequisitionView) v;
  }

  public void getSupportPrograms() {
    Observable.create((OnSubscribe<List<Program>>) subscriber -> {
      try {
        subscriber.onNext(programRepository.queryActiveProgram());
        subscriber.onCompleted();
      } catch (LMISException e) {
        subscriber.onError(e);
      }
    })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(getSupportProgramsSubscriber);
  }

  public interface RequisitionView extends BaseView {

    void updateSupportProgram(List<Program> programs);

    void loadProgramsError(Throwable e);
  }
}
