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

import android.content.Intent;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.openlmis.core.constant.IntentConstants;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseModel;
import org.openlmis.core.model.DraftIssueVoucherProductItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.IssueVoucherDraftRepository;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.IssueVoucherLotViewModel;
import org.openlmis.core.view.viewmodel.IssueVoucherProductViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableList;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class IssueVoucherDraftPresenter extends Presenter {

  @Getter
  private final List<IssueVoucherProductViewModel> currentViewModels = new ArrayList<>();
  @Inject
  PodRepository podRepository;
  private IssueVoucherDraftView issueVoucherDraftView;
  Observer<List<IssueVoucherProductViewModel>> viewModelsSubscribe =
      new Observer<List<IssueVoucherProductViewModel>>() {
        @Override
        public void onCompleted() {
          issueVoucherDraftView.loaded();
          issueVoucherDraftView.onRefreshViewModels();
        }

        @Override
        public void onError(Throwable e) {
          issueVoucherDraftView.loaded();
          issueVoucherDraftView.onLoadViewModelsFailed(e);
        }

        @Override
        public void onNext(List<IssueVoucherProductViewModel> issueVoucherProductViewModels) {
          currentViewModels.addAll(issueVoucherProductViewModels);
        }
      };
  Observer<Object> saveDraftSubscribe = new Observer<Object>() {
    @Override
    public void onCompleted() {
      issueVoucherDraftView.onSaveDraftFinished(true);
    }

    @Override
    public void onError(Throwable e) {
      issueVoucherDraftView.onSaveDraftFinished(false);
    }

    @Override
    public void onNext(Object o) {
      // do nothing
    }
  };
  Observer<Object> deleteDraftSubscribe = new Observer<Object>() {
    @Override
    public void onCompleted() {
      issueVoucherDraftView.loaded();
    }

    @Override
    public void onError(Throwable e) {
      issueVoucherDraftView.loaded();
    }

    @Override
    public void onNext(Object o) {
      // do nothing
    }
  };
  @Inject
  private StockRepository stockRepository;
  @Inject
  private IssueVoucherDraftRepository issueVoucherDraftRepository;
  @Setter
  private String orderNumber;
  @Setter
  private String movementReasonCode;
  @Setter
  @Getter
  private Pod pod;

  @Override
  public void attachView(BaseView v) {
    this.issueVoucherDraftView = (IssueVoucherDraftView) v;
  }

  public void initialViewModels(Intent intent) {
    orderNumber = (String) intent.getSerializableExtra(IntentConstants.PARAM_ORDER_NUMBER);
    movementReasonCode = (String) intent.getSerializableExtra(IntentConstants.PARAM_MOVEMENT_REASON_CODE);
    pod = (Pod) intent.getSerializableExtra(IntentConstants.PARAM_DRAFT_ISSUE_VOUCHER);
    currentViewModels.clear();
    Observable<List<IssueVoucherProductViewModel>> initObservable;
    if (pod != null) {
      initObservable = getObservableFromDraft(pod.getId());
    } else {
      initObservable = getObservableFromProducts(
          (List<Product>) intent.getSerializableExtra(IntentConstants.PARAM_SELECTED_PRODUCTS));
    }
    Subscription subscription = initObservable
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(viewModelsSubscribe);
    subscriptions.add(subscription);
  }

  public List<String> getAddedProductCodeList() {
    return FluentIterable.from(currentViewModels)
        .transform(viewModel -> Objects.requireNonNull(viewModel).getProduct().getCode())
        .toList();
  }

  public void addProducts(List<Product> products) {
    issueVoucherDraftView.loading();
    Subscription addProductsSubscription = getObservableFromProducts(products)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(viewModelsSubscribe);
    subscriptions.add(addProductsSubscription);
  }

  public void saveIssueVoucherDraft(String programCode) {
    issueVoucherDraftView.loading();
    Subscription saveDraftSubscription = Observable.create(subscriber -> {
      try {
        if (pod == null) {
          pod = coverToPodFromIssueVoucher(programCode, false);
        }
        ImmutableList<DraftIssueVoucherProductItem> productItems = FluentIterable.from(currentViewModels)
            .transform(productViewModel -> Objects.requireNonNull(productViewModel).covertToDraft(pod))
            .toList();
        issueVoucherDraftRepository.saveDraft(productItems, pod);
        subscriber.onNext(null);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "issue voucher save draft failed").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(saveDraftSubscribe);
    subscriptions.add(saveDraftSubscription);
  }

  public void deleteDraftPod() {
    issueVoucherDraftView.loading();
    Subscription deleteDraftSubscription = Observable.create(subscriber -> {
      if (pod.getId() != 0) {
        deleteDraft();
      }
      subscriber.onNext(null);
      subscriber.onCompleted();
    }).observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(deleteDraftSubscribe);
    subscriptions.add(deleteDraftSubscription);
  }

  public Pod coverToPodFromIssueVoucher(String programCode, boolean needChildrenItem) {
    return Pod.builder()
        .orderCode(orderNumber)
        .requisitionProgramCode(programCode)
        .stockManagementReason(movementReasonCode)
        .orderStatus(OrderStatus.SHIPPED)
        .requisitionIsEmergency(false)
        .isDraft(true)
        .isLocal(true)
        .isSynced(false)
        .podProductItemsWrapper(needChildrenItem ? buildPodProductItems() : new ArrayList<>())
        .build();
  }

  public boolean needConfirm() {
    try {
      if (pod == null) {
        return !currentViewModels.isEmpty();
      }
      List<DraftIssueVoucherProductItem> productItems = issueVoucherDraftRepository.queryByPodId(pod.getId());
      if (productItems.size() != currentViewModels.size()) {
        return true;
      }
      for (IssueVoucherProductViewModel productViewModel : currentViewModels) {
        if (productViewModel.hasChanged()) {
          return true;
        }
      }
      return false;
    } catch (LMISException e) {
      return true;
    }
  }

  public void deleteDraft() {
    podRepository.deleteLocalDraftPod(pod.getId());
  }

  private Observable<List<IssueVoucherProductViewModel>> getObservableFromProducts(List<Product> products) {
    return Observable.create(subscriber -> {
      try {
        ImmutableList<Long> productIds = FluentIterable.from(products).transform(BaseModel::getId).toList();
        List<StockCard> stockCards = stockRepository.listStockCardsByProductIds(productIds);
        final Map<Product, StockCard> productStockCardMap = new HashMap<>();
        for (StockCard stockCard : stockCards) {
          productStockCardMap.put(stockCard.getProduct(), stockCard);
        }
        List<IssueVoucherProductViewModel> issueVoucherProductViewModels = new ArrayList<>();
        for (Product product : products) {
          IssueVoucherProductViewModel issueVoucherProductViewModel;
          if (productStockCardMap.get(product) != null) {
            issueVoucherProductViewModel = new IssueVoucherProductViewModel(
                Objects.requireNonNull(productStockCardMap.get(product)));
          } else {
            issueVoucherProductViewModel = new IssueVoucherProductViewModel(product);
          }
          issueVoucherProductViewModels.add(issueVoucherProductViewModel);
        }
        subscriber.onNext(issueVoucherProductViewModels);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "add products failed").reportToFabric();
        subscriber.onError(e);
      }
    });
  }

  private Observable<List<IssueVoucherProductViewModel>> getObservableFromDraft(Long podId) {
    return Observable.create(subscriber -> {
      try {
        Pod pod = podRepository.queryById(podId);
        orderNumber = pod.getOrderCode();
        movementReasonCode = pod.getStockManagementReason();
        List<DraftIssueVoucherProductItem> productItems = issueVoucherDraftRepository.queryByPodId(podId);
        List<IssueVoucherProductViewModel> productViewModels = FluentIterable.from(productItems)
            .transform(DraftIssueVoucherProductItem::from).toList();
        ImmutableList<Long> productIds = FluentIterable.from(productViewModels).transform(productViewModel -> Objects
            .requireNonNull(productViewModel).getProduct().getId()).toList();
        List<StockCard> stockCards = stockRepository.listStockCardsByProductIds(productIds);
        for (IssueVoucherProductViewModel productViewModel : productViewModels) {
          for (StockCard stockCard : stockCards) {
            if (productViewModel.getProduct().equals(stockCard.getProduct())) {
              addNewLotToDraftProductViewModel(productViewModel, stockCard);
            }
          }
        }
        subscriber.onNext(productViewModels);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "get products from draft failed").reportToFabric();
        subscriber.onError(e);
      }
    });
  }

  private void addNewLotToDraftProductViewModel(IssueVoucherProductViewModel productViewModel, StockCard stockCard) {
    List<String> viewModelLotNumbers = FluentIterable.from(productViewModel.getLotViewModels())
        .transform(lotViewModel -> Objects.requireNonNull(lotViewModel).getLotNumber())
        .toList();
    for (LotOnHand lotOnHand : stockCard.getLotOnHandListWrapper()) {
      if (!viewModelLotNumbers.contains(lotOnHand.getLot().getLotNumber()) && lotOnHand.getQuantityOnHand() > 0) {
        productViewModel.getLotViewModels().add(IssueVoucherLotViewModel.build(lotOnHand));
      }
    }
  }

  private List<PodProductItem> buildPodProductItems() {
    return FluentIterable.from(currentViewModels)
        .transform(IssueVoucherProductViewModel::from)
        .toList();
  }

  public interface IssueVoucherDraftView extends BaseView {

    void onRefreshViewModels();

    void onLoadViewModelsFailed(Throwable throwable);

    void onSaveDraftFinished(boolean succeeded);

  }

}
