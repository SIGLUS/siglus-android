package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.AddedDrugInVIA;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.helper.RnrFormHelper;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Date;
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

    @Inject
    private StockRepository stockRepository;

    @Inject
    private RnrFormHelper rnrFormHelper;

    ArrayList<AddedDrugInVIA> addedDrugsInVIA = new ArrayList<>();

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

    protected Action1<Object> nextMainPageAction = new Action1<Object>() {
        @Override
        public void call(Object o) {
            view.loaded();
            view.goToParentPage(addedDrugsInVIA);
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
            Subscription subscription = convertViewModelsToParcelable(checkedViewModels).subscribe(nextMainPageAction, errorAction);
            subscriptions.add(subscription);
        }
    }

    protected Observable convertViewModelsToParcelable(final List<InventoryViewModel> inventoryViewModels) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                addedDrugsInVIA = new ArrayList<>(FluentIterable.from(inventoryViewModels).transform(new Function<InventoryViewModel, AddedDrugInVIA>() {
                    @Override
                    public AddedDrugInVIA apply(InventoryViewModel inventoryViewModel) {
                        return new AddedDrugInVIA(inventoryViewModel.getProduct().getCode(), Long.parseLong(inventoryViewModel.getQuantity()));
                    }
                }).toList());

                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public interface AddDrugsToVIAView extends BaseView {

        boolean validateInventory();

        void goToParentPage(ArrayList<AddedDrugInVIA> addedDrugInVIAList);

        void showErrorMessage(String message);
    }
}
