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

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.MovementReasonNotFoundException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.PodProductLotItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.IssueVoucherReportViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableMap;
import roboguice.inject.ContextSingleton;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

@ContextSingleton
public class IssueVoucherReportPresenter extends BaseReportPresenter {

  @Inject
  PodRepository podRepository;

  @Inject
  StockRepository stockRepository;

  @Inject
  private ProgramRepository programRepository;

  @Inject
  private ProductRepository productRepository;

  public String reasonCode;

  IssueVoucherView issueVoucherView;

  @Getter
  IssueVoucherReportViewModel issueVoucherReportViewModel;

  @Getter
  Pod pod;

  public void loadData(long podId) {
    Subscription subscription = getRnrFormObservable(podId)
        .subscribe(loadDataOnNextAction, loadDataOnErrorAction);
    subscriptions.add(subscription);
  }

  protected Observable<Pod> getRnrFormObservable(final long formId) {
    return Observable.create((Observable.OnSubscribe<Pod>) subscriber -> {
      try {
        pod = podRepository.queryById(formId);
        subscriber.onNext(pod);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "VIARequisitionPresenter.getRnrFormObservable").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  @Override
  public void deleteDraft() {

  }

  @Override
  public boolean isDraft() {
    return false;
  }

  @Override
  protected void addSignature(String signature) {

  }

  @Override
  public void attachView(BaseView v) throws ViewNotMatchException {
    issueVoucherView = (IssueVoucherView) v;
  }

  public void loadViewModelByPod(Pod podContent, boolean isBackToCurrentPage) {
    if (isBackToCurrentPage) {
      List<PodProductItem> existedProducts = new ArrayList<>(pod.getPodProductItemsWrapper());
      existedProducts.addAll(podContent.getPodProductItemsWrapper());
      pod.setPodProductItemsWrapper(existedProducts);
    } else {
      pod = podContent;
      if (pod.getOrderSupplyFacilityType() != null) {
        try {
          reasonCode = MovementReasonManager.getInstance()
              .getReasonCodeBySupplyFacilityType(pod.getOrderSupplyFacilityType());
        } catch (MovementReasonNotFoundException e) {
          new LMISException(e, "MovementReasonNotFoundException").reportToFabric();
        }
      }
    }
    try {
      Program program = programRepository.queryByCode(pod.getRequisitionProgramCode());
      issueVoucherReportViewModel = new IssueVoucherReportViewModel(pod);
      issueVoucherReportViewModel.setProgram(program);
      issueVoucherView.loaded();
      issueVoucherView.refreshIssueVoucherForm(pod);
    } catch (LMISException e) {
      new LMISException(e, "IssueVoucherReport.getProgram").reportToFabric();
    }
  }

  public void deleteIssueVoucher() {
    try {
      if (!pod.isLocal()) {
        pod.setPodProductItemsWrapper(FluentIterable.from(issueVoucherReportViewModel.getProductViewModels())
            .transform(productViewModel -> productViewModel.restoreToPodProductModelForRemote()).toList());
        podRepository.createOrUpdateWithItems(pod);
      }
    } catch (Exception e) {
      new LMISException(e, "deleteIssueVoucher").reportToFabric();
    }
  }

  public Observable<Void> getSaveFormObservable() {
    return Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
      try {
        pod.setDraft(true);
        if (reasonCode != null) {
          pod.setStockManagementReason(reasonCode);
        }
        setPodItems();
        podRepository.createOrUpdateWithItems(pod);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "Pod.getSaveFormObservable").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public Observable<Void> getCompleteFormObservable(String receivedBy) {
    return Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
      try {
        pod.setDraft(false);
        pod.setReceivedBy(receivedBy);
        pod.setReceivedDate(DateUtil.getCurrentDate());
        pod.setOrderStatus(OrderStatus.RECEIVED);
        if (reasonCode != null) {
          pod.setStockManagementReason(reasonCode);
        }
        setPodItems();
        podRepository.createOrUpdateWithItems(pod);
        saveStockManagement(pod);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "Pod.getCompleteFormObservable").reportToFabric();
        subscriber.onError(e);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public void updatePodItems() {
    setPodItems();
  }

  public void setPodItems() {
    pod.setPodProductItemsWrapper(FluentIterable.from(
        issueVoucherReportViewModel.getProductViewModels())
        .transform(productViewModel -> productViewModel.convertToPodProductModel()).toList());
  }

  public List<String> getAddedProductCodeList() {
    return FluentIterable.from(issueVoucherReportViewModel.getProductViewModels())
        .transform(viewModel -> Objects.requireNonNull(viewModel).getProduct().getCode())
        .toList();
  }

  protected Action1<Throwable> loadDataOnErrorAction = throwable -> {
    issueVoucherView.loaded();
    ToastUtil.show(throwable.getMessage());
  };

  public interface IssueVoucherView extends BaseView {

    void refreshIssueVoucherForm(Pod pod);
  }

  protected Action1<Pod> loadDataOnNextAction = podContent -> {
    loadViewModelByPod(podContent, false);
  };

  private void saveStockManagement(Pod pod) throws LMISException {
    List<StockCard> stockCards = stockRepository.getStockCardsAndLotsOnHandForProgram(pod.getRequisitionProgramCode());
    List<Long> needUpdatedArchived = new ArrayList<>();
    ImmutableMap<Long, StockCard> productIdToStockCard = FluentIterable.from(stockCards)
        .uniqueIndex(stockCard -> stockCard.getProduct().getId());
    List<StockCard> toUpdateStockCards = new ArrayList<>();
    List<StockCard> needInitialStockCards = new ArrayList<>();
    for (PodProductItem podProductItem : pod.getPodProductItemsWrapper()) {
      if (!needUpdateStockCard(podProductItem)) {
        continue;
      }
      Product product = podProductItem.getProduct();
      long productId = podProductItem.getProduct().getId();
      StockCard stockCard;
      if (productIdToStockCard.containsKey(productId)) {
        stockCard = productIdToStockCard.get(productId);
      } else {
        stockCard = new StockCard();
        needInitialStockCards.add(stockCard);
      }
      needUpdatedArchived.add(product.getId());
      stockCard.setProduct(podProductItem.getProduct());
      long soh = stockCard.getStockOnHand();
      long changeQuality = getChangeQuality(podProductItem);
      stockCard.setStockOnHand(soh + changeQuality);
      StockMovementItem movementItem = buildStockMovementItem(stockCard, podProductItem, changeQuality);
      stockCard.setStockMovementItemsWrapper(Collections.singletonList(movementItem));
      toUpdateStockCards.add(stockCard);
    }
    productRepository.updateProductInArchived(needUpdatedArchived);
    if (!toUpdateStockCards.isEmpty()) {
      stockRepository.addStockMovementsAndUpdateStockCards(needInitialStockCards, toUpdateStockCards);
    }
  }

  private boolean needUpdateStockCard(PodProductItem podProductItem) {
    boolean needUpdate = false;
    for (PodProductLotItem lotItem : podProductItem.getPodProductLotItemsWrapper()) {
      if (lotItem.getAcceptedQuantity() != 0) {
        return true;
      }
    }
    return needUpdate;
  }

  private long getChangeQuality(PodProductItem podProductItem) {
    long acceptedQuantity = 0;
    for (PodProductLotItem lotItem : podProductItem.getPodProductLotItemsWrapper()) {
      acceptedQuantity += lotItem.getAcceptedQuantity();
    }
    return acceptedQuantity;
  }

  private StockMovementItem buildStockMovementItem(StockCard stockCard, PodProductItem podProductItem,
      long changeQuality) {
    StockMovementItem stockMovementItem = new StockMovementItem();
    stockMovementItem.setMovementDate(DateUtil.getCurrentDate());
    stockMovementItem.setStockCard(stockCard);
    stockMovementItem.setMovementType(MovementType.RECEIVE);
    stockMovementItem.setReason(pod.getStockManagementReason());
    stockMovementItem.setMovementQuantity(changeQuality);
    stockMovementItem.setDocumentNumber(pod.getOrderCode());
    stockMovementItem.setStockOnHand(stockCard.getStockOnHand());
    stockMovementItem.setSignature(pod.getReceivedBy());
    if (stockCard.getProduct().isKit()) {
      return stockMovementItem;
    }
    ImmutableMap<Long, LotOnHand> lotIdToLotOnHands = FluentIterable.from(stockCard.getLotOnHandListWrapper())
        .uniqueIndex(lotOnHand -> lotOnHand.getId());
    stockMovementItem.setLotMovementItemListWrapper(
        FluentIterable.from(podProductItem.getPodProductLotItemsWrapper())
            .filter(podLotItem -> podLotItem.getAcceptedQuantity() > 0)
            .transform(podLotItem -> buildLotMovementItem(stockMovementItem, podLotItem, lotIdToLotOnHands)).toList());
    return stockMovementItem;
  }

  private LotMovementItem buildLotMovementItem(StockMovementItem stockMovementItem, PodProductLotItem podLotItem,
      ImmutableMap<Long, LotOnHand> idToLotOnHand) {
    LotMovementItem lotMovementItem = new LotMovementItem();
    Lot lot = podLotItem.getLot();
    lotMovementItem.setLot(podLotItem.getLot());
    long acceptedQuantity = podLotItem.getAcceptedQuantity();
    if (idToLotOnHand.containsKey(lot.getId())) {
      long lotOnHand = idToLotOnHand.get(lot.getId()).getQuantityOnHand();
      lotMovementItem.setStockOnHand(lotOnHand + acceptedQuantity);
    } else {
      lotMovementItem.setStockOnHand(acceptedQuantity);
    }
    lotMovementItem.setMovementQuantity(podLotItem.getAcceptedQuantity());
    lotMovementItem.setReason(stockMovementItem.getReason());
    lotMovementItem.setStockMovementItem(stockMovementItem);
    lotMovementItem.setDocumentNumber(stockMovementItem.getDocumentNumber());
    return lotMovementItem;
  }

}
