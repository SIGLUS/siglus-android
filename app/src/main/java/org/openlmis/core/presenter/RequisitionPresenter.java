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

import android.text.TextUtils;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.view.View;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.roboguice.shaded.goole.common.base.Function;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;


public class RequisitionPresenter implements Presenter {

    @Inject
    VIARepository viaRepository;

    RequisitionView view;

    protected RnRForm rnRForm;
    protected List<RequisitionFormItemViewModel> requisitionFormItemViewModelList;

    public RequisitionPresenter() {
        requisitionFormItemViewModelList = new ArrayList<>();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(View v) throws ViewNotMatchException {
        if (v instanceof RequisitionView) {
            this.view = (RequisitionView) v;
        } else {
            throw new ViewNotMatchException("required RequisitionView");
        }
    }

    public RnRForm loadRnrForm() {
        try {
            rnRForm = viaRepository.initVIA();
            return rnRForm;
        } catch (LMISException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<RequisitionFormItemViewModel> getRequisitionViewModelList() {
        return requisitionFormItemViewModelList;
    }

    protected List<RequisitionFormItemViewModel> createViewModelsFromRnrForm() {
        if (rnRForm == null) {
            loadRnrForm();
        }
        return from(rnRForm.getRnrFormItemList()).transform(new Function<RnrFormItem, RequisitionFormItemViewModel>() {
            @Override
            public RequisitionFormItemViewModel apply(RnrFormItem item) {
                return new RequisitionFormItemViewModel(item);
            }
        }).toList();
    }

    public void loadRequisitionFormList() {

        if (requisitionFormItemViewModelList.size() > 0) {
            return;
        }

        view.startLoading();

        Observable.create(new Observable.OnSubscribe<List<RequisitionFormItemViewModel>>() {
            @Override
            public void call(Subscriber<? super List<RequisitionFormItemViewModel>> subscriber) {
                subscriber.onNext(createViewModelsFromRnrForm());
            }
        }).observeOn(Schedulers.io()).subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<RequisitionFormItemViewModel>>() {
            @Override
            public void call(List<RequisitionFormItemViewModel> requisitionFormItemViewModels) {
                requisitionFormItemViewModelList.addAll(requisitionFormItemViewModels);
                view.refreshRequisitionForm();
                view.stopLoading();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                view.stopLoading();
            }
        });
    }

    public boolean isCompleted() {
        List<RequisitionFormItemViewModel> requisitionViewModelList = getRequisitionViewModelList();
        for (int i = 0; i < requisitionViewModelList.size(); i++) {
            RequisitionFormItemViewModel itemViewModel = requisitionViewModelList.get(i);
            if (TextUtils.isEmpty(itemViewModel.getRequestAmount())) {
                view.showInputError(i);
                return false;
            }
        }
        return true;
    }


    public interface RequisitionView extends View {

        void showInputError(int index);

        void refreshRequisitionForm();

        void startLoading();

        void stopLoading();
    }

}
