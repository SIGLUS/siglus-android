package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AddDrugsToVIAPresenter extends Presenter {

    @Inject
    private ProductRepository productRepository;

    ArrayList<RnrFormItem> addedRnrFormItemsInVIA = new ArrayList<>();

    AddDrugsToVIAView view;

    public AddDrugsToVIAPresenter() {
    }

    @Override
    public void attachView(BaseView v) {
        view = (AddDrugsToVIAView) v;
    }

    public Observable<List<InventoryViewModel>> loadActiveProductsNotInVIAForm(final List<String> existingProducts) {
        return Observable.create(new Observable.OnSubscribe<List<InventoryViewModel>>() {
            @Override
            public void call(final Subscriber<? super List<InventoryViewModel>> subscriber) {
                try {
                    List<InventoryViewModel> productsNotInVIAForm = FluentIterable.from(productRepository.queryActiveProductsInVIAProgramButNotInDraftVIAForm())
                            .filter(new Predicate<Product>() {
                                @Override
                                public boolean apply(Product product) {
                                    return !existingProducts.contains(product.getCode());
                                }
                            })
                            .transform(new Function<Product, InventoryViewModel>() {
                                @Override
                                public InventoryViewModel apply(Product product) {
                                    return new InventoryViewModel(product);
                                }
                            }).toList();
                    subscriber.onNext(productsNotInVIAForm);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    protected Action1<Object> nextMainPageAction = new Action1<Object>() {
        @Override
        public void call(Object o) {
            view.loaded();
            view.goToParentPage(addedRnrFormItemsInVIA);
        }
    };

    protected Action1<Throwable> errorAction = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            view.loaded();
            view.showErrorMessage(throwable.getMessage());
        }
    };

    public void convertViewModelsToDataAndPassToParentScreen(List<InventoryViewModel> checkedViewModels) {
        if (view.validateInventory()) {
            view.loading();
            Subscription subscription = convertViewModelsToRnrFormItems(checkedViewModels).subscribe(nextMainPageAction, errorAction);
            subscriptions.add(subscription);
        }
    }

    protected Observable convertViewModelsToRnrFormItems(final List<InventoryViewModel> inventoryViewModels) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                addedRnrFormItemsInVIA = new ArrayList<>(FluentIterable.from(inventoryViewModels).transform(new Function<InventoryViewModel, RnrFormItem>() {
                    @Override
                    public RnrFormItem apply(InventoryViewModel inventoryViewModel) {
                        RnrFormItem rnrFormItem = new RnrFormItem();
                        try {
                            Product product = productRepository.getByCode(inventoryViewModel.getFnm());
                            rnrFormItem.setProduct(product);
                            rnrFormItem.setRequestAmount(Long.valueOf(inventoryViewModel.getQuantity()));
                        } catch (LMISException e) {
                            e.reportToFabric();
                        }
                        return rnrFormItem;
                    }
                }).toList());

                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public interface AddDrugsToVIAView extends BaseView {

        boolean validateInventory();

        void goToParentPage(ArrayList<RnrFormItem> addedRnrFormItemsInVIAs);

        void showErrorMessage(String message);
    }
}
