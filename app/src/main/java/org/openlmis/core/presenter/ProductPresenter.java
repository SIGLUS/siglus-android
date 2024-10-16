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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Emergency;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
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

  @Inject
  private RnrFormRepository rnrFormRepository;

  @Inject
  private PodRepository podRepository;

  @Override
  public void attachView(BaseView v) {
    // do nothing
  }

  @SuppressWarnings("squid:S1905")
  public Observable<List<RegimeProductViewModel>> loadRegimeProducts(String programCode, Regimen.RegimeType type) {
    return Observable.create((Observable.OnSubscribe<List<RegimeProductViewModel>>) subscriber -> {
      try {
        List<Regimen> regimens = regimenRepository.listNonCustomRegimen(programCode, type);
        List<RegimeProductViewModel> regimeProductViewModels = new ArrayList<>();
        for (Regimen item : regimens) {
          RegimeProductViewModel regimeProductViewModel = new RegimeProductViewModel(
              item.getName());
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

  public Observable<Regimen> saveRegimes(RegimeProductViewModel viewModel, Regimen.RegimeType regimeType) {
    String regimenName = viewModel.getShortCode();
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
        List<StockCard> allEmergencyStockCards = stockRepository.listEmergencyStockCards();

        List<Long> invalidProductIds = getInvalidEmergencyProductsForLatestPeriod();
        if (invalidProductIds != null && !invalidProductIds.isEmpty()) {
          allEmergencyStockCards = from(allEmergencyStockCards)
              .filter(stockCard -> stockCard != null
                  && !invalidProductIds.contains(stockCard.getProduct().getId()))
              .toList();
        }

        ImmutableList<InventoryViewModel> inventoryViewModels = from(allEmergencyStockCards)
            .transform(InventoryViewModel::buildEmergencyModel)
            .toList();
        subscriber.onNext(inventoryViewModels);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "ProductPresenter.loadEmergencyProducts").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  @Nullable
  private List<Long> getInvalidEmergencyProductsForLatestPeriod() throws LMISException {
    String programCode = Program.VIA_CODE;
    List<RnRForm> allViaRnRForms = rnrFormRepository.listInclude(Emergency.NO, programCode);

    if (allViaRnRForms == null) {
      return null;
    }
    RnRForm latestViaRnrForm = from(allViaRnRForms)
        .last()
        .orNull();

    if (latestViaRnrForm == null) {
      return null;
    }

    Date firstDayOfCurrentMonth = DateUtil.getFirstDayForCurrentMonthByDate(
        latestViaRnrForm.getPeriodBegin());
    List<Pod> regularPods = podRepository.queryRegularRemotePodsByProgramCodeAndPeriod(
        programCode, firstDayOfCurrentMonth
    );

    if (regularPods == null || regularPods.isEmpty()) {
      return null;
    }

    List<Long> invalidProductIds = new ArrayList<>();
    HashMap<Long, Long> productIdToShippedQuantityPair = new HashMap<>();
    HashMap<Long, Long> productIdToOrderQuantityPair = new HashMap<>();

    collectShippedAndOrderQuantityBySubPods(
        regularPods, productIdToShippedQuantityPair, productIdToOrderQuantityPair
    );

    tryToAddPossibleInvalidProducts(
        invalidProductIds, productIdToShippedQuantityPair, productIdToOrderQuantityPair
    );

    return invalidProductIds;
  }

  private void tryToAddPossibleInvalidProducts(
      @NonNull List<Long> invalidProductIds,
      @NonNull HashMap<Long, Long> productIdToShippedQuantityPair,
      @NonNull HashMap<Long, Long> productIdToOrderQuantityPair
  ) {
    for (Long productId : productIdToShippedQuantityPair.keySet()) {
      if (productId != null && !invalidProductIds.contains(productId)) {
        Long shippedQuantity = productIdToShippedQuantityPair.get(productId);
        Long orderQuantity = productIdToOrderQuantityPair.get(productId);

        if (shippedQuantity != null && orderQuantity != null && shippedQuantity < orderQuantity) {
          invalidProductIds.add(productId);
        }
      }
    }
  }

  private void collectShippedAndOrderQuantityBySubPods(
      @NonNull List<Pod> subpodList,
      @NonNull HashMap<Long, Long> productIdToShippedQuantityPair,
      @NonNull HashMap<Long, Long> productIdToOrderQuantityPair
  ) {
    for (Pod subpod : subpodList) {
      for (PodProductItem podProductItem : subpod.getPodProductItemsWrapper()) {
        Long productId = podProductItem.getProduct().getId();
        // shipped quantity
        long currentAcceptedQuantity = podProductItem.getSumShippedQuantity();
        Long currentProductQuantity = productIdToShippedQuantityPair.get(productId);
        if (currentProductQuantity != null) {
          productIdToShippedQuantityPair.put(
              productId, currentProductQuantity + currentAcceptedQuantity
          );
        } else {
          productIdToShippedQuantityPair.put(productId, currentAcceptedQuantity);
        }
        // order quantity
        if (productIdToOrderQuantityPair.get(productId) == null) {
          productIdToOrderQuantityPair.put(productId, podProductItem.getOrderedQuantity());
        }
      }
    }
  }
}
