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
import org.openlmis.core.constant.IntentConstants;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.BaseModel;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.BaseView;
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

  private IssueVoucherDraftView issueVoucherDraftView;

  @Inject
  private StockRepository stockRepository;

  private String orderNumber;

  private String movementReasonCode;

  @Override
  public void attachView(BaseView v) throws ViewNotMatchException {
    this.issueVoucherDraftView = (IssueVoucherDraftView) v;
  }

  public void initialViewModels(Intent intent) {
    orderNumber = (String) intent.getSerializableExtra(IntentConstants.PARAM_ORDER_NUMBER);
    movementReasonCode = (String) intent.getSerializableExtra(IntentConstants.PARAM_MOVEMENT_REASON_CODE);
    issueVoucherDraftView.loading();
    currentViewModels.clear();
    Observable<List<IssueVoucherProductViewModel>> initObservable = getObservableFromProducts(
        (List<Product>) intent.getSerializableExtra(IntentConstants.PARAM_SELECTED_PRODUCTS));
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

  public Pod coverToPodFromIssueVoucher(String programCode) {
    return Pod.builder()
        .orderCode(orderNumber)
        .requisitionProgramCode(programCode)
        .stockManagementReason(movementReasonCode)
        .orderStatus(OrderStatus.SHIPPED)
        .requisitionIsEmergency(false)
        .isDraft(true)
        .isLocal(true)
        .isSynced(false)
        .podProductItemsWrapper(buildPodProductItems())
        .build();
  }

  private List<PodProductItem> buildPodProductItems() {
    return FluentIterable.from(currentViewModels)
        .transform(IssueVoucherProductViewModel::from)
        .toList();
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
            issueVoucherProductViewModel = new IssueVoucherProductViewModel(productStockCardMap.get(product));
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

  private Observer<List<IssueVoucherProductViewModel>> viewModelsSubscribe =
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

  public interface IssueVoucherDraftView extends BaseView {

    void onRefreshViewModels();

    void onLoadViewModelsFailed(Throwable throwable);
  }

}
