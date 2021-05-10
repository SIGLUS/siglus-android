package org.openlmis.core.presenter;

import android.support.annotation.Nullable;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.DraftInitialInventory;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.adapter.BulkInitialInventoryAdapter;
import org.openlmis.core.view.viewmodel.BulkInitialInventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class BulkInitialInventoryPresenter extends InventoryPresenter {
    private static final String TAG = BulkInitialInventoryPresenter.class.getSimpleName();
    private final int FIRST_ELEMENT_POSITION_OF_THE_LIST = 0;

    private List<InventoryViewModel> defaultViewModelList = new ArrayList<>();

    @Override
    public Observable<List<InventoryViewModel>> loadInventory() {
        return Observable.create((Observable.OnSubscribe<List<InventoryViewModel>>) subscriber -> {
            try {
                List<Product> inventoryProducts = productRepository.listBasicProducts();
                defaultViewModelList.clear();
                inventoryViewModelList.clear();
                defaultViewModelList.addAll(convertProductToStockCardViewModel(inventoryProducts,
                        BulkInitialInventoryAdapter.ITEM_BASIC));
                addHeaderForBasicProducts();
                restoreDraftInventory();
                subscriber.onNext(inventoryViewModelList);
                subscriber.onCompleted();
            } catch (LMISException e) {
                new LMISException(e, TAG + ":" + "loadInventory").reportToFabric();
                subscriber.onError(e);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    protected void restoreDraftInventory() throws LMISException {
        List<DraftInitialInventory> draftInventoryList = inventoryRepository.queryAllInitialDraft();
        List<BulkInitialInventoryViewModel> nonBasicLists = new ArrayList<>();
        List<DraftInitialInventory> noBasicDraftInventoryList = new ArrayList<>();
        for (DraftInitialInventory draftInventory : draftInventoryList) {
            for (InventoryViewModel viewModel : defaultViewModelList) {
                if ((viewModel.getProductId() == draftInventory.getProduct().getId())) {
                    ((BulkInitialInventoryViewModel) viewModel).setInitialDraftInventory(draftInventory);
                }
            }
            if (!draftInventory.getProduct().isBasic()) {
                noBasicDraftInventoryList.add(draftInventory);
            }
        }
        for (DraftInitialInventory draftInventory : noBasicDraftInventoryList) {
            BulkInitialInventoryViewModel bulkInitialInventoryViewModel = new BulkInitialInventoryViewModel(draftInventory.getProduct());
            bulkInitialInventoryViewModel.setInitialDraftInventory(draftInventory);
            bulkInitialInventoryViewModel.setViewType(BulkInitialInventoryAdapter.ITEM_NO_BASIC);
            nonBasicLists.add(bulkInitialInventoryViewModel);
        }

        if (nonBasicLists.size() >= 1) {
            buildNonBasicProductModels(nonBasicLists);
        }
    }

    @Nullable
    private List<BulkInitialInventoryViewModel> convertProductToStockCardViewModel(List<Product> products, int viewType) {
        return from(products).transform(product -> {
            try {
                BulkInitialInventoryViewModel viewModel;
                if (product.isArchived()) {
                    viewModel = new BulkInitialInventoryViewModel(stockRepository.queryStockCardByProductId(product.getId()));
                    viewModel.setMovementType(MovementReasonManager.MovementType.INITIAL_INVENTORY);
                    viewModel.setBasic(product.isBasic());
                } else {
                    viewModel = new BulkInitialInventoryViewModel(product);
                    viewModel.setBasic(BulkInitialInventoryAdapter.ITEM_BASIC == viewType);
                    viewModel.setMovementType(MovementReasonManager.MovementType.INITIAL_INVENTORY);
                }
                viewModel.setViewType(viewType);
                viewModel.setChecked(false);
                setExistingLotViewModels(viewModel);
                return viewModel;
            } catch (LMISException e) {
                new LMISException(e, TAG + ":" + "convertProductToInitialInventoryViewModel").reportToFabric();
            }
            return null;
        }).toList();
    }

    private void setExistingLotViewModels(BulkInitialInventoryViewModel bulkInitialInventoryViewModel) {
        if (bulkInitialInventoryViewModel.getStockCard() == null) {
            return;
        }
        List<LotMovementViewModel> lotMovementViewModels = FluentIterable
                .from(bulkInitialInventoryViewModel.getStockCard().getNonEmptyLotOnHandList())
                .transform(lotOnHand -> new LotMovementViewModel(lotOnHand.getLot().getLotNumber(),
                        DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR),
                        lotOnHand.getQuantityOnHand().toString(), MovementReasonManager.MovementType.RECEIVE))
                .toSortedList((lot1, lot2) -> {
                    Date localDate = DateUtil.parseString(lot1.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);
                    if (localDate != null) {
                        return localDate.compareTo(DateUtil.parseString(lot2.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
                    } else {
                        return 0;
                    }
                });
        bulkInitialInventoryViewModel.setExistingLotMovementViewModelList(lotMovementViewModels);
    }

    public void addNonBasicProductsToInventory(List<Product> nonBasicProducts) {
        List<BulkInitialInventoryViewModel> nonBasicProductsModels = convertProductToStockCardViewModel(nonBasicProducts,
                BulkInitialInventoryAdapter.ITEM_NO_BASIC);
        buildNonBasicProductModels(nonBasicProductsModels);
    }

    public List<String> getAllAddedNonBasicProduct(){
        return from(inventoryViewModelList)
                .filter(viewModel -> viewModel.getViewType() == BulkInitialInventoryAdapter.ITEM_NO_BASIC)
                .transform(viewModel -> viewModel.getProduct().getCode())
                .toList();
    }

    private void buildNonBasicProductModels(List<BulkInitialInventoryViewModel> nonBasicProductsModels) {
        // First Time
        boolean hasNonBasicProductHeader = false;
        for (InventoryViewModel viewModel : inventoryViewModelList) {
            if (viewModel.getViewType() == BulkInitialInventoryAdapter.ITEM_NON_BASIC_HEADER) {
                hasNonBasicProductHeader = true;
            }
        }
        if (!hasNonBasicProductHeader) {
            BulkInitialInventoryViewModel nonBasicHeaderInventoryModel = new BulkInitialInventoryViewModel(Product.dummyProduct());
            nonBasicHeaderInventoryModel.setDummyModel(true);
            nonBasicHeaderInventoryModel.setViewType(BulkInitialInventoryAdapter.ITEM_NON_BASIC_HEADER);
            inventoryViewModelList.add(nonBasicHeaderInventoryModel);
        }
        inventoryViewModelList.addAll(nonBasicProductsModels);

    }

    private void addHeaderForBasicProducts() {
        inventoryViewModelList.clear();
        inventoryViewModelList.addAll(defaultViewModelList);
        Product basicProductHeader = Product.dummyProduct();
        basicProductHeader.setBasic(true);
        BulkInitialInventoryViewModel inventoryModelBasicHeader = new BulkInitialInventoryViewModel(basicProductHeader);
        inventoryModelBasicHeader.setDummyModel(false);
        inventoryModelBasicHeader.setViewType(BulkInitialInventoryAdapter.ITEM_BASIC_HEADER);
        inventoryViewModelList.add(FIRST_ELEMENT_POSITION_OF_THE_LIST, inventoryModelBasicHeader);
    }

    public void removeNonBasicProductElement(InventoryViewModel inventoryViewModel) {
        synchronized (inventoryViewModelList) {
            ListIterator<InventoryViewModel> iterator = inventoryViewModelList.listIterator();
            while (iterator.hasNext()) {
                InventoryViewModel viewModel = iterator.next();
                if (Objects.equals(viewModel.getProduct().getCode(), inventoryViewModel.getProduct().getCode())) {
                    iterator.remove();
                }
            }
        }
    }

    public Observable<Object> saveDraftInventoryObservable() {
        return Observable.create(subscriber -> {
            try {
                inventoryRepository.clearInitialDraft();
                for (InventoryViewModel inventoryViewModel : inventoryViewModelList) {
                    if (inventoryViewModel.getViewType() == BulkInitialInventoryAdapter.ITEM_BASIC_HEADER
                            || inventoryViewModel.getViewType() == BulkInitialInventoryAdapter.ITEM_NON_BASIC_HEADER) {
                        continue;
                    }
                    inventoryRepository.createInitialDraft(new DraftInitialInventory((BulkInitialInventoryViewModel) inventoryViewModel));
                }
                subscriber.onNext(null);
                subscriber.onCompleted();
            } catch (LMISException e) {
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> doInventory() {
        return Observable.create(subscriber -> {
            try {
                for (InventoryViewModel viewModel : inventoryViewModelList) {
                    if (viewModel.getViewType() == BulkInitialInventoryAdapter.ITEM_NON_BASIC_HEADER
                            || viewModel.getViewType() == BulkInitialInventoryAdapter.ITEM_BASIC_HEADER) {
                        continue;
                    }
                    viewModel.setMovementType(MovementReasonManager.MovementType.INITIAL_INVENTORY);
                    if (viewModel.getProduct().isArchived()) {
                        StockCard stockCard = viewModel.getStockCard();
                        stockCard.getProduct().setArchived(false);
                        stockRepository.updateStockCardWithProduct(stockCard);
                        return;
                    }

                    StockCard stockCard = new StockCard();
                    stockCard.setProduct(viewModel.getProduct());
                    StockMovementItem movementItem = new StockMovementItem(stockCard, viewModel, true);
                    stockCard.setStockOnHand(movementItem.getStockOnHand());
                    movementItem.setStockCard(stockCard);
                    movementItem.setStockOnHand(movementItem.getStockOnHand());
                    stockRepository.addStockMovementAndUpdateStockCard(movementItem);
                }
                inventoryRepository.clearInitialDraft();
                saveInventoryDate();
                subscriber.onNext(null);
                subscriber.onCompleted();
            } catch (LMISException e) {
                subscriber.onError(e);
                new LMISException(e, "doInventory").reportToFabric();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private void saveInventoryDate() {
        inventoryRepository.save(new Inventory());
    }
}
