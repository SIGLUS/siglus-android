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
import android.text.TextUtils;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.openlmis.core.view.viewmodel.RnRFormItemAdjustmentViewModel;
import org.openlmis.core.view.viewmodel.ViaKitsViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.openlmis.core.model.Product.IsKit;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;


public class VIARequisitionPresenter extends BaseRequisitionPresenter {

    @Inject
    Context context;

    @Inject
    ProductRepository productRepository;

    @Inject
    StockRepository stockRepository;

    VIARequisitionView view;

    @Getter
    protected List<RequisitionFormItemViewModel> requisitionFormItemViewModels;

    @Getter
    @Setter
    private ViaKitsViewModel viaKitsViewModel;

    public VIARequisitionPresenter() {
        requisitionFormItemViewModels = new ArrayList<>();
        viaKitsViewModel = new ViaKitsViewModel();
    }

    @Override
    public void loadData(long formId, Date periodEndDate) {
        this.periodEndDate = periodEndDate;
        view.loading();
        Subscription subscription = getRnrFormObservable(formId).subscribe(loadDataOnNextAction, loadDataOnErrorAction);
        subscriptions.add(subscription);
    }

    @Override
    protected RnrFormRepository initRnrFormRepository() {
        return RoboGuice.getInjector(LMISApp.getContext()).getInstance(VIARepository.class);
    }

    @Override
    public void attachView(BaseView baseView) throws ViewNotMatchException {
        if (baseView instanceof VIARequisitionView) {
            this.view = (VIARequisitionView) baseView;
        } else {
            throw new ViewNotMatchException("required VIARequisitionView");
        }
        super.attachView(baseView);
    }

    protected List<RequisitionFormItemViewModel> getViewModelsFromRnrForm(RnRForm form) throws LMISException {
        if (requisitionFormItemViewModels.size() > 0) {
            return requisitionFormItemViewModels;
        }
        return from(form.getRnrItems(IsKit.No)).filter(new Predicate<RnrFormItem>() {
            @Override
            public boolean apply(RnrFormItem rnrFormItem) {
                return !rnrFormItem.getProduct().isArchived();
            }
        }).transform(new Function<RnrFormItem, RequisitionFormItemViewModel>() {
            @Override
            public RequisitionFormItemViewModel apply(RnrFormItem item) {
                RequisitionFormItemViewModel requisitionFormItemViewModel = new RequisitionFormItemViewModel(item);
                if (!view.isHistoryForm()) {
                    adjustTheoretical(requisitionFormItemViewModel);
                }
                return requisitionFormItemViewModel;
            }
        }).toList();
    }

    private void adjustTheoretical(RequisitionFormItemViewModel requisitionFormItemViewModel) {
        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_requisition_theoretical)) {
            return;
        }
        Product product = requisitionFormItemViewModel.getItem().getProduct();
        requisitionFormItemViewModel.setAdjustmentViewModels(generateAdjustInfo(product));
    }

    private List<RnRFormItemAdjustmentViewModel> generateAdjustInfo(Product product) {
        List<RnRFormItemAdjustmentViewModel> list = new ArrayList<>();
        try {
            List<KitProduct> kitProducts = productRepository.queryKitProductByProductCode(product.getCode());
            for (KitProduct kitProduct : kitProducts) {
                Product kit = productRepository.getByCode(kitProduct.getKitCode());
                long kitSOH = getKitSOH(kit);
                if (kitSOH == 0) {
                    continue;
                }
                RnRFormItemAdjustmentViewModel rnRFormItemAdjustmentViewModel = new RnRFormItemAdjustmentViewModel();
                rnRFormItemAdjustmentViewModel.setKitStockOnHand(kitSOH);
                rnRFormItemAdjustmentViewModel.setQuantity(kitProduct.getQuantity());
                rnRFormItemAdjustmentViewModel.setKitName(kit.getPrimaryName());

                list.add(rnRFormItemAdjustmentViewModel);
            }
        } catch (LMISException e) {
            e.reportToFabric();
        }
        return list;
    }

    private long getKitSOH(Product kit) throws LMISException {
        if (stockRepository.queryStockCardByProductId(kit.getId()) == null) {
            return 0;
        }
        return stockRepository.queryStockCardByProductId(kit.getId()).getStockOnHand();
    }

    @Override
    protected Observable<RnRForm> getRnrFormObservable(final long formId) {
        return Observable.create(new Observable.OnSubscribe<RnRForm>() {
            @Override
            public void call(Subscriber<? super RnRForm> subscriber) {
                try {
                    RnRForm rnrForm = getRnrForm(formId);
                    requisitionFormItemViewModels.clear();
                    requisitionFormItemViewModels.addAll(getViewModelsFromRnrForm(rnrForm));
                    viaKitsViewModel.convertRnrKitItemsToViaKit(rnrForm.getRnrItems(IsKit.Yes));
                    subscriber.onNext(rnrForm);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public void updateUIAfterSubmit() {
        view.highLightApprovedAmount();
        view.refreshRequisitionForm(rnRForm);
        view.setProcessButtonName(context.getResources().getString(R.string.btn_complete));
    }

    @Override
    public void updateFormUI() {
        if (rnRForm.isDraft()) {
            view.setProcessButtonName(context.getResources().getString(R.string.btn_submit));
            view.highLightRequestAmount();
        } else if (rnRForm.isSubmitted()) {
            view.setProcessButtonName(context.getString(R.string.btn_complete));
            view.highLightApprovedAmount();
        }
        view.refreshRequisitionForm(rnRForm);
    }

    protected boolean validateForm() {
        return view.validateConsultationNumber() && view.validateKitData() && validateRnrFormItems();
    }

    protected boolean validateRnrFormItems() {
        for (int i = 0; i < requisitionFormItemViewModels.size(); i++) {
            RequisitionFormItemViewModel itemViewModel = requisitionFormItemViewModels.get(i);
            if (TextUtils.isEmpty(itemViewModel.getRequestAmount())
                    || TextUtils.isEmpty(itemViewModel.getApprovedAmount())) {
                view.showListInputError(i);
                return false;
            }
        }
        return true;
    }

    public void processRequisition(String consultationNumbers) {
        if (!validateForm()) {
            return;
        }

        if (!rnrFormRepository.isPeriodUnique(rnRForm)) {
            view.showErrorMessage(context.getResources().getString(R.string.msg_requisition_not_unique));
            return;
        }

        dataViewToModel(consultationNumbers);

        view.showSignDialog(rnRForm.isDraft());
    }

    private void dataViewToModel(String consultationNumbers) {
        List<RnrFormItem> rnrFormItems = new ArrayList<>();
        rnrFormItems.addAll(convertRnrItemViewModelsToRnrItems());
        rnrFormItems.addAll(viaKitsViewModel.convertToRnrItems());

        rnRForm.setRnrFormItemListWrapper(rnrFormItems);
        if (!TextUtils.isEmpty(consultationNumbers)) {
            rnRForm.getBaseInfoItemListWrapper().get(0).setValue(Long.valueOf(consultationNumbers).toString());
        }
    }

    public void saveVIAForm(String consultationNumbers) {
        view.loading();

        dataViewToModel(consultationNumbers);
        saveRequisition();
    }

    private ImmutableList<RnrFormItem> convertRnrItemViewModelsToRnrItems() {
        return from(requisitionFormItemViewModels).transform(new Function<RequisitionFormItemViewModel, RnrFormItem>() {
            @Override
            public RnrFormItem apply(RequisitionFormItemViewModel requisitionFormItemViewModel) {
                return requisitionFormItemViewModel.toRnrFormItem();
            }
        }).toList();
    }

    public String getConsultationNumbers() {
        if (rnRForm == null) {
            return null;
        }
        List<BaseInfoItem> baseInfoItemListWrapper = rnRForm.getBaseInfoItemListWrapper();
        if (baseInfoItemListWrapper.size() == 0) {
            return null;
        }
        return rnRForm.getBaseInfoItemListWrapper().get(0).getValue();
    }

    public void setConsultationNumbers(String consultationNumbers) {
        if (rnRForm == null) {
            return;
        }
        List<BaseInfoItem> baseInfoItemListWrapper = rnRForm.getBaseInfoItemListWrapper();
        if (baseInfoItemListWrapper != null) {
            baseInfoItemListWrapper.get(0).setValue(consultationNumbers);
        }
    }

    public interface VIARequisitionView extends BaseRequisitionView {

        void showListInputError(int index);

        void highLightRequestAmount();

        void highLightApprovedAmount();

        void setProcessButtonName(String name);

        boolean validateConsultationNumber();

        boolean validateKitData();

        boolean isHistoryForm();
    }
}