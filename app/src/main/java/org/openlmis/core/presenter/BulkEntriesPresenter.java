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

import android.util.Log;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.DraftBulkEntriesProduct;
import org.openlmis.core.model.DraftBulkEntriesProductLotItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.BulkEntriesRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BulkEntriesPresenter extends Presenter {

  private static final String TAG = BulkEntriesPresenter.class.getSimpleName();
  @Inject
  StockRepository stockRepository;
  @Inject
  BulkEntriesRepository bulkEntriesRepository;
  @Getter
  private List<BulkEntriesViewModel> bulkEntriesViewModels;

  public BulkEntriesPresenter() {
    this.bulkEntriesViewModels = new ArrayList<>();
  }

  @Override
  public void attachView(BaseView v) {
    // do nothing
  }

  public Observable<List<BulkEntriesViewModel>> getBulkEntriesViewModelsFromDraft() {
    return Observable
        .create((Observable.OnSubscribe<List<BulkEntriesViewModel>>) subscriber -> {
          try {
            restoreDraftInventory();
            subscriber.onNext(bulkEntriesViewModels);
            subscriber.onCompleted();
          } catch (Exception e) {
            subscriber.onError(e);
          }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public List<String> getAddedProductCodes() {
    return from(bulkEntriesViewModels).transform(viewModel -> viewModel.getProduct().getCode())
        .toList();
  }

  public void addNewProductsToBulkEntriesViewModels(List<Product> addedProducts) {
    bulkEntriesViewModels.addAll(
        from(addedProducts).transform(this::convertProductToBulkEntriesViewModel)
            .toList());
  }

  public Observable<Object> saveDraftBulkEntriesObservable() {
    return Observable.create(subscriber -> {
      try {
        bulkEntriesRepository.clearBulkEntriesDraft();
        for (BulkEntriesViewModel bulkEntriesViewModel : bulkEntriesViewModels) {
          bulkEntriesRepository.createBulkEntriesProductDraft(
              new DraftBulkEntriesProduct(bulkEntriesViewModel));
        }
        subscriber.onNext(null);
        subscriber.onCompleted();
      } catch (LMISException e) {
        Log.w(TAG, e);
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }

  protected void restoreDraftInventory() throws LMISException {
    List<DraftBulkEntriesProduct> draftBulkEntriesProducts = bulkEntriesRepository
        .queryAllBulkEntriesDraft();

    bulkEntriesViewModels.addAll(FluentIterable.from(draftBulkEntriesProducts).transform(
        draftBulkEntriesProduct -> new BulkEntriesViewModel(draftBulkEntriesProduct.getProduct(),
            draftBulkEntriesProduct.isDone(), draftBulkEntriesProduct.getQuantity(),
            convertDraftLotMovementToLotMovement(
                draftBulkEntriesProduct.getDraftLotItemListWrapper()))).toList());

  }

  private BulkEntriesViewModel convertProductToBulkEntriesViewModel(Product product) {
    try {
      BulkEntriesViewModel viewModel;
      StockCard stockCard = stockRepository.queryStockCardByProductId(product.getId());
      if (stockCard != null) {
        viewModel = new BulkEntriesViewModel(stockCard);
      } else {
        viewModel = new BulkEntriesViewModel(product);
      }
      viewModel.setChecked(false);
      setExistingLotViewModels(viewModel);
      return viewModel;
    } catch (LMISException e) {
      new LMISException(e, "BulkEntriesPresenter.convertProductToBulkEntriesViewModel")
          .reportToFabric();
    }
    return null;
  }

  private List<LotMovementViewModel> convertDraftLotMovementToLotMovement(
      List<DraftBulkEntriesProductLotItem> draftBulkEntriesProductLotItems) {
    return FluentIterable.from(draftBulkEntriesProductLotItems)
        .transform(draftBulkEntriesProductLotItem -> LotMovementViewModel.builder()
            .documentNumber(draftBulkEntriesProductLotItem.getDocumentNumber())
            .lotNumber(draftBulkEntriesProductLotItem.getLotNumber())
            .expiryDate(DateUtil.formatDate(draftBulkEntriesProductLotItem.getExpirationDate(),
                DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR))
            .movementReason(draftBulkEntriesProductLotItem.getReason())
            .quantity(draftBulkEntriesProductLotItem.getQuantity().toString())
            .lotSoh(draftBulkEntriesProductLotItem.getLotSoh().toString())
            .build()).toList();
  }

  private void setExistingLotViewModels(BulkEntriesViewModel bulkEntriesViewModel) {
    if (bulkEntriesViewModel.getStockCard() == null) {
      return;
    }
    List<LotMovementViewModel> lotMovementViewModels = FluentIterable
        .from(bulkEntriesViewModel.getStockCard().getNonEmptyLotOnHandList())
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
    bulkEntriesViewModel.setExistingLotMovementViewModelList(lotMovementViewModels);
  }


}
