package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.BaseView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UnpackKitPresenter extends Presenter {
    UnpackKitView view;

    @Inject
    private ProductRepository productRepository;

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
        view = (UnpackKitView) v;
    }

    public UnpackKitPresenter() {
    }

    public Observable<List<Product>> loadKitProducts(final String kitCode) {
        return Observable.create(new Observable.OnSubscribe<List<Product>>() {
            @Override
            public void call(Subscriber<? super List<Product>> subscriber) {
                try {
                    List<KitProduct> kitProducts = productRepository.queryKitProductByKitCode(kitCode);

                    List<Product> products = new ArrayList<>();
                    for (KitProduct kitProduct : kitProducts) {
                        Product product = productRepository.getByCode(kitProduct.getProductCode());
                        products.add(product);
                    }

                    subscriber.onNext(products);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public interface UnpackKitView extends BaseView {
    }
}
