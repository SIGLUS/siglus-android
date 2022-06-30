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

import android.text.TextUtils;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class InitialInventoryPresenter extends InventoryPresenter {

  public static final String EMPTY_STRING = "";
  private static final int FIRST_ELEMENT_POSITION_OF_THE_LIST = 0;
  private static final int DEFAULT_PRODUCT_ID = 0;
  @Getter
  private List<InventoryViewModel> defaultViewModelList = new ArrayList<>();

  @Override
  public Observable<List<InventoryViewModel>> loadInventory() {
    return Observable.create((Observable.OnSubscribe<List<InventoryViewModel>>) subscriber -> {
      try {
        List<Product> inventoryProducts = productRepository.listProductsArchivedOrNotInStockCard();

        inventoryViewModelList.addAll(from(inventoryProducts)
            .transform(this::convertProductToStockCardViewModel).filter(inventoryViewModel ->
                !(inventoryViewModel.getProduct().isArchived() && inventoryViewModel.getStockCard() == null)).toList());
        subscriber.onNext(inventoryViewModelList);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "InitialInventoryPresenter.loadInventory").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  @Override
  public Observable<List<InventoryViewModel>> getInflatedInventory() {
    return loadInventory();
  }

  public Observable<List<InventoryViewModel>> loadInventoryWithBasicProducts() {
    return Observable.create((Observable.OnSubscribe<List<InventoryViewModel>>) subscriber -> {

      try {
        List<Product> basicProducts = productRepository.listBasicProducts();
        inventoryViewModelList.addAll(from(basicProducts).transform(this::convertProductToStockCardViewModel).toList());
        subscriber.onNext(inventoryViewModelList);
        defaultViewModelList = new ArrayList<>(inventoryViewModelList);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "InitialInventoryPresenter.loadInventoryWithBasicProducts")
            .reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  @Nullable
  private InventoryViewModel convertProductToStockCardViewModel(Product product) {
    try {
      InventoryViewModel viewModel;
      if (product.isArchived()) {
        StockCard stockCard = stockRepository.queryStockCardByProductId(product.getId());
        if (stockCard != null) {
          viewModel = new InventoryViewModel(stockCard);
        } else {
          viewModel = new InventoryViewModel(product);
        }
      } else {
        viewModel = new InventoryViewModel(product);
      }
      viewModel.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
      viewModel.setChecked(false);
      return viewModel;
    } catch (LMISException e) {
      new LMISException(e, "InitialInventoryPresenter.convertProductToStockCardViewModel").reportToFabric();
    }
    return null;
  }

  public Observable<Object> initStockCardObservable() {
    return Observable.create(subscriber -> {
      initOrArchiveBackStockCards();
      subscriber.onNext(null);
      subscriber.onCompleted();
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }

  void initOrArchiveBackStockCards() {
    defaultViewModelList.clear();
    defaultViewModelList.addAll(inventoryViewModelList);
    long createdTime = LMISApp.getInstance().getCurrentTimeMillis();
    for (InventoryViewModel inventoryViewModel : defaultViewModelList) {
      if (!inventoryViewModel.isChecked()) {
        continue;
      }
      try {
        if (inventoryViewModel.getProduct().isArchived()) {
          StockCard stockCard = inventoryViewModel.getStockCard();
          stockCard.getProduct().setArchived(false);
          stockRepository.updateStockCardWithProduct(stockCard);
          continue;
        }
        createStockCardAndInventoryMovementWithLot(inventoryViewModel, createdTime);
      } catch (LMISException e) {
        new LMISException(e, "InitialInventoryPresenter.initOrArchiveBackStockCard").reportToFabric();
      }
    }
  }

  private void createStockCardAndInventoryMovementWithLot(InventoryViewModel model, long createdTime) {
    StockCard stockCard = new StockCard();
    stockCard.setProduct(model.getProduct());
    StockMovementItem movementItem = new StockMovementItem(stockCard, model);
    movementItem.buildLotMovementReasonAndDocumentNumber();
    stockCard.setStockOnHand(movementItem.getStockOnHand());
    stockRepository.addStockMovementAndUpdateStockCard(movementItem, createdTime);
  }

  public void addNonBasicProductsToInventory(List<Product> nonBasicProducts) {
    setDefaultBasicProductsList();
    List<InventoryViewModel> nonBasicProductsModels = buildNonBasicProductViewModelsList(
        nonBasicProducts);
    removeExistentNonBasicProducts();
    defaultViewModelList.addAll(nonBasicProductsModels);
    filterViewModels(EMPTY_STRING);
  }

  private void removeExistentNonBasicProducts() {
    List<InventoryViewModel> defaultModels = new ArrayList<>(defaultViewModelList);
    for (InventoryViewModel model : defaultModels) {
      if (!model.isBasic()) {
        defaultViewModelList.remove(model);
      }
    }
  }

  private void setDefaultBasicProductsList() {
    List<InventoryViewModel> basicProductViewModels = new ArrayList<>();
    for (InventoryViewModel model : defaultViewModelList) {
      if (model.isBasic()) {
        basicProductViewModels.add(model);
      }
    }
    inventoryViewModelList.clear();
    inventoryViewModelList.addAll(basicProductViewModels);
  }

  private List<InventoryViewModel> buildNonBasicProductViewModelsList(List<Product> products) {
    List<InventoryViewModel> nonBasicProductsModels = new ArrayList<>();
    for (Product product : products) {
      InventoryViewModel model = convertProductToStockCardViewModel(product);
      nonBasicProductsModels.add(model);
    }
    return nonBasicProductsModels;
  }

  private void addHeaderForBasicProducts() {
    Product basicProductHeader = Product.dummyProduct();
    basicProductHeader.setBasic(true);
    InventoryViewModel inventoryModelBasicHeader = new InventoryViewModel(basicProductHeader);
    inventoryModelBasicHeader.setDummyModel(true);
    inventoryViewModelList.add(FIRST_ELEMENT_POSITION_OF_THE_LIST, inventoryModelBasicHeader);
  }

  private void addHeaderForNonBasicProducts(int position) {
    InventoryViewModel headerInventoryModel = new InventoryViewModel(Product.dummyProduct());
    headerInventoryModel.setDummyModel(true);
    inventoryViewModelList.add(position, headerInventoryModel);
  }

  private void removeHeaders() {
    for (Iterator<InventoryViewModel> iterator = inventoryViewModelList.iterator();
        iterator.hasNext(); ) {
      InventoryViewModel model = iterator.next();
      if (DEFAULT_PRODUCT_ID == model.getProductId()) {
        iterator.remove();
      }
    }
  }


  private void addHeaders(boolean areThereNonBasicProducts) {
    if (areThereNonBasicProducts) {
      int nonBasicProductsHeaderPosition = getNonBasicProductsHeaderPosition();
      if (nonBasicProductsHeaderPosition > FIRST_ELEMENT_POSITION_OF_THE_LIST) {
        addHeaderForBasicProducts();
        nonBasicProductsHeaderPosition++;
      }
      addHeaderForNonBasicProducts(nonBasicProductsHeaderPosition);
    } else {
      addHeaderForBasicProducts();
    }
  }

  private int getNonBasicProductsHeaderPosition() {
    int nonBasicProductsHeaderPosition = 0;
    while (inventoryViewModelList.size() > nonBasicProductsHeaderPosition && (inventoryViewModelList
        .get(nonBasicProductsHeaderPosition)).isBasic()) {
      nonBasicProductsHeaderPosition++;
    }
    return nonBasicProductsHeaderPosition;
  }

  private boolean checkIfNonBasicProductsExists() {
    for (InventoryViewModel model : inventoryViewModelList) {
      if (!model.isBasic()) {
        return true;
      }
    }
    return false;
  }

  public void filterViewModels(final String keyword) {
    filterViewModelsByProductFullName(keyword);
    removeHeaders();
    addHeaders(checkIfNonBasicProductsExists());
  }

  private void filterViewModelsByProductFullName(final String keyword) {
    if (TextUtils.isEmpty(keyword)) {
      inventoryViewModelList.clear();
      inventoryViewModelList.addAll(defaultViewModelList);
    } else {
      List<InventoryViewModel> filteredResult = from(defaultViewModelList).filter(
          inventoryViewModel -> inventoryViewModel.getProduct().getProductFullName().toLowerCase()
              .contains(keyword.toLowerCase())).toList();
      inventoryViewModelList.clear();
      inventoryViewModelList.addAll(filteredResult);
    }
  }

  public void removeNonBasicProductElement(InventoryViewModel model) {
    defaultViewModelList.remove(model);
    filterViewModels(EMPTY_STRING);
  }
}
