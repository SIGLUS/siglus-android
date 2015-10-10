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
import org.openlmis.core.service.SyncManager;
import org.openlmis.core.view.View;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.base.Preconditions.checkNotNull;

public class MMIAFormPresenter implements Presenter {

    RnRForm form;
    MMIAFormView view;

    @Inject
    MMIARepository mmiaRepository;

    @Inject
    ProgramRepository programRepository;

    @Inject
    Context context;

    @Inject
    SyncManager syncManager;

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

    public void loadData(final long formId) {
        view.loading();
        Observable.create(new Observable.OnSubscribe<RnRForm>() {
            @Override
            public void call(Subscriber<? super RnRForm> subscriber) {
                try {
                    subscriber.onNext(getRnrForm(formId));
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

    public RnRForm getRnrForm(final long formId) throws LMISException {

        if (form != null) {
            return form;
        }

        if (formId > 0) {
            form = mmiaRepository.queryRnRForm(formId);
        } else {
            Program program = programRepository.queryByCode(MMIARepository.MMIA_PROGRAM_CODE);
            RnRForm draftMMIAForm = mmiaRepository.getUnCompletedMMIA(program);
            if (draftMMIAForm != null) {
                form = draftMMIAForm;
            } else {
                form = mmiaRepository.initMMIA(program);
            }
        }
        return form;
    }

    public boolean formIsEditable() {
        return checkNotNull(form).getStatus().equals(RnRForm.STATUS.DRAFT);
    }

    public void completeMMIA(ArrayList<RegimenItem> regimenItemList, ArrayList<BaseInfoItem> baseInfoItemList, String comments) {
        form.setRegimenItemListWrapper(regimenItemList);
        form.setBaseInfoItemListWrapper(baseInfoItemList);
        form.setComments(comments);

        if (!validate(form) && comments.equals("")) {
            view.showValidationAlert();
            return;
        }
        authoriseForm();
    }

    private void authoriseForm() {
        view.loading();
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    mmiaRepository.authorise(form);
                    subscriber.onNext(null);
                } catch (LMISException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.loaded();
                        if (e instanceof PeriodNotUniqueException) {
                            view.showErrorMessage(context.getResources().getString(R.string.msg_mmia_not_unique));
                        } else {
                            view.showErrorMessage(context.getString(R.string.hint_complete_failed));
                        }
                    }

                    @Override
                    public void onNext(Void e) {
                        view.loaded();
                        view.completeSuccess();
                        syncManager.requestSyncImmediately();
                    }
                });
    }

    private boolean validate(RnRForm form) {
        return RnRForm.calculateTotalRegimenAmount(form.getRegimenItemListWrapper()) == mmiaRepository.getTotalPatients(form);
    }

    public void saveDraftForm(ArrayList<RegimenItem> regimenItemList, ArrayList<BaseInfoItem> baseInfoItemList, String comments) {
        form.setRegimenItemListWrapper(regimenItemList);
        form.setBaseInfoItemListWrapper(baseInfoItemList);
        form.setComments(comments);
        form.setStatus(RnRForm.STATUS.DRAFT);
        saveForm();
    }

    private void saveForm() {
        view.loading();
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                try {
                    mmiaRepository.save(form);
                    subscriber.onNext(null);
                } catch (LMISException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                view.loaded();
                view.saveSuccess();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                view.loaded();
                view.showErrorMessage(context.getString(R.string.hint_save_failed));
            }
        });
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

        void saveSuccess();
    }
}
