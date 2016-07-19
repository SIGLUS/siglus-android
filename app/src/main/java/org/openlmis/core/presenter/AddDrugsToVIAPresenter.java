package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

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

    @Inject
    private RnrFormItemRepository rnrFormItemRepository;

    AddDrugsToVIAView view;

    @Override
    public void attachView(BaseView v) {
        view = (AddDrugsToVIAView) v;
    }

    public Observable<List<InventoryViewModel>> loadActiveProductsNotInVIAForm() {
        return Observable.create(new Observable.OnSubscribe<List<InventoryViewModel>>() {
            @Override
            public void call(final Subscriber<? super List<InventoryViewModel>> subscriber) {
                try {
                    List<InventoryViewModel> productsNotInVIAForm = FluentIterable.from(productRepository.queryActiveProductsInVIAProgramButNotInDraftVIAForm()).transform(new Function<Product, InventoryViewModel>() {
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

    public void generateNewVIAItems(List<InventoryViewModel> checkedViewModels) throws LMISException {
        if (view.validateInventory()) {
            view.loading();
            Subscription subscription = saveRnrItemsObservable(checkedViewModels).subscribe(nextMainPageAction, errorAction);
            subscriptions.add(subscription);
        }
    }

    protected Action1<Object> nextMainPageAction = new Action1<Object>() {
        @Override
        public void call(Object o) {
            view.loaded();
            view.goToParentPage();
        }
    };

    protected Action1<Throwable> errorAction = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            view.loaded();
            view.showErrorMessage(throwable.getMessage());
        }
    };

    private void convertViewModelsToRnrItemsAndSave(List<InventoryViewModel> viewModels) throws LMISException {
        List<RnrFormItem> rnrFormItemList = FluentIterable.from(viewModels).transform(new Function<InventoryViewModel, RnrFormItem>() {
            @Override
            public RnrFormItem apply(InventoryViewModel inventoryViewModel) {
                RnrFormItem rnrFormItem = new RnrFormItem();
                rnrFormItem.setProduct(inventoryViewModel.getProduct());
                rnrFormItem.setRequestAmount(Long.parseLong(inventoryViewModel.getQuantity()));
                return rnrFormItem;
            }
        }).toList();
        rnrFormItemRepository.batchCreateOrUpdate(rnrFormItemList);
    }

    protected Observable saveRnrItemsObservable(final List<InventoryViewModel> viewModels) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                try {
                    convertViewModelsToRnrItemsAndSave(viewModels);
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                    e.reportToFabric();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public interface AddDrugsToVIAView extends BaseView {

        boolean validateInventory();

        void goToParentPage();

        void showErrorMessage(String message);
    }
}
