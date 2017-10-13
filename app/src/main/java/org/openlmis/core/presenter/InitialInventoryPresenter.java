package org.openlmis.core.presenter;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.view.adapter.BulkInitialInventoryAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class InitialInventoryPresenter extends InventoryPresenter {

    public static final String EMPTY_STRING = "";
    private final int FIRST_ELEMENT_POSITION_OF_THE_LIST = 0;
    private static final int DEFAULT_PRODUCT_ID = 0;
    private List<InventoryViewModel> defaultViewModelList;

    @Override
    public Observable<List<InventoryViewModel>> loadInventory() {
        return Observable.create(new Observable.OnSubscribe<List<InventoryViewModel>>() {
            @Override
            public void call(final Subscriber<? super List<InventoryViewModel>> subscriber) {
                try {
                    List<Product> inventoryProducts = productRepository.listProductsArchivedOrNotInStockCard();

                    inventoryViewModelList.addAll(from(inventoryProducts)
                            .transform(new Function<Product, InventoryViewModel>() {
                                @Override
                                public InventoryViewModel apply(Product product) {
                                    return convertProductToStockCardViewModel(product);
                                }
                            }).toList());
                    subscriber.onNext(inventoryViewModelList);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public Observable<List<InventoryViewModel>> loadInventoryWithBasicProducts() {
        return Observable.create(new Observable.OnSubscribe<List<InventoryViewModel>>() {
            @Override
            public void call(Subscriber<? super List<InventoryViewModel>> subscriber) {

                try {
                    List<Product> basicProducts = productRepository.listBasicProducts();
                    inventoryViewModelList.addAll(
                            from(basicProducts).transform(new Function<Product, InventoryViewModel>() {
                                @Override
                                public InventoryViewModel apply(Product product) {
                                    return convertProductToStockCardViewModel(product);
                                }
                            }).toList());
                    subscriber.onNext(inventoryViewModelList);
                    defaultViewModelList = new ArrayList<>(inventoryViewModelList);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Nullable
    private InventoryViewModel convertProductToStockCardViewModel(Product product) {
        try {
            InventoryViewModel viewModel;
            if (product.isArchived()) {
                viewModel = new InventoryViewModel(stockRepository.queryStockCardByProductId(product.getId()));
                viewModel.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
            } else {
                viewModel = new InventoryViewModel(product);
                viewModel.setViewType(BulkInitialInventoryAdapter.ITEM_LIST);
                viewModel.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
            }
            viewModel.setChecked(false);
            return viewModel;
        } catch (LMISException e) {
            e.reportToFabric();
        }
        return null;
    }

    public Observable<Object> initStockCardObservable() {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                initOrArchiveBackStockCards();
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    void initOrArchiveBackStockCards() {
        for (InventoryViewModel inventoryViewModel : inventoryViewModelList) {
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
            e.reportToFabric();
        }
    }

    private void createStockCardAndInventoryMovementWithLot(InventoryViewModel model) throws LMISException {
        StockCard stockCard = new StockCard();
        stockCard.setProduct(model.getProduct());
        StockMovementItem movementItem = new StockMovementItem(stockCard, model);
        stockCard.setStockOnHand(movementItem.getStockOnHand());
        stockRepository.addStockMovementAndUpdateStockCard(movementItem);
    }

    public void addNonBasicProductsToInventory(List<Product> nonBasicProducts) {
        setDefaultBasicProductsList();
        List<InventoryViewModel> nonBasicProductsModels = buildNonBasicProductViewModelsList(nonBasicProducts);
        defaultViewModelList.addAll(nonBasicProductsModels);
        arrangeViewModels(EMPTY_STRING);
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
        inventoryModelBasicHeader.setViewType(BulkInitialInventoryAdapter.ITEM_BASIC_HEADER);
        inventoryViewModelList.add(FIRST_ELEMENT_POSITION_OF_THE_LIST, inventoryModelBasicHeader);
    }

    private void addHeaderForNonBasicProducts(int position) {
        InventoryViewModel headerInventoryModel = new InventoryViewModel(Product.dummyProduct());
        headerInventoryModel.setDummyModel(true);
        headerInventoryModel.setViewType(BulkInitialInventoryAdapter.ITEM_NON_BASIC_HEADER);
        inventoryViewModelList.add(position, headerInventoryModel);
    }

    private void removeHeaders() {
        for (int i = 0; i < inventoryViewModelList.size(); i++) {
            if (inventoryViewModelList.get(i).getProductId() == DEFAULT_PRODUCT_ID) {
                inventoryViewModelList.remove(i);
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
        while (inventoryViewModelList.size() > nonBasicProductsHeaderPosition && (inventoryViewModelList.get(nonBasicProductsHeaderPosition)).isBasic()) {
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

    public void arrangeViewModels(final String keyword) {
        filterViewModels(keyword);
        removeHeaders();
        addHeaders(checkIfNonBasicProductsExists());
    }

    private void filterViewModels(final String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            inventoryViewModelList.clear();
            inventoryViewModelList.addAll(defaultViewModelList);
        } else {
            List<InventoryViewModel> filteredResult = from(defaultViewModelList).filter(new Predicate<InventoryViewModel>() {
                @Override
                public boolean apply(InventoryViewModel inventoryViewModel) {
                    return inventoryViewModel.getProduct().getProductFullName().toLowerCase().contains(keyword.toLowerCase());
                }
            }).toList();
            inventoryViewModelList.clear();
            inventoryViewModelList.addAll(filteredResult);
        }
    }

    private int getNumberOfHeaders(){
        int numberOfHeaders = 0;
        for(InventoryViewModel model: inventoryViewModelList){
            if(model.getProductId() == DEFAULT_PRODUCT_ID){
                numberOfHeaders++;
            }
        }
        return numberOfHeaders;
    }

    public void removeNonBasicProductElement(int position){
        int positionWithoutHeaders = position - getNumberOfHeaders();
        defaultViewModelList.remove(positionWithoutHeaders);
        arrangeViewModels(EMPTY_STRING);
    }
}
