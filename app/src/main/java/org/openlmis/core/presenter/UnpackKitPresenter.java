package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

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

    public Observable<List<StockCardViewModel>> loadKitProducts(final String kitCode) {
        return Observable.create(new Observable.OnSubscribe<List<StockCardViewModel>>() {
            @Override
            public void call(Subscriber<? super List<StockCardViewModel>> subscriber) {
                try {
                    List<KitProduct> kitProducts = productRepository.queryKitProductByKitCode(kitCode);

                    List<StockCardViewModel> stockCardViewModels = new ArrayList<>();

                    for (KitProduct kitProduct : kitProducts) {
                        Product product = productRepository.getByCode(kitProduct.getProductCode());
                        StockCardViewModel stockCardViewModel = new StockCardViewModel(product);
                        stockCardViewModel.setStockOnHand(kitProduct.getQuantity());
                        stockCardViewModels.add(stockCardViewModel);
                    }

                    subscriber.onNext(stockCardViewModels);
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
