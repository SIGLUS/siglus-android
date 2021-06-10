package org.openlmis.core.presenter;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;
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
  private final int FIRST_ELEMENT_POSITION_OF_THE_LIST = 0;
  private static final int DEFAULT_PRODUCT_ID = 0;
  @Getter
  private List<InventoryViewModel> defaultViewModelList = new ArrayList<>();

  @Override
  public Observable<List<InventoryViewModel>> loadInventory() {
    return Observable.create((Observable.OnSubscribe<List<InventoryViewModel>>) subscriber -> {
      try {
        List<Product> inventoryProducts = productRepository.listProductsArchivedOrNotInStockCard();

        inventoryViewModelList.addAll(from(inventoryProducts)
            .transform(product -> convertProductToStockCardViewModel(product)).toList());
        subscriber.onNext(inventoryViewModelList);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "InitialInventoryPresenter.loadInventory").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public Observable<List<InventoryViewModel>> loadInventoryWithBasicProducts() {
    return Observable.create((Observable.OnSubscribe<List<InventoryViewModel>>) subscriber -> {

      try {
        List<Product> basicProducts = productRepository.listBasicProducts();
        inventoryViewModelList.addAll(
            from(basicProducts).transform(product -> convertProductToStockCardViewModel(product))
                .toList());
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
        viewModel = new InventoryViewModel(
            stockRepository.queryStockCardByProductId(product.getId()));
        viewModel.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
      } else {
        viewModel = new InventoryViewModel(product);
//                viewModel.setViewType(BulkInventoryLotMovementAdapter.ITEM_LIST);
        viewModel.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
      }
      viewModel.setChecked(false);
      return viewModel;
    } catch (LMISException e) {
      new LMISException(e, "InitialInventoryPresenter.convertProductToStockCardViewModel")
          .reportToFabric();
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
    for (InventoryViewModel inventoryViewModel : defaultViewModelList) {
      if (inventoryViewModel.isChecked()) {
        initOrArchiveBackStockCard(inventoryViewModel);
      }
    }
  }

  private void initOrArchiveBackStockCard(InventoryViewModel inventoryViewModel) {
    try {
      if (inventoryViewModel.getProduct().isArchived()) {
        StockCard stockCard = inventoryViewModel.getStockCard();
        stockCard.getProduct().setArchived(false);
        stockRepository.updateStockCardWithProduct(stockCard);
        return;
      }
      createStockCardAndInventoryMovementWithLot(inventoryViewModel);
    } catch (LMISException e) {
      new LMISException(e, "InitialInventoryPresenter.initOrArchiveBackStockCard").reportToFabric();
    }
  }

  private void createStockCardAndInventoryMovementWithLot(InventoryViewModel model)
      throws LMISException {
    StockCard stockCard = new StockCard();
    stockCard.setProduct(model.getProduct());
    StockMovementItem movementItem = new StockMovementItem(stockCard, model);
    stockCard.setStockOnHand(movementItem.getStockOnHand());
    stockRepository.addStockMovementAndUpdateStockCard(movementItem);
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
//        inventoryModelBasicHeader.setViewType(BulkInventoryLotMovementAdapter.ITEM_BASIC_HEADER);
    inventoryViewModelList.add(FIRST_ELEMENT_POSITION_OF_THE_LIST, inventoryModelBasicHeader);
  }

  private void addHeaderForNonBasicProducts(int position) {
    InventoryViewModel headerInventoryModel = new InventoryViewModel(Product.dummyProduct());
    headerInventoryModel.setDummyModel(true);
//        headerInventoryModel.setViewType(BulkInventoryLotMovementAdapter.ITEM_NON_BASIC_HEADER);
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
