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

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.PeriodNotUniqueException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.view.View;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MMIAFormPresenter implements Presenter {

    RnRForm form;
    MMIAFormView view;

    @Inject
    MMIARepository mmiaRepository;

    @Inject
    ProgramRepository programRepository;

    @Inject
    Context context;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(View v) throws ViewNotMatchException {
        if (v instanceof MMIAFormView) {
            this.view = (MMIAFormView) v;
        } else {
            throw new ViewNotMatchException(MMIAFormView.class.getName());
        }
    }

    public void loadData() {
        view.loading();
        Observable.create(new Observable.OnSubscribe<RnRForm>() {
            @Override
            public void call(Subscriber<? super RnRForm> subscriber) {
                try {
                    subscriber.onNext(getRnrForm());
                } catch (LMISException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Action1<RnRForm>() {

            @Override
            public void call(RnRForm form) {
                if (form != null) {
                    view.initView(form);
                }
                view.loaded();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                view.loaded();
                view.showErrorMessage(throwable.getMessage());
            }
        });
    }

    public RnRForm getRnrForm() throws LMISException {

        if (form != null) {
            return form;
        }
        Program program = programRepository.queryByCode(MMIARepository.MMIA_PROGRAM_CODE);

        RnRForm draftMMIAForm = getDraftMMIAForm(program);
        if (draftMMIAForm != null) {
            form = draftMMIAForm;
        } else {
            form = initMMIA(program);
        }
        return form;
    }

    private RnRForm getDraftMMIAForm(Program program) throws LMISException {
        return mmiaRepository.getDraftMMIAForm(program);
    }

    private RnRForm initMMIA(Program program) throws LMISException {
        return mmiaRepository.initMMIA(program);
    }

    public void completeMMIA(ArrayList<RegimenItem> regimenItemList, ArrayList<BaseInfoItem> baseInfoItemList, String comments) {
        form.setRegimenItemListWrapper(regimenItemList);
        form.setBaseInfoItemListWrapper(baseInfoItemList);
        form.setComments(comments);
        if (validate(form)) {
            try {
                mmiaRepository.authorise(form);
                view.completeSuccess();
            } catch (LMISException e) {
                if (e instanceof PeriodNotUniqueException) {
                    view.showErrorMessage(context.getResources().getString(R.string.msg_mmia_not_unique));
                } else {
                    view.showErrorMessage(e.getMessage());
                }
            }
        } else {
            view.showValidationAlert();
        }
    }

    private boolean validate(RnRForm form) {
        return form.getRegimenItemListAmount(form.getRegimenItemListWrapper()) == mmiaRepository.getTotalPatients(form);
    }

    public void saveDraftForm(ArrayList<RegimenItem> regimenItemList, ArrayList<BaseInfoItem> baseInfoItemList, String comments) {
        form.setRegimenItemListWrapper(regimenItemList);
        form.setBaseInfoItemListWrapper(baseInfoItemList);
        form.setComments(comments);
        try {
            form.setStatus(RnRForm.STATUS.DRAFT);
            mmiaRepository.save(form);
        } catch (LMISException e) {
            view.showErrorMessage(e.getMessage());
        }
    }

    public void removeRnrForm() {
        try {
            mmiaRepository.removeRnrForm(form);
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    public interface MMIAFormView extends View {
        void showValidationAlert();

        void showErrorMessage(String msg);

        void completeSuccess();

        void initView(RnRForm form);
    }
}
