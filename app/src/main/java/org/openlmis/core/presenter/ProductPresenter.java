/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.presenter;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RegimeShortCode;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.RegimeProductViewModel;
import org.roboguice.shaded.goole.common.collect.ImmutableList;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ProductPresenter extends Presenter {

  @Inject
  private RegimenRepository regimenRepository;

  @Inject
  private StockRepository stockRepository;

  @Override
  public void attachView(BaseView v) {
    // do nothing
  }

  @SuppressWarnings("squid:S1905")
  public Observable<List<RegimeProductViewModel>> loadRegimeProducts(Regimen.RegimeType type) {
    return Observable.create((Observable.OnSubscribe<List<RegimeProductViewModel>>) subscriber -> {
      try {
        List<RegimeShortCode> regimeShortCodes = regimenRepository.listRegimeShortCode(type);
        List<RegimeProductViewModel> regimeProductViewModels = new ArrayList<>();
        for (RegimeShortCode item : regimeShortCodes) {
          RegimeProductViewModel regimeProductViewModel = new RegimeProductViewModel(
              item.getShortCode());
          regimeProductViewModels.add(regimeProductViewModel);
        }
        subscriber.onNext(regimeProductViewModels);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "ProductPresenter.loadRegimeProducts").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  @SuppressWarnings("squid:S1905")
  public Observable<Regimen> saveRegimes(RegimeProductViewModel viewModel,
      final Regimen.RegimeType regimeType) {
    final String regimenName = viewModel.getShortCode();

    return Observable.create((Observable.OnSubscribe<Regimen>) subscriber -> {
      Regimen regimen = null;
      try {
        regimen = regimenRepository.getByNameAndCategory(regimenName, regimeType);
        if (regimen == null) {
          regimen = new Regimen();
          regimen.setType(regimeType);
          regimen.setCode(regimeType + regimenName);
          regimen.setName(regimenName);
          regimen.setCustom(true);
          regimenRepository.create(regimen);
        }
      } catch (LMISException e) {
        new LMISException(e, "ProductPresenter.saveRegimes").reportToFabric();
        subscriber.onError(e);
      }
      subscriber.onNext(regimen);
      subscriber.onCompleted();
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  @SuppressWarnings("squid:S1905")
  public Observable<List<InventoryViewModel>> loadEmergencyProducts() {
    return Observable.create((Observable.OnSubscribe<List<InventoryViewModel>>) subscriber -> {
      try {
        ImmutableList<InventoryViewModel> inventoryViewModels = from(
            stockRepository.listEmergencyStockCards()).transform(InventoryViewModel::buildEmergencyModel).toList();
        subscriber.onNext(inventoryViewModels);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "ProductPresenter.loadEmergencyProducts").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }
}
