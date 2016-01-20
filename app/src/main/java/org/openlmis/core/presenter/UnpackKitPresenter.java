package org.openlmis.core.presenter;

import android.support.annotation.NonNull;

import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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
        Subscription subscription = saveUnpackProductsObservable().subscribe(unpackProductsSubscriber);
        subscriptions.add(subscription);
    }

    private Observable saveUnpackProductsObservable() {
        view.loading();

        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getConnectionSource(), new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            for(StockCardViewModel stockCardViewModel : stockCardViewModels) {
                                StockCard stockCard = stockRepository.queryStockCardByProductId(stockCardViewModel.getProductId());
                                if (stockCard == null) {
                                    stockCard = saveStockCard(stockCardViewModel);
                                }
                                saveStockMovementItemForProduct(stockCardViewModel, stockCard);
                            }
                            saveStockMovementItemForKit();
                            return null;
                        }
                    });
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private void saveStockMovementItemForKit() throws LMISException {
        int kitUnpackQuantity = 1;

        Product kit = productRepository.getByCode(kitCode);
        StockCard kitStockCard = stockRepository.queryStockCardByProductId(kit.getId());

        kitStockCard.setStockOnHand(kitStockCard.getStockOnHand() - kitUnpackQuantity);

        StockMovementItem movementItem = new StockMovementItem(kitStockCard);
        movementItem.setReason(MovementReasonManager.UNPACK_KIT);
        movementItem.setMovementType(StockMovementItem.MovementType.ISSUE);
        movementItem.setMovementQuantity(kitUnpackQuantity);

        stockRepository.addStockMovementAndUpdateStockCard(movementItem);
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

    private void saveStockMovementItemForProduct(StockCardViewModel stockCardViewModel, StockCard stockCard) throws LMISException {
        long movementQuantity = Long.parseLong(stockCardViewModel.getQuantity());
        stockCard.setStockOnHand(stockCard.getStockOnHand() + movementQuantity);
        stockCard.setExpireDates(DateUtil.uniqueExpiryDates(stockCardViewModel.getExpiryDates(), stockCard.getExpireDates()));

        StockMovementItem movementItem = new StockMovementItem(stockCard);
        movementItem.setReason(MovementReasonManager.DDM);
        movementItem.setMovementType(StockMovementItem.MovementType.RECEIVE);
        movementItem.setMovementQuantity(movementQuantity);

        stockRepository.addStockMovementAndUpdateStockCard(movementItem);
    }

    @NonNull
    private StockCard saveStockCard(StockCardViewModel stockCardViewModel) throws LMISException {
        StockCard stockCard = new StockCard();
        stockCard.setProduct(stockCardViewModel.getProduct());
        stockCard.setExpireDates(DateUtil.formatExpiryDateString(stockCardViewModel.getExpiryDates()));
        stockRepository.initStockCard(stockCard);
        return stockCard;
    }

    public interface UnpackKitView extends BaseView {
        void refreshList(List<StockCardViewModel> stockCardViewModels);

        void saveSuccess();
    }
}
