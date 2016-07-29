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
import org.openlmis.core.model.AddedDrugInVIA;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.helper.RnrFormHelper;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.fragment.VIARequisitionFragment;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.openlmis.core.view.viewmodel.RnRFormItemAdjustmentViewModel;
import org.openlmis.core.view.viewmodel.ViaKitsViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import roboguice.RoboGuice;
import roboguice.inject.ContextSingleton;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.openlmis.core.model.Product.IsKit;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

@ContextSingleton
public class VIARequisitionPresenter extends BaseRequisitionPresenter {

    @Inject
    Context context;

    @Inject
    ProductRepository productRepository;

    @Inject
    StockRepository stockRepository;

    VIARequisitionFragment view;

    @Getter
    protected List<RequisitionFormItemViewModel> requisitionFormItemViewModels;

    @Getter
    @Setter
    private ViaKitsViewModel viaKitsViewModel;

    @Inject
    RnrFormItemRepository rnrFormItemRepository;

    @Inject
    private RnrFormHelper rnrFormHelper;

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

    public void loadEmergencyData(final List<StockCard> stockCards, final Date periodEndDate) {
        this.periodEndDate = periodEndDate;
        view.loading();
        Subscription subscription = Observable.create(new Observable.OnSubscribe<RnRForm>() {
            @Override
            public void call(Subscriber<? super RnRForm> subscriber) {
                try {
                    RnRForm rnrForm = initEmergencyRnr(stockCards, periodEndDate);
                    convertRnrToViewModel(rnrForm);
                    subscriber.onNext(rnrForm);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(loadDataOnNextAction, loadDataOnErrorAction);
        subscriptions.add(subscription);
    }

    protected RnRForm initEmergencyRnr(List<StockCard> stockCards, Date periodEndDate) throws LMISException {
        return rnrFormRepository.initEmergencyRnrForm(periodEndDate, stockCards);
    }

    @Override
    protected RnrFormRepository initRnrFormRepository() {
        return RoboGuice.getInjector(LMISApp.getContext()).getInstance(VIARepository.class);
    }

    @Override
    public void attachView(BaseView baseView) throws ViewNotMatchException {
        if (baseView instanceof VIARequisitionView) {
            this.view = (VIARequisitionFragment) baseView;
        } else {
            throw new ViewNotMatchException("required VIARequisitionView");
        }
        super.attachView(baseView);
    }

    protected List<RequisitionFormItemViewModel> getViewModelsFromRnrForm(RnRForm form) throws LMISException {
        if (requisitionFormItemViewModels.size() > 0) {
            return requisitionFormItemViewModels;
        }
        return from(form.getRnrItems(IsKit.No)).transform(new Function<RnrFormItem, RequisitionFormItemViewModel>() {
            @Override
            public RequisitionFormItemViewModel apply(RnrFormItem item) {
                RequisitionFormItemViewModel requisitionFormItemViewModel = new RequisitionFormItemViewModel(item);
                if (!isHistoryForm()) {
                    adjustTheoretical(requisitionFormItemViewModel);
                }
                return requisitionFormItemViewModel;
            }
        }).toList();
    }

    private List<RequisitionFormItemViewModel> transformDataItemsToViewModels(List<RnrFormItem> additionalItems) throws LMISException {
        return from(additionalItems).transform(new Function<RnrFormItem, RequisitionFormItemViewModel>() {
            @Override
            public RequisitionFormItemViewModel apply(RnrFormItem rnrFormItem) {
                return new RequisitionFormItemViewModel(rnrFormItem);
            }
        }).toList();

    }

    private void adjustTheoretical(RequisitionFormItemViewModel requisitionFormItemViewModel) {
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
                    convertRnrToViewModel(rnrForm);
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
    protected int getSaveErrorMessage() {
        return R.string.hint_save_mmia_failed;
    }

    @Override
    protected int getCompleteErrorMessage() {
        return R.string.hint_requisition_complete_failed;
    }

    private void convertRnrToViewModel(RnRForm rnrForm) throws LMISException {
        requisitionFormItemViewModels.clear();
        requisitionFormItemViewModels.addAll(getViewModelsFromRnrForm(rnrForm));
        viaKitsViewModel.convertRnrKitItemsToViaKit(rnrForm.getRnrItems(IsKit.Yes));
    }

    public void populateAdditionalDrugsViewModels(List<AddedDrugInVIA> addedDrugInVIAs, Date periodBegin) {
        try {
            List<RnrFormItem> additionalProducts = generateRnrItemsForAdditionalProducts(addedDrugInVIAs, periodBegin);
            requisitionFormItemViewModels.addAll(transformDataItemsToViewModels(additionalProducts));
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    private List<RnrFormItem> generateRnrItemsForAdditionalProducts(List<AddedDrugInVIA> addedDrugInVIAs, final Date periodBegin) {
        List<RnrFormItem> rnrFormItemList = FluentIterable.from(addedDrugInVIAs).transform(new Function<AddedDrugInVIA, RnrFormItem>() {
            @Override
            public RnrFormItem apply(AddedDrugInVIA addedDrugInVIA) {
                RnrFormItem rnrFormItem = new RnrFormItem();
                try {
                    Product product = productRepository.getByCode(addedDrugInVIA.getProductCode());
                    rnrFormItem.setProduct(product);
                    rnrFormItem.setForm(rnRForm);
                    rnrFormItem.setManualAdd(true);
                    if (product.isArchived()) {
                        populateRnrItemWithQuantities(rnrFormItem, periodBegin, periodEndDate);
                    }
                    rnrFormItem.setRequestAmount(addedDrugInVIA.getQuantity());
                    rnrFormItem.setApprovedAmount(rnrFormItem.getRequestAmount());
                } catch (LMISException e) {
                    e.reportToFabric();
                }
                return rnrFormItem;
            }
        }).toList();
        return rnrFormItemList;
    }

    private void populateRnrItemWithQuantities(RnrFormItem rnrFormItem, Date periodBegin, Date periodEnd) throws LMISException {
        StockCard stockCard = stockRepository.queryStockCardByProductId(rnrFormItem.getProduct().getId());
        List<StockMovementItem> stockMovementItems = stockRepository.queryStockItemsByPeriodDates(stockCard, periodBegin, periodEnd);
        if (stockMovementItems.size() > 0) {
            rnrFormItem.setInitialAmount(stockMovementItems.get(0).calculatePreviousSOH());
            rnrFormHelper.assignTotalValues(rnrFormItem, stockMovementItems);
        }
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
        if (rnRForm.isEmergency()) {
            return validateRnrFormItems();
        } else {
            return view.validateConsultationNumber() && validateRnrFormItems();
        }
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

    @Override
    protected void submitRequisition(RnRForm rnRForm) {
        if (rnRForm.isEmergency()) {
            updateUIAfterSubmit();
            return;
        }
        super.submitRequisition(rnRForm);
    }

    public void processRequisition(String consultationNumbers) {
        if (!validateForm()) {
            return;
        }

        if (!rnrFormRepository.isPeriodUnique(rnRForm)) {
            ToastUtil.show(R.string.msg_requisition_not_unique);
            return;
        }

        dataViewToModel(consultationNumbers);

        view.showSignDialog(rnRForm.isDraft());
    }

    private void dataViewToModel(String consultationNumbers) {
        List<RnrFormItem> rnrFormItems = new ArrayList<>();

        rnrFormItems.addAll(convertRnrItemViewModelsToRnrItems());
        rnrFormItems.addAll(viaKitsViewModel.getKitItems());

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

    @Override
    protected Observable<Void> createOrUpdateRnrForm(final RnRForm rnRForm) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    rnrFormRepository.createOrUpdateWithItems(rnRForm);
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                } finally {
                    stockService.monthlyUpdateAvgMonthlyConsumption();
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    protected void createStockCardsOrUnarchiveAndAddToFormForAdditionalRnrItems(RnRForm form) {
        try {
            List<RnrFormItem> rnrFormItems = new ArrayList<>();
            rnrFormItems.addAll(form.getRnrFormItemListWrapper());
            for (RnrFormItem rnrFormItem : rnrFormItemRepository.listAllNewRnrItems()) {
                if (rnrFormItem.getProduct().isArchived()) {
                    rnrFormItem.getProduct().setArchived(false);
                    productRepository.updateProduct(rnrFormItem.getProduct());
                } else {
                    StockCard newStockCard = new StockCard();
                    newStockCard.setProduct(rnrFormItem.getProduct());
                    stockRepository.createOrUpdateStockCardWithStockMovement(newStockCard);
                }
                rnrFormItem.setForm(form);
                rnrFormItems.add(rnrFormItem);
            }
            form.setRnrFormItemListWrapper(rnrFormItems);
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public void removeAllNewRnrItems() {
        try {
            rnrFormItemRepository.deleteAllNewRnrItems();
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public Observable<Void> removeOneNewRnrItems(final RnrFormItem rnrFormItem) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    rnrFormItemRepository.deleteRnrItem(rnrFormItem);
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public void refreshProductListInVIAForm() {
        try {
            convertRnrToViewModel(rnRForm);
        } catch (LMISException e) {
            e.printStackTrace();
        }
        view.refreshRequisitionForm(rnRForm);
    }
}