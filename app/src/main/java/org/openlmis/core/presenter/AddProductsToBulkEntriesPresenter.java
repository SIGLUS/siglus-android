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
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.ProductsToBulkEntriesViewModel;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddProductsToBulkEntriesPresenter extends Presenter {

  @Getter
  private final List<ProductsToBulkEntriesViewModel> models;

  @Inject
  private ProductRepository productRepository;

  @Inject
  public AddProductsToBulkEntriesPresenter() {
    this.models = new ArrayList<>();
  }

  @Override
  public void attachView(BaseView v) {
    // do nothing
  }

  public Observable<List<ProductsToBulkEntriesViewModel>> getProducts(List<String> addedProducts,
      boolean isFromBulkIssue) {
    return Observable
        .create((Observable.OnSubscribe<List<ProductsToBulkEntriesViewModel>>) subscriber -> {
          try {
            List<Product> products;
            if (isFromBulkIssue) {
              products = productRepository.queryProductsInStockCard();
            } else {
              products = productRepository.listAllProductsWithoutKit();
            }
            Collections.sort(products);
            for (Product product : products) {
              if (addedProducts.contains(product.getCode())) {
                continue;
              }
              ProductsToBulkEntriesViewModel currentModel = new ProductsToBulkEntriesViewModel(product);
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
