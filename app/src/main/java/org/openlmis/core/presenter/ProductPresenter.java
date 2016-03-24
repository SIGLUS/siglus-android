package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;

import java.util.ArrayList;
import java.util.List;

import roboguice.util.Strings;
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

    @Inject
    private RegimenRepository regimenRepository;

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
    }

    public Observable<List<InventoryViewModel>> loadRegimeProducts() {
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

    public Observable<Regimen> saveRegimes(List<InventoryViewModel> viewModels, final Regimen.RegimeType regimeType) {
        final String regimenName = generateRegimeName(viewModels);

        return Observable.create(new Observable.OnSubscribe<Regimen>() {
            @Override
            public void call(Subscriber<? super Regimen> subscriber) {
                try {
                    Regimen regimen = regimenRepository.getByName(regimenName);
                    if (regimen == null) {
                        regimen = new Regimen();
                        regimen.setType(regimeType);
                        regimen.setName(regimenName);
                        regimenRepository.create(regimen);
                    }
                    subscriber.onNext(regimen);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private String generateRegimeName(List<InventoryViewModel> viewModels) {
        List<String> list = new ArrayList<>();
        for (InventoryViewModel model : viewModels) {
            list.add(model.getProduct().getCode());
        }
        return Strings.join("+", list);
    }
}
