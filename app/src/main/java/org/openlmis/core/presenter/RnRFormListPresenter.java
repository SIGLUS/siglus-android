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


import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.view.View;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Setter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RnRFormListPresenter implements Presenter{

    RnRFormListView view;

    @Inject
    RnrFormRepository repository;

    @Setter
    String programCode;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(View v) throws ViewNotMatchException {
        if (v instanceof RnRFormListView) {
            view = (RnRFormListView) v;
        } else {
            throw new ViewNotMatchException("Need RnRFormListView");
        }
    }


    public Observable<List<RnRFormViewModel>> loadRnRFormList() {
        return Observable.create(new Observable.OnSubscribe<List<RnRFormViewModel>>() {
            @Override
            public void call(Subscriber<? super List<RnRFormViewModel>> subscriber) {
                try {
                    subscriber.onNext(buildFormListViewModels());
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    private List<RnRFormViewModel> buildFormListViewModels() throws LMISException {
        List<RnRFormViewModel> viewModels = new ArrayList<>();

        List<RnRForm> rnRForms = repository.list(programCode);

        if (rnRForms == null || rnRForms.isEmpty()){
            return viewModels;
        }

        viewModels.add(new RnRFormViewModel("Current period"));
        Collections.reverse(rnRForms);

        if (rnRForms.get(0).getStatus() == RnRForm.STATUS.DRAFT){
            viewModels.add(new RnRFormViewModel(rnRForms.get(0)));
            rnRForms.remove(0);
        }
        viewModels.add(new RnRFormViewModel("Previous period"));

        viewModels.addAll(FluentIterable.from(rnRForms).transform(new Function<RnRForm, RnRFormViewModel>() {
            @Override
            public RnRFormViewModel apply(RnRForm form) {
                return new RnRFormViewModel(form);
            }
        }).toList());
        return viewModels;
    }


    public interface RnRFormListView extends View {

    }
}
