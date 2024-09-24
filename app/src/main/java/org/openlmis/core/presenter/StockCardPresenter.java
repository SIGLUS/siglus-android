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

import static org.openlmis.core.presenter.StockCardPresenter.ArchiveStatus.ACTIVE;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.inject.Inject;
import com.j256.ormlite.dao.GenericRawResults;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Product.IsKit;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.service.DirtyDataManager;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class StockCardPresenter extends Presenter {

  private static final String TAG = StockCardPresenter.class.getSimpleName();
  private static final String SHOULD_SHOW_ALERT_MSG = "should_show_alert_msg";
  final Map<String, String> lotsOnHands = new HashMap<>();
  private final List<InventoryViewModel> inventoryViewModels;
  @Inject
  StockRepository stockRepository;
  @Inject
  ProductRepository productRepository;
  @Inject
  StockService stockService;
  @Inject
  DirtyDataManager dirtyDataManager;
  @Inject
  SharedPreferenceMgr sharedPreferenceMgr;
  private StockCardListView view;
  Observer<List<StockCard>> afterLoadHandler = getLoadStockCardsSubscriber();

  public StockCardPresenter() {
    inventoryViewModels = new ArrayList<>();
  }

  public List<InventoryViewModel> getInventoryViewModels() {
    return inventoryViewModels;
  }

  @Override
  public void attachView(BaseView v) {
    view = (StockCardListView) v;
  }

  public Observable<List<StockCard>> correctDirtyObservable(ArchiveStatus status) {
    return Observable.create((Observable.OnSubscribe<List<StockCard>>) subscriber ->
        checkDataAndEmitter(subscriber, status))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }

  private void checkDataAndEmitter(Subscriber<? super List<StockCard>> subscriber, ArchiveStatus status) {
    List<StockCard> allStockCards = stockRepository.list();
    if (sharedPreferenceMgr.shouldStartHourlyDirtyDataCheck()) {
      dirtyDataManager.checkAndGetDirtyData(allStockCards, lotsOnHands);
    }
    stockService.monthlyUpdateAvgMonthlyConsumption();
    subscriber.onNext(from(allStockCards).filter(stockCard -> {
      if (status.isArchived()) {
        return showInArchiveView(stockCard);
      }
      return showInOverview(stockCard);
    }).toList());

    if (!CollectionUtils.isEmpty(sharedPreferenceMgr.getDeletedProduct())
        || !CollectionUtils.isEmpty(sharedPreferenceMgr.getDeletedMovementItems())) {
      subscriber.onError(new LMISException(SHOULD_SHOW_ALERT_MSG));
    } else {
      subscriber.onCompleted();
    }
  }

  public void loadStockCards(ArchiveStatus status) {
    loadStockCards(status, true);
  }

  public void loadStockCards(ArchiveStatus status, boolean showLoading) {
    if (showLoading) {
      view.loading();
    }
    lotsOnHands.putAll(stockRepository.lotOnHands());
    if (!CollectionUtils.isEmpty(sharedPreferenceMgr.getDeletedProduct())
        || !CollectionUtils.isEmpty(sharedPreferenceMgr.getDeletedMovementItems())) {
      view.showWarning();
      return;
    }
    filterSpecificStatusStockCards(status);
  }

  public void filterSpecificStatusStockCards(ArchiveStatus status) {
    if (sharedPreferenceMgr.shouldStartHourlyDirtyDataCheck()) {
      Subscription subscription = correctDirtyObservable(status).subscribe(afterLoadHandler);
      subscriptions.add(subscription);
    } else {
      loadStockCardsInner(status);
    }
  }

  private void loadStockCardsInner(ArchiveStatus status) {
    Subscription subscription = getLoadStockCardsObservable(status).subscribe(afterLoadHandler);
    subscriptions.add(subscription);
  }

  public void loadKits() {
    view.loading();
    Subscription subscription = createOrGetKitStockCardsObservable().subscribe(afterLoadHandler);
    subscriptions.add(subscription);
  }

  public void refreshStockCardsObservable(@NonNull long[] stockCardIds) {
    view.loading();
    Observable.create((OnSubscribe<List<StockCard>>) subscriber -> checkDataAndEmitter(subscriber, ACTIVE))
        .doOnNext(stockCards -> {
          refreshViewModels(stockCards);
          for (long stockCardId : stockCardIds) {
            refreshStockCardViewModelsSOH(stockCardId);
          }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<List<StockCard>>() {
          @Override
          public void onCompleted() {
            view.loaded();
          }

          @Override
          public void onError(Throwable e) {
            ToastUtil.show(e.getMessage());
            view.loaded();
          }

          @Override
          public void onNext(List<StockCard> stockCards) {
            view.loaded();
            view.refreshBannerText();
            view.refresh(inventoryViewModels);
          }
        });
  }

  private void refreshViewModels(List<StockCard> stockCards) {
    List<InventoryViewModel> inventoryViewModelList = new ArrayList<>();
    for (StockCard stockCard : stockCards) {
      inventoryViewModelList.add(new InventoryViewModel(stockCard, lotsOnHands));
    }
    inventoryViewModels.clear();
    inventoryViewModels.addAll(inventoryViewModelList);
  }

  private void refreshStockCardViewModelsSOH(long stockCardId) {
    for (InventoryViewModel inventoryViewModel : inventoryViewModels) {
      final StockCard stockCard = inventoryViewModel.getStockCard();
      if (stockCardId == stockCard.getId()) {
        stockRepository.refresh(stockCard);
        try {
          GenericRawResults<String[]> rawResults = stockRepository.refreshedLotOnHands(stockCardId);
          for (String[] resultArray : rawResults) {
            lotsOnHands.put(resultArray[0], resultArray[1]);
          }
        } catch (LMISException e) {
          Log.w(TAG, e);
        }
        inventoryViewModel.setStockOnHand(stockCard.calculateSOHFromLots(lotsOnHands));
        break;
      }
    }
  }

  public void archiveBackStockCard(StockCard stockCard) {
    stockCard.getProduct().setArchived(false);
    try {
      stockRepository.updateStockCardWithProduct(stockCard);
    } catch (LMISException e) {
      new LMISException(e, "StockCardPresenter.archiveBackStockCard").reportToFabric();
    }
  }

  private Observable<List<StockCard>> getLoadStockCardsObservable(final ArchiveStatus status) {
    return Observable.create((Observable.OnSubscribe<List<StockCard>>) subscriber -> {
      stockService.monthlyUpdateAvgMonthlyConsumption();
      subscriber.onNext(from(stockRepository.list()).filter(stockCard -> {
        if (stockCard == null) {
          return false;
        }
        if (status.isArchived()) {
          return showInArchiveView(stockCard);
        }
        return showInOverview(stockCard);
      }).toList());
      subscriber.onCompleted();
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }

  private boolean showInOverview(StockCard stockCard) {
    return !stockCard.getProduct().isKit()
        && (stockCard.getProduct().isActive() && !stockCard.getProduct().isArchived());
  }

  private boolean showInArchiveView(StockCard stockCard) {
    return stockCard.calculateSOHFromLots(lotsOnHands) == 0
        && (stockCard.getProduct().isArchived()
        || !stockCard.getProduct().isActive());
  }

  private Observer<List<StockCard>> getLoadStockCardsSubscriber() {
    return new Observer<List<StockCard>>() {
      @Override
      public void onCompleted() {
        view.loaded();
      }

      @Override
      public void onError(Throwable e) {
        Log.w(TAG, e);
        if (SHOULD_SHOW_ALERT_MSG.equals(e.getMessage())) {
          view.showWarning();
        } else {
          ToastUtil.show(e.getMessage());
        }
        view.loaded();
      }

      @Override
      public void onNext(List<StockCard> stockCards) {
        refreshViewModels(stockCards);
        view.refresh(inventoryViewModels);
      }
    };
  }

  private Observable<List<StockCard>> createOrGetKitStockCardsObservable() {
    return Observable.create((Observable.OnSubscribe<List<StockCard>>) subscriber -> {
      try {
        final List<Product> kits = productRepository.listActiveProducts(IsKit.YES);
        subscriber.onNext(createStockCardsIfNotExist(kits));
        subscriber.onCompleted();
      } catch (LMISException e) {
        subscriber.onError(e);
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }

  private List<StockCard> createStockCardsIfNotExist(List<Product> kits) {
    long createdTime = LMISApp.getInstance().getCurrentTimeMillis();
    return from(kits).transform(product -> {
      StockCard stockCard = null;
      try {
        stockCard = stockRepository.queryStockCardByProductId(product.getId());
        if (stockCard == null) {
          stockCard = new StockCard();
          stockCard.setProduct(product);
          stockRepository.createOrUpdateStockCardWithStockMovement(stockCard, createdTime);
        }
      } catch (LMISException e) {
        new LMISException(e, "createStockCardsIfNotExist").reportToFabric();
      }
      return stockCard;
    }).toList();
  }

  public enum ArchiveStatus {
    ARCHIVED(true),
    ACTIVE(false);

    private final boolean isArchived;

    ArchiveStatus(boolean isArchived) {
      this.isArchived = isArchived;
    }

    public boolean isArchived() {
      return isArchived;
    }
  }

  public interface StockCardListView extends BaseView {

    void refresh(List<InventoryViewModel> data);

    void refreshBannerText();

    void showWarning();
  }
}