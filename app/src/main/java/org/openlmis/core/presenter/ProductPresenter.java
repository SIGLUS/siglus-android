package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.RegimeShortCode;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RegimeProductViewModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.util.Strings;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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

    public Observable<List<RegimeProductViewModel>> loadRegimeProducts() {
        return Observable.create(new Observable.OnSubscribe<List<RegimeProductViewModel>>() {
            @Override
            public void call(final Subscriber<? super List<RegimeProductViewModel>> subscriber) {
                try {
                    List<RegimeShortCode> regimeShortCodes = regimenRepository.listRegimeShortCode();
                    List<RegimeProductViewModel> regimeProductViewModels = new ArrayList<>();
                    for (RegimeShortCode item : regimeShortCodes) {
                        RegimeProductViewModel regimeProductViewModel = new RegimeProductViewModel(item.getShortCode(), productRepository.getByCode(item.getCode()).getPrimaryName());
                        regimeProductViewModels.add(regimeProductViewModel);
                    }
                    subscriber.onNext(regimeProductViewModels);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public Observable<Regimen> saveRegimes(List<RegimeProductViewModel> viewModels, final Regimen.RegimeType regimeType) {
        final String regimenName = generateRegimeName(viewModels);

        return Observable.create(new Observable.OnSubscribe<Regimen>() {
            @Override
            public void call(Subscriber<? super Regimen> subscriber) {
                try {
                    Regimen regimen = regimenRepository.getByNameAndCategory(regimenName, regimeType);
                    if (regimen == null) {
                        regimen = new Regimen();
                        regimen.setType(regimeType);
                        regimen.setName(regimenName);
                        regimen.setCustom(true);
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

    private String generateRegimeName(List<RegimeProductViewModel> viewModels) {
        List<String> list = new ArrayList<>();
        for (RegimeProductViewModel model : viewModels) {
            list.add(model.getShortCode());
        }
        return Strings.join("+", list);
    }
}
