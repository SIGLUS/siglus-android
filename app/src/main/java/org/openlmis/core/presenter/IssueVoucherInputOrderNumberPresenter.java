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
import java.util.HashMap;
import java.util.List;
import lombok.AccessLevel;
import lombok.Setter;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.view.BaseView;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class IssueVoucherInputOrderNumberPresenter extends Presenter {

  @Inject
  ProgramRepository programRepository;

  @Inject
  PodRepository podRepository;

  @Setter(AccessLevel.PACKAGE)
  private List<Pod> existingPods = new ArrayList<>();

  private final HashMap<String, Program> programCodeToProgram = new HashMap<>();

  @Override
  public void attachView(BaseView v) {
    // do nothing
  }

  public Observable<List<Program>> loadData() {
    return Observable.create((OnSubscribe<List<Program>>) subscriber -> {
      try {
        List<Program> queryActiveProgram = programRepository.queryProgramWithoutML();
        existingPods = podRepository.listAllPods();
        collectIssueVoucherPrograms(queryActiveProgram);
        subscriber.onNext(queryActiveProgram);
      } catch (LMISException exception) {
        subscriber.onError(exception);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public boolean isOrderNumberExisted(String orderNumber) {
    return FluentIterable.from(existingPods)
        .transform(Pod::getOrderCode)
        .toList()
        .contains(orderNumber);
  }

  public boolean isProgramAvailable(Program program) {
    return programCodeToProgram.get(program.getProgramCode()) == null;
  }

  private void collectIssueVoucherPrograms(List<Program> programs) {
    programCodeToProgram.clear();
    HashMap<String, Program> allProgramCodeToProgram = new HashMap<>();
    for (Program program : programs) {
      allProgramCodeToProgram.put(program.getProgramCode(), program);
    }
    for (Pod existingPod : existingPods) {
      if (OrderStatus.SHIPPED != existingPod.getOrderStatus()) {
        continue;
      }
      String podProgramCode = existingPod.getRequisitionProgramCode();
      programCodeToProgram.put(podProgramCode, allProgramCodeToProgram.get(podProgramCode));
    }
  }
}
