package org.openlmis.core.presenter;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ExpiredStockCardListPresenter extends StockCardPresenter {

    public void loadExpiredStockCards() {
        view.loading();

        lotsOnHands.putAll(stockRepository.lotOnHands());
        Subscription subscription = loadExpiredStockCardsObservable().subscribe(afterLoadHandler);
        subscriptions.add(subscription);
    }

    private Observable<List<StockCard>> loadExpiredStockCardsObservable() {
        return Observable.create((Observable.OnSubscribe<List<StockCard>>) subscriber -> {
            subscriber.onNext(from(stockRepository.list()).filter(stockCard -> {
                if (stockCard != null && isActiveProduct(stockCard)
                        && !isArchivedProduct(stockCard)) {
                    List<LotOnHand> expiredLot = filterExpiredAndNonEmptyLot(stockCard);
                    if (expiredLot.size() > 0) {
                        stockCard.setLotOnHandListWrapper(expiredLot);
                        return true;
                    }
                }
                return false;
            }).toList());
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private List<LotOnHand> filterExpiredAndNonEmptyLot(StockCard stockCard) {
        List<LotOnHand> lotOnHandListWrapper = stockCard.getLotOnHandListWrapper();
        return from(lotOnHandListWrapper)
                .filter(lotOnHand -> lotOnHand.getLot().isExpired() && lotOnHand.getQuantityOnHand() > 0)
                .toList();
    }
}