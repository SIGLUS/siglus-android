package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class ProductPresenter extends Presenter {

    @Inject
    private ProductRepository productRepository;

    @Inject
    private ProgramRepository programRepository;

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {}

    public Observable<List<InventoryViewModel>> loadMMIAProducts() {
        return Observable.create(new Observable.OnSubscribe<List<InventoryViewModel>>() {
            @Override
            public void call(Subscriber<? super List<InventoryViewModel>> subscriber) {
                try {
                    List<Product> products = productRepository.queryProducts(programRepository.queryByCode(Constants.MMIA_PROGRAM_CODE).getId());
                    List<InventoryViewModel> inventoryViewModels = from(products).transform(new Function<Product, InventoryViewModel>() {
                        @Override
                        public InventoryViewModel apply(Product product) {
                            return new InventoryViewModel(product);
                        }
                    }).toList();
                    subscriber.onNext(inventoryViewModels);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public void saveRegimes() {

    }
}
