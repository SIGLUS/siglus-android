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

import androidx.annotation.NonNull;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.UnpackKitInventoryViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@SuppressWarnings("squid:S1905")
public class UnpackKitPresenter extends Presenter {

  @Inject
  private ProductRepository productRepository;

  @Inject
  private StockRepository stockRepository;

  protected String kitCode;

  @Getter
  protected final List<InventoryViewModel> inventoryViewModels = new ArrayList<>();

  @Override
  public void attachView(BaseView v) {
    // do nothing
  }

  public Observable<List<InventoryViewModel>> getKitProductsObservable(final String kitCode,
      final int kitNum) {
    return Observable.create((Observable.OnSubscribe<List<InventoryViewModel>>) subscriber -> {
      try {
        UnpackKitPresenter.this.kitCode = kitCode;
        inventoryViewModels.clear();
        List<KitProduct> kitProducts = productRepository.queryKitProductByKitCode(kitCode);
        for (KitProduct kitProduct : kitProducts) {
          final Product product = productRepository.getByCode(kitProduct.getProductCode());
          InventoryViewModel inventoryViewModel = new UnpackKitInventoryViewModel(product);
          setExistingLotViewModels(inventoryViewModel);
          inventoryViewModel.setKitExpectQuantity(kitProduct.getQuantity() * (long) kitNum);
          inventoryViewModel.setChecked(true);
          inventoryViewModels.add(inventoryViewModel);
        }

        subscriber.onNext(inventoryViewModels);
        subscriber.onCompleted();
      } catch (LMISException e) {
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public Observable<Void> saveUnpackProductsObservable(final int kitUnpackQuantity,
      final String documentNumber, final String signature) {
    return Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
      try {
        List<StockCard> stockCards = new ArrayList<>();
        Collection<StockCard> list = FluentIterable.from(inventoryViewModels)
            .filter(inventoryViewModel -> inventoryViewModel.getLotListQuantityTotalAmount() > 0)
            .transform(inventoryViewModel -> {
              try {
                return createStockCardForProductWithLot(inventoryViewModel, documentNumber,
                    signature);
              } catch (LMISException e) {
                subscriber.onError(e);
              }
              return null;
            }).toList();
        for (StockCard stockCard : list) {
          stockCards.add(stockCard);
        }

        stockCards.add(getStockCardForKit(kitUnpackQuantity, documentNumber, signature));
        stockRepository.batchSaveUnpackStockCardsWithMovementItemsAndUpdateProduct(stockCards);

        subscriber.onNext(null);
        subscriber.onCompleted();
      } catch (LMISException exception) {
        subscriber.onError(exception);
      }

    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  protected StockCard getStockCardForKit(int kitUnpackQuantity, String documentNumber,
      String signature) throws LMISException {
    Product kit = productRepository.getByCode(kitCode);
    StockCard kitStockCard = stockRepository.queryStockCardByProductId(kit.getId());

    kitStockCard.setStockOnHand(kitStockCard.getStockOnHand() - kitUnpackQuantity);

    if (0 == kitStockCard.getStockOnHand()) {
      kitStockCard.setExpireDates("");
    }

    StockMovementItem kitMovementItem = new StockMovementItem(kitStockCard);
    kitMovementItem.setReason(MovementReasonManager.UNPACK_KIT);
    kitMovementItem.setMovementType(MovementReasonManager.MovementType.ISSUE);
    kitMovementItem.setMovementQuantity(kitUnpackQuantity);
    kitMovementItem.setSignature(signature);
    kitMovementItem.setDocumentNumber(documentNumber);

    List<StockMovementItem> stockMovementItems = new ArrayList<>();
    stockMovementItems.add(kitMovementItem);

    kitStockCard.setStockMovementItemsWrapper(stockMovementItems);

    return kitStockCard;
  }

  @NonNull
  protected StockCard createStockCardForProductWithLot(InventoryViewModel inventoryViewModel,
      String documentNumber, String signature) throws LMISException {
    List<StockMovementItem> stockMovementItems = new ArrayList<>();

    StockCard stockCard = stockRepository.queryStockCardByProductId(inventoryViewModel.getProductId());
    if (stockCard == null) {
      stockCard = new StockCard();
      stockCard.setProduct(inventoryViewModel.getProduct());
      stockMovementItems.add(stockCard.generateInitialStockMovementItem());
    }
    stockCard.getProduct().setArchived(false);
    List<LotMovementViewModel> totalLotMovementViewModelList = new ArrayList<>();
    for (LotMovementViewModel lotMovementViewModel : inventoryViewModel.getExistingLotMovementViewModelList()) {
      final String quantity = lotMovementViewModel.getQuantity();
      if (quantity == null) {
        lotMovementViewModel.setQuantity("0");
      }
      totalLotMovementViewModelList.add(lotMovementViewModel);
    }
    totalLotMovementViewModelList.addAll(inventoryViewModel.getNewLotMovementViewModelList());
    stockMovementItems.add(
        createUnpackMovementItemAndLotMovement(stockCard, documentNumber, signature,
            totalLotMovementViewModelList));

    stockCard
        .setStockOnHand(stockMovementItems.get(stockMovementItems.size() - 1).getStockOnHand());

    stockCard.setStockMovementItemsWrapper(stockMovementItems);

    return stockCard;
  }

  @NonNull
  private StockMovementItem createUnpackMovementItemAndLotMovement(StockCard stockCard,
      String documentNumber, String signature,
      List<LotMovementViewModel> lotMovementViewModelList) {
    StockMovementItem unpackMovementItem = new StockMovementItem(stockCard);
    unpackMovementItem.setReason(MovementReasonManager.DDM);
    unpackMovementItem.setMovementType(MovementReasonManager.MovementType.RECEIVE);
    unpackMovementItem.setDocumentNumber(documentNumber);
    unpackMovementItem.setSignature(signature);
    unpackMovementItem.populateLotQuantitiesAndCalculateNewSOH(lotMovementViewModelList);
    return unpackMovementItem;
  }

  private void setExistingLotViewModels(InventoryViewModel inventoryViewModel)
      throws LMISException {
    StockCard stockCard = stockRepository
        .queryStockCardByProductId(inventoryViewModel.getProductId());
    if (stockCard != null) {
      List<LotMovementViewModel> lotMovementViewModels = FluentIterable
          .from(stockCard.getNonEmptyLotOnHandList())
          .transform(lotOnHand -> new LotMovementViewModel(lotOnHand.getLot().getLotNumber(),
              DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(),
                  DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR),
              lotOnHand.getQuantityOnHand().toString(), MovementReasonManager.MovementType.RECEIVE))
          .toSortedList((lot1, lot2) -> {
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
}
