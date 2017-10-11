package org.openlmis.core.presenter;

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
import rx.Subscriber;
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

    public Observable<List<NonBasicProductsViewModel>> getAllNonBasicProductsViewModels() {
        return Observable.create(new Observable.OnSubscribe<List<NonBasicProductsViewModel>>() {
            @Override
            public void call(Subscriber<? super List<NonBasicProductsViewModel>> subscriber) {
                try {
                    List<Product> products = productRepository.listNonBasicProducts();
                    for (Product product : products) {
                        NonBasicProductsViewModel currentModel = new NonBasicProductsViewModel(product);
                        models.add(currentModel);
                    }
                    subscriber.onNext(models);
                    subscriber.onCompleted();
                }catch (Exception e){
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }
}
