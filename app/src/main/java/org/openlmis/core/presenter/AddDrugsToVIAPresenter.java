package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.AddDrugsToViaInventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddDrugsToVIAPresenter extends Presenter {

    @Inject
    private ProductRepository productRepository;

    @Getter
    final List<InventoryViewModel> inventoryViewModelList = new ArrayList<>();

    public AddDrugsToVIAPresenter() {
    }

    @Override
    public void attachView(BaseView v) {
    }

    public Observable<Void> loadActiveProductsNotInVIAForm(final List<String> existingProducts) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                try {
                    inventoryViewModelList.addAll(FluentIterable.from(productRepository.queryActiveProductsInVIAProgramButNotInDraftVIAForm())
                            .filter(new Predicate<Product>() {
                                @Override
                                public boolean apply(Product product) {
                                    return !existingProducts.contains(product.getCode());
                                }
                            })
                            .transform(new Function<Product, InventoryViewModel>() {
                                @Override
                                public InventoryViewModel apply(Product product) {
                                    return new AddDrugsToViaInventoryViewModel(product);
                                }
                            }).toList());
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public Observable<ArrayList<RnrFormItem>> convertViewModelsToRnrFormItems() {
        return Observable.just(new ArrayList<>(
                FluentIterable.from(inventoryViewModelList).filter(new Predicate<InventoryViewModel>() {
                    @Override
                    public boolean apply(InventoryViewModel viewModel) {
                        return viewModel.isChecked();
                    }
                }).transform(new Function<InventoryViewModel, RnrFormItem>() {
                    @Override
                    public RnrFormItem apply(InventoryViewModel inventoryViewModel) {
                        RnrFormItem rnrFormItem = new RnrFormItem();
                        try {
                            Product product = productRepository.getByCode(inventoryViewModel.getFnm());
                            rnrFormItem.setProduct(product);
                            rnrFormItem.setRequestAmount(Long.valueOf(((AddDrugsToViaInventoryViewModel) inventoryViewModel).getQuantity()));
                        } catch (LMISException e) {
                            e.reportToFabric();
                        }
                        return rnrFormItem;
                    }
                }).toList()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
