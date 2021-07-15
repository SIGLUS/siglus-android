package org.openlmis.core.presenter;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.NonBasicProductsViewModel;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddNonBasicProductsPresenter extends Presenter {

    @Getter
    private List<NonBasicProductsViewModel> models;

    @Inject
    private ProductRepository productRepository;

    public AddNonBasicProductsPresenter() {
        models = new ArrayList<>();
    }

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {

    }

    public Observable<List<NonBasicProductsViewModel>> getAllNonBasicProductsViewModels(final List<String> selectedProducts) {
        return Observable.create((Observable.OnSubscribe<List<NonBasicProductsViewModel>>) subscriber -> {
            try {
                List<Product> products = from(productRepository.listNonBasicProducts())
                    .filter(product -> !product.isKit()).toList();
                for (Product product : products) {
                    if (selectedProducts.contains(product.getCode())) {
                        continue;
                    }
                    NonBasicProductsViewModel currentModel = new NonBasicProductsViewModel(product);
                    models.add(currentModel);
                }
                subscriber.onNext(models);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }
}
