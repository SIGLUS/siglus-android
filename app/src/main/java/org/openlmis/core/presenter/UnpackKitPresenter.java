package org.openlmis.core.presenter;

import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UnpackKitPresenter extends Presenter {
    UnpackKitView view;

    protected Subscriber<List<InventoryViewModel>> kitProductsSubscriber = getKitProductSubscriber();
    protected Subscriber<Void> unpackProductsSubscriber = getUnpackProductSubscriber();

    @Inject
    private ProductRepository productRepository;

    @Inject
    private StockRepository stockRepository;

    protected String kitCode;

    protected List<InventoryViewModel> inventoryViewModels;

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
        view = (UnpackKitView) v;
        inventoryViewModels = new ArrayList<>();
    }

    public UnpackKitPresenter() {
    }

    public void loadKitProducts(String kitCode, int kitNum) {
        this.kitCode = kitCode;
        Subscription subscription = getKitProductsObservable(kitCode, kitNum).subscribe(kitProductsSubscriber);
        subscriptions.add(subscription);
    }

    public Observable<List<InventoryViewModel>> getKitProductsObservable(final String kitCode, final int kitNum) {
        return Observable.create(new Observable.OnSubscribe<List<InventoryViewModel>>() {
            @Override
            public void call(Subscriber<? super List<InventoryViewModel>> subscriber) {
                try {
                    inventoryViewModels.clear();
                    List<KitProduct> kitProducts = productRepository.queryKitProductByKitCode(kitCode);
                    for (KitProduct kitProduct : kitProducts) {
                        Product product = productRepository.getByCode(kitProduct.getProductCode());
                        InventoryViewModel inventoryViewModel = new InventoryViewModel(product);
                        inventoryViewModel.setKitExpectQuantity(kitProduct.getQuantity() * kitNum);
                        inventoryViewModel.setChecked(true);
                        inventoryViewModels.add(inventoryViewModel);
                    }

                    subscriber.onNext(inventoryViewModels);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private Subscriber<List<InventoryViewModel>> getKitProductSubscriber() {
        return new Subscriber<List<InventoryViewModel>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.show(e.getMessage());
                view.loaded();
            }

            @Override
            public void onNext(List<InventoryViewModel> inventoryViewModels) {
                view.refreshList(inventoryViewModels);
                view.loaded();
            }
        };
    }

    public void saveUnpackProducts(int kitUnpackQuantity) {
        view.loading();
        Subscription subscription = saveUnpackProductsObservable(kitUnpackQuantity).subscribe(unpackProductsSubscriber);
        subscriptions.add(subscription);
    }

    private Observable saveUnpackProductsObservable(final int kitUnpackQuantity) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                try {

                    List<StockCard> stockCards = new ArrayList<>();

                    stockCards.addAll(FluentIterable.from(inventoryViewModels).transform(new Function<InventoryViewModel, StockCard>() {
                        @Override
                        public StockCard apply(InventoryViewModel inventoryViewModel) {
                            try {
                                return createStockCardForProduct(inventoryViewModel);
                            } catch (LMISException e) {
                                subscriber.onError(e);
                            }
                            return null;
                        }
                    }).toList());

                    stockCards.add(getStockCardForKit(kitUnpackQuantity));
                    stockRepository.batchSaveStockCardsWithMovementItemsAndUpdateProduct(stockCards);

                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (LMISException exception) {
                    subscriber.onError(exception);
                }

            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    protected StockCard getStockCardForKit(int kitUnpackQuantity) throws LMISException {

        Product kit = productRepository.getByCode(kitCode);
        StockCard kitStockCard = stockRepository.queryStockCardByProductId(kit.getId());

        kitStockCard.setStockOnHand(kitStockCard.getStockOnHand() - kitUnpackQuantity);

        StockMovementItem kitMovementItem = new StockMovementItem(kitStockCard);
        kitMovementItem.setReason(MovementReasonManager.UNPACK_KIT);
        kitMovementItem.setMovementType(StockMovementItem.MovementType.ISSUE);
        kitMovementItem.setMovementQuantity(kitUnpackQuantity);

        List<StockMovementItem> stockMovementItems = new ArrayList<>();
        stockMovementItems.add(kitMovementItem);

        kitStockCard.setStockMovementItemsWrapper(stockMovementItems);

        return kitStockCard;
    }

    private Subscriber<Void> getUnpackProductSubscriber() {
        return new Subscriber<Void>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.show(e.getMessage());
                view.loaded();
            }

            @Override
            public void onNext(Void object) {
                view.loaded();
                view.saveSuccess();
            }
        };
    }

    @NonNull
    protected StockCard createStockCardForProduct(InventoryViewModel inventoryViewModel) throws LMISException {
        List<StockMovementItem> stockMovementItems = new ArrayList<>();

        StockCard stockCard = stockRepository.queryStockCardByProductId(inventoryViewModel.getProductId());
        if (stockCard == null) {
            stockCard = new StockCard();
            stockCard.setProduct(inventoryViewModel.getProduct());

            stockMovementItems.add(stockCard.generateInitialStockMovementItem());
        }

        long movementQuantity = Long.parseLong(inventoryViewModel.getQuantity());

        stockCard.setStockOnHand(stockCard.getStockOnHand() + movementQuantity);
        stockCard.setExpireDates(DateUtil.uniqueExpiryDates(inventoryViewModel.getExpiryDates(), stockCard.getExpireDates()));
        stockCard.getProduct().setArchived(false);

        stockMovementItems.add(createUnpackMovementItem(stockCard, movementQuantity));
        stockCard.setStockMovementItemsWrapper(stockMovementItems);

        return stockCard;
    }

    @NonNull
    private StockMovementItem createUnpackMovementItem(StockCard stockCard, long movementQuantity) {
        StockMovementItem unpackMovementItem = new StockMovementItem(stockCard);
        unpackMovementItem.setReason(MovementReasonManager.DDM);
        unpackMovementItem.setMovementType(StockMovementItem.MovementType.RECEIVE);
        unpackMovementItem.setMovementQuantity(movementQuantity);
        return unpackMovementItem;
    }

    public interface UnpackKitView extends BaseView {
        void refreshList(List<InventoryViewModel> inventoryViewModels);

        void saveSuccess();
    }
}
