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
import java.util.List;
import lombok.Getter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.view.BaseView;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EditOrderNumberPresenter extends Presenter {

  private EditOrderNumberView view;

  @Getter
  private List<String> orderNumbers = new ArrayList<>();

  @Inject
  private PodRepository podRepository;

  Observer<Object> loadDataObserver = new Observer<Object>() {
    @Override
    public void onCompleted() {
      view.loaded();
    }

    @Override
    public void onError(Throwable e) {
      view.loaded();
      view.loadDataFailed();
    }

    @Override
    public void onNext(Object o) {
      // do nothing
    }
  };

  Observer<Object> changeOrderNumberObserver = new Observer<Object>() {
    @Override
    public void onCompleted() {
      view.loaded();
      view.changeOrderNumberSuccess();
    }

    @Override
    public void onError(Throwable e) {
      view.loaded();
      view.changeOrderNumberFailed();
    }

    @Override
    public void onNext(Object o) {
      // do nothing
    }
  };

  @Override
  public void attachView(BaseView v) {
    // do nothing
    view = (EditOrderNumberView) v;
  }

  public void loadData(String podOrderNumber) {
    view.loading();
    Observable.create(subscriber -> {
      orderNumbers.clear();
      orderNumbers.addAll(podRepository.queryIssueVoucherOrderCodesBelongProgram(podOrderNumber));
      subscriber.onNext(null);
      subscriber.onCompleted();
    }).observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(loadDataObserver);
  }

  public void changeOrderNumber(String podOrderNumber, String issueVoucherOrderNumber) {
    view.loading();
    Observable.create(subscriber -> {
      try {
        podRepository.updateOrderCode(podOrderNumber, issueVoucherOrderNumber);
      } catch (LMISException e) {
        e.reportToFabric();
        subscriber.onError(e);
      }
      subscriber.onNext(null);
      subscriber.onCompleted();
    }).observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(changeOrderNumberObserver);
  }

  public interface EditOrderNumberView extends BaseView {

    void loadDataFailed();

    void changeOrderNumberFailed();

    void changeOrderNumberSuccess();
  }
}
