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
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import rx.Observable;
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
        return Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
            try {
                inventoryViewModelList.addAll(FluentIterable
                        .from(productRepository.queryActiveProductsInVIAProgramButNotInDraftVIAForm())
                        .filter(product -> !existingProducts.contains(product.getCode()))
                        .transform((Function<Product, InventoryViewModel>) product -> new AddDrugsToViaInventoryViewModel(product))
                        .toList());
                subscriber.onNext(null);
                subscriber.onCompleted();
            } catch (LMISException e) {
                new LMISException(e, "AddDrugsToVIAPresenter,loadActiveProductsNotInVIAForm").reportToFabric();
                subscriber.onError(e);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public Observable<ArrayList<RnrFormItem>> convertViewModelsToRnrFormItems() {
        return Observable.just(new ArrayList<>(
                FluentIterable.from(inventoryViewModelList)
                        .filter(viewModel -> viewModel.isChecked()).transform(inventoryViewModel -> {
                    RnrFormItem rnrFormItem = new RnrFormItem();
                    try {
                        Product product = productRepository.getByCode(inventoryViewModel.getFnm());
                        rnrFormItem.setProduct(product);
                        rnrFormItem.setRequestAmount(Long.valueOf(((AddDrugsToViaInventoryViewModel) inventoryViewModel).getQuantity()));
                    } catch (LMISException e) {
                        new LMISException(e, "AddDrugsToVIAPresenter,convertViewModelsToRnrFormItems").reportToFabric();
                    }
                    return rnrFormItem;
                }).toList()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
