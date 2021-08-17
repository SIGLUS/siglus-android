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

import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.SELECTED_PRODUCTS;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseModel;
import org.openlmis.core.model.DraftBulkIssueProduct;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.BulkIssueRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.BulkIssueProductViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableList;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class BulkIssuePresenter extends Presenter {

  public static final String MOVEMENT_REASON_CODE = "MOVEMENT_REASON_CODE";

  public static final String DOCUMENT_NUMBER = "DOCUMENT_NUMBER";

  @Inject
  private BulkIssueRepository bulkIssueRepository;

  @Inject
  private StockRepository stockRepository;

  private BulkIssueView bulkIssueView;

  private String movementReasonCode;

  private String documentNumber;

  @Getter
  private final List<BulkIssueProductViewModel> currentViewModels = new ArrayList<>();

  Observer<List<BulkIssueProductViewModel>> viewModelsSubscribe = new Observer<List<BulkIssueProductViewModel>>() {
    @Override
    public void onCompleted() {
      Collections.sort(currentViewModels);
      bulkIssueView.loaded();
      bulkIssueView.onRefreshViewModels();
    }

    @Override
    public void onError(Throwable e) {
      bulkIssueView.loaded();
      bulkIssueView.onLoadViewModelsFailed(e);
    }

    @Override
    public void onNext(List<BulkIssueProductViewModel> bulkIssueProductViewModels) {
      currentViewModels.addAll(bulkIssueProductViewModels);
    }
  };

  Observer<Object> saveDraftSubscribe = new Observer<Object>() {
    @Override
    public void onCompleted() {
      bulkIssueView.loaded();
      bulkIssueView.onSaveDraftFinished(true);
    }

    @Override
    public void onError(Throwable e) {
      bulkIssueView.loaded();
      bulkIssueView.onSaveDraftFinished(false);
    }

    @Override
    public void onNext(Object o) {
      // do nothing
    }
  };

  Observer<Object> doIssueSubscribe = new Observer<Object>() {
    @Override
    public void onCompleted() {
      bulkIssueView.loaded();
      bulkIssueView.onSaveMovementSuccess();
    }

    @Override
    public void onError(Throwable e) {
      bulkIssueView.loaded();
      if (e instanceof LMISException) {
        bulkIssueView.onSaveMovementFailed((LMISException) e);
      } else {
        LMISException unknownException = new LMISException(e, "unknown exception when save bulk issue movement");
        unknownException.reportToFabric();
        bulkIssueView.onSaveMovementFailed(unknownException);
      }
    }

    @Override
    public void onNext(Object o) {
      // do nothing
    }
  };

  @Override
  public void attachView(BaseView v) {
    this.bulkIssueView = (BulkIssueView) v;
  }

  public void initialViewModels(Intent intent) {
    bulkIssueView.loading();
    currentViewModels.clear();
    Observable<List<BulkIssueProductViewModel>> initObservable;
    documentNumber = intent.getStringExtra(DOCUMENT_NUMBER);
    if (intent.hasExtra(SELECTED_PRODUCTS) && intent.hasExtra(MOVEMENT_REASON_CODE)) {
      movementReasonCode = intent.getStringExtra(MOVEMENT_REASON_CODE);
      initObservable = getObservableFromProducts((List<Product>) intent.getSerializableExtra(SELECTED_PRODUCTS));
    } else {
      initObservable = getObservableFromDrafts();
    }
    Subscription initialSubscription = initObservable
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(viewModelsSubscribe);
    subscriptions.add(initialSubscription);
  }

  public void addProducts(List<Product> products) {
    bulkIssueView.loading();
    Subscription addProductsSubscription = getObservableFromProducts(products)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(viewModelsSubscribe);
    subscriptions.add(addProductsSubscription);
  }

  public List<String> getAddedProductCodeList() {
    return FluentIterable.from(currentViewModels)
        .transform(viewModel -> Objects.requireNonNull(viewModel).getStockCard().getProduct().getCode())
        .toList();
  }

  public long[] getEffectedStockCardIds() {
    long[] effectedStockCardIds = new long[currentViewModels.size()];
    for (int i = 0; i < currentViewModels.size(); i++) {
      effectedStockCardIds[i] = currentViewModels.get(i).getStockCard().getId();
    }
    return effectedStockCardIds;
  }

  public void saveDraft() {
    bulkIssueView.loading();
    Subscription saveDraftSubscription = Observable.create(subscriber -> {
      try {
        ImmutableList<DraftBulkIssueProduct> draftProducts = FluentIterable.from(currentViewModels)
            .transform(viewModel -> viewModel.convertToDraft(documentNumber, movementReasonCode))
            .toList();
        bulkIssueRepository.saveDraft(draftProducts);
        subscriber.onNext(null);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "bulk issue save draft failed").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(saveDraftSubscribe);
    subscriptions.add(saveDraftSubscription);
  }

  public void deleteDraft() {
    try {
      bulkIssueRepository.deleteDraft();
    } catch (LMISException e) {
      new LMISException(e, "delete bulk issue draft failed").reportToFabric();
    }
  }

  public boolean needConfirm() {
    try {
      List<DraftBulkIssueProduct> draftProducts = bulkIssueRepository.queryUsableProductAndLotDraft();
      if (draftProducts == null) {
        return !currentViewModels.isEmpty();
      }
      for (BulkIssueProductViewModel productViewModel : currentViewModels) {
        if (productViewModel.hasChanged()) {
          return true;
        }
      }
      return draftProducts.size() != currentViewModels.size();
    } catch (LMISException e) {
      return true;
    }
  }

  public void doIssue(String signature) {
    bulkIssueView.loading();
    Subscription doIssueSubscription = Observable.create(subscriber -> {
      try {
        bulkIssueRepository.saveMovement(FluentIterable.from(currentViewModels).transform(productViewModel -> {
          StockMovementItem stockMovementItem = productViewModel.convertToMovement(movementReasonCode, documentNumber);
          stockMovementItem.setSignature(signature);
          return stockMovementItem;
        }).toList());
        deleteDraft();
        subscriber.onNext(null);
        subscriber.onCompleted();
      } catch (LMISException e) {
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(doIssueSubscribe);
    subscriptions.add(doIssueSubscription);
  }

  private Observable<List<BulkIssueProductViewModel>> getObservableFromProducts(List<Product> products) {
    return Observable.create(subscriber -> {
      try {
        ImmutableList<Long> productIds = FluentIterable
            .from(products)
            .transform(BaseModel::getId)
            .toList();
        List<StockCard> stockCards = stockRepository.listStockCardsByProductIds(productIds);
        subscriber.onNext(createViewModelFromStockCards(stockCards));
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "add products failed").reportToFabric();
        subscriber.onError(e);
      }
    });
  }

  private Observable<List<BulkIssueProductViewModel>> getObservableFromDrafts() {
    return Observable.create(subscriber -> {
      try {
        List<DraftBulkIssueProduct> draftBulkIssueProducts = bulkIssueRepository.queryUsableProductAndLotDraft();
        if (draftBulkIssueProducts.isEmpty()) {
          subscriber.onError(new IllegalStateException("no draft when restore from draft"));
          return;
        }
        documentNumber = draftBulkIssueProducts.get(0).getDocumentNumber();
        movementReasonCode = draftBulkIssueProducts.get(0).getMovementReasonCode();
        ImmutableList<StockCard> productIds = FluentIterable
            .from(draftBulkIssueProducts)
            .transform(DraftBulkIssueProduct::getStockCard)
            .toList();
        List<BulkIssueProductViewModel> viewModels = createViewModelFromStockCards(productIds);
        for (BulkIssueProductViewModel viewModel : viewModels) {
          for (DraftBulkIssueProduct draftProduct : draftBulkIssueProducts) {
            if (Objects.equals(viewModel.getStockCard(), draftProduct.getStockCard())) {
              viewModel.restoreFromDraft(draftProduct);
              break;
            }
          }
        }
        subscriber.onNext(viewModels);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "restore viewModels from draft failed").reportToFabric();
        subscriber.onError(e);
      }
    });
  }

  @NonNull
  private List<BulkIssueProductViewModel> createViewModelFromStockCards(List<StockCard> stockCards) {
    ArrayList<BulkIssueProductViewModel> viewModels = new ArrayList<>();
    for (StockCard stockCard : stockCards) {
      List<LotOnHand> lotOnHandList = stockCard.getLotOnHandListWrapper();
      if (lotOnHandList.isEmpty()) {
        continue;
      }
      viewModels.add(BulkIssueProductViewModel.build(stockCard, lotOnHandList));
    }
    return viewModels;
  }

  public interface BulkIssueView extends BaseView {

    void onRefreshViewModels();

    void onLoadViewModelsFailed(Throwable throwable);

    void onSaveDraftFinished(boolean succeeded);

    void onSaveMovementSuccess();

    void onSaveMovementFailed(LMISException e);
  }
}
