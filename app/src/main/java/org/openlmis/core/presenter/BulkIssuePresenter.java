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
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftBulkIssueProduct;
import org.openlmis.core.model.DraftBulkIssueProductLotItem;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.BulkIssueRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.BulkIssueLotViewModel;
import org.openlmis.core.view.viewmodel.BulkIssueProductViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BulkIssuePresenter extends Presenter {

  @Inject
  private BulkIssueRepository bulkIssueRepository;

  @Inject
  private StockRepository stockRepository;

  private BulkIssueView bulkIssueView;

  @Getter
  private final List<BulkIssueProductViewModel> currentViewModels = new ArrayList<>();

  @Override
  public void attachView(BaseView v) {
    this.bulkIssueView = (BulkIssueView) v;
  }

  public void initialViewModels(Intent intent) {
    bulkIssueView.loading();
    currentViewModels.clear();
    Observable<List<BulkIssueProductViewModel>> initObservable;
    if (intent.hasExtra(SELECTED_PRODUCTS)) {
      initObservable = createViewModelsFromProducts((List<Product>) intent.getSerializableExtra(SELECTED_PRODUCTS));
    } else {
      initObservable = restoreViewModelsFromDraft();
    }
    Subscription initialSubscription = initObservable
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new Subscriber<List<BulkIssueProductViewModel>>() {
          @Override
          public void onCompleted() {
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
        });
    subscriptions.add(initialSubscription);
  }

  public void addProducts(List<Product> productCodes) {
    bulkIssueView.loading();
    Subscription addProductsSubscription = createViewModelsFromProducts(productCodes)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new Subscriber<List<BulkIssueProductViewModel>>() {
          @Override
          public void onCompleted() {
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
        });
    subscriptions.add(addProductsSubscription);
  }

  public List<String> getAddedProductCodes() {
    return FluentIterable.from(currentViewModels)
        .transform(viewModel -> Objects.requireNonNull(viewModel).getProduct().getCode())
        .toList();
  }

  private Observable<List<BulkIssueProductViewModel>> createViewModelsFromProducts(List<Product> products) {
    return Observable.create(subscriber -> {
      try {
        ArrayList<BulkIssueProductViewModel> viewModels = new ArrayList<>();
        List<StockCard> stockCards = stockRepository
            .listStockCardsByProductIds(FluentIterable.from(products).transform(Product::getId).toList());
        for (StockCard stockCard : stockCards) {
          List<LotOnHand> lotOnHandList = stockCard.getLotOnHandListWrapper();
          if (lotOnHandList.isEmpty()) {
            continue;
          }
          viewModels.add(BulkIssueProductViewModel
              .create(stockCard.getProduct(), buildLotViewModelsFromLotOnHands(lotOnHandList)));
        }
        subscriber.onNext(viewModels);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "add products failed").reportToFabric();
        subscriber.onError(e);
      }
    });
  }

  private Observable<List<BulkIssueProductViewModel>> restoreViewModelsFromDraft() {
    return Observable.create(subscriber -> {
      try {
        ArrayList<BulkIssueProductViewModel> viewModels = new ArrayList<>();
        List<DraftBulkIssueProduct> draftBulkIssueProducts = bulkIssueRepository.queryAllBulkIssueDraft();
        for (DraftBulkIssueProduct draftProduct : draftBulkIssueProducts) {
          viewModels.add(BulkIssueProductViewModel.create(draftProduct.getProduct(),
              buildLotViewModelsFromDraftLots(draftProduct.getDraftLotItemListWrapper())));
        }
        subscriber.onNext(viewModels);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "restore viewModels from draft failed").reportToFabric();
        subscriber.onError(e);
      }
    });
  }

  private List<BulkIssueLotViewModel> buildLotViewModelsFromLotOnHands(Collection<LotOnHand> lotOnHands) {
    final ArrayList<BulkIssueLotViewModel> lotViewModels = new ArrayList<>();
    for (LotOnHand lotOnHand : lotOnHands) {
      Lot lot = lotOnHand.getLot();
      long lotSoh = lotOnHand.getQuantityOnHand() == null ? 0 : lotOnHand.getQuantityOnHand();
      lotViewModels.add(BulkIssueLotViewModel.create(lotSoh, lot.getLotNumber(), lot.getExpirationDate()));
    }
    return lotViewModels;
  }

  private List<BulkIssueLotViewModel> buildLotViewModelsFromDraftLots(List<DraftBulkIssueProductLotItem> draftLots) {
    final ArrayList<BulkIssueLotViewModel> lotViewModels = new ArrayList<>();
    for (DraftBulkIssueProductLotItem draftLot : draftLots) {
      BulkIssueLotViewModel viewModel = BulkIssueLotViewModel
          .create(draftLot.getLotSoh(), draftLot.getLotNumber(), draftLot.getExpirationDate());
      viewModel.setAmount(draftLot.getAmount());
      lotViewModels.add(viewModel);
    }
    return lotViewModels;
  }

  public interface BulkIssueView extends BaseView {

    void onRefreshViewModels();

    void onLoadViewModelsFailed(Throwable throwable);
  }
}
