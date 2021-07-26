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
import lombok.Getter;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.NonBasicProductsViewModel;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddNonBasicProductsPresenter extends Presenter {

  @Getter
  private final List<NonBasicProductsViewModel> models;

  @Inject
  private ProductRepository productRepository;

  public AddNonBasicProductsPresenter() {
    models = new ArrayList<>();
  }

  @Override
  public void attachView(BaseView v) {
    // do nothing
  }

  public Observable<List<NonBasicProductsViewModel>> getAllNonBasicProductsViewModels(
      final List<String> selectedProducts) {
    return Observable
        .create((Observable.OnSubscribe<List<NonBasicProductsViewModel>>) subscriber -> {
          try {
            List<Product> products = from(productRepository.listNonBasicProducts())
                .filter(product -> !product.isKit()).toList();
            for (Product product : products) {
              if (selectedProducts.contains(product.getCode())) {
                continue;
              }
              NonBasicProductsViewModel currentModel = new NonBasicProductsViewModel(product);
              models.add(currentModel);
            }
            subscriber.onNext(models);
            subscriber.onCompleted();
          } catch (Exception e) {
            subscriber.onError(e);
          }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }
}
