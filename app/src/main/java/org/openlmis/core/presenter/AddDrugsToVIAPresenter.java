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

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.AddDrugsToViaInventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddDrugsToVIAPresenter extends Presenter {

  @Inject
  private ProductRepository productRepository;

  @Getter
  final List<InventoryViewModel> inventoryViewModelList = new ArrayList<>();

  @Override
  public void attachView(BaseView v) {
    // do nothing
  }

  @SuppressWarnings("squid:S1905")
  public Observable<Void> loadActiveProductsNotInVIAForm(final List<String> existingProducts) {
    return Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
      try {
        inventoryViewModelList.addAll(FluentIterable
            .from(productRepository.queryActiveProductsInVIAProgramButNotInDraftVIAForm())
            .filter(product -> !existingProducts.contains(product.getCode()))
            .transform(AddDrugsToViaInventoryViewModel::new)
            .toList());
        subscriber.onNext(null);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "AddDrugsToVIAPresenter,loadActiveProductsNotInVIAForm")
            .reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public Observable<ArrayList<RnrFormItem>> convertViewModelsToRnrFormItems() {
    return Observable.just(new ArrayList<>(
        FluentIterable.from(inventoryViewModelList).filter(InventoryViewModel::isChecked)
            .transform(inventoryViewModel -> {
              RnrFormItem rnrFormItem = new RnrFormItem();
              try {
                Product product = productRepository.getByCode(inventoryViewModel.getFnm());
                rnrFormItem.setProduct(product);
                rnrFormItem.setRequestAmount(
                    Long.valueOf(
                        ((AddDrugsToViaInventoryViewModel) inventoryViewModel).getQuantity()));
              } catch (LMISException e) {
                new LMISException(e, "AddDrugsToVIAPresenter,convertViewModelsToRnrFormItems")
                    .reportToFabric();
              }
              return rnrFormItem;
            }).toList()))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }
}
