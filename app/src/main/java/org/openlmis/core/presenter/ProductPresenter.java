package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.RegimeProduct;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RegimeProductViewModel;
import org.roboguice.shaded.goole.common.base.Function;

import java.util.ArrayList;
import java.util.List;

import roboguice.util.Strings;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

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
            public void call(Subscriber<? super List<RegimeProductViewModel>> subscriber) {
                try {
                    List<RegimeProduct> products = getRegimeProducts();
                    List<RegimeProductViewModel> regimeProductViewModels = from(products).transform(new Function<RegimeProduct, RegimeProductViewModel>() {
                        @Override
                        public RegimeProductViewModel apply(RegimeProduct product) {
                            return new RegimeProductViewModel(product);
                        }
                    }).toList();
                    subscriber.onNext(regimeProductViewModels);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private ArrayList<RegimeProduct> getRegimeProducts() throws LMISException {
        return newArrayList(
                new RegimeProduct("3TC 150mg", "Lamivudina 150mg"),
                new RegimeProduct("3TC 300 mg", "Lamivudina 300mg"),
                new RegimeProduct("AZT 300 mg", "zidovudina 300mg"),
                new RegimeProduct("NVP 200 mg", "Nevirapina 200mg"),
                new RegimeProduct("TDF 300 mg", "Tenofovir 300mg"),
                new RegimeProduct("EFV 600 mg", "Efavirenze 600mg"),
                new RegimeProduct("Lpv/r 200/50mg", "Lopinavir/Ritonavir 200/50 mg"),
                new RegimeProduct("ABC 300mg", "Abacavir 300mg"),
                new RegimeProduct("D4T 30mg", "Stavudina 30mg"),
                new RegimeProduct("3TC30mg", "Lamivudina 30mg"),
                new RegimeProduct("D4T 6mg", "Stavudina 6mg"),
                new RegimeProduct("AZT60mg", "Zidovudina60mg"),
                new RegimeProduct("NVP 50mg", "Nevirapina 50mg"),
                new RegimeProduct("Lpv/r 100/25mg", "Lopinavir/Ritonavir 100/25mg"),
                new RegimeProduct("Lpv/r 80/20mL Solucao oral", "Lopinavir/Ritonavir 80/20mL Solucao oral"),
                new RegimeProduct("EFV 200mg", "Efavirenze 200mg"),
                new RegimeProduct("EFV 50mg", "Efavirenze 50mg"),
                new RegimeProduct("ABC60mg", "Abacavir 60mg"),
                new RegimeProduct("NVP 50mg/5ml sol oral", "Nevirapina 50mg/5ml sol oral"),
                new RegimeProduct("AZT 50mg/5ml sol oral", "zidovudina 50mg/5ml sol oral"));
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
