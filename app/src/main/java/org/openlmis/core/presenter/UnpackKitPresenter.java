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
import org.openlmis.core.view.viewmodel.StockCardViewModel;
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

    protected Subscriber<List<StockCardViewModel>> kitProductsSubscriber = getKitProductSubscriber();
    protected Subscriber<Void> unpackProductsSubscriber = getUnpackProductSubscriber();

    @Inject
    private ProductRepository productRepository;

    @Inject
    private StockRepository stockRepository;

    protected String kitCode;

    protected List<StockCardViewModel> stockCardViewModels;

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
        view = (UnpackKitView) v;
        stockCardViewModels = new ArrayList<>();
    }

    public UnpackKitPresenter() {
    }

    public void loadKitProducts(String kitCode) {
        this.kitCode = kitCode;
        Subscription subscription = getKitProductsObservable(kitCode).subscribe(kitProductsSubscriber);
        subscriptions.add(subscription);
    }

    public Observable<List<StockCardViewModel>> getKitProductsObservable(final String kitCode) {
        return Observable.create(new Observable.OnSubscribe<List<StockCardViewModel>>() {
            @Override
            public void call(Subscriber<? super List<StockCardViewModel>> subscriber) {
                try {
                    stockCardViewModels.clear();
                    List<KitProduct> kitProducts = productRepository.queryKitProductByKitCode(kitCode);
                    for (KitProduct kitProduct : kitProducts) {
                        Product product = productRepository.getByCode(kitProduct.getProductCode());
                        StockCardViewModel stockCardViewModel = new StockCardViewModel(product);
                        stockCardViewModel.setKitExpectQuantity(kitProduct.getQuantity());
                        stockCardViewModel.setChecked(true);
                        stockCardViewModels.add(stockCardViewModel);
                    }

                    subscriber.onNext(stockCardViewModels);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private Subscriber<List<StockCardViewModel>> getKitProductSubscriber() {
        return new Subscriber<List<StockCardViewModel>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.show(e.getMessage());
                view.loaded();
            }

            @Override
            public void onNext(List<StockCardViewModel> stockCardViewModels) {
                view.refreshList(stockCardViewModels);
                view.loaded();
            }
        };
    }

    public void saveUnpackProducts() {
        view.loading();
        Subscription subscription = saveUnpackProductsObservable().subscribe(unpackProductsSubscriber);
        subscriptions.add(subscription);
    }

    private Observable saveUnpackProductsObservable() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                try {

                    List<StockCard> stockCards = new ArrayList<>();

                    stockCards.addAll(FluentIterable.from(stockCardViewModels).transform(new Function<StockCardViewModel, StockCard>() {
                        @Override
                        public StockCard apply(StockCardViewModel stockCardViewModel) {
                            try {
                                return createStockCardForProduct(stockCardViewModel);
                            } catch (LMISException e) {
                                subscriber.onError(e);
                            }
                            return null;
                        }
                    }).toList());

                    stockCards.add(getStockCardForKit());
                    stockRepository.batchSaveStockCardsWithMovementItems(stockCards);

                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (LMISException exception) {
                    subscriber.onError(exception);
                }

            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    protected StockCard getStockCardForKit() throws LMISException {
        int kitUnpackQuantity = 1;

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
    protected StockCard createStockCardForProduct(StockCardViewModel stockCardViewModel) throws LMISException {
        List<StockMovementItem> stockMovementItems = new ArrayList<>();

        StockCard stockCard = stockRepository.queryStockCardByProductId(stockCardViewModel.getProductId());
        if (stockCard == null) {
            stockCard = new StockCard();
            stockCard.setProduct(stockCardViewModel.getProduct());

            stockMovementItems.add(stockCard.generateInitialStockMovementItem());
        }

        long movementQuantity = Long.parseLong(stockCardViewModel.getQuantity());

        stockCard.setStockOnHand(stockCard.getStockOnHand() + movementQuantity);
        stockCard.setExpireDates(DateUtil.uniqueExpiryDates(stockCardViewModel.getExpiryDates(), stockCard.getExpireDates()));

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
        void refreshList(List<StockCardViewModel> stockCardViewModels);

        void saveSuccess();
    }
}
