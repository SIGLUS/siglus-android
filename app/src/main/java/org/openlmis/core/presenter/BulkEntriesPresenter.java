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
import java.util.Date;
import java.util.List;
import lombok.Getter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BulkEntriesPresenter extends Presenter {

  @Inject
  StockRepository stockRepository;

  @Getter
  private List<BulkEntriesViewModel> bulkEntriesViewModels;


  public BulkEntriesPresenter() {
    this.bulkEntriesViewModels = new ArrayList<>();
  }

  @Override
  public void attachView(BaseView v) throws ViewNotMatchException {

  }

  public Observable<List<BulkEntriesViewModel>> getAllAddedBulkEntriesViewModels(
      List<Product> addedProducts) {
    return Observable
        .create((Observable.OnSubscribe<List<BulkEntriesViewModel>>) subscriber -> {
          try {
            bulkEntriesViewModels.addAll(from(addedProducts)
                .transform(product -> convertProductToBulkEntriesViewModel(product)).toList());
            subscriber.onNext(bulkEntriesViewModels);
            subscriber.onCompleted();
          } catch (Exception e) {
            subscriber.onError(e);
          }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  private BulkEntriesViewModel convertProductToBulkEntriesViewModel(Product product) {
    try {
      BulkEntriesViewModel viewModel;
      viewModel = new BulkEntriesViewModel(
          stockRepository.queryStockCardByProductId(product.getId()));
      viewModel.setChecked(false);
      setExistingLotViewModels(viewModel);
      return viewModel;
    } catch (LMISException e) {
      new LMISException(e, "BulkEntriesPresenter.convertProductToBulkEntriesViewModel")
          .reportToFabric();
    }
    return null;
  }

  public List<Long> getAddedProductIds() {
    return from(bulkEntriesViewModels).transform(viewModel -> viewModel.getProduct().getId())
        .toList();
  }

  public void addNewProductsToBulkEntriesViewModels(List<Product> addedProducts) {
    bulkEntriesViewModels.addAll(
        from(addedProducts).transform(product -> convertProductToBulkEntriesViewModel(product))
            .toList());

  }

  private void setExistingLotViewModels(InventoryViewModel inventoryViewModel) {
    List<LotMovementViewModel> lotMovementViewModels = FluentIterable
        .from(inventoryViewModel.getStockCard().getNonEmptyLotOnHandList())
        .transform(lotOnHand -> new LotMovementViewModel(lotOnHand.getLot().getLotNumber(),
            DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(),
                DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR),
            lotOnHand.getQuantityOnHand().toString(),
            MovementReasonManager.MovementType.RECEIVE)).toSortedList((lot1, lot2) -> {
              Date localDate = DateUtil
                  .parseString(lot1.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);
              if (localDate != null) {
                return localDate.compareTo(DateUtil
                .parseString(lot2.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
              } else {
                return 0;
              }
            });
    inventoryViewModel.setExistingLotMovementViewModelList(lotMovementViewModels);
  }

}
