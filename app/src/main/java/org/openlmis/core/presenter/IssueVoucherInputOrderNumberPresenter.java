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
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.view.BaseView;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class IssueVoucherInputOrderNumberPresenter extends Presenter {

  @Inject
  ProgramRepository programRepository;

  @Inject
  PodRepository podRepository;

  @Override
  public void attachView(BaseView v) throws ViewNotMatchException {
    // do nothing
  }

  public Observable<List<Program>> loadPrograms() {
    return Observable.create((OnSubscribe<List<Program>>) subscriber -> {
      try {
        final List<Program> queryActiveProgram = programRepository.queryProgramWithoutML();
        subscriber.onNext(queryActiveProgram);
      } catch (LMISException exception) {
        subscriber.onError(exception);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public boolean isOrderNumberExisted(String orderNumber) {
    Pod pod = null;
    try {
      pod = podRepository.queryByOrderCode(orderNumber);
    } catch (LMISException e) {
      new LMISException(e, "query by order code failed").reportToFabric();
    }
    return pod != null;
  }
}
